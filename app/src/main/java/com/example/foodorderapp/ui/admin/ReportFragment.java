package com.example.foodorderapp.ui.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderapp.R;

// ui/admin/ReportFragment.java
public class ReportFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO: TV4 Tuần 2 sẽ implement báo cáo + biểu đồ
        return inflater.inflate(R.layout.fragment_report,
                container, false);
    }
}