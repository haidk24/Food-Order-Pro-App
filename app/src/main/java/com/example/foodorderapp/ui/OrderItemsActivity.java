package com.example.foodorderapp.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderItemsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_items);

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvOrderId = findViewById(R.id.tv_order_id);
        TextView tvStatus = findViewById(R.id.tv_order_status);
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
        tvStatus.setText(status == null ? "pending" : status);
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
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "Chua cap nhat" : value.trim();
    }
}

