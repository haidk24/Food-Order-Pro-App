package com.example.foodorderapp.data.model;

import java.io.Serializable;

public class Food implements Serializable {
    private int id;
    private String name;
    private int image;
    private int oldPrice;
    private int newPrice;
    private String discount;
    private String description;

    public Food(int id, String name, int image, int oldPrice, int newPrice, String discount, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.discount = discount;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getImage() { return image; }
    public void setImage(int image) { this.image = image; }

    public int getOldPrice() { return oldPrice; }
    public void setOldPrice(int oldPrice) { this.oldPrice = oldPrice; }

    public int getNewPrice() { return newPrice; }
    public void setNewPrice(int newPrice) { this.newPrice = newPrice; }

    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
