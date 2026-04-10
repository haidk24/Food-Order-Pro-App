package com.example.foodorderapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.FoodAdapter;
import com.example.foodorderapp.adapter.PhotoAdapter;
import com.example.foodorderapp.data.model.Food;
import com.example.foodorderapp.data.model.Photo;
import com.example.foodorderapp.ui.activity.FoodDetailActivity;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment {
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private PhotoAdapter photoAdapter;

    private RecyclerView rvCategories;
    private FoodAdapter foodAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Slide Image
        viewPager = view.findViewById(R.id.vp);
        circleIndicator = view.findViewById(R.id.circle_indicator);

        photoAdapter = new PhotoAdapter(getContext(), getListPhoto());
        viewPager.setAdapter(photoAdapter);

        circleIndicator.setViewPager(viewPager);
        photoAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());

        // Categories RecyclerView (Grid 2 columns)
        rvCategories = view.findViewById(R.id.rv_categories);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvCategories.setLayoutManager(gridLayoutManager);

        foodAdapter = new FoodAdapter(getListFood(), food -> {
            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("object_food", food);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        rvCategories.setAdapter(foodAdapter);
    }

    private List<Photo> getListPhoto() {
        List<Photo> list = new ArrayList<>();
        list.add(new Photo(R.drawable.banghoa));
        list.add(new Photo(R.drawable.giatocrong));
        list.add(new Photo(R.drawable.hoanhon));
        return list;
    }

    private List<Food> getListFood() {
        List<Food> list = new ArrayList<>();
        String longDescription = "Bò cuộn phô mai xốt nấm với hương vị tuyệt vời, chút béo, thơm phức, ngọt thịt chính là món ăn ngon mà chúng tôi muốn giới thiệu đến bạn hôm nay. Cùng Bếp Trưởng Á Âu vào bếp và thực hiện món ăn này ngay thôi nào cả nhà. Bạn đã bao giờ từng tìm kiếm cách làm bò cuộn phô mai nhưng vẫn chưa thể thực hiện theo vì các công đoạn quá tỉ mỉ và cầu kì? Thực chất những món Âu đều có điểm chung là có khá nhiều bước thực hiện, tuy nhiên các bước này lại không quá khó. Cùng với công thức cụ thể và video hướng dẫn trực quan dưới đây, hy vọng bạn sẽ có được thành phẩm hoàn hảo nhất cho bữa ăn gia đình.";
        
        list.add(new Food(1, "Bò cuộn phô mai", R.drawable.banh, 250000, 213000, "Giảm 15%", longDescription));
        list.add(new Food(2, "Sườn xào chua ngọt", R.drawable.banh, 100000, 90000, "Giảm 10%", longDescription));
        list.add(new Food(3, "Bánh mì kẹp thịt", R.drawable.banh, 30000, 25000, "Giảm 5%", longDescription));
        list.add(new Food(4, "Gà rán KFC", R.drawable.banh, 150000, 120000, "Giảm 20%", longDescription));
        list.add(new Food(5, "Pizza hải sản", R.drawable.banh, 200000, 180000, "Giảm 10%", longDescription));
        list.add(new Food(6, "Mỳ Ý sốt bò băm", R.drawable.banh, 80000, 70000, "Giảm 12%", longDescription));
        return list;
    }
}
