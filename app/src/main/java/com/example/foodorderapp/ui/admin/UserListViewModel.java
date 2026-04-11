package com.example.foodorderapp.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class UserListViewModel extends ViewModel {

    private final UserRepository repo = UserRepository.getInstance();

    // Danh sách gốc từ Firestore/test data
    private List<User> allUsers = new ArrayList<>();

    // Danh sách đang hiển thị sau filter/search
    private final MutableLiveData<List<User>> filteredUsers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading       = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage     = new MutableLiveData<>();

    // Trạng thái filter + search hiện tại
    private String currentRole   = "all";
    private String currentSearch = "";

    public UserListViewModel() {
        loadUsers();
    }

    // ── Load danh sách từ Repository ─────────────────────────────
    public void loadUsers() {
        isLoading.postValue(true);
        repo.getAllUsers().observeForever(users -> {
            allUsers = users != null ? users : new ArrayList<>();
            applyFilterAndSearch();
            isLoading.postValue(false);
        });
    }

    // ── Filter theo role ──────────────────────────────────────────
    public void filterByRole(String role) {
        currentRole = role;
        applyFilterAndSearch();
    }

    // ── Tìm kiếm theo tên hoặc SĐT ───────────────────────────────
    public void search(String query) {
        currentSearch = query.trim().toLowerCase();
        applyFilterAndSearch();
    }

    // ── Kết hợp filter + search ───────────────────────────────────
    private void applyFilterAndSearch() {
        List<User> result = new ArrayList<>();

        for (User u : allUsers) {
            // Kiểm tra role
            boolean matchRole = currentRole.equals("all")
                    || u.getRole().equals(currentRole);

            // Kiểm tra search (tên hoặc SĐT)
            boolean matchSearch = currentSearch.isEmpty()
                    || u.getDisplayName().toLowerCase().contains(currentSearch)
                    || (u.getPhone() != null
                    && u.getPhone().contains(currentSearch));

            if (matchRole && matchSearch) result.add(u);
        }

        filteredUsers.postValue(result);
    }
//
//    // ── Khóa / Mở khóa tài khoản ─────────────────────────────────
//    public void toggleBan(User user) {
//        String uid = user.getUid();
//
//        LiveData<Boolean> result = isBanned
//                ? repo.unbanUser(uid)
//                : repo.banUser(uid);
//
//        result.observeForever(success -> {
//            if (success != null) {
//                if (success) {
//                    // Cập nhật trực tiếp trong list local để UI phản hồi ngay
//                    updateUserStatusLocally(uid, isBanned ? "active" : "banned");
//                    toastMessage.postValue(isBanned
//                            ? "Đã mở khóa tài khoản"
//                            : "Đã khóa tài khoản");
//                } else {
//                    toastMessage.postValue("Thao tác thất bại, thử lại");
//                }
//            }
//        });
//    }

    // Cập nhật status trong bộ nhớ mà không cần reload toàn bộ list
    private void updateUserStatusLocally(String uid, String newStatus) {
        for (User u : allUsers) {
            if (u.getUid().equals(uid)) {
                u.setStatus(newStatus);
                break;
            }
        }
        applyFilterAndSearch();
    }

    // ── Getters ───────────────────────────────────────────────────
    public LiveData<List<User>> getFilteredUsers() { return filteredUsers; }
    public LiveData<Boolean>    getIsLoading()      { return isLoading; }
    public LiveData<String>     getToastMessage()   { return toastMessage; }
}