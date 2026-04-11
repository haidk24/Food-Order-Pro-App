package com.example.foodorderapp.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderItemsActivity extends AppCompatActivity {

    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_items);

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvOrderId = findViewById(R.id.tv_order_id);
        TextView tvStatus = findViewById(R.id.tv_order_status);
        TextView tvTracking = findViewById(R.id.tv_order_tracking);
        TextView tvTotal = findViewById(R.id.tv_order_total);
        TextView tvCustomerName = findViewById(R.id.tv_customer_name);
        TextView tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        TextView tvCustomerAddress = findViewById(R.id.tv_customer_address);
        TextView tvItems = findViewById(R.id.tv_order_items);

        btnBack.setOnClickListener(v -> onBackPressed());

        String orderId = getIntent().getStringExtra("EXTRA_ORDER_ID");
        String status = getIntent().getStringExtra("EXTRA_STATUS");
        double total = getIntent().getDoubleExtra("EXTRA_TOTAL", 0);
        String customerName = getIntent().getStringExtra("EXTRA_CUSTOMER_NAME");
        String customerPhone = getIntent().getStringExtra("EXTRA_CUSTOMER_PHONE");
        String customerAddress = getIntent().getStringExtra("EXTRA_CUSTOMER_ADDRESS");
        ArrayList<String> itemLines = getIntent().getStringArrayListExtra("EXTRA_ITEM_LINES");

        tvOrderId.setText(orderId == null ? "--" : "#" + orderId);
        tvStatus.setText(renderStatus(status));
        tvTracking.setText(renderTracking(status, null, null, null));
        tvTotal.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(total));
        tvCustomerName.setText("Khach: " + safeText(customerName));
        tvCustomerPhone.setText("SDT: " + safeText(customerPhone));
        tvCustomerAddress.setText("Dia chi: " + safeText(customerAddress));

        if (itemLines == null || itemLines.isEmpty()) {
            tvItems.setText("Khong co du lieu mon an trong don nay.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (String line : itemLines) {
                builder.append(line).append("\n");
            }
            tvItems.setText(builder.toString().trim());
        }

        if (orderId != null && !orderId.trim().isEmpty()) {
            orderListener = FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(orderId)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null || snapshot == null || !snapshot.exists()) {
                            return;
                        }
                        Order order = snapshot.toObject(Order.class);
                        if (order == null) {
                            return;
                        }
                        tvStatus.setText(renderStatus(order.getStatus()));
                        tvTracking.setText(renderTracking(
                                order.getStatus(),
                                order.getConfirmedAt(),
                                order.getShippingAt(),
                                order.getDeliveredAt()
                        ));
                        tvTotal.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(order.getTotalAmount()));
                        tvCustomerName.setText("Khach: " + safeText(order.getCustomerName()));
                        tvCustomerPhone.setText("SDT: " + safeText(order.getCustomerPhone()));
                        tvCustomerAddress.setText("Dia chi: " + safeText(order.getCustomerAddress()));
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }

    private String renderStatus(String status) {
        String safeStatus = normalizeStatus(status);
        switch (safeStatus) {
            case "confirmed":
                return "Da xac nhan";
            case "shipping":
                return "Dang giao";
            case "delivered":
            case "completed":
                return "Da giao";
            case "cancelled":
                return "Da huy";
            case "pending":
            default:
                return "Dang cho xac nhan";
        }
    }

    private String renderTracking(String status, Date confirmedAt, Date shippingAt, Date deliveredAt) {
        String safeStatus = normalizeStatus(status);
        boolean confirmedDone = confirmedAt != null
                || "confirmed".equals(safeStatus)
                || "shipping".equals(safeStatus)
                || "delivered".equals(safeStatus)
                || "completed".equals(safeStatus);
        boolean shippingDone = shippingAt != null
                || "shipping".equals(safeStatus)
                || "delivered".equals(safeStatus)
                || "completed".equals(safeStatus);
        boolean deliveredDone = deliveredAt != null
                || "delivered".equals(safeStatus)
                || "completed".equals(safeStatus);

        StringBuilder builder = new StringBuilder();
        builder.append("1. Don da duoc tao");
        builder.append("\n2. ").append(renderStepText(
                confirmedDone,
                confirmedAt,
                "Nha hang da xac nhan",
                "Cho nha hang xac nhan"
        ));
        builder.append("\n3. ").append(renderStepText(
                shippingDone,
                shippingAt,
                "Shipper dang giao",
                "Chua ban giao shipper"
        ));
        builder.append("\n4. ").append(renderStepText(
                deliveredDone,
                deliveredAt,
                "Don da giao thanh cong",
                "Chua giao thanh cong"
        ));

        if ("cancelled".equals(safeStatus)) {
            builder.append("\n\nDon nay da bi huy.");
        }
        return builder.toString();
    }

    private String renderStepText(boolean done, Date at, String doneText, String pendingText) {
        if (!done) {
            return pendingText;
        }
        return at != null ? doneText + " (" + formatTime(at) + ")" : doneText;
    }

    private String normalizeStatus(String status) {
        return status == null ? "pending" : status.trim().toLowerCase(Locale.ROOT);
    }

    private String formatTime(Date date) {
        return new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(date);
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Chua cap nhat" : value.trim();
    }
}

