package com.example.foodorderapp.ui.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodorderapp.R;
import com.example.foodorderapp.viewModel.AdminViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private AdminViewModel viewModel;
    private BottomNavigationView bottomNav;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // Setup Navigation
        setupNavigation();

        // Kiểm tra quyền Admin
        checkAdminRole();

        // Observe badge nhà hàng chờ duyệt
        observeBadge();

        // Observe loading và lỗi
        observeStates();
    }

    private void setupNavigation() {
        bottomNav = findViewById(R.id.admin_bottom_nav);

        androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager()
                .findFragmentById(R.id.admin_nav_host);

        if (navHostFragment instanceof NavHostFragment) {
            navController = ((NavHostFragment) navHostFragment).getNavController();
            
            if (bottomNav != null && navController != null) {
                NavigationUI.setupWithNavController(bottomNav, navController);

                navController.addOnDestinationChangedListener((controller, destination, args) -> {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(destination.getLabel());
                    }
                });
            }
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy Navigation Host", Toast.LENGTH_LONG).show();
        }
    }

    private void checkAdminRole() {
        viewModel.getCurrentUserRole().observe(this, role -> {
            // Nếu role đã trả về (không null) và không phải admin
            if (role != null && !role.equals("admin")) {
                Toast.makeText(this, "Bạn không có quyền truy cập khu vực Quản trị!", Toast.LENGTH_LONG).show();
                finish(); // Đóng màn hình Admin
            }
        });
    }

    private void observeBadge() {
        viewModel.getPendingRestaurantCount().observe(this, count -> {
            if (bottomNav != null && count != null && count > 0) {
                BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_restaurants);
                badge.setNumber(count);
                badge.setVisible(true);
            } else if (bottomNav != null) {
                bottomNav.removeBadge(R.id.nav_restaurants);
            }
        });
    }

    private void observeStates() {
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
