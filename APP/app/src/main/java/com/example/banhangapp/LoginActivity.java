package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.utils.AnimationHelper;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private View cardLogin, ivLogo, tvTitle, tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        // Check if already logged in
        if (prefsHelper.isLoggedIn()) {
            navigateToRoleActivity();
            return;
        }

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        cardLogin = findViewById(R.id.cardLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Apply entrance animations
        animateEntrance();

        btnLogin.setOnClickListener(v -> {
            AnimationHelper.pulse(v);
            login();
        });
        btnRegister.setOnClickListener(v -> {
            AnimationHelper.pulse(v);
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
        });
    }

    private void animateEntrance() {
        // Stagger animations
        if (ivLogo != null) {
            AnimationHelper.scaleIn(ivLogo, 400);
        }
        if (tvTitle != null) {
            tvTitle.postDelayed(() -> AnimationHelper.fadeIn(tvTitle, 400), 200);
        }
        if (tvSubtitle != null) {
            tvSubtitle.postDelayed(() -> AnimationHelper.fadeIn(tvSubtitle, 400), 400);
        }
        if (cardLogin != null) {
            cardLogin.postDelayed(() -> AnimationHelper.slideUp(cardLogin, 500), 600);
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.LoginRequest request = new ApiService.LoginRequest(email, password);
        Call<ApiService.AuthResponse> call = apiService.login(request);

        call.enqueue(new Callback<ApiService.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiService.AuthResponse> call, Response<ApiService.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.AuthResponse authResponse = response.body();
                    if (authResponse.token != null && authResponse.user != null) {
                        prefsHelper.saveToken("Bearer " + authResponse.token);
                        prefsHelper.saveUser(
                            authResponse.user.getId(),
                            authResponse.user.getName(),
                            authResponse.user.getEmail(),
                            authResponse.user.getRole()
                        );
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToRoleActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Dữ liệu phản hồi không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Try to read error body
                    String errorMessage = "Đăng nhập thất bại";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("error")) {
                                // Try to parse JSON error
                                errorMessage = errorBody;
                            } else {
                                errorMessage = "Code: " + response.code() + " - " + errorBody;
                            }
                        } else {
                            errorMessage = "Code: " + response.code() + " - " + response.message();
                        }
                    } catch (Exception e) {
                        errorMessage = "Code: " + response.code() + " - " + response.message();
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.AuthResponse> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: ";
                if (t.getMessage() != null) {
                    errorMsg += t.getMessage();
                } else {
                    errorMsg += "Không thể kết nối đến server. Kiểm tra:\n- API server đã chạy chưa?\n- URL đúng chưa? (10.0.2.2:3000 cho emulator)";
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                t.printStackTrace(); // Log to console
            }
        });
    }

    private void navigateToRoleActivity() {
        String role = prefsHelper.getUserRole();
        Intent intent;

        if ("admin".equals(role)) {
            // Admin should use web interface, not Android app
            Toast.makeText(this, "Tài khoản Admin chỉ có thể đăng nhập trên website", Toast.LENGTH_LONG).show();
            prefsHelper.clear();
            return;
        } else if ("seller".equals(role)) {
            // Seller functionality removed - redirect to customer view
            Toast.makeText(this, "Chức năng người bán đã bị vô hiệu hóa", Toast.LENGTH_LONG).show();
            intent = new Intent(this, CustomerProductListActivity.class);
        } else {
            intent = new Intent(this, CustomerProductListActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

