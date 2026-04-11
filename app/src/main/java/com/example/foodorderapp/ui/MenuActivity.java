package com.example.foodorderapp.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.ui.adapter.FoodAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView rvMenu;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAddFood;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;
    private FirebaseFirestore db;

    private final String CURRENT_RESTAURANT_ID = "REST_001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rvMenu = findViewById(R.id.rvMenu);
        fabAddFood = findViewById(R.id.fabAddFood);
        db = FirebaseFirestore.getInstance();

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        rvMenu.setAdapter(foodAdapter);

        loadMenuFromFirebase();

        fabAddFood.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MenuActivity.this, AddFoodActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_order) {
                startActivity(new android.content.Intent(getApplicationContext(), OrderDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
    }

    private void loadMenuFromFirebase() {
        db.collection("restaurants")
                .document(CURRENT_RESTAURANT_ID)
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
}