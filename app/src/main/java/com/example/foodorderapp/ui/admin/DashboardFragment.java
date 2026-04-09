package com.example.foodorderapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.AdminStats;

public class DashboardFragment extends Fragment {

    private AdminViewModel viewModel;

    // Views
    private TextView tvTotalUsers, tvTotalRestaurants;
    private TextView tvTotalOrders, tvRevenue;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy ViewModel từ Activity (dùng chung với AdminActivity)
        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        bindViews(view);
        observeData();
    }

    private void bindViews(View view) {
        tvTotalUsers        = view.findViewById(R.id.tv_total_users);
        tvTotalRestaurants  = view.findViewById(R.id.tv_total_restaurants);
        tvTotalOrders       = view.findViewById(R.id.tv_total_orders);
        tvRevenue           = view.findViewById(R.id.tv_revenue);
        progressBar         = view.findViewById(R.id.progress_bar);
    }

    private void observeData() {
        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Dữ liệu thống kê
        viewModel.getAdminStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) updateUI(stats);
        });
    }

    private void updateUI(AdminStats stats) {
        tvTotalUsers.setText(String.valueOf(stats.totalUsers));
        tvTotalRestaurants.setText(String.valueOf(stats.totalRestaurants));
        tvTotalOrders.setText(String.valueOf(stats.totalOrdersToday));

        // Format tiền: 52400000 → "52.4M"
        tvRevenue.setText(formatRevenue(stats.revenueToday));
    }

    private String formatRevenue(long amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.1fK", amount / 1_000.0);
        }
        return String.valueOf(amount);
    }
}