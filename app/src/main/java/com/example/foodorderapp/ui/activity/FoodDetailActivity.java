package com.example.foodorderapp.ui.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView imgFood, imgBack, imgCart;
    private TextView tvDiscount, tvFoodName, tvNewPrice, tvOldPrice, tvDescription;

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

        imgBack.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        Food food = (Food) bundle.get("object_food");

        imgFood.setImageResource(food.getImage());
        tvDiscount.setText(food.getDiscount());
        tvFoodName.setText(food.getName());
        tvNewPrice.setText(String.format("%s VNĐ", food.getNewPrice()));
        tvOldPrice.setText(String.format("%s VNĐ", food.getOldPrice()));
        tvDescription.setText(food.getDescription());
    }
}
