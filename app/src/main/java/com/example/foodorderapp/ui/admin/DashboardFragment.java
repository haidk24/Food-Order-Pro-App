package com.example.foodorderapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.AdminStats;
import com.example.foodorderapp.viewModel.AdminViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private AdminViewModel viewModel;

    // Views
    private ProgressBar    progressBar;
    private TextView       tvTotalUsers;
    private TextView       tvTotalRestaurants, tvPendingBadge;
    private TextView       tvTotalOrders;
    private TextView       tvRevenue;
    private MaterialButton btnRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dùng chung ViewModel với AdminActivity
        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        bindViews(view);
        observeData();
        setupRefreshButton();
    }

    // ── Bind views ────────────────────────────────────────────────
    private void bindViews(View v) {
        progressBar        = v.findViewById(R.id.progress_bar);
        tvTotalUsers       = v.findViewById(R.id.tv_total_users);
        tvTotalRestaurants = v.findViewById(R.id.tv_total_restaurants);
        tvPendingBadge     = v.findViewById(R.id.tv_pending_badge);
        tvTotalOrders      = v.findViewById(R.id.tv_total_orders);
        tvRevenue          = v.findViewById(R.id.tv_revenue);
        btnRefresh         = v.findViewById(R.id.btn_refresh);
    }

    // ── Observe LiveData ──────────────────────────────────────────
    private void observeData() {

        // Loading spinner
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            btnRefresh.setEnabled(!Boolean.TRUE.equals(loading));
        });

        // 4 metric cards
        viewModel.getAdminStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) renderStats(stats);
        });

        // Badge nhà hàng chờ duyệt — cập nhật realtime
        viewModel.getPendingRestaurantCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                tvPendingBadge.setText(count + " chờ duyệt");
                tvPendingBadge.setTextColor(0xFFFF9500);
            } else {
                tvPendingBadge.setText("Không có đơn chờ");
                tvPendingBadge.setTextColor(0xFF8A8A8E);
            }
        });

        // Lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Render 4 metric cards ─────────────────────────────────────
    private void renderStats(AdminStats stats) {
        // Card 1: Người dùng
        tvTotalUsers.setText(formatNumber(stats.totalUsers));

        // Card 2: Nhà hàng (badge xử lý ở observePendingRestaurantCount)
        tvTotalRestaurants.setText(formatNumber(stats.totalRestaurants));

        // Card 3: Đơn hàng hôm nay
        tvTotalOrders.setText(formatNumber(stats.totalOrdersToday));

        // Card 4: Doanh thu
        tvRevenue.setText(formatRevenue(stats.revenueToday));
    }

    private void setupRefreshButton() {
        btnRefresh.setOnClickListener(v -> viewModel.loadDashboardStats());
    }

    // ── Format helpers ────────────────────────────────────────────

    // 1248 → "1,248"
    private String formatNumber(long n) {
        return NumberFormat.getNumberInstance(Locale.US).format(n);
    }

    // 52400000 → "52.4M đ"
    private String formatRevenue(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format(Locale.US, "%.1fB đ", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000L) {
            return String.format(Locale.US, "%.1fM đ", amount / 1_000_000.0);
        } else if (amount >= 1_000L) {
            return String.format(Locale.US, "%.0fK đ", amount / 1_000.0);
        }
        return NumberFormat.getNumberInstance(Locale.US).format(amount) + " đ";
    }
}