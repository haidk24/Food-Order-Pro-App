package com.example.foodorderapp.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Restaurant;
import com.example.foodorderapp.ui.admin.adapter.RestaurantAdapter;
import com.example.foodorderapp.viewModel.AdminViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class RestaurantApprovalFragment extends Fragment {

    private AdminViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_restaurant_approval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        setupTabs(view);
        viewModel.observeRestaurants();

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok))
                Toast.makeText(requireContext(),
                        "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty())
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabs(View view) {
        TabLayout  tabs  = view.findViewById(R.id.tab_layout);
        ViewPager2 pager = view.findViewById(R.id.view_pager);

        pager.setAdapter(new androidx.viewpager2.adapter
                .FragmentStateAdapter(this) {
            @NonNull @Override
            public Fragment createFragment(int pos) {
                switch (pos) {
                    case 0:  return new PendingTab();
                    case 1:  return new ActiveTab();
                    default: return new SuspendedTab();
                }
            }
            @Override public int getItemCount() { return 3; }
        });

        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            switch (pos) {
                case 0: tab.setText("Chờ duyệt"); break;
                case 1: tab.setText("Hoạt động"); break;
                case 2: tab.setText("Tạm ngưng"); break;
            }
        }).attach();
    }

    private static void updateEmptyState(View root, List<?> list) {
        TextView tvEmpty = root.findViewById(R.id.tv_empty);
        RecyclerView rv  = root.findViewById(R.id.rv_restaurants);

        boolean isEmpty = (list == null || list.isEmpty());
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private static void navigateToDetail(Fragment fragment, Restaurant r) {
        Bundle args = new Bundle();
        args.putString("restaurantId", r.restaurantId);
        Navigation.findNavController(fragment.requireView())
                .navigate(R.id.action_restaurants_to_detail, args);
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 0: pending
    // ════════════════════════════════════════════════════════════
    public static class PendingTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(
                    R.layout.fragment_restaurant_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view,
                                  @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            vm = new ViewModelProvider(requireActivity())
                    .get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(
                    new RestaurantAdapter.Listener() {
                        @Override
                        public void onApprove(Restaurant r) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Duyệt nhà hàng")
                                    .setMessage("Duyệt \"" + r.name + "\"?")
                                    .setPositiveButton("Duyệt", (d, w) ->
                                            vm.approveRestaurant(r.restaurantId))
                                    .setNegativeButton("Huỷ", null).show();
                        }

                        @Override
                        public void onSuspend(Restaurant r) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Từ chối nhà hàng")
                                    .setMessage("Từ chối \"" + r.name + "\"?")
                                    .setPositiveButton("Từ chối", (d, w) ->
                                            vm.rejectRestaurant(r.restaurantId))
                                    .setNegativeButton("Huỷ", null).show();
                        }

                        @Override public void onReinstate(Restaurant r) {}

                        @Override
                        public void onViewDetail(Restaurant r) {
                            navigateToDetail(PendingTab.this, r);
                        }
                    },
                    RestaurantAdapter.TYPE_PENDING
            );

            rv.setAdapter(adapter);
            vm.getPendingRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                updateEmptyState(view, list);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 1: active
    // ════════════════════════════════════════════════════════════
    public static class ActiveTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(
                    R.layout.fragment_restaurant_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view,
                                  @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            vm = new ViewModelProvider(requireActivity())
                    .get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(
                    new RestaurantAdapter.Listener() {
                        @Override public void onApprove(Restaurant r) {}

                        @Override
                        public void onSuspend(Restaurant r) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Tạm ngưng nhà hàng")
                                    .setMessage("Tạm ngưng \"" + r.name + "\"?")
                                    .setPositiveButton("Tạm ngưng", (d, w) ->
                                            vm.suspendRestaurant(r.restaurantId))
                                    .setNegativeButton("Huỷ", null).show();
                        }

                        @Override public void onReinstate(Restaurant r) {}

                        @Override
                        public void onViewDetail(Restaurant r) {
                            navigateToDetail(ActiveTab.this, r);
                        }
                    },
                    RestaurantAdapter.TYPE_ACTIVE
            );

            rv.setAdapter(adapter);
            vm.getActiveRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                updateEmptyState(view, list);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 2: suspended
    // ════════════════════════════════════════════════════════════
    public static class SuspendedTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Nullable @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(
                    R.layout.fragment_restaurant_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view,
                                  @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            vm = new ViewModelProvider(requireActivity())
                    .get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(
                    new RestaurantAdapter.Listener() {
                        @Override public void onApprove(Restaurant r) {}
                        @Override public void onSuspend(Restaurant r) {}

                        @Override
                        public void onReinstate(Restaurant r) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Phục hồi nhà hàng")
                                    .setMessage("Phục hồi \"" + r.name + "\"?")
                                    .setPositiveButton("Phục hồi", (d, w) ->
                                            vm.reinstateRestaurant(r.restaurantId))
                                    .setNegativeButton("Huỷ", null).show();
                        }

                        @Override
                        public void onViewDetail(Restaurant r) {
                            navigateToDetail(SuspendedTab.this, r);
                        }
                    },
                    RestaurantAdapter.TYPE_SUSPENDED
            );

            rv.setAdapter(adapter);
            vm.getSuspendedRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                updateEmptyState(view, list);
            });
        }
    }
}
