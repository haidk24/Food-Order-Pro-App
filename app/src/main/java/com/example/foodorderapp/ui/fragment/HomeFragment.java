package com.example.foodorderapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.PhotoAdapter;
import com.example.foodorderapp.adapter.RestaurantAdapter;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.data.model.Photo;
import com.example.foodorderapp.data.model.Restaurant;
import com.example.foodorderapp.ui.activity.FoodDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private PhotoAdapter photoAdapter;

    private RecyclerView rvPopular;
    private RestaurantAdapter restaurantAdapter;
    private List<Restaurant> mListRestaurant;
    private FirebaseFirestore mFirestore;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirestore = FirebaseFirestore.getInstance();

        // Slide Image
        viewPager = view.findViewById(R.id.vp);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        photoAdapter = new PhotoAdapter(getContext(), getListPhoto());
        viewPager.setAdapter(photoAdapter);

        circleIndicator.setViewPager(viewPager);
        photoAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());

        // Restaurants RecyclerView
        rvPopular = view.findViewById(R.id.rv_popular);
        initRestaurantRecyclerView();
        getListRestaurantFromFirestore();
    }

    private void initRestaurantRecyclerView() {
        mListRestaurant = new ArrayList<>();
        restaurantAdapter = new RestaurantAdapter(getContext(), mListRestaurant, food -> {
            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("object_food", food);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvPopular.setLayoutManager(linearLayoutManager);
        rvPopular.setAdapter(restaurantAdapter);
    }

    private void getListRestaurantFromFirestore() {
        mFirestore.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mListRestaurant.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Restaurant restaurant = new Restaurant();
                        restaurant.setId(document.getId());
                        restaurant.setName(document.getString("name"));
                        
                        List<Food> foodList = new ArrayList<>();
                        // Lấy sub-collection "foods" của từng nhà hàng
                        document.getReference().collection("foods").get()
                                .addOnSuccessListener(foodSnapshots -> {
                                    for (QueryDocumentSnapshot foodDoc : foodSnapshots) {
                                        Food food = foodDoc.toObject(Food.class);
                                        foodList.add(food);
                                    }
                                    restaurant.setFoods(foodList);
                                    restaurantAdapter.notifyDataSetChanged();
                                });
                        
                        mListRestaurant.add(restaurant);
                    }
                    restaurantAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Photo> getListPhoto() {
        List<Photo> list = new ArrayList<>();
        list.add(new Photo(R.drawable.banghoa));
        list.add(new Photo(R.drawable.giatocrong));
        list.add(new Photo(R.drawable.hoanhon));
        return list;
    }
}
