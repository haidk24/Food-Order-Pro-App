package com.example.foodorderapp.ui.admin.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;

public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    // Callback xử lý sự kiện ban/unban
    public interface OnUserActionListener {
        void onToggleBan(User user);
    }

    private final OnUserActionListener listener;

    public UserAdapter(OnUserActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // DiffUtil tự động animate thay đổi list
    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User a, @NonNull User b) {
                    return a.getUid().equals(b.getUid());
                }
                @Override
                public boolean areContentsTheSame(@NonNull User a, @NonNull User b) {
                    return a.getStatus().equals(b.getStatus())
                            && a.getDisplayName().equals(b.getDisplayName());
                }
            };

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // ── ViewHolder ────────────────────────────────────────────────
    static class UserViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAvatar, tvName, tvEmail;
        private final TextView tvPhone, tvOrderCount;
        private final TextView tvRoleBadge, tvStatusBadge;
        private final Button btnToggleBan;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar      = itemView.findViewById(R.id.tv_avatar);
            tvName        = itemView.findViewById(R.id.tv_name);
            tvEmail       = itemView.findViewById(R.id.tv_email);
            tvPhone       = itemView.findViewById(R.id.tv_phone);
            tvOrderCount  = itemView.findViewById(R.id.tv_order_count);
            tvRoleBadge   = itemView.findViewById(R.id.tv_role_badge);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            btnToggleBan  = itemView.findViewById(R.id.btn_toggle_ban);
        }

        public void bind(User user, OnUserActionListener listener) {
            // Avatar: lấy 2 ký tự đầu tên
            String name = user.getDisplayName();
            tvAvatar.setText(getInitials(name));
            tvName.setText(name);
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhone());
            tvOrderCount.setText(user.getOrderCount() + " đơn");

            // Badge role
            bindRoleBadge(user.getRole());

            // Badge status + nút hành động
            bindStatus(user.isBanned());

            // Click nút Khóa / Mở khóa
            btnToggleBan.setOnClickListener(v -> listener.onToggleBan(user));
        }

        private void bindRoleBadge(String role) {
            Context ctx = itemView.getContext();
            switch (role) {
                case "restaurant":
                    tvRoleBadge.setText("Nhà hàng");
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_green);
                    break;
                case "shipper":
                    tvRoleBadge.setText("Shipper");
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_amber);
                    break;
                case "admin":
                    tvRoleBadge.setText("Admin");
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_purple);
                    break;
                default: // customer
                    tvRoleBadge.setText("Khách hàng");
                    tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_blue);
                    break;
            }
        }

        private void bindStatus(boolean isBanned) {
            if (isBanned) {
                tvStatusBadge.setText("Bị khóa");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
                btnToggleBan.setText("Mở khóa");
            } else {
                tvStatusBadge.setText("Hoạt động");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
                btnToggleBan.setText("Khóa");
            }
        }

        private String getInitials(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                // Lấy chữ cái đầu của từ đầu và từ cuối
                return String.valueOf(parts[0].charAt(0)).toUpperCase()
                        + String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
            }
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        }
    }
}