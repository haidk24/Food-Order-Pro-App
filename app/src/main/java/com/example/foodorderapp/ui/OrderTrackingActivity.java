package com.example.foodorderapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;
import com.example.foodorderapp.viewModel.OrderTrackingViewModel;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private OrderTrackingViewModel viewModel;
    private GoogleMap mMap;

    // UI Elements
    private TextView tvOrderTitle, tvDriverName, tvEstimatedTime;
    private TextView tvStatusConfirmed, tvStatusPreparing, tvStatusShipping, tvStatusDelivered;

    private int currentOrderId = 1042; // Lấy từ Intent thực tế

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        initViews();
        initMap();

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(OrderTrackingViewModel.class);

        // Lắng nghe dữ liệu
        observeViewModel();

        // Bắt đầu load dữ liệu
        viewModel.loadOrderDetails(currentOrderId);
    }

    private void initViews() {
        tvOrderTitle = findViewById(R.id.tvOrderTitle);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);

        tvStatusConfirmed = findViewById(R.id.tvStatusConfirmed);
        tvStatusPreparing = findViewById(R.id.tvStatusPreparing);
        tvStatusShipping = findViewById(R.id.tvStatusShipping);
        tvStatusDelivered = findViewById(R.id.tvStatusDelivered);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void observeViewModel() {
        // Lắng nghe thay đổi của Đơn hàng
        viewModel.getOrder().observe(this, order -> {
            if (order != null) {
                tvOrderTitle.setText("Theo dõi đơn #" + order.getOrderId());
                updateTimelineUI(order.getStatus());

                // Nếu có shipperId thì lấy thông tin tài xế
                if (order.getShipperId() != null) {
                    viewModel.loadDriverInfo(order.getShipperId());
                }
            }
        });

        // Lắng nghe thay đổi thông tin Tài xế
        viewModel.getDriver().observe(this, driver -> {
            if (driver != null) {
                tvDriverName.setText("Tài xế: " + driver.getDisplayName());
            }
        });

        // Lắng nghe thay đổi thời gian ước tính
        viewModel.getEstimatedTime().observe(this, time -> {
            tvEstimatedTime.setText("Ước tính: " + time);
        });
    }

    private void updateTimelineUI(String status) {
        // Reset color trước
        int colorInactive = Color.parseColor("#9E9E9E"); // Xám
        int colorActive = Color.parseColor("#388E3C");   // Xanh lá (Hoàn thành)
        int colorCurrent = Color.parseColor("#1A73E8");  // Xanh dương (Đang xử lý)

        // Tùy theo status trong Enum của DB: pending -> confirmed -> preparing -> shipping -> delivered
        switch (status) {
            case "confirmed":
                tvStatusConfirmed.setTextColor(colorCurrent);
                tvStatusPreparing.setTextColor(colorInactive);
                tvStatusShipping.setTextColor(colorInactive);
                tvStatusDelivered.setTextColor(colorInactive);
                break;
            case "preparing":
                tvStatusConfirmed.setTextColor(colorActive);
                tvStatusPreparing.setTextColor(colorCurrent);
                tvStatusShipping.setTextColor(colorInactive);
                tvStatusDelivered.setTextColor(colorInactive);
                break;
            case "shipping":
                tvStatusConfirmed.setTextColor(colorActive);
                tvStatusPreparing.setTextColor(colorActive);
                tvStatusShipping.setTextColor(colorCurrent);
                tvStatusDelivered.setTextColor(colorInactive);
                break;
            case "delivered":
                tvStatusConfirmed.setTextColor(colorActive);
                tvStatusPreparing.setTextColor(colorActive);
                tvStatusShipping.setTextColor(colorActive);
                tvStatusDelivered.setTextColor(colorActive);
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Logic mô phỏng vị trí map (Trong thực tế bạn sẽ lấy GeoPoint từ Order/Driver Realtime)
        LatLng hanoi = new LatLng(21.0285, 105.8542);
        mMap.addMarker(new MarkerOptions().position(hanoi).title("Vị trí tài xế"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 15f));
    }
}
