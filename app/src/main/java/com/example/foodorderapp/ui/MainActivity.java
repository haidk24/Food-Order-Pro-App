package com.example.foodorderapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Address;
import com.example.foodorderapp.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Chạy hàm test ngay khi mở app
        testFirestoreConnection();
    }

    private void testFirestoreConnection() {
        // 1. Gọi cánh cửa kết nối với Tủ hồ sơ Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. Tạo một đối tượng Address (Địa chỉ giả lập)
        Address testAddress = new Address();
        testAddress.setStreet("123 Đường Cầu Giấy");
        testAddress.setWard("Dịch Vọng");
        testAddress.setDistrict("Cầu Giấy");
        testAddress.setCity("Hà Nội");

        // 3. Tạo một đối tượng User (Khách hàng giả lập)
        User testUser = new User();
        testUser.setUid("TEST_UID_001");
        testUser.setDisplayName("Nguyễn Văn Test");
        testUser.setEmail("test@gmail.com");
        testUser.setPhone("0987654321");
        testUser.setRole("customer");
        testUser.setAddress(testAddress);

        // 4. Ra lệnh đẩy dữ liệu lên ngăn kéo "users"
        db.collection("users").document(testUser.getUid())
                .set(testUser)
                .addOnSuccessListener(aVoid -> {
                    // Nếu thành công, báo Log và hiện Toast
                    Log.d("TEST_FIREBASE", "Đẩy dữ liệu User thành công rực rỡ!");
                    Toast.makeText(MainActivity.this, "Kết nối Database OK!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    // Nếu thất bại, in ra nguyên nhân đỏ chót
                    Log.e("TEST_FIREBASE", "Lỗi rồi: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}