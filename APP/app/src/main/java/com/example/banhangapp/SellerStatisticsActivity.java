package com.example.banhangapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerStatisticsActivity extends AppCompatActivity {
    private TextView tvDailyRevenue, tvMonthlyRevenue, tvYearlyRevenue;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_statistics);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        tvDailyRevenue = findViewById(R.id.tvDailyRevenue);
        tvMonthlyRevenue = findViewById(R.id.tvMonthlyRevenue);
        tvYearlyRevenue = findViewById(R.id.tvYearlyRevenue);

        loadRevenue("day");
        loadRevenue("month");
        loadRevenue("year");
    }

    private void loadRevenue(String period) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiService.RevenueResponse> call = apiService.getRevenue(token, period);
        
        call.enqueue(new Callback<ApiService.RevenueResponse>() {
            @Override
            public void onResponse(Call<ApiService.RevenueResponse> call, Response<ApiService.RevenueResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.RevenueResponse revenue = response.body();
                    String revenueText = String.format("%,.0f VNĐ", revenue.totalRevenue);
                    
                    switch (period) {
                        case "day":
                            if (tvDailyRevenue != null) {
                                tvDailyRevenue.setText("Doanh thu ngày: " + revenueText);
                            }
                            break;
                        case "month":
                            if (tvMonthlyRevenue != null) {
                                tvMonthlyRevenue.setText("Doanh thu tháng: " + revenueText);
                            }
                            break;
                        case "year":
                            if (tvYearlyRevenue != null) {
                                tvYearlyRevenue.setText("Doanh thu năm: " + revenueText);
                            }
                            break;
                    }
                } else {
                    android.util.Log.e("SellerStatistics", "Error loading revenue for " + period + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.RevenueResponse> call, Throwable t) {
                android.util.Log.e("SellerStatistics", "Network error loading revenue for " + period, t);
            }
        });
    }
}

