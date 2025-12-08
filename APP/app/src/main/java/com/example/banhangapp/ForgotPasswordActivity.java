package com.example.banhangapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.utils.AnimationHelper;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendResetLink;
    private ProgressBar progressBar;
    private ApiService apiService;
    private Call<Map<String, String>> forgotPasswordCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = RetrofitClient.getApiService();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        progressBar = findViewById(R.id.progressBar);

        // Set up back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quên mật khẩu");
        }

        btnSendResetLink.setOnClickListener(v -> {
            AnimationHelper.pulse(v);
            sendResetLink();
        });

        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
            });
        }
    }

    private void sendResetLink() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnSendResetLink.setEnabled(false);

        ApiService.ForgotPasswordRequest request = new ApiService.ForgotPasswordRequest(email);
        forgotPasswordCall = apiService.forgotPassword(request);

        forgotPasswordCall.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnSendResetLink.setEnabled(true);

                if (response.isSuccessful()) {
                    String message = "Đã gửi link đặt lại mật khẩu đến email của bạn. Vui lòng kiểm tra hộp thư.";
                    if (response.body() != null && response.body().containsKey("message")) {
                        message = response.body().get("message");
                    }
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate back to login after a delay
                    etEmail.postDelayed(() -> {
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
                    }, 2000);
                } else {
                    String errorMsg = "Không thể gửi email đặt lại mật khẩu";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("not found") || errorBody.contains("không tồn tại")) {
                                errorMsg = "Email không tồn tại trong hệ thống";
                            } else if (errorBody.contains("message")) {
                                errorMsg = errorBody;
                            }
                        }
                    } catch (Exception e) {
                        errorMsg += " (Mã lỗi: " + response.code() + ")";
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnSendResetLink.setEnabled(true);

                String errorMsg = "Lỗi kết nối";
                if (t != null && t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel ongoing network calls
        if (forgotPasswordCall != null && !forgotPasswordCall.isCanceled()) {
            forgotPasswordCall.cancel();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
        return true;
    }
}

