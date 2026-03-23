package com.example.foodorderapp.data.model;

public class AdminStats {
    public int totalUsers;
    public int totalRestaurants;
    public int totalOrdersToday;
    public long revenueToday;
    public int pendingRestaurants;

    public AdminStats() {}

    public AdminStats(int totalUsers, int totalRestaurants,
                      int totalOrdersToday, long revenueToday,
                      int pendingRestaurants) {
        this.totalUsers = totalUsers;
        this.totalRestaurants = totalRestaurants;
        this.totalOrdersToday = totalOrdersToday;
        this.revenueToday = revenueToday;
        this.pendingRestaurants = pendingRestaurants;
    }
}