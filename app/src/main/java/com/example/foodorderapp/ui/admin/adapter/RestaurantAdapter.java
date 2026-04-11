package com.example.foodorderapp.ui.admin.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Restaurant;

public class RestaurantAdapter
        extends ListAdapter<Restaurant, RestaurantAdapter.VH> {

    public interface Listener {
        void onApprove(Restaurant r);    // pending → active
        void onSuspend(Restaurant r);    // active → suspended
        void onReinstate(Restaurant r);  // suspended → active
        void onViewDetail(Restaurant r);
    }

    // 3 loại view đúng với 3 status trong schema
    public static final int TYPE_PENDING   = 0;
    public static final int TYPE_ACTIVE    = 1;
    public static final int TYPE_SUSPENDED = 2;

    private final Listener listener;
    private final int      viewType;

    public RestaurantAdapter(Listener listener, int viewType) {
        super(new DiffUtil.ItemCallback<Restaurant>() {
            @Override public boolean areItemsTheSame(
                    @NonNull Restaurant a, @NonNull Restaurant b) {
                return a.restaurantId.equals(b.restaurantId);
            }
            @Override public boolean areContentsTheSame(
                    @NonNull Restaurant a, @NonNull Restaurant b) {
                return a.restaurantId.equals(b.restaurantId)
                        && safeEq(a.status, b.status)
                        && safeEq(a.name, b.name)
                        && safeEq(a.imageUrl, b.imageUrl);
            }
            private boolean safeEq(String a, String b) {
                return a == null ? b == null : a.equals(b);
            }
        });
        this.listener = listener;
        this.viewType = viewType;
    }

    @Override public int getItemViewType(int pos) { return viewType; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        int layout;
        switch (type) {
            case TYPE_PENDING:   layout = R.layout.item_restaurant_pending;   break;
            case TYPE_SUSPENDED: layout = R.layout.item_restaurant_suspended; break;
            default:             layout = R.layout.item_restaurant_active;    break;
        }
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false), type);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.bind(getItem(pos), listener);
    }

    // ── ViewHolder ────────────────────────────────────────────────
    public static class VH extends RecyclerView.ViewHolder {

        final int type;

        // Views dùng chung
        TextView tvName, tvAddress, tvRating;
        ImageView imgRestaurant;

        // pending
        View btnApprove, btnReject;

        // active
        View btnSuspend;

        // suspended
        View btnReinstate;

        // Tất cả có nút xem chi tiết
        View btnDetail;

        VH(View v, int type) {
            super(v);
            this.type = type;
            tvName        = v.findViewById(R.id.tv_rest_name);
            tvAddress     = v.findViewById(R.id.tv_rest_address);
            tvRating      = v.findViewById(R.id.tv_rating);
            imgRestaurant = v.findViewById(R.id.img_restaurant);
            btnDetail     = v.findViewById(R.id.btn_detail);

            if (type == TYPE_PENDING) {
                btnApprove = v.findViewById(R.id.btn_approve);
                btnReject  = v.findViewById(R.id.btn_reject);
            } else if (type == TYPE_ACTIVE) {
                btnSuspend = v.findViewById(R.id.btn_suspend);
            } else {
                btnReinstate = v.findViewById(R.id.btn_reinstate);
            }
        }

        void bind(Restaurant r, Listener listener) {
            tvName.setText(r.name != null ? r.name : "—");
            tvAddress.setText(r.getAddressText());

            if (tvRating != null) {
                tvRating.setText(r.rating > 0
                        ? String.format("★ %.1f", r.rating)
                        : "Chưa có đánh giá");
            }

            // Load ảnh nhà hàng
            if (imgRestaurant != null) {
                Glide.with(itemView.getContext())
                        .load(r.imageUrl)
                        .placeholder(R.drawable.bg_card)
                        .error(R.drawable.bg_card)
                        .centerCrop()
                        .into(imgRestaurant);
            }

            // Buttons theo từng status
            if (type == TYPE_PENDING) {
                btnApprove.setOnClickListener(v -> listener.onApprove(r));
                btnReject.setOnClickListener(v -> listener.onSuspend(r));
            } else if (type == TYPE_ACTIVE) {
                btnSuspend.setOnClickListener(v -> listener.onSuspend(r));
            } else {
                btnReinstate.setOnClickListener(v -> listener.onReinstate(r));
            }

            if (btnDetail != null)
                btnDetail.setOnClickListener(v -> listener.onViewDetail(r));

            itemView.setOnClickListener(v -> listener.onViewDetail(r));
        }
    }
}