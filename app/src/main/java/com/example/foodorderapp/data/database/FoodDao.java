package com.example.foodorderapp.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodorderapp.data.model.Food;

import java.util.List;

@Dao
public interface FoodDao {
    @Insert
    void insertFood(Food food);

    @Query("SELECT * FROM foods")
    List<Food> getListFoodCart();

    @Query("SELECT * FROM foods WHERE foodId=:foodId")
    List<Food> checkFoodInCart(String foodId);

    @Update
    void updateFood(Food food);

    @Delete
    void deleteFood(Food food);

    @Query("DELETE FROM foods")
    void deleteAllFood();
}
