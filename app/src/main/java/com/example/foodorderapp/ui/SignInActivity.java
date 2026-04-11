package com.example.foodorderapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.data.model.User;
import com.example.foodorderapp.ui.admin.AdminActivity;
import com.example.foodorderapp.viewModel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText edtSignInEmail;
    private TextInputEditText edtSignInPassword;
    private MaterialButton btnSignIn;
    private MaterialButton btnGoogle;
    private CircularProgressIndicator signInProgress;
    private AuthViewModel authViewModel;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Log.w("GoogleSignIn", "Google sign in failed", e);
                        Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

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
        signInProgress = findViewById(R.id.signInProgress);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cấu hình Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        findViewById(R.id.tvGoToSignUp).setOnClickListener(
                v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class))
        );

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        authViewModel.signInWithGoogle(idToken).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                handleLoginSuccess(task.getResult());
            } else {
                Toast.makeText(this, "Xác thực với hệ thống thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                setLoading(true);
                authViewModel.getCurrentUserProfile().addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        handleLoginSuccess(task.getResult());
                    }
                });
            }
        } catch (IllegalStateException ignored) {
        }
    }

    private void attemptSignIn() {
        String email = edtSignInEmail.getText() != null ? edtSignInEmail.getText().toString().trim() : "";
        String password = edtSignInPassword.getText() != null ? edtSignInPassword.getText().toString().trim() : "";

        if (!isInputValid(email, password)) return;

        setLoading(true);
        authViewModel.login(email, password).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                handleLoginSuccess(task.getResult());
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
        navigateByRole(user.getRole());
    }

    private void navigateByRole(String role) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        Intent intent;

        if ("admin".equals(normalizedRole)) {
            intent = new Intent(SignInActivity.this, AdminActivity.class);
        } else if ("restaurant".equals(normalizedRole)) {
            // Cần đảm bảo có MenuActivity hoặc thay bằng MainActivity
            intent = new Intent(SignInActivity.this, MainActivity.class); 
        } else {
            intent = new Intent(SignInActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
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

    private void setLoading(boolean isLoading) {
        if (signInProgress != null) {
            signInProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        btnSignIn.setEnabled(!isLoading);
        btnGoogle.setEnabled(!isLoading);
    }
}