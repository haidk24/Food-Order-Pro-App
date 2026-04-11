package com.example.foodorderapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {
    private static final int MAX_SLIDER_IMAGES = 8;
    private static final long AUTO_SLIDE_DELAY_MS = 3500L;

    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private PhotoAdapter photoAdapter;

    private RecyclerView rvPopular;
    private RestaurantAdapter restaurantAdapter;
    private final List<Restaurant> mListRestaurant = new ArrayList<>();
    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private FirebaseFirestore mFirestore;

    private EditText etSearchFood;
    private Spinner spinnerCategory;
    private String currentQuery = "";
    private String selectedCategory = "";

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || photoAdapter == null || viewPager == null) {
                return;
            }

            int count = photoAdapter.getCount();
            if (count <= 1) {
                return;
            }

            int nextItem = (viewPager.getCurrentItem() + 1) % count;
            viewPager.setCurrentItem(nextItem, true);
            sliderHandler.postDelayed(this, AUTO_SLIDE_DELAY_MS);
        }
    };

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

        viewPager = view.findViewById(R.id.vp);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        photoAdapter = new PhotoAdapter(requireContext(), new ArrayList<>());
        viewPager.setAdapter(photoAdapter);
        circleIndicator.setViewPager(viewPager);
        photoAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    stopAutoSlide();
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    startAutoSlide();
                }
            }
        });
        loadSliderPhotosFromFirestore();

        etSearchFood = view.findViewById(R.id.et_search_food);
        spinnerCategory = view.findViewById(R.id.spinner_category);

        rvPopular = view.findViewById(R.id.rv_popular);
        initRestaurantRecyclerView();
        setupFilterControls();
        getListRestaurantFromFirestore();
    }

    private void initRestaurantRecyclerView() {
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

    private void setupFilterControls() {
        selectedCategory = getString(R.string.home_category_all);

        ArrayList<String> initialCategories = new ArrayList<>();
        initialCategories.add(selectedCategory);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                initialCategories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString().trim();
                applyRestaurantFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object value = parent.getItemAtPosition(position);
                selectedCategory = value == null ? getString(R.string.home_category_all) : value.toString();
                applyRestaurantFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void rebuildCategoryOptions() {
        if (!isAdded() || spinnerCategory == null) {
            return;
        }

        String allCategoryLabel = getString(R.string.home_category_all);
        String previousSelection = TextUtils.isEmpty(selectedCategory) ? allCategoryLabel : selectedCategory;

        TreeSet<String> categorySet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant == null || restaurant.getFoods() == null) {
                continue;
            }
            for (Food food : restaurant.getFoods()) {
                if (food == null) {
                    continue;
                }
                String category = food.getCategory();
                if (!TextUtils.isEmpty(category)) {
                    categorySet.add(category.trim());
                }
            }
        }

        ArrayList<String> categories = new ArrayList<>();
        categories.add(allCategoryLabel);
        categories.addAll(categorySet);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        int selectedIndex = categories.indexOf(previousSelection);
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        spinnerCategory.setSelection(selectedIndex, false);
        selectedCategory = categories.get(selectedIndex);
    }

    private void applyRestaurantFilters() {
        if (!isAdded() || restaurantAdapter == null) {
            return;
        }

        String allCategoryLabel = getString(R.string.home_category_all);
        String normalizedQuery = TextUtils.isEmpty(currentQuery)
                ? ""
                : currentQuery.toLowerCase(Locale.ROOT);
        boolean isAllCategory = TextUtils.isEmpty(selectedCategory)
                || selectedCategory.equalsIgnoreCase(allCategoryLabel);

        List<Restaurant> filteredRestaurants = new ArrayList<>();
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant == null || restaurant.getFoods() == null) {
                continue;
            }
            List<Food> matchedFoods = new ArrayList<>();
            String restaurantName = restaurant.getName() == null ? "" : restaurant.getName();

            for (Food food : restaurant.getFoods()) {
                if (food == null) {
                    continue;
                }
                String foodName = food.getName() == null ? "" : food.getName();
                String foodCategory = food.getCategory() == null ? "" : food.getCategory().trim();

                boolean matchCategory = isAllCategory || selectedCategory.equalsIgnoreCase(foodCategory);
                boolean matchQuery = TextUtils.isEmpty(normalizedQuery)
                        || foodName.toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || restaurantName.toLowerCase(Locale.ROOT).contains(normalizedQuery);

                if (matchCategory && matchQuery) {
                    matchedFoods.add(food);
                }
            }

            if (!matchedFoods.isEmpty()) {
                Restaurant displayRestaurant = new Restaurant();
                displayRestaurant.setId(restaurant.getId());
                displayRestaurant.setName(restaurant.getName());
                displayRestaurant.setImageUrl(restaurant.getImageUrl());
                displayRestaurant.setFoods(matchedFoods);
                filteredRestaurants.add(displayRestaurant);
            }
        }

        mListRestaurant.clear();
        mListRestaurant.addAll(filteredRestaurants);
        restaurantAdapter.setData(mListRestaurant);
    }

    private void loadSliderPhotosFromFirestore() {
        mFirestore.collectionGroup("foods")
                .limit(MAX_SLIDER_IMAGES * 3L)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || photoAdapter == null || viewPager == null) {
                        return;
                    }

                    List<Photo> photos = new ArrayList<>();
                    Set<String> uniqueUrls = new HashSet<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String imageUrl = document.getString("imageUrl");
                        if (TextUtils.isEmpty(imageUrl)) {
                            continue;
                        }

                        String normalized = imageUrl.trim();
                        if (TextUtils.isEmpty(normalized) || !uniqueUrls.add(normalized)) {
                            continue;
                        }

                        photos.add(new Photo(normalized));
                        if (photos.size() >= MAX_SLIDER_IMAGES) {
                            break;
                        }
                    }

                    if (photos.isEmpty()) {
                        photos = getFallbackSliderPhotos();
                    }
                    photoAdapter.setData(photos);
                    viewPager.setCurrentItem(0, false);
                    startAutoSlide();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || photoAdapter == null || viewPager == null) {
                        return;
                    }
                    photoAdapter.setData(getFallbackSliderPhotos());
                    viewPager.setCurrentItem(0, false);
                    startAutoSlide();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải ảnh slide: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getListRestaurantFromFirestore() {
        mFirestore.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) {
                        return;
                    }

                    allRestaurants.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        rebuildCategoryOptions();
                        applyRestaurantFilters();
                        return;
                    }

                    AtomicInteger pendingRequests = new AtomicInteger(queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Restaurant restaurant = new Restaurant();
                        restaurant.setId(document.getId());
                        restaurant.setName(document.getString("name"));
                        restaurant.setImageUrl(document.getString("imageUrl"));

                        document.getReference().collection("foods").get()
                                .addOnSuccessListener(foodSnapshots -> {
                                    List<Food> foodList = new ArrayList<>();
                                    for (QueryDocumentSnapshot foodDoc : foodSnapshots) {
                                        Food food = foodDoc.toObject(Food.class);
                                        if (food == null) {
                                            continue;
                                        }
                                        if (TextUtils.isEmpty(food.getFoodId())) {
                                            food.setFoodId(foodDoc.getId());
                                        }
                                        food.setRestaurantId(document.getId());
                                        foodList.add(food);
                                    }

                                    if (!foodList.isEmpty()) {
                                        restaurant.setFoods(foodList);
                                        allRestaurants.add(restaurant);
                                    }
                                    onRestaurantFoodsLoaded(pendingRequests);
                                })
                                .addOnFailureListener(e -> onRestaurantFoodsLoaded(pendingRequests));
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onRestaurantFoodsLoaded(AtomicInteger pendingRequests) {
        if (pendingRequests.decrementAndGet() == 0) {
            rebuildCategoryOptions();
            applyRestaurantFilters();
        }
    }

    private void startAutoSlide() {
        stopAutoSlide();
        if (photoAdapter != null && photoAdapter.getCount() > 1) {
            sliderHandler.postDelayed(sliderRunnable, AUTO_SLIDE_DELAY_MS);
        }
    }

    private void stopAutoSlide() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private List<Photo> getFallbackSliderPhotos() {
        List<Photo> list = new ArrayList<>();
        if (!isAdded() || getContext() == null) {
            return list;
        }

        String packageName = getContext().getPackageName();
        list.add(new Photo("android.resource://" + packageName + "/" + R.drawable.banghoa));
        list.add(new Photo("android.resource://" + packageName + "/" + R.drawable.giatocrong));
        list.add(new Photo("android.resource://" + packageName + "/" + R.drawable.hoanhon));
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoSlide();
    }

    @Override
    public void onPause() {
        stopAutoSlide();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        stopAutoSlide();
        super.onDestroyView();
    }
}
