package com.example.foodorderapp.ui.adapter;

import android.content.Context;
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
import com.example.foodorderapp.data.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
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

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - dd/MM/yyyy", Locale.getDefault());
        if (order.getCreatedAt() != null) {
            holder.tvOrderTime.setText(sdf.format(order.getCreatedAt()));
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvTotalAmount.setText(format.format(order.getTotalAmount()));

        String status = order.getStatus();
        if ("pending".equals(status)) {
            holder.tvOrderStatus.setText("ĐANG CHỜ XÁC NHẬN");
            holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"));
            holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.btnUpdateStatus.setText("XÁC NHẬN ĐƠN");
            holder.btnUpdateStatus.setEnabled(true);
        } else if ("confirmed".equals(status)) {
            holder.tvOrderStatus.setText("ĐANG CHUẨN BỊ MÓN");
            holder.tvOrderStatus.setTextColor(Color.parseColor("#1565C0"));
            holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.btnUpdateStatus.setText("GIAO CHO SHIPPER");
            holder.btnUpdateStatus.setEnabled(true);
        } else {
            holder.tvOrderStatus.setText("ĐANG GIAO HÀNG");
            holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.btnUpdateStatus.setEnabled(false);
        }

        holder.btnUpdateStatus.setOnClickListener(v -> {
            String nextStatus = "pending".equals(status) ? "confirmed" : "shipping";
            db.collection("restaurants").document("REST_001")
                    .collection("orders").document(order.getOrderId())
                    .update("status", nextStatus)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderTime, tvTotalAmount, tvOrderStatus;
        Button btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}