package com.example.foodorderapp.data.repository;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodorderapp.data.model.User;
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static UserRepository instance;
    // Tạm thời comment Firebase để tránh crash khi chưa có config
    // private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Singleton
    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    // ── Lấy toàn bộ danh sách user ───────────────────────────────
    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();

        // Tạm thời luôn trả về Test Data để tránh crash Firebase
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            liveData.postValue(getTestUsers());
        }, 500);

        /* Code gốc dùng Firebase - Bỏ comment khi đã config Firebase thành công
        db.collection("users")
                .orderBy("displayName")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        User u = doc.toObject(User.class);
                        u.setUid(doc.getId());
                        users.add(u);
                    }
                    liveData.postValue(users);
                })
                .addOnFailureListener(e -> {
                    liveData.postValue(getTestUsers());
                });
        */

        return liveData;
    }

    // ── Khóa tài khoản ───────────────────────────────────────────
    public LiveData<Boolean> banUser(String uid) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // Giả lập thành công cho test data
        result.postValue(true);
        return result;
    }

    // ── Mở khóa tài khoản ────────────────────────────────────────
    public LiveData<Boolean> unbanUser(String uid) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // Giả lập thành công cho test data
        result.postValue(true);
        return result;
    }

    // ── TEST DATA ───────────────────────────────────────────────
    public List<User> getTestUsers() {
        List<User> list = new ArrayList<>();
        list.add(new User("uid1", "Nguyễn Minh",     "minh@gmail.com",       "0901234567", "customer",   "active", 24));
        list.add(new User("uid2", "Bà Lan (Cơm Tấm)","balan@restaurant.com", "0912345678", "restaurant", "active", 312));
        list.add(new User("uid3", "Nguyễn Văn A",    "vana@shipper.com",     "0923456789", "shipper",    "active", 156));
        list.add(new User("uid4", "Phạm Quốc Bảo",   "bao@gmail.com",        "0934567890", "customer",   "banned", 3));
        list.add(new User("uid5", "Trần Thị Hoa",    "hoa@gmail.com",        "0945678901", "customer",   "active", 47));
        list.add(new User("uid6", "Lê Văn Bình",     "binh@shipper.com",     "0956789012", "shipper",    "active", 89));
        list.add(new User("uid7", "Phở 24 Restaurant","pho24@restaurant.com","0967890123", "restaurant", "active", 245));
        list.add(new User("uid8", "Hoàng Thu Trang",  "trang@gmail.com",     "0978901234", "customer",   "active", 12));
        return list;
    }
}