package com.example.foodorderapp.data.model;

import com.google.firebase.firestore.GeoPoint; // Import thư viện tọa độ

public class Restaurant {
    private String restaurantId;
    private String ownerId;
    private String name;
    private String imageUrl;
    private Address address; // Kiểu map
    private GeoPoint location; // Kiểu tọa độ bản đồ

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

    private String status;
    private double rating; // Đổi thành double cho số thập phân (VD: 4.5 sao)

    public Restaurant() {}

    // TODO: Tạo Getter và Setter
}
