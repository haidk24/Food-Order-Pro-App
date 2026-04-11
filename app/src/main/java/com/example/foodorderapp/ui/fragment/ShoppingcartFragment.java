package com.example.foodorderapp.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.CartAdapter;
import com.example.foodorderapp.data.database.FoodDatabase;
import com.example.foodorderapp.data.model.CartItem;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.data.model.Order;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShoppingcartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private AppCompatButton btnCheckout;
    private EditText etCustomerName;
    private EditText etCustomerPhone;
    private EditText etCustomerAddress;

    private CartAdapter cartAdapter;
    private List<Food> mListFoodCart;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shoppingcart, container, false);

        initViews(view);
        displayListFoodCart();
        setupCheckoutAction();

        return view;
    }

    private void initViews(View view) {
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        etCustomerName = view.findViewById(R.id.et_customer_name);
        etCustomerPhone = view.findViewById(R.id.et_customer_phone);
        etCustomerAddress = view.findViewById(R.id.et_customer_address);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvCartItems.setLayoutManager(linearLayoutManager);
        prefillCustomerInfo();
    }

    private void prefillCustomerInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        if (!TextUtils.isEmpty(currentUser.getDisplayName())) {
            etCustomerName.setText(currentUser.getDisplayName());
        }

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(this::applyUserProfile)
                .addOnFailureListener(e -> {
                });
    }

    private void applyUserProfile(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return;
        }

        if (TextUtils.isEmpty(etCustomerName.getText())) {
            String displayName = document.getString("displayName");
            if (!TextUtils.isEmpty(displayName)) {
                etCustomerName.setText(displayName);
            }
        }

        String phone = document.getString("phone");
        if (!TextUtils.isEmpty(phone)) {
            etCustomerPhone.setText(phone);
        }

        String address = document.getString("address");
        if (!TextUtils.isEmpty(address)) {
            etCustomerAddress.setText(address);
        }
    }

    private void displayListFoodCart() {
        mListFoodCart = FoodDatabase.getInstance(requireContext()).foodDao().getListFoodCart();
        cartAdapter = new CartAdapter(mListFoodCart, new CartAdapter.IClickCartListener() {
            @Override
            public void onClickMinusFood(Food food, int position) {
                int count = food.getCount();
                if (count <= 1) {
                    return;
                }
                int newCount = count - 1;
                food.setCount(newCount);
                FoodDatabase.getInstance(requireContext()).foodDao().updateFood(food);
                cartAdapter.notifyItemChanged(position);
                calculateTotalPrice();
            }

            @Override
            public void onClickPlusFood(Food food, int position) {
                int newCount = food.getCount() + 1;
                food.setCount(newCount);
                FoodDatabase.getInstance(requireContext()).foodDao().updateFood(food);
                cartAdapter.notifyItemChanged(position);
                calculateTotalPrice();
            }

            @Override
            public void onClickDeleteFood(Food food, int position) {
                FoodDatabase.getInstance(requireContext()).foodDao().deleteFood(food);
                mListFoodCart.remove(position);
                cartAdapter.notifyItemRemoved(position);
                calculateTotalPrice();
            }
        });
        rvCartItems.setAdapter(cartAdapter);
        calculateTotalPrice();
    }

    private void setupCheckoutAction() {
        btnCheckout.setOnClickListener(v -> checkoutOrder());
    }

    private void checkoutOrder() {
        if (mListFoodCart == null || mListFoodCart.isEmpty()) {
            Toast.makeText(getContext(), "Gio hang dang trong", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Ban can dang nhap de dat hang", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerName = etCustomerName.getText() == null ? "" : etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText() == null ? "" : etCustomerPhone.getText().toString().trim();
        String customerAddress = etCustomerAddress.getText() == null ? "" : etCustomerAddress.getText().toString().trim();

        if (TextUtils.isEmpty(customerName) || TextUtils.isEmpty(customerPhone) || TextUtils.isEmpty(customerAddress)) {
            Toast.makeText(getContext(), "Vui long nhap day du thong tin khach hang", Toast.LENGTH_SHORT).show();
            return;
        }

        String restaurantId = mListFoodCart.get(0).getRestaurantId();
        if (TextUtils.isEmpty(restaurantId)) {
            Toast.makeText(getContext(), "Khong xac dinh duoc nha hang", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Food food : mListFoodCart) {
            if (TextUtils.isEmpty(food.getRestaurantId()) || !restaurantId.equals(food.getRestaurantId())) {
                Toast.makeText(getContext(), "Chi ho tro dat mon trong cung mot nha hang", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Order order = buildOrder(currentUser.getUid(), restaurantId, customerName, customerPhone, customerAddress);
        String orderId = db.collection("orders").document().getId();
        order.setOrderId(orderId);

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderId), order);
        batch.set(db.collection("restaurants").document(restaurantId).collection("orders").document(orderId), order);

        btnCheckout.setEnabled(false);
        batch.commit()
                .addOnSuccessListener(unused -> {
                    FoodDatabase.getInstance(requireContext()).foodDao().deleteAllFood();
                    mListFoodCart.clear();
                    cartAdapter.notifyDataSetChanged();
                    calculateTotalPrice();
                    btnCheckout.setEnabled(true);
                    Toast.makeText(getContext(), "Dat hang thanh cong", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnCheckout.setEnabled(true);
                    Toast.makeText(getContext(), "Dat hang that bai: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Order buildOrder(String customerId, String restaurantId, String customerName,
                             String customerPhone, String customerAddress) {
        List<CartItem> items = new ArrayList<>();
        double total = 0;

        for (Food food : mListFoodCart) {
            CartItem item = new CartItem();
            item.setFoodId(food.getFoodId());
            item.setName(food.getName());
            item.setPrice(food.getNewPrice());
            item.setQuantity(food.getCount());
            items.add(item);

            total += food.getNewPrice() * food.getCount();
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setCustomerAddress(customerAddress);
        order.setRestaurantId(restaurantId);
        order.setItems(items);
        order.setTotalAmount(total);
        order.setStatus("pending");
        order.setPaymentMethod("COD");
        order.setCreatedAt(Timestamp.now().toDate());
        return order;
    }

    private void calculateTotalPrice() {
        double totalPrice = 0;
        if (mListFoodCart != null) {
            for (Food food : mListFoodCart) {
                totalPrice += food.getNewPrice() * food.getCount();
            }
        }
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", totalPrice));
    }
}
