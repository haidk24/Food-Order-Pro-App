package com.example.foodorderapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.foodorderapp.data.model.Order;
import com.example.foodorderapp.data.model.User;

public class OrderRepository {
    // Trong thực tế, bạn sẽ dùng FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void listenToOrder(int orderId, MutableLiveData<Order> orderLiveData) {
        // Mô phỏng lắng nghe Realtime từ Firestore
        /*
        db.collection("orders").document(orderId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Order order = snapshot.toObject(Order.class);
                orderLiveData.postValue(order);
            }
        });
        */

        // Mock data để test
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setShipperId("shipper_123");
        mockOrder.setStatus("shipping");
        orderLiveData.postValue(mockOrder);
    }

    public void getDriverInfo(String shipperId, MutableLiveData<User> driverLiveData) {
        // Mô phỏng fetch user (tài xế)
        /*
        db.collection("users").document(shipperId).get().addOnSuccessListener(doc -> {
            User driver = doc.toObject(User.class);
            driverLiveData.postValue(driver);
        });
        */

        // Mock data
        User mockDriver = new User();
        mockDriver.setDisplayName("Nguyễn Văn A");
        driverLiveData.postValue(mockDriver);
    }
}
