package com.example.foodorderapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.CartAdapter;
import com.example.foodorderapp.data.database.FoodDatabase;
import com.example.foodorderapp.data.model.Food;

import java.util.ArrayList;
import java.util.List;

public class ShoppingcartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private CartAdapter cartAdapter;
    private List<Food> mListFoodCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shoppingcart, container, false);

        initViews(view);
        displayListFoodCart();

        return view;
    }

    private void initViews(View view) {
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvCartItems.setLayoutManager(linearLayoutManager);
    }

    private void displayListFoodCart() {
        mListFoodCart = FoodDatabase.getInstance(getContext()).foodDao().getListFoodCart();
        cartAdapter = new CartAdapter(mListFoodCart, new CartAdapter.IClickCartListener() {
            @Override
            public void onClickMinusFood(Food food, int position) {
                int count = food.getCount();
                if (count <= 1) {
                    return;
                }
                int newCount = count - 1;
                food.setCount(newCount);
                FoodDatabase.getInstance(getContext()).foodDao().updateFood(food);
                cartAdapter.notifyItemChanged(position);
                calculateTotalPrice();
            }

            @Override
            public void onClickPlusFood(Food food, int position) {
                int newCount = food.getCount() + 1;
                food.setCount(newCount);
                FoodDatabase.getInstance(getContext()).foodDao().updateFood(food);
                cartAdapter.notifyItemChanged(position);
                calculateTotalPrice();
            }

            @Override
            public void onClickDeleteFood(Food food, int position) {
                FoodDatabase.getInstance(getContext()).foodDao().deleteFood(food);
                mListFoodCart.remove(position);
                cartAdapter.notifyItemRemoved(position);
                calculateTotalPrice();
            }
        });
        rvCartItems.setAdapter(cartAdapter);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        int totalPrice = 0;
        for (Food food : mListFoodCart) {
            totalPrice += food.getNewPrice() * food.getCount();
        }
        tvTotalPrice.setText(String.format("%s VNĐ", totalPrice));
    }
}
