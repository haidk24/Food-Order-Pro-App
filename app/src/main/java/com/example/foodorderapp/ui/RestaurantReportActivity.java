package com.example.foodorderapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.foodorderapp.R;
import com.example.foodorderapp.ui.fragment.ReportFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RestaurantReportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String currentRestaurantId;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_report);

        currentRestaurantId = getIntent().getStringExtra("EXTRA_RESTAURANT_ID");

        drawerLayout = findViewById(R.id.drawer_layout_report);
        navigationView = findViewById(R.id.nav_right_report);
        navigationView.setNavigationItemSelectedListener(this);
        showUserInformation();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_open_right_nav) {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            ReportFragment fragment = new ReportFragment();
            Bundle args = new Bundle();
            args.putString(ReportFragment.ARG_RESTAURANT_ID, currentRestaurantId);
            fragment.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.report_container, fragment)
                    .commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_report);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_menu) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_order) {
                Intent intent = new Intent(getApplicationContext(), OrderDashboardActivity.class);
                intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
    }

    private void showUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.textView);
        tvEmail.setText(user.getEmail());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_report) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_menu) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_order) {
            Intent intent = new Intent(this, OrderDashboardActivity.class);
            intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}


