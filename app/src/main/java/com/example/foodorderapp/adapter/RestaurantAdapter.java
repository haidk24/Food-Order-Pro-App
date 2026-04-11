package com.example.foodorderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private Context mContext;
    private List<Restaurant> mListRestaurant;
    private FoodAdapter.IClickItemFoodListener iClickItemFoodListener;

    public RestaurantAdapter(Context mContext, List<Restaurant> mListRestaurant, FoodAdapter.IClickItemFoodListener listener) {
        this.mContext = mContext;
        this.mListRestaurant = mListRestaurant;
        this.iClickItemFoodListener = listener;
    }

    public void setData(List<Restaurant> list) {
        this.mListRestaurant = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = mListRestaurant.get(position);
        if (restaurant == null) {
            return;
        }
        holder.tvRestaurantName.setText(restaurant.getName());

        // Thay đổi sang GridLayoutManager với 2 cột
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
        holder.rvFoods.setLayoutManager(gridLayoutManager);

        FoodAdapter foodAdapter = new FoodAdapter(restaurant.getFoods(), iClickItemFoodListener);
        holder.rvFoods.setAdapter(foodAdapter);
    }

    @Override
    public int getItemCount() {
        if (mListRestaurant != null) {
            return mListRestaurant.size();
        }
        return 0;
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRestaurantName;
        private RecyclerView rvFoods;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            rvFoods = itemView.findViewById(R.id.rv_foods);
        }
    }
}
