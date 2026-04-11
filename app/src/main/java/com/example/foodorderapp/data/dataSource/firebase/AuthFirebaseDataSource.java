package com.example.foodorderapp.data.dataSource.firebase;

import com.example.foodorderapp.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AuthFirebaseDataSource {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthFirebaseDataSource() {
        FirebaseAuth authInstance;
        FirebaseFirestore dbInstance;
        try {
            authInstance = FirebaseAuth.getInstance();
            dbInstance = FirebaseFirestore.getInstance();
        } catch (Exception ignored) {
            authInstance = null;
            dbInstance = null;
        }

        auth = authInstance;
        db = dbInstance;
    }

    public Task<Void> register(String email, String password, User user) {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase chua duoc cau hinh. Vui long them google-services.json"));
        }

        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        user.setUid(uid);
                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> taskSource.setResult(null))
                                .addOnFailureListener(taskSource::setException);
                    } else {
                        taskSource.setException(task.getException());
                    }
                });

        return taskSource.getTask();
    }

    public Task<User> login(String email, String password) {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase chua duoc cau hinh. Vui long them google-services.json"));
        }

        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(document -> taskSource.setResult(document.toObject(User.class)))
                                .addOnFailureListener(taskSource::setException);
                    } else {
                        taskSource.setException(task.getException());
                    }
                });

        return taskSource.getTask();
    }

    public Task<User> signInWithGoogle(String idToken) {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase error"));
        }
        if (idToken == null || idToken.trim().isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Google token khong hop le"));
        }

        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser == null) {
                    taskSource.setException(new IllegalStateException("Khong lay duoc thong tin tai khoan Google"));
                    return;
                }
                String uid = firebaseUser.getUid();

                db.collection("users").document(uid).get().addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User existing = document.toObject(User.class);
                        User normalized = normalizeUser(existing, firebaseUser);
                        Map<String, Object> patch = buildUserPatch(normalized);
                        db.collection("users").document(uid)
                                .set(patch, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> taskSource.setResult(normalized))
                                .addOnFailureListener(taskSource::setException);
                    } else {
                        User newUser = buildDefaultUser(firebaseUser);

                        db.collection("users").document(uid).set(newUser)
                                .addOnSuccessListener(aVoid -> taskSource.setResult(newUser))
                                .addOnFailureListener(taskSource::setException);
                    }
                }).addOnFailureListener(taskSource::setException);
            } else {
                taskSource.setException(task.getException());
            }
        });

        return taskSource.getTask();
    }

    public Task<User> getCurrentUserProfile() {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase chua duoc cau hinh"));
        }

        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("Chua dang nhap"));
        }

        FirebaseUser firebaseUser = auth.getCurrentUser();
        String uid = auth.getCurrentUser().getUid();
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User existing = document.toObject(User.class);
                        User normalized = normalizeUser(existing, firebaseUser);
                        db.collection("users").document(uid)
                                .set(buildUserPatch(normalized), SetOptions.merge())
                                .addOnSuccessListener(aVoid -> taskSource.setResult(normalized))
                                .addOnFailureListener(taskSource::setException);
                    } else {
                        User newUser = buildDefaultUser(firebaseUser);
                        db.collection("users").document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> taskSource.setResult(newUser))
                                .addOnFailureListener(taskSource::setException);
                    }
                })
                .addOnFailureListener(taskSource::setException);
        return taskSource.getTask();
    }

    private User buildDefaultUser(FirebaseUser firebaseUser) {
        User newUser = new User();
        newUser.setUid(firebaseUser.getUid());
        newUser.setDisplayName(firebaseUser.getDisplayName());
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setPhone("");
        newUser.setRole("customer");
        newUser.setStatus("active");
        newUser.setOrderCount(0);
        newUser.setCreatedAt(FieldValue.serverTimestamp());
        return newUser;
    }

    private User normalizeUser(User user, FirebaseUser firebaseUser) {
        User safe = user == null ? new User() : user;
        safe.setUid(firebaseUser.getUid());
        if (safe.getEmail() == null || safe.getEmail().trim().isEmpty()) {
            safe.setEmail(firebaseUser.getEmail());
        }
        if (safe.getDisplayName() == null || safe.getDisplayName().trim().isEmpty()) {
            safe.setDisplayName(firebaseUser.getDisplayName());
        }
        if (safe.getPhone() == null) {
            safe.setPhone("");
        }
        if (safe.getRole() == null || safe.getRole().trim().isEmpty()) {
            safe.setRole("customer");
        }
        if (safe.getStatus() == null || safe.getStatus().trim().isEmpty()) {
            safe.setStatus("active");
        }
        if (safe.getCreatedAt() == null) {
            safe.setCreatedAt(FieldValue.serverTimestamp());
        }
        return safe;
    }

    private Map<String, Object> buildUserPatch(User user) {
        Map<String, Object> patch = new HashMap<>();
        patch.put("uid", user.getUid());
        patch.put("displayName", user.getDisplayName());
        patch.put("email", user.getEmail());
        patch.put("phone", user.getPhone());
        patch.put("role", user.getRole());
        patch.put("status", user.getStatus());
        patch.put("orderCount", Math.max(user.getOrderCount(), 0));
        if (user.getCreatedAt() == null) {
            patch.put("createdAt", FieldValue.serverTimestamp());
        } else {
            patch.put("createdAt", user.getCreatedAt());
        }
        return patch;
    }

    public Task<Void> updateUser(User user) {
        if (db == null || user.getUid() == null) {
            return Tasks.forException(new IllegalStateException("Lỗi dữ liệu"));
        }
        return db.collection("users").document(user.getUid()).set(user);
    }

    public Task<Void> updateFields(String uid, Map<String, Object> updates) {
        if (db == null || uid == null) {
            return Tasks.forException(new IllegalStateException("Lỗi dữ liệu"));
        }
        return db.collection("users").document(uid).update(updates);
    }
}