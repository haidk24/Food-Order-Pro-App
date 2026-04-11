package com.example.foodorderapp.data.model;


public class User {
    private String uid;
    private String displayName;
    private String email;
    private String phone ;
    private String role;       // customer | restaurant | shipper | admin
    private String status;     // active | banned
    private int orderCount;

    public Object getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }
    public Object createdAt;

    public User() {}

    public User(String uid, String displayName, String email,
                String phone, String role, String status, int orderCount) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.orderCount = orderCount;
    }

    // Getters
    public String getUid()         { return uid; }
    public String getDisplayName() { return displayName; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public String getRole()        { return role; }
    public String getStatus()      { return status; }
    public int getOrderCount()     { return orderCount; }

    // Setters
    public void setUid(String uid)               { this.uid = uid; }
    public void setDisplayName(String n)         { this.displayName = n; }
    public void setEmail(String e)               { this.email = e; }
    public void setPhone(String p)               { this.phone = p; }
    public void setRole(String r)                { this.role = r; }
    public void setStatus(String s)              { this.status = s; }
    public void setOrderCount(int c)             { this.orderCount = c; }

    public boolean isBanned() {
        return "banned".equals(status);
    }
}