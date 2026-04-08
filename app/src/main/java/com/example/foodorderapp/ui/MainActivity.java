package com.example.foodorderapp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodorderapp.R;
import com.example.foodorderapp.ui.fragment.HomeFragment;
import com.example.foodorderapp.ui.fragment.MyOrderFragment;
import com.example.foodorderapp.ui.fragment.ShoppingcartFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_bottom);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                navigationView.setCheckedItem(R.id.nav_home);
            } else if (id == R.id.nav_cart) {
                replaceFragment(new ShoppingcartFragment());
                navigationView.setCheckedItem(R.id.nav_cart);
            } else if (id == R.id.nav_myorder) {
                replaceFragment(new MyOrderFragment());
                navigationView.setCheckedItem(R.id.nav_myorder);
            }
            return true;
        });

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        
    }






    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            replaceFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (id == R.id.nav_cart) {
            replaceFragment(new ShoppingcartFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        } else if (id == R.id.nav_myorder) {
            replaceFragment(new MyOrderFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_myorder);
        } else if (id == R.id.nav_logout) {
            // Xử lý logout
        } else if (id == R.id.nav_replacepassword) {
            // Xử lý đổi mật khẩu
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        // 1. Xóa trạng thái đăng nhập trong Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

        // 2. Chuyển về màn hình SignInActivity
        android.content.Intent intent = new Intent(this, com.example.foodorderapp.ui.SignInActivity.class);

        // 3. QUAN TRỌNG: Xóa toàn bộ lịch sử các Activity trước đó
        // Để người dùng không thể nhấn nút "Back" quay lại trang Admin/Main
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
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
