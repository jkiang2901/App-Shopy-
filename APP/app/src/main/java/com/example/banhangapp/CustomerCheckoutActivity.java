package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCheckoutActivity extends AppCompatActivity {
    private EditText etAddress, etPromotionCode;
    private RadioGroup rgPaymentMethod;
    private Button btnPlaceOrder;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_checkout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        etAddress = findViewById(R.id.etAddress);
        etPromotionCode = findViewById(R.id.etPromotionCode);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String address = etAddress.getText().toString().trim();
        String promotionCode = etPromotionCode.getText().toString().trim();
        
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        String paymentMethod = "cash"; // default
        if (selectedId == R.id.rbCard) {
            paymentMethod = "card";
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = prefsHelper.getToken();
        ApiService.OrderRequest request = new ApiService.OrderRequest(paymentMethod, address, promotionCode);
        Call<Order> call = apiService.createOrder(token, request);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    android.util.Log.d("CheckoutActivity", "Order created successfully: " + order.getId());
                    Toast.makeText(CustomerCheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Lỗi đặt hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("CheckoutActivity", errorMsg);
                    Toast.makeText(CustomerCheckoutActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                android.util.Log.e("CheckoutActivity", "Network error placing order", t);
                Toast.makeText(CustomerCheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

