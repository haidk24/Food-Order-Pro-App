package com.example.foodorderapp.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

public class RestaurantApprovalFragment extends Fragment {

    private AdminViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_restaurant_approval, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AdminViewModel.class);

        setupTabs(view);

        // Lắng nghe realtime Firestore
        viewModel.observeRestaurants();

        // Observe kết quả action
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

    // ── 3 tabs tương ứng 3 status trong schema ────────────────────
    private void setupTabs(View view) {
        TabLayout  tabs   = view.findViewById(R.id.tab_layout);
        ViewPager2 pager  = view.findViewById(R.id.view_pager);

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
                case 0: tab.setText("Chờ duyệt");  break;
                case 1: tab.setText("Hoạt động");  break;
                case 2: tab.setText("Tạm ngưng");  break;
            }
        }).attach();
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 0: pending
    // ════════════════════════════════════════════════════════════
    public static class PendingTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater i, ViewGroup c, Bundle s) {
            return i.inflate(R.layout.fragment_restaurant_list, c, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            vm = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(new RestaurantAdapter.Listener() {

                @Override
                public void onApprove(Restaurant r) {
                    // pending → active
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Duyệt nhà hàng")
                            .setMessage("Duyệt \"" + r.name + "\"?\nStatus sẽ chuyển → active")
                            .setPositiveButton("Duyệt", (d, w) ->
                                    vm.approveRestaurant(r.restaurantId))
                            .setNegativeButton("Huỷ", null)
                            .show();
                }

                @Override
                public void onSuspend(Restaurant r) {
                    // pending → suspended (từ chối)
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Từ chối nhà hàng")
                            .setMessage("Từ chối \"" + r.name + "\"?\nStatus sẽ chuyển → suspended")
                            .setPositiveButton("Từ chối", (d, w) ->
                                    vm.rejectRestaurant(r.restaurantId))
                            .setNegativeButton("Huỷ", null)
                            .show();
                }

                @Override public void onReinstate(Restaurant r) {}

                @Override
                public void onViewDetail(Restaurant r) {
                    Toast.makeText(requireContext(),
                            r.name + " · " + r.getAddressText(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: navigate to RestaurantDetailFragment
                }

            }, RestaurantAdapter.TYPE_PENDING);

            rv.setAdapter(adapter);

            vm.getPendingRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                showEmpty(view, list == null || list.isEmpty());
            });
        }

        private void showEmpty(View view, boolean empty) {
            view.findViewById(R.id.tv_empty).setVisibility(
                    empty ? View.VISIBLE : View.GONE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 1: active
    // ════════════════════════════════════════════════════════════
    public static class ActiveTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater i, ViewGroup c, Bundle s) {
            return i.inflate(R.layout.fragment_restaurant_list, c, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            vm = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(new RestaurantAdapter.Listener() {

                @Override public void onApprove(Restaurant r) {}

                @Override
                public void onSuspend(Restaurant r) {
                    // active → suspended
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Tạm ngưng nhà hàng")
                            .setMessage("Tạm ngưng \"" + r.name + "\"?")
                            .setPositiveButton("Tạm ngưng", (d, w) ->
                                    vm.suspendRestaurant(r.restaurantId))
                            .setNegativeButton("Huỷ", null)
                            .show();
                }

                @Override public void onReinstate(Restaurant r) {}

                @Override
                public void onViewDetail(Restaurant r) {
                    Toast.makeText(requireContext(),
                            r.name + " · ★ " + r.rating,
                            Toast.LENGTH_SHORT).show();
                }

            }, RestaurantAdapter.TYPE_ACTIVE);

            rv.setAdapter(adapter);

            vm.getActiveRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                showEmpty(view, list == null || list.isEmpty());
            });
        }

        private void showEmpty(View view, boolean empty) {
            view.findViewById(R.id.tv_empty).setVisibility(
                    empty ? View.VISIBLE : View.GONE);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TAB 2: suspended
    // ════════════════════════════════════════════════════════════
    public static class SuspendedTab extends Fragment {

        private AdminViewModel    vm;
        private RestaurantAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater i, ViewGroup c, Bundle s) {
            return i.inflate(R.layout.fragment_restaurant_list, c, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            vm = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

            RecyclerView rv = view.findViewById(R.id.rv_restaurants);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new RestaurantAdapter(new RestaurantAdapter.Listener() {

                @Override public void onApprove(Restaurant r) {}
                @Override public void onSuspend(Restaurant r)  {}

                @Override
                public void onReinstate(Restaurant r) {
                    // suspended → active
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Phục hồi nhà hàng")
                            .setMessage("Phục hồi \"" + r.name + "\"?\nStatus sẽ chuyển → active")
                            .setPositiveButton("Phục hồi", (d, w) ->
                                    vm.reinstateRestaurant(r.restaurantId))
                            .setNegativeButton("Huỷ", null)
                            .show();
                }

                @Override
                public void onViewDetail(Restaurant r) {
                    Toast.makeText(requireContext(),
                            r.name, Toast.LENGTH_SHORT).show();
                }

            }, RestaurantAdapter.TYPE_SUSPENDED);

            rv.setAdapter(adapter);

            vm.getSuspendedRestaurantList().observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list != null ? list : new ArrayList<>());
                showEmpty(view, list == null || list.isEmpty());
            });
        }

        private void showEmpty(View view, boolean empty) {
            view.findViewById(R.id.tv_empty).setVisibility(
                    empty ? View.VISIBLE : View.GONE);
        }
    }
}