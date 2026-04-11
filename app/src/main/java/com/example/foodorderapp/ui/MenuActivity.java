package com.example.foodorderapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.ui.adapter.FoodAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvMenu;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAddFood;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;
    private FirebaseFirestore db;
    private String currentRestaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        drawerLayout = findViewById(R.id.drawer_layout_menu);
        navigationView = findViewById(R.id.nav_right_menu);
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

        rvMenu = findViewById(R.id.rvMenu);
        fabAddFood = findViewById(R.id.fabAddFood);
        db = FirebaseFirestore.getInstance();

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList, "");
        rvMenu.setAdapter(foodAdapter);

        fabAddFood.setOnClickListener(v -> {
            if (currentRestaurantId == null || currentRestaurantId.isEmpty()) {
                Toast.makeText(this, "Khong tim thay nha hang hien tai", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MenuActivity.this, AddFoodActivity.class);
            intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_order) {
                Intent intent = new Intent(getApplicationContext(), OrderDashboardActivity.class);
                intent.putExtra("EXTRA_RESTAURANT_ID", currentRestaurantId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });

        verifyRestaurantRoleAndLoadMenu();
    }

    private void showUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        TextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.textView);
        tvEmail.setText(user.getEmail());
    }

    private void verifyRestaurantRoleAndLoadMenu() {
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null) {
            redirectToSignIn("Vui long dang nhap de tiep tuc");
            return;
        }

        String uid = authUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String role = document.getString("role");
                    if (!"restaurant".equalsIgnoreCase(role)) {
                        Toast.makeText(this, "Chi tai khoan nha hang moi vao duoc quan ly menu", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MenuActivity.this, MainActivity.class));
                        finish();
                        return;
                    }

                    String mappedRestaurantId = document.getString("restaurantId");
                    if (mappedRestaurantId != null && !mappedRestaurantId.trim().isEmpty()) {
                        currentRestaurantId = mappedRestaurantId.trim();
                        bindRestaurantContextAndLoad();
                        return;
                    }

                    // Fallback cho du lieu cu: tim restaurant theo ownerId = uid.
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
                                bindRestaurantContextAndLoad();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MENU_AUTH", "Resolve restaurant failed: " + e.getMessage());
                                redirectToSignIn("Khong the xac thuc tai khoan");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MENU_AUTH", "Load role failed: " + e.getMessage());
                    redirectToSignIn("Khong the xac thuc tai khoan");
                });
    }

    private void autoLinkSingleRestaurant(String uid) {
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(allRestaurants -> {
                    List<DocumentSnapshot> docs = allRestaurants.getDocuments();
                    if (docs.isEmpty()) {
                        Toast.makeText(this, "Chua co du lieu nha hang", Toast.LENGTH_LONG).show();
                        performLogout();
                        return;
                    }

                    if (docs.size() == 1) {
                        currentRestaurantId = docs.get(0).getId();
                        persistRestaurantMapping(uid, currentRestaurantId);
                        Toast.makeText(this, "Da tu dong lien ket tai khoan nha hang", Toast.LENGTH_SHORT).show();
                        bindRestaurantContextAndLoad();
                        return;
                    }

                    showRestaurantPicker(uid, docs);
                })
                .addOnFailureListener(e -> {
                    Log.e("MENU_AUTH", "Auto link failed: " + e.getMessage());
                    redirectToSignIn("Khong the xac thuc tai khoan");
                });
    }

    private void showRestaurantPicker(String uid, List<DocumentSnapshot> docs) {
        String[] labels = new String[docs.size()];
        for (int i = 0; i < docs.size(); i++) {
            String name = docs.get(i).getString("name");
            labels[i] = (name == null || name.trim().isEmpty()) ? docs.get(i).getId() : name + " (" + docs.get(i).getId() + ")";
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chon nha hang de lien ket")
                .setItems(labels, (dialog, which) -> {
                    currentRestaurantId = docs.get(which).getId();
                    persistRestaurantMapping(uid, currentRestaurantId);
                    Toast.makeText(this, "Da lien ket nha hang: " + currentRestaurantId, Toast.LENGTH_SHORT).show();
                    bindRestaurantContextAndLoad();
                })
                .setCancelable(false)
                .setNegativeButton("Dang xuat", (dialog, which) -> performLogout())
                .show();
    }

    private void persistRestaurantMapping(String uid, String restaurantId) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("restaurantId", restaurantId);
        db.collection("users").document(uid).update(userUpdate);

        Map<String, Object> restaurantUpdate = new HashMap<>();
        restaurantUpdate.put("ownerId", uid);
        db.collection("restaurants").document(restaurantId).update(restaurantUpdate);
    }

    private void bindRestaurantContextAndLoad() {
        foodAdapter = new FoodAdapter(this, foodList, currentRestaurantId);
        rvMenu.setAdapter(foodAdapter);
        loadMenuFromFirebase();
    }

    private void loadMenuFromFirebase() {
        db.collection("restaurants")
                .document(currentRestaurantId)
                .collection("foods")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("MENU_ERROR", "Error: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        foodList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Food food = doc.toObject(Food.class);
                            foodList.add(food);
                        }
                        foodAdapter.notifyDataSetChanged();
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
        redirectToSignIn("Da dang xuat");
    }

    private void redirectToSignIn(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MenuActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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