package com.example.foodorderapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.ui.admin.AdminActivity;
import com.example.foodorderapp.viewModel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText edtSignInEmail;
    private TextInputEditText edtSignInPassword;
    private MaterialButton btnSignIn;
    private MaterialButton btnGoogle;
    private MaterialButton btnFacebook;
    private CircularProgressIndicator signInProgress;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        authViewModel = new AuthViewModel();
        edtSignInEmail = findViewById(R.id.edtSignInEmail);
        edtSignInPassword = findViewById(R.id.edtSignInPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        signInProgress = findViewById(R.id.signInProgress);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        findViewById(R.id.tvGoToSignUp).setOnClickListener(
                v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class))
        );
    }

    private void attemptSignIn() {
        String email = edtSignInEmail.getText() != null ? edtSignInEmail.getText().toString().trim() : "";
        String password = edtSignInPassword.getText() != null ? edtSignInPassword.getText().toString().trim() : "";

        if (!isInputValid(email, password)) return;

        setLoading(true);
        // Đăng nhập thật từ Firebase
        authViewModel.login(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                User user = task.getResult();
                handleLoginSuccess(user);
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginSuccess(User user) {
        if (user == null) {
            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Chào mừng " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        
        if ("admin".equals(user.getRole())) {
            // Role Admin -> Vào trang quản trị
            startActivity(new Intent(SignInActivity.this, AdminActivity.class));
        } else {
            // Role khác (customer, shipper...) -> Vào trang chủ chính
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }
        finish();
    }

    private boolean isInputValid(String email, String password) {
        if (email.isEmpty()) {
            edtSignInEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtSignInEmail.setError("Email không hợp lệ");
            return false;
        }
        if (password.length() < 6) {
            edtSignInPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        if (signInProgress != null) {
            signInProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        btnSignIn.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
        btnFacebook.setEnabled(!isLoading);
    }
}