package com.example.foodorderapp.ui.admin.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;

public class UserAdapter extends ListAdapter<User, UserAdapter.VH> {

    public interface OnUserClick { void onClick(User user); }

    private final OnUserClick listener;

    public UserAdapter(OnUserClick listener) {
        super(new DiffUtil.ItemCallback<User>() {
            @Override public boolean areItemsTheSame(@NonNull User a, @NonNull User b) {
                return a.getUid().equals(b.getUid());
            }
            @Override public boolean areContentsTheSame(@NonNull User a, @NonNull User b) {
                return a.getUid().equals(b.getUid())
                        && safeEqual(a.getDisplayName(), b.getDisplayName())
                        && safeEqual(a.getRole(), b.getRole())
                        && safeEqual(a.getStatus(), b.getStatus());
            }
            private boolean safeEqual(String a, String b) {
                if (a == null && b == null) return true;
                if (a == null || b == null) return false;
                return a.equals(b);
            }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.bind(getItem(pos), listener);
    }

    // ── ViewHolder ────────────────────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {

        final TextView tvInitials, tvName, tvEmail,
                tvRole, tvOrderCount, tvStatus;

        VH(View v) {
            super(v);
            tvInitials   = v.findViewById(R.id.tv_initials);
            tvName       = v.findViewById(R.id.tv_user_name);
            tvEmail      = v.findViewById(R.id.tv_user_email);
            tvRole       = v.findViewById(R.id.tv_user_role);
            tvOrderCount = v.findViewById(R.id.tv_order_count);
            tvStatus     = v.findViewById(R.id.tv_user_status);
        }

        void bind(User user, OnUserClick listener) {
            tvInitials.setText(initials(user.getDisplayName()));
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "—");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
            tvRole.setText(mapRole(user.getRole()));
            tvOrderCount.setText((user.getOrderCount() > 0 ? user.getOrderCount() : 0) + " đơn");

            // Màu trạng thái
            if ("banned".equals(user.getStatus())) {
                tvStatus.setText("Bị khóa");
                tvStatus.setTextColor(0xFFFF3B30);
            } else {
                tvStatus.setText("Hoạt động");
                tvStatus.setTextColor(0xFF34C759);
            }

            itemView.setOnClickListener(v -> listener.onClick(user));
        }

        // "Nguyễn Văn Minh" → "NM"
        private String initials(String name) {
            if (name == null || name.trim().isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1)
                return String.valueOf(parts[0].charAt(0)).toUpperCase();
            return (String.valueOf(parts[0].charAt(0))
                    + String.valueOf(parts[parts.length - 1].charAt(0)))
                    .toUpperCase();
        }

        private String mapRole(String role) {
            if (role == null) return "—";
            switch (role) {
                case "customer":   return "Khách hàng";
                case "restaurant": return "Nhà hàng";
                case "shipper":    return "Shipper";
                case "admin":      return "Admin";
                default:           return role;
            }
        }
    }
}