package com.example.foodorderapp.data.model;



import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;
import java.util.Map;

public class Restaurant {

    @DocumentId
    public String   restaurantId;

    // Từ schema: restaurants collection
    public String   ownerId;       // FK → users
    public String   name;
    public String   imageUrl;
    public Map<String, Object> address;  // map: { street, district, city }
    public GeoPoint location;      // GeoPoint
    public String   status;        // pending | active | suspended
    public double   rating;
    public Object   createdAt;

    public Restaurant() {}

    // Helper lấy địa chỉ dạng text để hiển thị
    public String getAddressText() {
        if (address == null) return "Chưa cập nhật";
        StringBuilder sb = new StringBuilder();
        if (address.containsKey("street"))
            sb.append(address.get("street"));
        if (address.containsKey("district")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.get("district"));
        }
        if (address.containsKey("city")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.get("city"));
        }
        return sb.length() > 0 ? sb.toString() : "Chưa cập nhật";
    }

    // Helper lấy initials từ tên nhà hàng
    // "Cơm Tấm Bà Lan" → "CT"
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0))
                + String.valueOf(parts[1].charAt(0)))
                .toUpperCase();
    }

    // Helper status text tiếng Việt
    public String getStatusText() {
        if (status == null) return "—";
        switch (status) {
            case "pending":   return "Chờ duyệt";
            case "active":    return "Hoạt động";
            case "suspended": return "Tạm ngưng";
            default:          return status;
        }
    }
}