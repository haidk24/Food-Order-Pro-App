package com.example.foodorderapp.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodorderapp.R;
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
        setSupportActionBar(toolbar);

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

        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.admin_nav_host);

        if (navHost != null) {
            navController = navHost.getNavController();
            // Kết nối BottomNavigationView với NavController
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Cập nhật Toolbar title theo tab đang chọn
            navController.addOnDestinationChangedListener((controller, destination, args) -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(destination.getLabel());
                }
            });
        }
    }

    private void checkAdminRole() {
        viewModel.getCurrentUserRole().observe(this, role -> {
            if (role == null || !role.equals("admin")) {
                // Tạm thời log để test
                android.util.Log.w("AdminActivity", "User không phải admin, role = " + role);
            }
        });
    }

    private void observeBadge() {
        viewModel.getPendingRestaurantCount().observe(this, count -> {
            if (count != null && count > 0) {
                BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_restaurants);
                badge.setNumber(count);
                badge.setVisible(true);
            } else {
                bottomNav.removeBadge(R.id.nav_restaurants);
            }
        });
    }

    private void observeStates() {
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}