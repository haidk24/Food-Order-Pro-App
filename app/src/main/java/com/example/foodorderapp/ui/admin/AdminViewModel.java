package com.example.foodorderapp.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodorderapp.data.model.AdminStats;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;
import java.util.Date;

public class AdminViewModel extends ViewModel {

    // ── Firebase (Tạm thời comment để tránh crash khi chưa config Firebase) ──
    // private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // ── LiveData ──────────────────────────────────────────────────
    private final MutableLiveData<AdminStats> adminStats = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingRestaurantCount = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserRole = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // ── Constructor ───────────────────────────────────────────────
    public AdminViewModel() {
        loadCurrentUserRole();
        observePendingRestaurants();
        loadDashboardStats();
    }

    private void loadCurrentUserRole() {
        //        // Trả về role admin mặc định để test giao diện
        currentUserRole.postValue("admin");
    }

    public void loadDashboardStats() {
        isLoading.postValue(true);
        loadTestData();
    }

    private void loadTestData() {
        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {
                    AdminStats testStats = new AdminStats(
                            1248,       // totalUsers
                            38,         // totalRestaurants
                            342,        // totalOrdersToday
                            52400000L,  // revenueToday
                            3           // pendingRestaurants
                    );
                    adminStats.postValue(testStats);
                    pendingRestaurantCount.postValue(3);
                    isLoading.postValue(false);
                }, 800);
    }

    private void observePendingRestaurants() {
        pendingRestaurantCount.postValue(3); // Test data
    }

    // ── Getters ───────────────────────────────────────────────────
    public LiveData<AdminStats> getAdminStats()             { return adminStats; }
    public LiveData<Integer> getPendingRestaurantCount()    { return pendingRestaurantCount; }
    public LiveData<String> getCurrentUserRole()            { return currentUserRole; }
    public LiveData<Boolean> getIsLoading()                 { return isLoading; }
    public LiveData<String> getErrorMessage()               { return errorMessage; }
}