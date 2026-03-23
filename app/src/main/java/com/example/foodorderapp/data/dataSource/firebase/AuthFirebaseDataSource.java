package com.example.foodorderapp.data.dataSource.firebase;

import com.example.foodorderapp.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthFirebaseDataSource {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public AuthFirebaseDataSource() {
        FirebaseAuth authInstance;
        FirebaseFirestore dbInstance;
        try {
            authInstance = FirebaseAuth.getInstance();
            dbInstance = FirebaseFirestore.getInstance();
        } catch (IllegalStateException ex) {
            authInstance = null;
            dbInstance = null;
        }
        auth = authInstance;
        db = dbInstance;
    }

    // ================= REGISTER =================
    public Task<Void> register(String email, String password, User user) {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase chua duoc cau hinh. Vui long them google-services.json"));
        }

        TaskCompletionSource<Void> taskSource = new TaskCompletionSource<>();

        // 1. tạo tài khoản Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // 2. lấy uid
                        String uid = auth.getCurrentUser().getUid();
                        user.setId(uid);

                        // 3. lưu user vào Firestore
                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    taskSource.setResult(null); // SUCCESS
                                })
                                .addOnFailureListener(e -> {
                                    taskSource.setException(e);
                                });

                    } else {
                        taskSource.setException(task.getException());
                    }
                });

        return taskSource.getTask();
    }

    // ================= LOGIN =================
    public Task<User> login(String email, String password) {
        if (auth == null || db == null) {
            return Tasks.forException(new IllegalStateException("Firebase chua duoc cau hinh. Vui long them google-services.json"));
        }

        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        // 1. đăng nhập
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();

                        // 2. lấy thông tin user
                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(document -> {

                                    User user = document.toObject(User.class);

                                    // 3. trả về user
                                    taskSource.setResult(user);
                                })
                                .addOnFailureListener(e -> {
                                    taskSource.setException(e);
                                });

                    } else {
                        taskSource.setException(task.getException());
                    }
                });

        return taskSource.getTask();
    }
}
