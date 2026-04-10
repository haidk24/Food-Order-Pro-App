package com.example.foodorderapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<Food> mListFood;
    private IClickItemFoodListener iClickItemFoodListener;

    public interface IClickItemFoodListener {
        void onClickItemFood(Food food);
    }

    public FoodAdapter(List<Food> mListFood, IClickItemFoodListener listener) {
        this.mListFood = mListFood;
        this.iClickItemFoodListener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = mListFood.get(position);
        if (food == null) {
            return;
        }
        holder.imgFood.setImageResource(food.getImage());
        holder.tvFoodName.setText(food.getName());
        holder.tvOldPrice.setText(String.format("%s VNĐ", food.getOldPrice()));
        holder.tvNewPrice.setText(String.format("%s VNĐ", food.getNewPrice()));
        holder.tvDiscount.setText(food.getDiscount());

        holder.itemView.setOnClickListener(v -> iClickItemFoodListener.onClickItemFood(food));
    }

    @Override
    public int getItemCount() {
        if (mListFood != null) {
            return mListFood.size();
        }
        return 0;
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFood;
        private TextView tvDiscount;
        private TextView tvFoodName;
        private TextView tvOldPrice;
        private TextView tvNewPrice;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvOldPrice = itemView.findViewById(R.id.tv_old_price);
            tvNewPrice = itemView.findViewById(R.id.tv_new_price);
        }
    }
}
