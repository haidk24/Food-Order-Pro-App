package com.example.foodorderapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Restaurant;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterRestaurantFragment extends Fragment {

    private TextInputEditText edtName, edtPhone, edtStreet, edtDistrict, edtCity;
    private CheckBox cbLicence;
    private Button btnRegister;
    private ProgressBar progressBar;
    private LinearLayout layoutForm, layoutPending;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_restaurant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews(view);
        checkRegistrationStatus();
        setupListeners();
    }

    private void initViews(View v) {
        tvTitle = v.findViewById(R.id.tv_title);
        edtName = v.findViewById(R.id.edt_restaurant_name);
        edtPhone = v.findViewById(R.id.edt_restaurant_phone);
        edtStreet = v.findViewById(R.id.edt_street);
        edtDistrict = v.findViewById(R.id.edt_district);
        edtCity = v.findViewById(R.id.edt_city);
        cbLicence = v.findViewById(R.id.cb_licence);
        btnRegister = v.findViewById(R.id.btn_register);
        progressBar = v.findViewById(R.id.progress_bar);
        layoutForm = v.findViewById(R.id.layout_form);
        layoutPending = v.findViewById(R.id.layout_pending);
    }

    private void checkRegistrationStatus() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        setLoading(true);
        // Kiểm tra xem user này đã có nhà hàng nào đang "pending" hoặc "active" chưa
        db.collection("restaurants")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setLoading(false);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        boolean hasPending = false;
                        boolean hasActive = false;

                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String status = doc.getString("status");
                            if ("pending".equals(status)) hasPending = true;
                            if ("active".equals(status)) hasActive = true;
                        }

                        if (hasActive) {
                            Toast.makeText(getContext(), "Bạn đã có nhà hàng đang hoạt động!", Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else if (hasPending) {
                            showPendingUI();
                        } else {
                            showFormUI();
                            loadUserPhone();
                        }
                    } else {
                        showFormUI();
                        loadUserPhone();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showFormUI();
                    loadUserPhone();
                });
    }

    private void loadUserPhone() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phone = documentSnapshot.getString("phone");
                            if (phone != null && !phone.isEmpty()) {
                                edtPhone.setText(phone);
                            }
                        }
                    });
        }
    }

    private void showPendingUI() {
        layoutForm.setVisibility(View.GONE);
        layoutPending.setVisibility(View.VISIBLE);
        tvTitle.setText("Trạng thái đăng ký");
    }

    private void showFormUI() {
        layoutForm.setVisibility(View.VISIBLE);
        layoutPending.setVisibility(View.GONE);
        tvTitle.setText("Đăng ký mở nhà hàng");
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                submitRegistration();
            }
        });
    }

    private boolean validateForm() {
        if (edtName.getText().toString().trim().isEmpty()) {
            edtName.setError("Vui lòng nhập tên nhà hàng");
            return false;
        }
        if (edtPhone.getText().toString().trim().isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            return false;
        }
        if (edtStreet.getText().toString().trim().isEmpty() ||
            edtDistrict.getText().toString().trim().isEmpty() ||
            edtCity.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ địa chỉ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitRegistration() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        setLoading(true);

        Map<String, Object> address = new HashMap<>();
        address.put("street", edtStreet.getText().toString().trim());
        address.put("district", edtDistrict.getText().toString().trim());
        address.put("city", edtCity.getText().toString().trim());

        Restaurant restaurant = new Restaurant();
        restaurant.ownerId = user.getUid();
        restaurant.name = edtName.getText().toString().trim();
        restaurant.phone = edtPhone.getText().toString().trim();
        restaurant.address = address;
        restaurant.hasLicence = cbLicence.isChecked();
        restaurant.status = "pending";
        restaurant.rating = 0.0;
        restaurant.createdAt = FieldValue.serverTimestamp();

        db.collection("restaurants").add(restaurant)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    showPendingUI();
                    Toast.makeText(getContext(), "Gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            layoutForm.setVisibility(View.GONE);
            layoutPending.setVisibility(View.GONE);
        }
    }
}