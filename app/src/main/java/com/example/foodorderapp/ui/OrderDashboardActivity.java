package com.example.foodorderapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Order;
import com.example.foodorderapp.ui.adapter.OrderAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private String currentRestaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_dashboard);

        drawerLayout = findViewById(R.id.drawer_layout_order);
        navigationView = findViewById(R.id.nav_right_order);
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

        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList, "");
        rvOrders.setAdapter(orderAdapter);

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_order);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_menu) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });

        resolveRestaurantAndListenOrders();
    }

    private void showUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.textView);
        tvEmail.setText(user.getEmail());
    }

    private void resolveRestaurantAndListenOrders() {
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null) {
            performLogout();
            return;
        }

        String uid = authUser.getUid();
        String passedRestaurantId = getIntent().getStringExtra("EXTRA_RESTAURANT_ID");
        if (passedRestaurantId != null && !passedRestaurantId.trim().isEmpty()) {
            currentRestaurantId = passedRestaurantId.trim();
            bindRestaurantContextAndListen();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String mappedRestaurantId = document.getString("restaurantId");
                    if (mappedRestaurantId != null && !mappedRestaurantId.trim().isEmpty()) {
                        currentRestaurantId = mappedRestaurantId.trim();
                        bindRestaurantContextAndListen();
                        return;
                    }

                    db.collection("restaurants")
                            .whereEqualTo("ownerId", uid)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.isEmpty()) {
                                    autoLinkSingleRestaurant(uid);
                                    return;
                                }
                                currentRestaurantId = snapshot.getDocuments().get(0).getId();
                                persistRestaurantMapping(uid, currentRestaurantId);
                                bindRestaurantContextAndListen();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DASHBOARD_ERROR", "Resolve restaurant failed: " + e.getMessage());
                                performLogout();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("DASHBOARD_ERROR", "Load profile failed: " + e.getMessage());
                    performLogout();
                });
    }

    private void autoLinkSingleRestaurant(String uid) {
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(allRestaurants -> {
                    if (allRestaurants.size() == 1) {
                        currentRestaurantId = allRestaurants.getDocuments().get(0).getId();
                        persistRestaurantMapping(uid, currentRestaurantId);
                        bindRestaurantContextAndListen();
                        return;
                    }
                    performLogout();
                })
                .addOnFailureListener(e -> {
                    Log.e("DASHBOARD_ERROR", "Auto link failed: " + e.getMessage());
                    performLogout();
                });
    }

    private void persistRestaurantMapping(String uid, String restaurantId) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("restaurantId", restaurantId);
        db.collection("users").document(uid).update(userUpdate);

        Map<String, Object> restaurantUpdate = new HashMap<>();
        restaurantUpdate.put("ownerId", uid);
        db.collection("restaurants").document(restaurantId).update(restaurantUpdate);
    }

    private void bindRestaurantContextAndListen() {
        orderAdapter = new OrderAdapter(this, orderList, currentRestaurantId);
        rvOrders.setAdapter(orderAdapter);
        listenForOrders();
    }

    private void listenForOrders() {
        db.collection("restaurants").document(currentRestaurantId)
                .collection("orders")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("DASHBOARD_ERROR", "Error: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_logout) {
            performLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, SignInActivity.class);
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