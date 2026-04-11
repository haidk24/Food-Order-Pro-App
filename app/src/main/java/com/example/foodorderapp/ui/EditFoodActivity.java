package com.example.foodorderapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditFoodActivity extends AppCompatActivity {

    private EditText edtFoodName, edtFoodOldPrice, edtFoodNewPrice, edtFoodDiscount, edtFoodDescription;
    private Button btnSaveFood;
    private ImageView imgFoodPreview;
    private ImageButton btnBack;

    private FirebaseFirestore db;

    private final String CURRENT_RESTAURANT_ID = "REST_001";

    private String currentFoodId;
    private String oldImageUrl;
    private Uri newImageUri = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    imgFoodPreview.setImageURI(newImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food);

        edtFoodName = findViewById(R.id.edtFoodName);
        edtFoodOldPrice = findViewById(R.id.edtFoodOldPrice);
        edtFoodNewPrice = findViewById(R.id.edtFoodNewPrice);
        edtFoodDiscount = findViewById(R.id.edtFoodDiscount);
        edtFoodDescription = findViewById(R.id.edtFoodDescription);
        btnSaveFood = findViewById(R.id.btnSaveFood);
        imgFoodPreview = findViewById(R.id.imgFoodPreview);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        currentFoodId = getIntent().getStringExtra("EXTRA_FOOD_ID");
        String oldName = getIntent().getStringExtra("EXTRA_NAME");
        double oldPrice = getIntent().getDoubleExtra("EXTRA_OLD_PRICE",
                getIntent().getDoubleExtra("EXTRA_PRICE", 0));
        double newPrice = getIntent().getDoubleExtra("EXTRA_NEW_PRICE", oldPrice);
        int oldDiscount = getIntent().getIntExtra("EXTRA_DISCOUNT", 0);
        String oldDesc = getIntent().getStringExtra("EXTRA_DESC");
        oldImageUrl = getIntent().getStringExtra("EXTRA_IMAGE_URL");

        edtFoodName.setText(oldName);
        edtFoodOldPrice.setText(String.valueOf((long) oldPrice));
        edtFoodNewPrice.setText(String.valueOf((long) newPrice));
        edtFoodDiscount.setText(String.valueOf(oldDiscount));
        edtFoodDescription.setText(oldDesc);

        showImagePreview(oldImageUrl);

        btnBack.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Hủy bỏ thay đổi?")
                    .setMessage("Những thông tin bạn vừa nhập sẽ không được lưu lại trên hệ thống. Bạn có chắc chắn muốn thoát?")
                    .setPositiveButton("THOÁT", (dialog, which) -> finish())
                    .setNegativeButton("Ở LẠI", null)
                    .show();
        });

        imgFoodPreview.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageLauncher.launch(intent);
        });

        btnSaveFood.setOnClickListener(v -> processForm());
    }

    private void processForm() {
        String newName = edtFoodName.getText().toString().trim();
        String oldPriceStr = edtFoodOldPrice.getText().toString().trim();
        String newPriceStr = edtFoodNewPrice.getText().toString().trim();
        String discountStr = edtFoodDiscount.getText().toString().trim();
        String newDesc = edtFoodDescription.getText().toString().trim();

        if (newName.isEmpty() || newPriceStr.isEmpty()) {
            Toast.makeText(this, "Vui long nhap Ten mon va Gia moi!", Toast.LENGTH_SHORT).show();
            return;
        }

        double oldPrice;
        double newPrice;
        int discountPercent;

        try {
            newPrice = Double.parseDouble(newPriceStr);
            oldPrice = oldPriceStr.isEmpty() ? newPrice : Double.parseDouble(oldPriceStr);
            discountPercent = discountStr.isEmpty() ? calculateDiscount(oldPrice, newPrice) : Integer.parseInt(discountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Gia tri gia hoac giam gia khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPrice <= 0 || oldPrice <= 0) {
            Toast.makeText(this, "Gia tien phai lon hon 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountPercent < 0 || discountPercent > 100) {
            Toast.makeText(this, "Discount phai nam trong khoang 0-100", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveFood.setEnabled(false);

        if (newImageUri != null) {
            btnSaveFood.setText("Đang tải ảnh mới lên...");
            encodeImageAndUpdate(newName, oldPrice, newPrice, discountPercent, newDesc, newImageUri);
        } else {
            updateDataToFirestore(newName, oldPrice, newPrice, discountPercent, newDesc, oldImageUrl);
        }
    }

    private int calculateDiscount(double oldPrice, double newPrice) {
        if (oldPrice <= 0 || newPrice >= oldPrice) {
            return 0;
        }
        return (int) Math.round(((oldPrice - newPrice) / oldPrice) * 100);
    }

    private void encodeImageAndUpdate(String name, double oldPrice, double newPrice,
                                      int discountPercent, String desc, Uri uri) {
        new Thread(() -> {
            try {
                String imageData = buildImageDataUri(uri);
                runOnUiThread(() -> updateDataToFirestore(name, oldPrice, newPrice, discountPercent, desc, imageData));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetButton();
                });
            }
        }).start();
    }

    private String buildImageDataUri(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) {
            throw new Exception("Khong mo duoc anh da chon");
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        if (bitmap == null) {
            throw new Exception("Dinh dang anh khong hop le");
        }

        Bitmap resizedBitmap = resizeBitmap(bitmap, 800);
        String encoded = encodeBitmapSafely(resizedBitmap);
        if (resizedBitmap != bitmap) {
            resizedBitmap.recycle();
        }
        bitmap.recycle();

        return "data:image/jpeg;base64," + encoded;
    }

    private Bitmap resizeBitmap(Bitmap source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        int longestSide = Math.max(width, height);
        if (longestSide <= maxSize) {
            return source;
        }

        float ratio = (float) maxSize / (float) longestSide;
        int targetWidth = Math.round(width * ratio);
        int targetHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }

    private String encodeBitmapSafely(Bitmap bitmap) throws Exception {
        int quality = 70;
        String encoded;

        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            quality -= 10;
        } while (encoded.length() > 700000 && quality >= 30);

        if (encoded.length() > 700000) {
            throw new Exception("Anh qua lon, vui long chon anh nho hon");
        }
        return encoded;
    }

    private void showImagePreview(String imageValue) {
        if (imageValue == null || imageValue.isEmpty()) {
            return;
        }

        if (imageValue.startsWith("data:image")) {
            int commaIndex = imageValue.indexOf(',');
            if (commaIndex > 0 && commaIndex < imageValue.length() - 1) {
                String base64Part = imageValue.substring(commaIndex + 1);
                byte[] decoded = Base64.decode(base64Part, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    imgFoodPreview.setImageBitmap(bitmap);
                    return;
                }
            }
        }

        Glide.with(this).load(imageValue).centerCrop().into(imgFoodPreview);
    }

    private void updateDataToFirestore(String name, double oldPrice, double newPrice,
                                       int discountPercent, String desc, String finalImageUrl) {
        btnSaveFood.setText("Đang cập nhật dữ liệu...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("oldPrice", oldPrice);
        updates.put("newPrice", newPrice);
        updates.put("discountPercent", discountPercent);
        updates.put("price", newPrice);
        updates.put("description", desc);

        if (finalImageUrl != null) {
            updates.put("imageUrl", finalImageUrl);
        }

        db.collection("restaurants").document(CURRENT_RESTAURANT_ID)
                .collection("foods").document(currentFoodId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        btnSaveFood.setEnabled(true);
        btnSaveFood.setText("CẬP NHẬT MÓN ĂN");
    }
}