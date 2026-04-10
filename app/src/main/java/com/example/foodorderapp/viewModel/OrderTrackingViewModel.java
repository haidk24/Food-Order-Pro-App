package com.example.foodorderapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodorderapp.data.model.Order;
import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.data.repository.OrderRepository;

public class OrderTrackingViewModel extends ViewModel {
    private final OrderRepository repository;

    private final MutableLiveData<Order> orderLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> driverLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> estimatedTimeLiveData = new MutableLiveData<>();

    public OrderTrackingViewModel() {
        repository = new OrderRepository();
        // Mặc định set thời gian ước tính (Thực tế tính toán bằng Google Distance Matrix API)
        estimatedTimeLiveData.setValue("15 phút");
    }

    public LiveData<Order> getOrder() { return orderLiveData; }
    public LiveData<User> getDriver() { return driverLiveData; }
    public LiveData<String> getEstimatedTime() { return estimatedTimeLiveData; }

    public void loadOrderDetails(int orderId) {
        repository.listenToOrder(orderId, orderLiveData);
    }

    // Được gọi khi order thay đổi, để fetch thông tin tài xế nếu có shipperId
    public void loadDriverInfo(String shipperId) {
        if (shipperId != null && !shipperId.isEmpty()) {
            repository.getDriverInfo(shipperId, driverLiveData);
        }
    }
}
