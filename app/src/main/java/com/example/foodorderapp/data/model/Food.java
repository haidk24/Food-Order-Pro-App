package com.example.foodorderapp.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "foods")
public class Food implements Serializable {
    @PrimaryKey
    @NonNull
    private String foodId;
    private String name;
    private String imageUrl;
    private int oldPrice;
    private int newPrice;
    private int price;
    private int discountPercent;
    private String description;
    private String category;
    private boolean isAvailable;
    private int orderCount;
    private int count; // Field for quantity in shopping cart

    public Food() {
        // Required for Firebase
    }

    @Ignore
    public Food(int foodId, String name, int imageRes, int oldPrice, int newPrice, String discountPercentStr, String description) {
        this.foodId = String.valueOf(foodId);
        this.name = name;
        this.imageUrl = String.valueOf(imageRes);
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.price = newPrice;
        try {
            this.discountPercent = Integer.parseInt(discountPercentStr.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            this.discountPercent = 0;
        }
        this.description = description;
        this.isAvailable = true;
        this.count = 1;
    }

    public Food(@NonNull String foodId, String name, String imageUrl, int oldPrice, int newPrice, int price, int discountPercent, String description, String category, boolean isAvailable, int orderCount) {
        this.foodId = foodId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.price = price;
        this.discountPercent = discountPercent;
        this.description = description;
        this.category = category;
        this.isAvailable = isAvailable;
        this.orderCount = orderCount;
        this.count = 1;
    }

    @NonNull
    public String getFoodId() { return foodId; }
    public void setFoodId(@NonNull String foodId) { this.foodId = foodId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getOldPrice() { return oldPrice; }
    public void setOldPrice(int oldPrice) { this.oldPrice = oldPrice; }

    public int getNewPrice() { return newPrice; }
    public void setNewPrice(int newPrice) { this.newPrice = newPrice; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
