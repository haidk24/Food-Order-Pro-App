package com.example.foodorderapp.data.model;

public class AdminStats {
    public long totalUsers;
    public long totalRestaurants;
    public long totalOrdersToday;
    public long revenueToday;
    public long pendingRestaurants;

    public AdminStats() {}

    public AdminStats(long totalUsers, long totalRestaurants,
                      long totalOrdersToday, long revenueToday,
                      long pendingRestaurants) {
        this.totalUsers = totalUsers;
        this.totalRestaurants = totalRestaurants;
        this.totalOrdersToday = totalOrdersToday;
        this.revenueToday = revenueToday;
        this.pendingRestaurants = pendingRestaurants;
    }
}