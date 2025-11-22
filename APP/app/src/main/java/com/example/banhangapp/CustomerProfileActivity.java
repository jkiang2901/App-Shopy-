package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.User;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPhone, etAddress;
    private Button btnSave;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ");
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);

        etEmail.setEnabled(false); // Email cannot be changed

        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProfile() {
        String token = prefsHelper.getToken();
        Call<User> call = apiService.getCurrentUser(token);
        
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    etName.setText(user.getName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhone());
                    etAddress.setText(user.getAddress());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(CustomerProfileActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        User user = new User();
        user.setName(etName.getText().toString().trim());
        user.setPhone(etPhone.getText().toString().trim());
        user.setAddress(etAddress.getText().toString().trim());

        String token = prefsHelper.getToken();
        Call<User> call = apiService.updateCurrentUser(token, user);
        
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CustomerProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CustomerProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(CustomerProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

