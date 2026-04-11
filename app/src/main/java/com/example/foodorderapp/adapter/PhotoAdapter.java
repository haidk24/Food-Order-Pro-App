package com.example.foodorderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends PagerAdapter {
    private final Context context;
    private final List<Photo> mListPhoto;

    public PhotoAdapter(Context context, List<Photo> mListPhoto) {
        this.context = context;
        this.mListPhoto = mListPhoto != null ? mListPhoto : new ArrayList<>();
    }

    public void setData(List<Photo> photos) {
        mListPhoto.clear();
        if (photos != null) {
            mListPhoto.addAll(photos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, container, false);
        ImageView imageView = view.findViewById(R.id.imageView2);

        Photo photo = mListPhoto.get(position);
        Glide.with(context)
                .load(photo.getImageUrl())
                .placeholder(R.drawable.banghoa)
                .error(R.drawable.banghoa)
                .into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mListPhoto.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
