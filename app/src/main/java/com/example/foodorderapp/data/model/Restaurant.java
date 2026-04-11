package com.example.foodorderapp.data.model;

import java.io.Serializable;
import java.util.List;

public class Restaurant implements Serializable {
    private String id;
    private String name;
    private List<Food> foods;

    public Restaurant() {
    }

    public Restaurant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }
}
