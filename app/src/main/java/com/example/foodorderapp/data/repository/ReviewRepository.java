package com.example.foodorderapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.foodorderapp.data.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewRepository {

    private static ReviewRepository instance;
    private final FirebaseFirestore db;

    private ReviewRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static ReviewRepository getInstance() {
        if (instance == null) {
            instance = new ReviewRepository();
        }
        return instance;
    }

    public void submitReview(Review review, MutableLiveData<Boolean> isSuccess,
                             MutableLiveData<String> errorMessage) {
        db.collection("reviews")
            .add(review)
            .addOnSuccessListener(ref -> {
                review.setReviewId(ref.getId());
                isSuccess.setValue(true);
            })
            .addOnFailureListener(e -> {
                errorMessage.setValue(e.getMessage());
            });
    }
}