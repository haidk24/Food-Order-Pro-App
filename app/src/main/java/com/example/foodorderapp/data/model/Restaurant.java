package com.example.foodorderapp.data.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String restaurantId;
    private String ownerId;
    private String name;
    private String imageUrl;
    private Address address;
    private GeoPoint location;
    private String status;
    private double rating;
    private List<Food> foods = new ArrayList<>();

    public Restaurant() {}

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    // Compatibility alias for older merged code paths.
    public String getId() {
        return restaurantId;
    }

    // Compatibility alias for older merged code paths.
    public void setId(String id) {
        this.restaurantId = id;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods == null ? new ArrayList<>() : foods;
    }
}
