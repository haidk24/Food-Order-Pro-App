package com.example.foodorderapp.data.model;

public class Photo {
    private String imageUrl;

    public Photo() {
    }

    public Photo(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
