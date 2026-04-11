package com.example.foodorderapp.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodorderapp.data.model.AdminStats;
import com.example.foodorderapp.data.model.Restaurant;
import com.example.foodorderapp.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AdminViewModel extends ViewModel {

    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<AdminStats> adminStats = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingRestaurantCount = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserRole = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<List<User>> userList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasMoreUsers = new MutableLiveData<>(true);

    private static final int    PAGE_SIZE        = 20;
    private DocumentSnapshot lastUserDocument = null;
    private boolean             isPaging         = false;
    private String              currentRoleFilter = "all";

    private ListenerRegistration pendingListener;

    public AdminViewModel() {
        loadCurrentUserRole();
        observePendingRestaurants();
        loadDashboardStats();
    }

    private void loadCurrentUserRole() {
        if (auth.getCurrentUser() == null) return;
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) currentUserRole.setValue(doc.getString("role"));
                });
    }

    public void loadDashboardStats() {
        isLoading.setValue(true);
        AtomicLong totalUsers = new AtomicLong(0), totalRestaurants = new AtomicLong(0), 
                   pendingCount = new AtomicLong(0), ordersToday = new AtomicLong(0), revenueToday = new AtomicLong(0);
        
        // Giảm số lượng query xuống 4 (Bỏ Shipper)
        AtomicInteger remaining = new AtomicInteger(4);
        Runnable oneDone = () -> {
            if (remaining.decrementAndGet() == 0) {
                adminStats.setValue(new AdminStats(totalUsers.get(), totalRestaurants.get(), ordersToday.get(), revenueToday.get(), pendingCount.get()));
                isLoading.setValue(false);
            }
        };

        db.collection("users").whereEqualTo("role", "customer").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(s -> { totalUsers.set(s.getCount()); oneDone.run(); }).addOnFailureListener(e -> oneDone.run());
        db.collection("restaurants").whereEqualTo("status", "active").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(s -> { totalRestaurants.set(s.getCount()); oneDone.run(); }).addOnFailureListener(e -> oneDone.run());
        db.collection("restaurants").whereEqualTo("status", "pending").count().get(AggregateSource.SERVER)
                .addOnSuccessListener(s -> { pendingCount.set(s.getCount()); pendingRestaurantCount.setValue((int)s.getCount()); oneDone.run(); }).addOnFailureListener(e -> oneDone.run());
        
        // BỎ QUERY ĐẾM SHIPPER TẠI ĐÂY

        db.collection("orders").whereGreaterThanOrEqualTo("createdAt", getStartOfToday()).get()
                .addOnSuccessListener(snapshot -> {
                    long rev = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if ("delivered".equals(doc.getString("status"))) {
                            Long amt = doc.getLong("totalAmount");
                            if (amt != null) rev += amt;
                        }
                    }
                    ordersToday.set(snapshot.size());
                    revenueToday.set(rev);
                    oneDone.run();
                }).addOnFailureListener(e -> oneDone.run());
    }

    public void loadFirstPageUsers(String roleFilter) {
        if (isPaging) return;
        currentRoleFilter = roleFilter;
        lastUserDocument  = null;
        hasMoreUsers.setValue(true);
        userList.setValue(new ArrayList<>()); 
        fetchUserPage(true);
    }

    public void loadNextPageUsers() {
        if (isPaging || Boolean.FALSE.equals(hasMoreUsers.getValue())) return;
        fetchUserPage(false);
    }

    private void fetchUserPage(boolean isFirstPage) {
        isPaging = true;
        if (!isFirstPage) isLoadingMore.setValue(true);

        Query query = db.collection("users").orderBy("createdAt", Query.Direction.DESCENDING);
        if (!"all".equals(currentRoleFilter)) {
            query = db.collection("users").whereEqualTo("role", currentRoleFilter)
                      .orderBy("createdAt", Query.Direction.DESCENDING);
        }

        if (lastUserDocument != null) query = query.startAfter(lastUserDocument);

        query.limit(PAGE_SIZE).get().addOnSuccessListener(snapshot -> {
            List<DocumentSnapshot> docs = snapshot.getDocuments();
            List<User> newUsers = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                User u = doc.toObject(User.class);
                if (u != null) { u.setUid(doc.getId()); newUsers.add(u); }
            }

            List<User> current = isFirstPage ? new ArrayList<>() : userList.getValue();
            List<User> merged = new ArrayList<>(current != null ? current : new ArrayList<>());
            merged.addAll(newUsers);
            
            userList.setValue(merged);
            lastUserDocument = docs.isEmpty() ? null : docs.get(docs.size() - 1);
            hasMoreUsers.setValue(docs.size() == PAGE_SIZE);
            isLoadingMore.setValue(false);
            isPaging = false;
        }).addOnFailureListener(e -> {
            errorMessage.setValue("Lỗi tải danh sách: " + e.getMessage());
            isLoadingMore.setValue(false);
            isPaging = false;
        });
    }

    private void observePendingRestaurants() {
        pendingListener = db.collection("restaurants").whereEqualTo("status", "pending")
                .addSnapshotListener((s, e) -> { if (s != null) pendingRestaurantCount.setValue(s.size()); });
    }

    private Date getStartOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public LiveData<List<User>> getUserList() { return userList; }
    public LiveData<Boolean> getIsLoadingMore() { return isLoadingMore; }
    public LiveData<Boolean> getHasMoreUsers() { return hasMoreUsers; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AdminStats> getAdminStats() { return adminStats; }
    public LiveData<Integer> getPendingRestaurantCount() { return pendingRestaurantCount; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getCurrentUserRole() { return currentUserRole; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (pendingListener != null) pendingListener.remove();
        if (restaurantListener != null) restaurantListener.remove();
    }

    private final MutableLiveData<List<Restaurant>> pendingRestaurantList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Restaurant>> activeRestaurantList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Restaurant>> suspendedRestaurantList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
    private ListenerRegistration restaurantListener;

    public void observeRestaurants() {
        if (restaurantListener != null) return;
        restaurantListener = db.collection("restaurants")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Restaurant> pending = new ArrayList<>();
                    List<Restaurant> active  = new ArrayList<>();
                    List<Restaurant> suspended = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r == null) continue;
                        r.restaurantId = doc.getId();
                        if ("pending".equals(r.status)) pending.add(r);
                        else if ("active".equals(r.status)) active.add(r);
                        else if ("suspended".equals(r.status)) suspended.add(r);
                    }
                    pendingRestaurantList.postValue(pending);
                    activeRestaurantList.postValue(active);
                    suspendedRestaurantList.postValue(suspended);
                    pendingRestaurantCount.postValue(pending.size());
                });
    }

    public void approveRestaurant(String restaurantId) {
        isLoading.postValue(true);
        db.collection("restaurants").document(restaurantId).update("status", "active")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("duyệt nhà hàng", e); isLoading.postValue(false); });
    }

    public void suspendRestaurant(String restaurantId) {
        isLoading.postValue(true);
        db.collection("restaurants").document(restaurantId).update("status", "suspended")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("tạm ngưng nhà hàng", e); isLoading.postValue(false); });
    }

    public void rejectRestaurant(String restaurantId) {
        isLoading.postValue(true);
        db.collection("restaurants").document(restaurantId).update("status", "suspended")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("từ chối nhà hàng", e); isLoading.postValue(false); });
    }

    public void reinstateRestaurant(String restaurantId) {
        isLoading.postValue(true);
        db.collection("restaurants").document(restaurantId).update("status", "active")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("phục hồi nhà hàng", e); isLoading.postValue(false); });
    }

    public void banUser(String uid) {
        isLoading.postValue(true);
        db.collection("users").document(uid).update("status", "banned")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("khóa tài khoản", e); isLoading.postValue(false); });
    }

    public void unbanUser(String uid) {
        isLoading.postValue(true);
        db.collection("users").document(uid).update("status", "active")
                .addOnSuccessListener(v -> { actionSuccess.postValue(true); isLoading.postValue(false); })
                .addOnFailureListener(e -> { logError("mở khóa tài khoản", e); isLoading.postValue(false); });
    }

    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    public void loadUserDetail(String uid) {
        isLoading.postValue(true);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) user.setUid(doc.getId());
                        selectedUser.postValue(user);
                    }
                    isLoading.postValue(false);
                })
                .addOnFailureListener(e -> { logError("tải chi tiết user", e); isLoading.postValue(false); });
    }

    private final MutableLiveData<Restaurant> selectedRestaurant = new MutableLiveData<>();
    public void loadRestaurantDetail(String restaurantId) {
        isLoading.postValue(true);
        db.collection("restaurants").document(restaurantId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Restaurant r = doc.toObject(Restaurant.class);
                        if (r != null) r.restaurantId = doc.getId();
                        selectedRestaurant.postValue(r);
                    }
                    isLoading.postValue(false);
                })
                .addOnFailureListener(e -> {
                    logError("tải chi tiết nhà hàng", e);
                    isLoading.postValue(false);
                });
    }

    private void logError(String tag, Exception e) {
        errorMessage.postValue("Lỗi " + tag + ": " + e.getMessage());
    }

    public LiveData<List<Restaurant>> getPendingRestaurantList() { return pendingRestaurantList; }
    public LiveData<List<Restaurant>> getActiveRestaurantList() { return activeRestaurantList; }
    public LiveData<List<Restaurant>> getSuspendedRestaurantList() { return suspendedRestaurantList; }
    public LiveData<Boolean> getActionSuccess() { return actionSuccess; }
    public LiveData<User>    getSelectedUser()  { return selectedUser; }
    public LiveData<Restaurant> getSelectedRestaurant() { return selectedRestaurant; }
}
