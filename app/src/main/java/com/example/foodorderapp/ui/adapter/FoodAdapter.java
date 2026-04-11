package com.example.foodorderapp.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.ui.EditFoodActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private String restaurantId;

    public FoodAdapter(Context context, List<Food> foodList, String restaurantId) {
        this.context = context;
        this.foodList = foodList;
        this.restaurantId = restaurantId == null ? "" : restaurantId;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_manage, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        // 1. SET TÊN MÓN
        holder.tvFoodName.setText(food.getName());

        // 2. XỬ LÝ MÔ TẢ (Ẩn đi nếu chủ quán không nhập)
        if (food.getDescription() != null && !food.getDescription().isEmpty()) {
            holder.tvFoodDesc.setText(food.getDescription());
            holder.tvFoodDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvFoodDesc.setVisibility(View.GONE);
        }

        // 3. FORMAT VA SET GIA (gia moi, gia cu, discount)
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        double newPrice = food.getNewPrice();
        double oldPrice = food.getOldPrice();
        int discountPercent = food.getDiscountPercent();

        holder.tvFoodNewPrice.setText(format.format(newPrice));

        holder.tvFoodOldPrice.setVisibility(View.VISIBLE);
        holder.tvFoodDiscount.setVisibility(View.VISIBLE);

        if (oldPrice > newPrice) {
            holder.tvFoodOldPrice.setText(format.format(oldPrice));
            holder.tvFoodOldPrice.setPaintFlags(holder.tvFoodOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvFoodDiscount.setText("-" + Math.max(discountPercent, 0) + "%");
        } else {
            holder.tvFoodOldPrice.setText(format.format(oldPrice));
            holder.tvFoodOldPrice.setPaintFlags(holder.tvFoodOldPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvFoodDiscount.setText("0%");
        }

        // 4. LOAD ẢNH BẰNG GLIDE
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            if (food.getImageUrl().startsWith("data:image")) {
                int commaIndex = food.getImageUrl().indexOf(',');
                if (commaIndex > 0 && commaIndex < food.getImageUrl().length() - 1) {
                    String base64Part = food.getImageUrl().substring(commaIndex + 1);
                    byte[] decoded = Base64.decode(base64Part, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bitmap != null) {
                        holder.imgFood.setImageBitmap(bitmap);
                    } else {
                        holder.imgFood.setImageDrawable(null);
                    }
                } else {
                    holder.imgFood.setImageDrawable(null);
                }
            } else {
                Glide.with(context).load(food.getImageUrl()).centerCrop().into(holder.imgFood);
            }
        } else {
            holder.imgFood.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"));
            holder.imgFood.setImageDrawable(null);
        }

        // 5. HIỆU ỨNG THỊ GIÁC (Làm mờ thẻ nếu trạng thái là Tạm ẩn)
        holder.itemView.setAlpha(food.isAvailable() ? 1.0f : 0.5f);

        // 6. XỬ LÝ MENU 3 CHẤM (Sửa - Ẩn - Xóa)
        holder.btnOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnOptions);
            popup.getMenu().add("Sửa món");
            popup.getMenu().add(food.isAvailable() ? "Tạm ẩn" : "Mở bán lại");
            popup.getMenu().add("Xóa vĩnh viễn");

            popup.setOnMenuItemClickListener(item -> {
                if (restaurantId.isEmpty()) {
                    Toast.makeText(context, "Khong tim thay nha hang hien tai", Toast.LENGTH_SHORT).show();
                    return true;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference foodRef = db.collection("restaurants")
                        .document(restaurantId).collection("foods").document(food.getFoodId());

                if (item.getTitle().equals("Sửa món")) {
                    // Chuyển sang màn hình Edit và gửi kèm dữ liệu cũ
                    Intent intent = new Intent(context, EditFoodActivity.class);
                    intent.putExtra("EXTRA_RESTAURANT_ID", restaurantId);
                    intent.putExtra("EXTRA_FOOD_ID", food.getFoodId());
                    intent.putExtra("EXTRA_NAME", food.getName());
                    intent.putExtra("EXTRA_PRICE", food.getPrice());
                    intent.putExtra("EXTRA_OLD_PRICE", food.getOldPrice());
                    intent.putExtra("EXTRA_NEW_PRICE", food.getNewPrice());
                    intent.putExtra("EXTRA_DISCOUNT", food.getDiscountPercent());
                    intent.putExtra("EXTRA_DESC", food.getDescription());
                    intent.putExtra("EXTRA_IMAGE_URL", food.getImageUrl());
                    context.startActivity(intent);
                }
                else if (item.getTitle().equals("Tạm ẩn") || item.getTitle().equals("Mở bán lại")) {
                    // Đổi trạng thái isAvailable
                    boolean newStatus = !food.isAvailable();
                    foodRef.update("isAvailable", newStatus)
                            .addOnSuccessListener(a -> Toast.makeText(context, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show());
                }
                else if (item.getTitle().equals("Xóa vĩnh viễn")) {
                    // Xóa hoàn toàn khỏi database
                    foodRef.delete().addOnSuccessListener(a -> Toast.makeText(context, "Đã xóa món ăn!", Toast.LENGTH_SHORT).show());
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    // KHUÔN GIỮ VIEW (Ánh xạ các thành phần từ file XML)
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodNewPrice, tvFoodOldPrice, tvFoodDiscount, tvFoodDesc;
        ImageView imgFood;
        ImageButton btnOptions;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodNewPrice = itemView.findViewById(R.id.tvFoodNewPrice);
            tvFoodOldPrice = itemView.findViewById(R.id.tvFoodOldPrice);
            tvFoodDiscount = itemView.findViewById(R.id.tvFoodDiscount);
            tvFoodDesc = itemView.findViewById(R.id.tvFoodDesc);
            imgFood = itemView.findViewById(R.id.imgFood);
            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }
}

