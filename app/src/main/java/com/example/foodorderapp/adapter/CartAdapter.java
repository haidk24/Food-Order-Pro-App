package com.example.foodorderapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Food> mListFoodCart;
    private IClickCartListener iClickCartListener;

    public interface IClickCartListener {
        void onClickMinusFood(Food food, int position);
        void onClickPlusFood(Food food, int position);
        void onClickDeleteFood(Food food, int position);
    }

    public CartAdapter(List<Food> mListFoodCart, IClickCartListener listener) {
        this.mListFoodCart = mListFoodCart;
        this.iClickCartListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Food food = mListFoodCart.get(position);
        if (food == null) {
            return;
        }
        Glide.with(holder.imgFood.getContext()).load(food.getImageUrl()).into(holder.imgFood);
        holder.tvFoodName.setText(food.getName());
        holder.tvPrice.setText(String.format("%s VNĐ", food.getNewPrice()));
        holder.tvCount.setText(String.valueOf(food.getCount()));

        holder.tvMinus.setOnClickListener(v -> iClickCartListener.onClickMinusFood(food, holder.getAdapterPosition()));
        holder.tvPlus.setOnClickListener(v -> iClickCartListener.onClickPlusFood(food, holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v -> iClickCartListener.onClickDeleteFood(food, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        if (mListFoodCart != null) {
            return mListFoodCart.size();
        }
        return 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFood;
        private TextView tvFoodName, tvPrice, tvCount, tvMinus, tvPlus;
        private AppCompatButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.img_food);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCount = itemView.findViewById(R.id.tv_count);
            tvMinus = itemView.findViewById(R.id.tv_minus);
            tvPlus = itemView.findViewById(R.id.tv_plus);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
