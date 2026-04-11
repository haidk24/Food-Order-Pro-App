package com.example.foodorderapp.ui.admin;


import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.ui.admin.adapter.UserAdapter;
import com.example.foodorderapp.viewModel.AdminViewModel;
import com.google.android.material.chip.ChipGroup;

public class UserListFragment extends Fragment {

    private AdminViewModel viewModel;
    private UserAdapter adapter;
    private ProgressBar    progressLoadMore;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        progressLoadMore = view.findViewById(R.id.progress_load_more);

        setupRecyclerView(view);
        setupFilterChips(view);
        observeData();

        // Load trang đầu với filter "all"
        viewModel.loadFirstPageUsers("all");
    }

    // ── RecyclerView + infinite scroll ───────────────────────────
    private void setupRecyclerView(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_users);

        adapter = new UserAdapter(user -> {
            // Navigate sang UserDetailFragment với uid
            Bundle args = new Bundle();
            args.putString("uid", user.getUid());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_users_to_detail, args);
        });

        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);

        // Infinite scroll: load thêm khi còn 5 item trước cuối
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return; // chỉ trigger khi scroll xuống
                int total      = lm.getItemCount();
                int lastVisible = lm.findLastVisibleItemPosition();
                if (lastVisible >= total - 5) {
                    viewModel.loadNextPageUsers();
                }
            }
        });
    }

    // ── Filter chips ──────────────────────────────────────────────
    private void setupFilterChips(View view) {
        ChipGroup chips = view.findViewById(R.id.chip_group_filter);
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chip_all)        viewModel.loadFirstPageUsers("all");
            else if (id == R.id.chip_customer)   viewModel.loadFirstPageUsers("customer");
            else if (id == R.id.chip_restaurant) viewModel.loadFirstPageUsers("restaurant");
        });
    }

    // ── Observe LiveData ──────────────────────────────────────────
    private void observeData() {
        viewModel.getUserList().observe(getViewLifecycleOwner(), users -> {
            if (users != null) adapter.submitList(users);
        });

        viewModel.getIsLoadingMore().observe(getViewLifecycleOwner(), loading ->
                progressLoadMore.setVisibility(
                        Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );

        viewModel.getHasMoreUsers().observe(getViewLifecycleOwner(), hasMore -> {
            if (Boolean.FALSE.equals(hasMore)
                    && adapter.getItemCount() > 0) {
                Toast.makeText(requireContext(),
                        "Đã hiển thị tất cả " + adapter.getItemCount() + " người dùng",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }
}