package com.example.foodorderapp.ui.admin;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.ui.admin.adapter.UserAdapter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class UserListFragment extends Fragment {

    private UserListViewModel viewModel;
    private UserAdapter adapter;

    // Views
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextInputEditText etSearch;
    private ChipGroup chipGroup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserListViewModel.class);

        bindViews(view);
        setupRecyclerView();
        setupSearch();
        setupChipFilter();
        observeData();
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.rv_users);
        progressBar  = view.findViewById(R.id.progress_bar);
        tvEmpty      = view.findViewById(R.id.tv_empty);
        etSearch     = view.findViewById(R.id.et_search);
        chipGroup    = view.findViewById(R.id.chip_group_filter);
    }

    // ── Setup RecyclerView ────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new UserAdapter(user -> showBanConfirmDialog(user));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    // ── Setup tìm kiếm realtime ───────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ── Setup filter theo role ────────────────────────────────────
    private void setupChipFilter() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int chipId = checkedIds.get(0);
            String role;

            if (chipId == R.id.chip_customer)    role = "customer";
            else if (chipId == R.id.chip_restaurant) role = "restaurant";
            else if (chipId == R.id.chip_shipper)    role = "shipper";
            else                                      role = "all";

            viewModel.filterByRole(role);
        });
    }

    // ── Observe dữ liệu từ ViewModel ─────────────────────────────
    private void observeData() {
        // Danh sách user sau filter/search
        viewModel.getFilteredUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.submitList(users);

            // Hiển thị empty state nếu không có kết quả
            boolean isEmpty = users == null || users.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Toast thông báo kết quả ban/unban
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Dialog xác nhận Khóa / Mở khóa ──────────────────────────
    private void showBanConfirmDialog(User user) {
        boolean isBanned = user.isBanned();
        String action = isBanned ? "mở khóa" : "khóa";
        String name   = user.getDisplayName();

        new AlertDialog.Builder(requireContext())
                .setTitle((isBanned ? "Mở khóa" : "Khóa") + " tài khoản")
                .setMessage("Bạn có chắc muốn " + action + " tài khoản của "
                        + name + " không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    viewModel.toggleBan(user);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}