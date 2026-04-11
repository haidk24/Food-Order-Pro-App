package com.example.foodorderapp.ui.admin;
// ui/admin/RestaurantApprovalFragment.java

import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;

import com.example.foodorderapp.R;

public class RestaurantApprovalFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO: TV4 Ngày 3-4 sẽ implement duyệt nhà hàng
        return inflater.inflate(R.layout.fragment_restaurant_approval,
                container, false);
    }
}