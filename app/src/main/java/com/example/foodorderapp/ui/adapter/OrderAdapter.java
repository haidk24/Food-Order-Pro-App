package com.example.foodorderapp.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.CartItem;
import com.example.foodorderapp.data.model.Order;
import com.example.foodorderapp.ui.OrderItemsActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final FirebaseFirestore db;
    private final String restaurantId;
    private final boolean restaurantMode;

    public OrderAdapter(Context context, List<Order> orderList, String restaurantId) {
        this.context = context;
        this.orderList = orderList;
        this.restaurantId = restaurantId == null ? "" : restaurantId;
        this.restaurantMode = true;
        this.db = FirebaseFirestore.getInstance();
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.restaurantId = "";
        this.restaurantMode = false;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        if (order.getCreatedAt() != null) {
            holder.tvOrderTime.setText(sdf.format(order.getCreatedAt()));
        } else {
            holder.tvOrderTime.setText("--:--");
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvTotalAmount.setText(format.format(order.getTotalAmount()));
        holder.tvCustomerInfo.setText(buildCustomerInfo(order));

        bindStatusUI(holder, order.getStatus());

        if (restaurantMode) {
            holder.itemView.setOnClickListener(v -> openOrderItemsScreen(order));
        } else {
            holder.itemView.setOnClickListener(null);
        }

        if (!restaurantMode) {
            holder.btnUpdateStatus.setVisibility(View.GONE);
            return;
        }

        holder.btnUpdateStatus.setVisibility(View.VISIBLE);
        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (restaurantId.isEmpty()) {
                Toast.makeText(context, "Khong tim thay nha hang hien tai", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentStatus = order.getStatus() == null ? "pending" : order.getStatus();
            String nextStatus = resolveNextStatus(currentStatus);
            if (nextStatus == null) {
                return;
            }

            WriteBatch batch = db.batch();
            batch.update(db.collection("restaurants").document(restaurantId)
                    .collection("orders").document(order.getOrderId()), "status", nextStatus);
            batch.update(db.collection("orders").document(order.getOrderId()), "status", nextStatus);

            batch.commit()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Da cap nhat trang thai", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Cap nhat that bai: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void bindStatusUI(OrderViewHolder holder, String status) {
        String safeStatus = status == null ? "pending" : status;
        switch (safeStatus) {
            case "pending":
                holder.tvOrderStatus.setText("DANG CHO XAC NHAN");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
                holder.btnUpdateStatus.setText("XAC NHAN DON");
                holder.btnUpdateStatus.setEnabled(true);
                break;
            case "confirmed":
                holder.tvOrderStatus.setText("DANG CHUAN BI MON");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1565C0"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E3F2FD"));
                holder.btnUpdateStatus.setText("GIAO CHO SHIPPER");
                holder.btnUpdateStatus.setEnabled(true);
                break;
            case "shipping":
                holder.tvOrderStatus.setText("DANG GIAO HANG");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.btnUpdateStatus.setText("HOAN TAT DON");
                holder.btnUpdateStatus.setEnabled(true);
                break;
            case "delivered":
                holder.tvOrderStatus.setText("DA GIAO THANH CONG");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#1B5E20"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.btnUpdateStatus.setText("DA HOAN TAT");
                holder.btnUpdateStatus.setEnabled(false);
                break;
            default:
                holder.tvOrderStatus.setText(safeStatus.toUpperCase(Locale.ROOT));
                holder.tvOrderStatus.setTextColor(Color.parseColor("#455A64"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#ECEFF1"));
                holder.btnUpdateStatus.setEnabled(false);
                break;
        }
    }

    private String resolveNextStatus(String currentStatus) {
        if ("pending".equals(currentStatus)) {
            return "confirmed";
        }
        if ("confirmed".equals(currentStatus)) {
            return "shipping";
        }
        if ("shipping".equals(currentStatus)) {
            return "delivered";
        }
        return null;
    }

    private void openOrderItemsScreen(Order order) {
        ArrayList<String> itemLines = new ArrayList<>();
        List<CartItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            itemLines.add("Khong co du lieu mon an trong don nay.");
        } else {
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                if (item == null) {
                    continue;
                }
                String itemName = item.getName() == null || item.getName().trim().isEmpty()
                        ? "Mon an"
                        : item.getName().trim();
                int quantity = Math.max(item.getQuantity(), 0);
                itemLines.add((i + 1) + ". " + itemName + "  x" + quantity);
            }
        }

        if (itemLines.isEmpty()) {
            itemLines.add("Khong co du lieu mon an trong don nay.");
        }

        Intent intent = new Intent(context, OrderItemsActivity.class);
        intent.putExtra("EXTRA_ORDER_ID", order.getOrderId());
        intent.putExtra("EXTRA_TOTAL", order.getTotalAmount());
        intent.putExtra("EXTRA_STATUS", order.getStatus());
        intent.putExtra("EXTRA_CUSTOMER_NAME", order.getCustomerName());
        intent.putExtra("EXTRA_CUSTOMER_PHONE", order.getCustomerPhone());
        intent.putExtra("EXTRA_CUSTOMER_ADDRESS", order.getCustomerAddress());
        intent.putStringArrayListExtra("EXTRA_ITEM_LINES", itemLines);
        context.startActivity(intent);
    }

    private String buildCustomerInfo(Order order) {
        String name = safeText(order.getCustomerName(), "Chua cap nhat");
        String phone = safeText(order.getCustomerPhone(), "Chua cap nhat");
        String address = safeText(order.getCustomerAddress(), "Chua cap nhat");
        return "Khach: " + name + " - " + phone + "\nDia chi: " + address;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderTime, tvTotalAmount, tvOrderStatus, tvCustomerInfo;
        Button btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvCustomerInfo = itemView.findViewById(R.id.tvCustomerInfo);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}