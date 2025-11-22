package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.UserAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.User;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerManageCustomersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_manage_customers);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        // Initialize adapter - Seller can only view customers, not edit/delete
        adapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Show user details
            }

            @Override
            public void onUserEdit(User user) {
                Toast.makeText(SellerManageCustomersActivity.this, "Bạn không có quyền chỉnh sửa khách hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserDelete(User user) {
                Toast.makeText(SellerManageCustomersActivity.this, "Bạn không có quyền xóa khách hàng", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        loadCustomers();
    }

    private void loadCustomers() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Seller can only see customers who have ordered from them
        Call<List<User>> call = apiService.getCustomers(token, null);
        
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    android.util.Log.d("SellerCustomers", "Loaded " + users.size() + " customers");
                    adapter.updateUsers(users);
                    Toast.makeText(SellerManageCustomersActivity.this, "Đã tải " + users.size() + " khách hàng", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi tải dữ liệu: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("SellerCustomers", errorMsg);
                    Toast.makeText(SellerManageCustomersActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                android.util.Log.e("SellerCustomers", "Network error", t);
                Toast.makeText(SellerManageCustomersActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

