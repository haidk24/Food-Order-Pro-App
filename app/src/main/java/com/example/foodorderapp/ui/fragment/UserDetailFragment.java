package com.example.foodorderapp.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.viewModel.AdminViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDetailFragment extends Fragment {

    private AdminViewModel viewModel;
    private User currentUser;

    // Views
    private TextView tvInitials, tvName, tvEmail, tvStatusBadge;
    private TextView tvPhone, tvRole, tvCreatedAt, tvOrderCount;
    private Button   btnBanUnban, btnSendNotif;
    private ImageButton btnBack;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        bindViews(view);
        setupListeners(view);
        observeData();

        // Lấy uid từ arguments (Navigation Safe Args)
        if (getArguments() != null) {
            String uid = getArguments().getString("uid", "");
            if (!uid.isEmpty()) {
                viewModel.loadUserDetail(uid);
            }
        }
    }

    private void bindViews(View v) {
        btnBack      = v.findViewById(R.id.btn_back);
        tvInitials   = v.findViewById(R.id.tv_initials);
        tvName       = v.findViewById(R.id.tv_name);
        tvEmail      = v.findViewById(R.id.tv_email);
        tvStatusBadge = v.findViewById(R.id.tv_status_badge);
        tvPhone      = v.findViewById(R.id.tv_phone);
        tvRole       = v.findViewById(R.id.tv_role);
        tvCreatedAt  = v.findViewById(R.id.tv_created_at);
        tvOrderCount = v.findViewById(R.id.tv_order_count);
        btnBanUnban  = v.findViewById(R.id.btn_ban_unban);
        btnSendNotif = v.findViewById(R.id.btn_send_notification);
    }

    private void setupListeners(View view) {
        // Nút quay lại màn hình danh sách user
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        }
    }

    private void observeData() {
        // Load xong user
        viewModel.getSelectedUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                renderUser(user);
            }
        });

        // Kết quả action (ban/unban)
        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(),
                        "Thao tác thành công!", Toast.LENGTH_SHORT).show();
                // Load lại thông tin user
                if (currentUser != null) {
                    viewModel.loadUserDetail(currentUser.getUid());
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderUser(User user) {
        // Avatar initials
        tvInitials.setText(getInitials(user.getDisplayName()));
        tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "—");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
        tvRole.setText(mapRole(user.getRole()));
        tvOrderCount.setText(user.getOrderCount() + " đơn");
        tvCreatedAt.setText(formatDate(user.createdAt));

        // Trạng thái
        boolean isBanned = "banned".equals(user.getStatus());
        if (isBanned) {
            tvStatusBadge.setText("Bị khóa");
            tvStatusBadge.setTextColor(0xFFFF3B30);
            btnBanUnban.setText("Mở khóa tài khoản");
            btnBanUnban.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE8F5EC));
            btnBanUnban.setTextColor(0xFF1E6B3A);
        } else {
            tvStatusBadge.setText("Hoạt động");
            tvStatusBadge.setTextColor(0xFF34C759);
            btnBanUnban.setText("Khóa tài khoản");
            btnBanUnban.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFDECEA));
            btnBanUnban.setTextColor(0xFF8B1A1A);
        }

        // Không cho phép Admin tự khóa chính mình
        String currentUid = "";
        try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        } catch (Exception ignored) {}
        
        btnBanUnban.setEnabled(!user.getUid().equals(currentUid));

        // Nút Ban/Unban
        btnBanUnban.setOnClickListener(v -> {
            if (isBanned) {
                showConfirmDialog(
                        "Mở khóa tài khoản",
                        "Mở khóa \"" + user.getDisplayName() + "\"?",
                        () -> viewModel.unbanUser(user.getUid()));
            } else {
                showConfirmDialog(
                        "Khóa tài khoản",
                        "Khóa \"" + user.getDisplayName() + "\"?\nHọ sẽ không thể đăng nhập.",
                        () -> viewModel.banUser(user.getUid()));
            }
        });

        // Nút Gửi thông báo
        btnSendNotif.setOnClickListener(v -> showSendNotifDialog(user));
    }

    // Dialog xác nhận Ban/Unban
    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (d, w) -> onConfirm.run())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // Dialog gửi thông báo tới user
    private void showSendNotifDialog(User user) {
        android.widget.EditText etMessage = new android.widget.EditText(
                requireContext());
        etMessage.setHint("Nhập nội dung thông báo...");
        etMessage.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Gửi thông báo tới " + user.getDisplayName())
                .setView(etMessage)
                .setPositiveButton("Gửi", (d, w) -> {
                    String msg = etMessage.getText().toString().trim();
                    if (msg.isEmpty()) return;
                    Toast.makeText(requireContext(),
                            "Đã gửi thông báo!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String getInitials(String name) {
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

    private String formatDate(Object createdAt) {
        if (createdAt == null) return "—";
        try {
            if (createdAt instanceof com.google.firebase.Timestamp) {
                Date date = ((com.google.firebase.Timestamp) createdAt).toDate();
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(date);
            }
        } catch (Exception e) { /* ignore */ }
        return "—";
    }
}