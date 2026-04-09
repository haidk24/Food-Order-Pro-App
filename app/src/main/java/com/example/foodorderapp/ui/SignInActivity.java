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
import com.example.foodorderapp.viewModel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

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

        btnGoogle.setOnClickListener(v -> Toast.makeText(
                SignInActivity.this,
                "Tính năng đăng nhập mạng xã hội sẽ cập nhật sau",
                Toast.LENGTH_SHORT
        ).show());

        btnFacebook.setOnClickListener(v -> Toast.makeText(
                SignInActivity.this,
                "Tính năng đăng nhập mạng xã hội sẽ cập nhật sau",
                Toast.LENGTH_SHORT
        ).show());
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                openMainAndFinish();
            }
        } catch (IllegalStateException ignored) {
            // Firebase is optional in local/dev builds without google-services.json.
        }
    }

    private void attemptSignIn() {
        String email = edtSignInEmail.getText() != null ? edtSignInEmail.getText().toString().trim() : "";
        String password = edtSignInPassword.getText() != null ? edtSignInPassword.getText().toString().trim() : "";

        if (!isInputValid(email, password)) {
            return;
        }

        setLoading(true);
        authViewModel.login(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                openMainAndFinish();
            } else {
                Toast.makeText(this, mapLoginError(task.getException()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isInputValid(String email, String password) {
        if (email.isEmpty()) {
            edtSignInEmail.setError("Không được để trống email");
            edtSignInEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtSignInEmail.setError("Email không hợp lệ");
            edtSignInEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            edtSignInPassword.setError("Không được để trống mật khẩu");
            edtSignInPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            edtSignInPassword.setError("Mật khẩu phải >= 6 ký tự");
            edtSignInPassword.requestFocus();
            return false;
        }
        return true;
    }

    private String mapLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_INVALID_CREDENTIAL".equals(code) ||
                    "ERROR_WRONG_PASSWORD".equals(code) ||
                    "ERROR_USER_NOT_FOUND".equals(code) ||
                    "ERROR_INVALID_LOGIN_CREDENTIALS".equals(code)) {
                return "Email hoặc mật khẩu chưa đúng";
            }
            if ("ERROR_TOO_MANY_REQUESTS".equals(code)) {
                return "Bạn đã thử quá nhiều lần, vui lòng thử lại sau";
            }
        }
        return exception != null && exception.getMessage() != null
                ? exception.getMessage()
                : "Đăng nhập thất bại, vui lòng thử lại";
    }

    private void setLoading(boolean isLoading) {
        signInProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSignIn.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
        btnFacebook.setEnabled(!isLoading);
    }

    private void openMainAndFinish() {
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }
}