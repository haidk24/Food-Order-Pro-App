package com.example.foodorderapp.data.model;

import java.io.Serializable;

public class Order implements Serializable {
    private int orderId;
    private String customerId;
    private String restaurantId;
    private String shipperId;
    private double totalAmount;
    private String status; // pending, confirmed, preparing, shipping, delivered
    // Bỏ qua các field không cần thiết cho màn hình này để code ngắn gọn

    public Order() {} // Cần cho Firebase

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getShipperId() { return shipperId; }
    public void setShipperId(String shipperId) { this.shipperId = shipperId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
