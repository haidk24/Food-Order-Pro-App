package com.example.foodorderapp.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.data.database.FoodDatabase;
import com.example.foodorderapp.data.model.Food;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView imgFood, imgBack, imgCart;
    private TextView tvDiscount, tvFoodName, tvNewPrice, tvOldPrice, tvDescription, tvRestaurantRating;
    private AppCompatButton btnAddToCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        initViews();
        getIntentData();
    }

    private void initViews() {
        imgFood = findViewById(R.id.img_food);
        imgBack = findViewById(R.id.img_back);
        imgCart = findViewById(R.id.img_cart);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFoodName = findViewById(R.id.tv_food_name);
        tvNewPrice = findViewById(R.id.tv_new_price);
        tvOldPrice = findViewById(R.id.tv_old_price);
        tvDescription = findViewById(R.id.tv_description);
        tvRestaurantRating = findViewById(R.id.tv_restaurant_rating);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);

        imgBack.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Food food = (Food) bundle.get("object_food");

        Glide.with(this).load(food.getImageUrl()).into(imgFood);
        tvDiscount.setText(String.format("Giảm %s%%", food.getDiscountPercent()));
        tvFoodName.setText(food.getName());
        tvNewPrice.setText(String.format("%s VNĐ", food.getNewPrice()));
        tvOldPrice.setText(String.format("%s VNĐ", food.getOldPrice()));
        tvDescription.setText(food.getDescription());
        loadRestaurantAverageRating(food.getRestaurantId());

        btnAddToCart.setOnClickListener(v -> {
            addFoodToCart(food);
        });
    }

    private void loadRestaurantAverageRating(String restaurantId) {
        if (restaurantId == null || restaurantId.trim().isEmpty()) {
            tvRestaurantRating.setText("Đánh giá nhà hàng: Chưa có");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("reviews")
                .whereEqualTo("restaurantId", restaurantId.trim())
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) {
                        tvRestaurantRating.setText("Đánh giá nhà hàng: Chưa có");
                        return;
                    }

                    double total = 0;
                    int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        Number rating = doc.getDouble("rating");
                        if (rating == null) {
                            Long longRating = doc.getLong("rating");
                            if (longRating != null) {
                                rating = longRating;
                            }
                        }
                        if (rating != null) {
                            total += rating.doubleValue();
                            count++;
                        }
                    }

                    if (count == 0) {
                        tvRestaurantRating.setText("Đánh giá nhà hàng: Chưa có");
                        return;
                    }

                    double avg = total / count;
                    tvRestaurantRating.setText(String.format(Locale.getDefault(), "Đánh giá nhà hàng: %.1f/5 (%d)", avg, count));
                })
                .addOnFailureListener(e -> tvRestaurantRating.setText("Đánh giá nhà hàng: Chưa có"));
    }

    private void addFoodToCart(Food food) {
        List<Food> list = FoodDatabase.getInstance(this).foodDao().checkFoodInCart(food.getFoodId());
        if (list != null && !list.isEmpty()) {
            Food foodInCart = list.get(0);
            foodInCart.setCount(foodInCart.getCount() + 1);
            FoodDatabase.getInstance(this).foodDao().updateFood(foodInCart);
        } else {
            food.setCount(1);
            FoodDatabase.getInstance(this).foodDao().insertFood(food);
        }
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}
