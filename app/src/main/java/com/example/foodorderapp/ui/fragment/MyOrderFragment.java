package com.example.foodorderapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.CartItem;
import com.example.foodorderapp.data.model.Order;
import com.example.foodorderapp.ui.OrderItemsActivity;
import com.example.foodorderapp.ui.adapter.OrderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyOrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvEmpty;
    private OrderAdapter orderAdapter;
    private final List<Order> orderList = new ArrayList<>();
    private ListenerRegistration orderListener;
    private boolean fallbackNoticeShown;

    public MyOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_orders);
        tvEmpty = view.findViewById(R.id.tv_empty_orders);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(requireContext(), orderList);
        orderAdapter.setCustomerActionListener(new OrderAdapter.CustomerActionListener() {
            @Override
            public void onTrackOrder(Order order) {
                openOrderDetail(order);
            }

            @Override
            public void onReviewOrder(Order order) {
                orderAdapter.openReviewScreen(order);
            }
        });
        rvOrders.setAdapter(orderAdapter);

        loadMyOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }

    private void loadMyOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showEmptyState(true);
            return;
        }

        if (orderListener != null) {
            orderListener.remove();
        }

        startOrderListener(currentUser.getUid(), true);
    }

    private void startOrderListener(String customerId, boolean useOrderedQuery) {
        Query query = FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("customerId", customerId);

        if (useOrderedQuery) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        orderListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                if (useOrderedQuery && shouldFallbackToUnordered(error)) {
                    if (orderListener != null) {
                        orderListener.remove();
                        orderListener = null;
                    }
                    startOrderListener(customerId, false);
                    return;
                }

                // Keep current list visible if already loaded, avoid disappearing UI.
                if (orderList.isEmpty()) {
                    showEmptyState(true);
                }
                return;
            }

            if (snapshots == null) {
                if (orderList.isEmpty()) {
                    showEmptyState(true);
                }
                return;
            }

            List<Order> newOrders = new ArrayList<>();
            snapshots.getDocuments().forEach(doc -> {
                Order order = doc.toObject(Order.class);
                if (order != null) {
                    order.setOrderId(doc.getId());
                    newOrders.add(order);
                }
            });

            if (!useOrderedQuery) {
                Collections.sort(newOrders, new Comparator<Order>() {
                    @Override
                    public int compare(Order left, Order right) {
                        if (left.getCreatedAt() == null && right.getCreatedAt() == null) return 0;
                        if (left.getCreatedAt() == null) return 1;
                        if (right.getCreatedAt() == null) return -1;
                        return right.getCreatedAt().compareTo(left.getCreatedAt());
                    }
                });
            }

            orderList.clear();
            orderList.addAll(newOrders);
            orderAdapter.notifyDataSetChanged();
            showEmptyState(orderList.isEmpty());

            if (!useOrderedQuery && !fallbackNoticeShown) {
                fallbackNoticeShown = true;
                Toast.makeText(requireContext(), "Da dung che do tai du lieu du phong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean shouldFallbackToUnordered(Exception error) {
        if (!(error instanceof FirebaseFirestoreException)) {
            return false;
        }
        FirebaseFirestoreException.Code code = ((FirebaseFirestoreException) error).getCode();
        return code == FirebaseFirestoreException.Code.FAILED_PRECONDITION
                || code == FirebaseFirestoreException.Code.INVALID_ARGUMENT;
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(requireContext(), OrderItemsActivity.class);
        intent.putExtra("EXTRA_ORDER_ID", order.getOrderId());
        intent.putExtra("EXTRA_TOTAL", order.getTotalAmount());
        intent.putExtra("EXTRA_STATUS", order.getStatus());
        intent.putExtra("EXTRA_CUSTOMER_NAME", order.getCustomerName());
        intent.putExtra("EXTRA_CUSTOMER_PHONE", order.getCustomerPhone());
        intent.putExtra("EXTRA_CUSTOMER_ADDRESS", order.getCustomerAddress());
        intent.putExtra("EXTRA_RESTAURANT_ID", order.getRestaurantId());
        intent.putStringArrayListExtra("EXTRA_ITEM_LINES", buildItemLines(order));
        startActivity(intent);
    }

    private ArrayList<String> buildItemLines(Order order) {
        ArrayList<String> itemLines = new ArrayList<>();
        List<CartItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            itemLines.add("Khong co du lieu mon an trong don nay.");
            return itemLines;
        }

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item == null) {
                continue;
            }
            String itemName = item.getName() == null || item.getName().trim().isEmpty()
                    ? "Mon an"
                    : item.getName().trim();
            int quantity = Math.max(item.getQuantity(), 0);
            itemLines.add((i + 1) + ". " + itemName + "  x" + quantity);
        }

        if (itemLines.isEmpty()) {
            itemLines.add("Khong co du lieu mon an trong don nay.");
        }
        return itemLines;
    }

    private void showEmptyState(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}