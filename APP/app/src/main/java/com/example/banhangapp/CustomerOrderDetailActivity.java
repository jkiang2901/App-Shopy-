package com.example.banhangapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.OrderItemAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerOrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderId, tvDate, tvTotalAmount, tvDiscountAmount, tvFinalAmount;
    private TextView tvPaymentMethod, tvDeliveryAddress;
    private com.google.android.material.chip.Chip tvStatus;
    private RecyclerView recyclerViewItems;
    private OrderItemAdapter adapter;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Chi tiết đơn hàng");
            }
        }

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        initViews();
        loadOrderDetail();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvDate = findViewById(R.id.tvDate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount);
        tvFinalAmount = findViewById(R.id.tvFinalAmount);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter();
        recyclerViewItems.setAdapter(adapter);
    }

    private void loadOrderDetail() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<Order> call = apiService.getOrderById(token, orderId);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                try {
                    if (response.isSuccessful()) {
                        Order order = null;
                        try {
                            order = response.body();
                            android.util.Log.d("OrderDetail", "Order loaded: " + (order != null ? order.getId() : "null"));
                        } catch (IllegalStateException e) {
                            android.util.Log.e("OrderDetail", "Error reading response body", e);
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(CustomerOrderDetailActivity.this, 
                                    "Lỗi đọc dữ liệu đơn hàng", 
                                    Toast.LENGTH_SHORT).show();
                            }
                            return;
                        } catch (Exception e) {
                            android.util.Log.e("OrderDetail", "Unexpected error parsing order", e);
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(CustomerOrderDetailActivity.this, 
                                    "Lỗi xử lý dữ liệu đơn hàng", 
                                    Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        
                        if (order != null) {
                            displayOrder(order);
                        } else {
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(CustomerOrderDetailActivity.this, 
                                    "Không tìm thấy đơn hàng", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (response.code() == 401) {
                            handleUnauthorized();
                        } else {
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(CustomerOrderDetailActivity.this, 
                                    "Lỗi tải chi tiết đơn hàng: " + response.code(), 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("OrderDetail", "Error in onResponse", e);
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(CustomerOrderDetailActivity.this, 
                            "Lỗi xử lý dữ liệu", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                String errorMsg = "Lỗi kết nối";
                if (t != null) {
                    if (t instanceof IllegalStateException) {
                        errorMsg = "Lỗi xử lý dữ liệu phản hồi";
                    } else if (t.getMessage() != null && t.getMessage().length() < 100) {
                        errorMsg += ": " + t.getMessage();
                    }
                }
                
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(CustomerOrderDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayOrder(Order order) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        if (tvOrderId != null) {
            String orderIdText = order.getId();
            if (orderIdText != null && orderIdText.length() > 8) {
                tvOrderId.setText("Đơn hàng: " + orderIdText.substring(0, 8) + "...");
            } else {
                tvOrderId.setText("Đơn hàng: " + orderIdText);
            }
        }

        if (tvStatus != null) {
            String status = order.getStatus() != null ? order.getStatus() : "pending";
            String statusText = getStatusText(status);
            tvStatus.setText(statusText);
            
            // Set chip color based on status
            int chipBackgroundColor = R.color.info_light;
            int chipStrokeColor = R.color.info;
            
            switch (status) {
                case "pending":
                    chipBackgroundColor = R.color.warning_light;
                    chipStrokeColor = R.color.warning;
                    break;
                case "confirmed":
                    chipBackgroundColor = R.color.info_light;
                    chipStrokeColor = R.color.info;
                    break;
                case "shipping":
                    chipBackgroundColor = R.color.info_light;
                    chipStrokeColor = R.color.info;
                    break;
                case "delivered":
                    chipBackgroundColor = R.color.success_light;
                    chipStrokeColor = R.color.success;
                    break;
                case "cancelled":
                    chipBackgroundColor = R.color.error_light;
                    chipStrokeColor = R.color.error;
                    break;
            }
            tvStatus.setChipBackgroundColorResource(chipBackgroundColor);
            tvStatus.setChipStrokeColorResource(chipStrokeColor);
        }

        if (tvDate != null && order.getCreatedAt() != null) {
            tvDate.setText(sdf.format(order.getCreatedAt()));
        }

        if (tvTotalAmount != null) {
            tvTotalAmount.setText(format.format(order.getTotalAmount()));
        }

        if (tvDiscountAmount != null) {
            View layoutDiscount = findViewById(R.id.layoutDiscount);
            if (order.getDiscountAmount() > 0) {
                tvDiscountAmount.setText("-" + format.format(order.getDiscountAmount()));
                if (layoutDiscount != null) {
                    layoutDiscount.setVisibility(android.view.View.VISIBLE);
                }
            } else {
                if (layoutDiscount != null) {
                    layoutDiscount.setVisibility(android.view.View.GONE);
                }
            }
        }

        if (tvFinalAmount != null) {
            tvFinalAmount.setText(format.format(order.getFinalAmount()));
        }

        if (tvPaymentMethod != null) {
            String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod() : "Chưa xác định";
            tvPaymentMethod.setText(paymentMethod);
        }

        if (tvDeliveryAddress != null) {
            String address = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Chưa có địa chỉ";
            tvDeliveryAddress.setText(address);
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            adapter.updateItems(order.getItems());
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "⏳ Chờ xử lý";
            case "confirmed":
                return "✅ Đã xác nhận";
            case "shipping":
                return "🚚 Đang giao hàng";
            case "delivered":
                return "🎉 Đã giao hàng";
            case "cancelled":
                return "❌ Đã hủy";
            default:
                return status;
        }
    }

    private void handleUnauthorized() {
        prefsHelper.clear();
        Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

