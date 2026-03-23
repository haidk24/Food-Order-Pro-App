package com.example.foodorderapp.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class User {
    private String uid; // Đổi tên cho khớp ảnh (PK)
    private String displayName;
    private String email;
    private String phone;
    private String role;
    private Address address; // Kiểu map -> Trỏ về class Address
    private String fcmToken; // Dùng để gửi thông báo Push Notification

    @ServerTimestamp
    private Date createdAt;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User() {} // Bắt buộc cho Firestore

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
// TODO: Bấm Alt + Insert để tự động tạo toàn bộ Getter và Setter ở đây
}