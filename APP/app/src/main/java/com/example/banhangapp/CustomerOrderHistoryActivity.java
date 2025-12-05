package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.OrderAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerOrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private View emptyState;
    private ProgressBar progressBar;
    private Call<List<Order>> ordersCall;
    private Call<Order> cancelOrderCall;
    private Call<Order> cancelOrderDirectCall;
    private Call<Map<String, String>> deleteOrderCall;
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerViewOrders);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar == null) {
            // Try to find progress bar with different ID
            progressBar = findViewById(android.R.id.progress);
        }
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        
        // Initialize adapter
        adapter = new OrderAdapter(new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                // Open order detail activity
                Intent intent = new Intent(CustomerOrderHistoryActivity.this, CustomerOrderDetailActivity.class);
                intent.putExtra("orderId", order.getId());
                startActivity(intent);
            }

            @Override
            public void onOrderStatusChange(Order order, String newStatus) {
                // Customers can't change order status
                Toast.makeText(CustomerOrderHistoryActivity.this, "Không thể thay đổi trạng thái đơn hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOrderCancel(Order order) {
                // Show confirmation dialog before canceling
                showCancelOrderDialog(order);
            }
        }, false); // Don't allow status update for customers
        recyclerView.setAdapter(adapter);
        
        // Initialize with empty list
        adapter.updateOrders(new ArrayList<>());

        loadOrders();
    }

    private void loadOrders() {
        android.util.Log.d("OrderHistory", "loadOrders() called");
        
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            android.util.Log.w("OrderHistory", "Token is null or empty");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        
        android.util.Log.d("OrderHistory", "Token found, making API call");
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        recyclerView.setVisibility(View.GONE);
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        
        ordersCall = apiService.getOrders(token);
        android.util.Log.d("OrderHistory", "API call created, enqueueing...");
        
        ordersCall.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                try {
                    android.util.Log.d("OrderHistory", "onResponse called. isSuccessful: " + response.isSuccessful() + ", code: " + response.code());
                    
                    // Check if activity is still valid
                    if (isFinishing() || isDestroyed() || isDestroyed) {
                        android.util.Log.w("OrderHistory", "Activity is finishing or destroyed, ignoring response");
                        return;
                    }
                    
                    // Check if call was canceled
                    if (call.isCanceled()) {
                        android.util.Log.w("OrderHistory", "Call was canceled, ignoring response");
                        return;
                    }
                    
                    // Parse response body outside of UI thread to catch parsing errors early
                    List<Order> orders = null;
                    Exception parseException = null;
                    
                    if (response.isSuccessful()) {
                        try {
                            android.util.Log.d("OrderHistory", "Parsing response body...");
                            
                            // Try to get response body - this might throw if already consumed
                            orders = response.body();
                            
                            if (orders != null) {
                                android.util.Log.d("OrderHistory", "Response parsed successfully. Orders count: " + orders.size());
                                
                                // Validate each order
                                List<Order> validOrders = new ArrayList<>();
                                for (int i = 0; i < orders.size(); i++) {
                                    try {
                                        Order order = orders.get(i);
                                        if (order != null) {
                                            // Basic validation
                                            if (order.getId() != null) {
                                                validOrders.add(order);
                                                android.util.Log.d("OrderHistory", "Order " + i + " validated: ID=" + order.getId());
                                            } else {
                                                android.util.Log.w("OrderHistory", "Order " + i + " has null ID, skipping");
                                            }
                                        } else {
                                            android.util.Log.w("OrderHistory", "Order " + i + " is null, skipping");
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("OrderHistory", "Error validating order " + i, e);
                                    }
                                }
                                
                                orders = validOrders;
                                android.util.Log.d("OrderHistory", "Valid orders count: " + orders.size());
                            } else {
                                android.util.Log.w("OrderHistory", "Response body is null");
                            }
                        } catch (IllegalStateException e) {
                            // Response body already consumed or parsing error
                            parseException = e;
                            android.util.Log.e("OrderHistory", "IllegalStateException parsing response body", e);
                            e.printStackTrace();
                        } catch (com.google.gson.JsonSyntaxException e) {
                            // JSON parsing error
                            parseException = e;
                            android.util.Log.e("OrderHistory", "JSON syntax error parsing response", e);
                            e.printStackTrace();
                        } catch (com.google.gson.JsonParseException e) {
                            // Gson parsing error
                            parseException = e;
                            android.util.Log.e("OrderHistory", "Gson parse error", e);
                            e.printStackTrace();
                        } catch (Exception e) {
                            // Other parsing errors
                            parseException = e;
                            android.util.Log.e("OrderHistory", "Unexpected error parsing response", e);
                            e.printStackTrace();
                        }
                    } else {
                        android.util.Log.w("OrderHistory", "Response not successful. Code: " + response.code());
                    }
                
                    // Create final variables for lambda
                    final List<Order> finalOrders = orders;
                    final Exception finalException = parseException;
                    final boolean isSuccessful = response.isSuccessful();
                    
                    runOnUiThread(() -> {
                        try {
                            // Double check after runOnUiThread
                            if (isFinishing() || isDestroyed() || isDestroyed) {
                                android.util.Log.w("OrderHistory", "Activity destroyed before UI update, aborting");
                                return;
                            }
                            
                            // Verify views are still available
                            if (recyclerView == null || adapter == null) {
                                android.util.Log.w("OrderHistory", "Views are null, aborting UI update");
                                return;
                            }
                            
                            try {
                                // Hide loading
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                
                                if (isSuccessful) {
                                    if (finalException != null) {
                                        // Parsing error occurred
                                        if (!isFinishing() && !isDestroyed()) {
                                            String errorMsg = "Dữ liệu phản hồi không hợp lệ. Vui lòng thử lại sau";
                                            if (finalException.getMessage() != null && finalException.getMessage().contains("Expected")) {
                                                errorMsg = "Định dạng dữ liệu không đúng. Vui lòng liên hệ hỗ trợ";
                                            }
                                            Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                        
                                        recyclerView.setVisibility(View.GONE);
                                        if (emptyState != null) {
                                            emptyState.setVisibility(View.VISIBLE);
                                        }
                                    } else if (finalOrders != null) {
                                        try {
                                            android.util.Log.d("OrderHistory", "Updating UI with " + finalOrders.size() + " orders");
                                            
                                            // Update adapter with orders
                                            if (adapter != null) {
                                                adapter.updateOrders(finalOrders);
                                                android.util.Log.d("OrderHistory", "Adapter updated successfully");
                                            } else {
                                                android.util.Log.w("OrderHistory", "Adapter is null!");
                                            }
                                            
                                            // Update UI visibility
                                            if (finalOrders.isEmpty()) {
                                                android.util.Log.d("OrderHistory", "No orders, showing empty state");
                                                if (recyclerView != null) {
                                                    recyclerView.setVisibility(View.GONE);
                                                }
                                                if (emptyState != null) {
                                                    emptyState.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                android.util.Log.d("OrderHistory", "Showing " + finalOrders.size() + " orders");
                                                if (recyclerView != null) {
                                                    recyclerView.setVisibility(View.VISIBLE);
                                                }
                                                if (emptyState != null) {
                                                    emptyState.setVisibility(View.GONE);
                                                }
                                            }
                                        } catch (Exception e) {
                                            android.util.Log.e("OrderHistory", "Error updating UI", e);
                                            e.printStackTrace();
                                            if (recyclerView != null) {
                                                recyclerView.setVisibility(View.GONE);
                                            }
                                            if (emptyState != null) {
                                                emptyState.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    } else {
                                        // Empty response body
                                        if (recyclerView != null) {
                                            recyclerView.setVisibility(View.GONE);
                                        }
                                        if (emptyState != null) {
                                            emptyState.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } else {
                                    handleErrorResponse(response);
                                }
                            } catch (Exception e) {
                                android.util.Log.e("OrderHistory", "Unexpected error in onResponse UI update", e);
                                
                                // Hide loading
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                
                                // Filter out technical error messages
                                String errorMsg = "Lỗi xử lý dữ liệu";
                                if (e.getMessage() != null) {
                                    String msg = e.getMessage();
                                    if (msg.contains("IllegalStateException") || 
                                        msg.contains("Expected") ||
                                        msg.contains("BEGIN_OBJECT") ||
                                        msg.contains("BEGIN_ARRAY")) {
                                        errorMsg = "Dữ liệu phản hồi không hợp lệ. Vui lòng thử lại sau";
                                    } else if (msg.length() < 100) {
                                        errorMsg += ": " + msg;
                                    }
                                }
                                
                                if (!isFinishing() && !isDestroyed()) {
                                    Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                }
                                
                                if (recyclerView != null) {
                                    recyclerView.setVisibility(View.GONE);
                                }
                                if (emptyState != null) {
                                    emptyState.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("OrderHistory", "Critical error in runOnUiThread", e);
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("OrderHistory", "Critical error in onResponse callback", e);
                    e.printStackTrace();
                    
                    // Try to update UI on error
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (recyclerView != null) {
                                recyclerView.setVisibility(View.GONE);
                            }
                            if (emptyState != null) {
                                emptyState.setVisibility(View.VISIBLE);
                            }
                            Toast.makeText(CustomerOrderHistoryActivity.this, "Lỗi xử lý phản hồi từ server", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                android.util.Log.e("OrderHistory", "onFailure called. Error: " + (t != null ? t.getMessage() : "null"), t);
                
                // Check if activity is still valid
                if (isFinishing() || isDestroyed() || isDestroyed || call.isCanceled()) {
                    android.util.Log.w("OrderHistory", "Activity destroyed or call canceled, ignoring failure");
                    return;
                }
                
                runOnUiThread(() -> {
                    // Double check after runOnUiThread
                    if (isFinishing() || isDestroyed() || isDestroyed) {
                        return;
                    }
                    
                    // Verify views are still available
                    if (recyclerView == null) {
                        return;
                    }
                    
                    // Hide loading
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    String errorMsg = "Lỗi kết nối";
                    if (t != null) {
                        // Handle different types of exceptions
                        if (t instanceof IllegalStateException) {
                            // IllegalStateException usually means response parsing issue
                            errorMsg += ": Dữ liệu phản hồi không hợp lệ. Vui lòng thử lại sau";
                        } else if (t instanceof java.net.UnknownHostException) {
                            errorMsg += ": Không thể kết nối đến server. Kiểm tra kết nối mạng";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMsg += ": Hết thời gian chờ. Vui lòng thử lại";
                        } else if (t instanceof java.io.IOException) {
                            errorMsg += ": Lỗi kết nối mạng. Vui lòng kiểm tra kết nối";
                        } else {
                            String message = t.getMessage();
                            if (message != null && !message.isEmpty()) {
                                // Filter out technical error messages that users don't need to see
                                if (message.contains("IllegalStateException") || 
                                    message.contains("Expected") ||
                                    message.contains("BEGIN_OBJECT") ||
                                    message.contains("BEGIN_ARRAY")) {
                                    errorMsg += ": Dữ liệu phản hồi không hợp lệ";
                                } else {
                                    // Truncate long error messages
                                    if (message.length() > 100) {
                                        errorMsg += ": " + message.substring(0, 100) + "...";
                                    } else {
                                        errorMsg += ": " + message;
                                    }
                                }
                            } else {
                                errorMsg += ": Không thể kết nối đến server";
                            }
                        }
                    } else {
                        errorMsg += ": Không thể kết nối đến server";
                    }
                    
                    Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    
                    // Show empty state on network error
                    recyclerView.setVisibility(View.GONE);
                    if (emptyState != null) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            int code = response.code();
            
            if (code == 401) {
                // Token expired or user not found - logout and redirect to login
                prefsHelper.clear();
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
                navigateToLogin();
                return;
            }
            
            String errorMsg = "Không thể tải đơn hàng";
            String message = response.message();
            
            // Try to read error body safely
            try {
                okhttp3.ResponseBody errorBody = response.errorBody();
                if (errorBody != null) {
                    try {
                        // Create a source to read the error body without consuming it
                        String errorBodyString = errorBody.string();
                        if (errorBodyString != null && !errorBodyString.isEmpty()) {
                            if (errorBodyString.contains("User not found or inactive")) {
                                errorMsg = "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa. Vui lòng đăng nhập lại";
                                prefsHelper.clear();
                                navigateToLogin();
                                return;
                            } else {
                                // Truncate long error messages and filter technical errors
                                String cleanError = errorBodyString;
                                if (cleanError.contains("IllegalStateException") || 
                                    cleanError.contains("Expected") ||
                                    cleanError.contains("BEGIN_OBJECT") ||
                                    cleanError.contains("BEGIN_ARRAY")) {
                                    errorMsg = getErrorMessageFromCode(code, message);
                                } else {
                                    if (cleanError.length() > 150) {
                                        errorMsg = "Lỗi: " + cleanError.substring(0, 150) + "...";
                                    } else {
                                        errorMsg = "Lỗi: " + cleanError;
                                    }
                                }
                            }
                        } else {
                            errorMsg = getErrorMessageFromCode(code, message);
                        }
                    } catch (IllegalStateException e) {
                        // Error body already consumed or parsing error, use status code instead
                        errorMsg = getErrorMessageFromCode(code, message);
                    } catch (Exception e) {
                        // Any other exception reading error body
                        errorMsg = getErrorMessageFromCode(code, message);
                    }
                } else {
                    errorMsg = getErrorMessageFromCode(code, message);
                }
            } catch (Exception e) {
                // If we can't read error body, use status code
                errorMsg = getErrorMessageFromCode(code, message);
            }
            
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            
            // Show empty state on error
            recyclerView.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Đã xảy ra lỗi không xác định", Toast.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getErrorMessageFromCode(int code, String message) {
        switch (code) {
            case 403:
                return "Bạn không có quyền xem đơn hàng";
            case 404:
                return "Không tìm thấy đơn hàng";
            case 500:
                return "Lỗi server. Vui lòng thử lại sau";
            default:
                return "Lỗi " + code + ": " + (message != null ? message : "Đã xảy ra lỗi");
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
    
    private void showCancelOrderDialog(Order order) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        String status = order.getStatus() != null ? order.getStatus() : "pending";
        String message;
        
        // Customize message based on order status
        switch (status) {
            case "pending":
                message = "Bạn có chắc chắn muốn hủy đơn hàng này?\n\n" +
                         "Sau khi hủy, bạn có thể:\n" +
                         "• Thay đổi thông tin đơn hàng\n" +
                         "• Đặt đơn hàng mới với sản phẩm khác\n" +
                         "• Điều chỉnh số lượng hoặc địa chỉ giao hàng\n\n" +
                         "Hành động này không thể hoàn tác.";
                break;
            case "confirmed":
                message = "Bạn có chắc chắn muốn hủy đơn hàng đã được xác nhận?\n\n" +
                         "Sau khi hủy, bạn có thể:\n" +
                         "• Đặt đơn hàng mới\n" +
                         "• Thay đổi thông tin hoặc sản phẩm\n\n" +
                         "Lưu ý: Đơn hàng đã được xác nhận, việc hủy có thể ảnh hưởng đến quá trình xử lý.";
                break;
            case "shipping":
                message = "Bạn có chắc chắn muốn hủy đơn hàng đang được giao?\n\n" +
                         "Việc hủy đơn hàng đang giao có thể:\n" +
                         "• Yêu cầu liên hệ với người bán\n" +
                         "• Ảnh hưởng đến quá trình vận chuyển\n\n" +
                         "Bạn vẫn muốn tiếp tục?";
                break;
            default:
                message = "Bạn có chắc chắn muốn hủy đơn hàng này? Hành động này không thể hoàn tác.";
                break;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage(message)
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    cancelOrder(order);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void cancelOrder(Order order) {
        if (isFinishing() || isDestroyed() || order == null || order.getId() == null) {
            return;
        }
        
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Try customer-specific cancel endpoint first (if available)
        // If this endpoint doesn't exist, it will fallback to status update
        cancelOrderDirectCall = apiService.cancelOrder(token, order.getId());
        cancelOrderDirectCall.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerOrderHistoryActivity.this, 
                            "Đã hủy đơn hàng thành công. Bạn có thể đặt đơn hàng mới hoặc thay đổi thông tin.", 
                            Toast.LENGTH_LONG).show();
                        // Reload orders list
                        loadOrders();
                    } else {
                        // If cancel endpoint doesn't work (404 or 403), try status update endpoint
                        if (response.code() == 404 || response.code() == 403) {
                            // Endpoint not found or no permission, try status update method
                            cancelOrderViaStatusUpdate(order, token);
                        } else {
                            handleCancelError(response, order);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                // If cancel endpoint fails (network error or endpoint doesn't exist), try status update method
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    // Try alternative method
                    String token = prefsHelper.getToken();
                    if (token != null && !token.isEmpty()) {
                        cancelOrderViaStatusUpdate(order, token);
                    } else {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(CustomerOrderHistoryActivity.this, 
                            "Lỗi kết nối. Vui lòng thử lại.", 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void cancelOrderViaStatusUpdate(Order order, String token) {
        if (token == null || token.isEmpty() || order == null || order.getId() == null) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            return;
        }
        
        // Prepare status update request
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "cancelled");
        
        cancelOrderCall = apiService.updateOrderStatus(token, order.getId(), statusMap);
        cancelOrderCall.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerOrderHistoryActivity.this, 
                            "Đã hủy đơn hàng thành công. Bạn có thể đặt đơn hàng mới hoặc thay đổi thông tin.", 
                            Toast.LENGTH_LONG).show();
                        // Reload orders list
                        loadOrders();
                    } else {
                        handleCancelError(response, order);
                    }
                });
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    String errorMsg = "Lỗi kết nối";
                    if (t != null && t.getMessage() != null) {
                        errorMsg += ": " + t.getMessage();
                    }
                    Toast.makeText(CustomerOrderHistoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void handleCancelError(Response<?> response, Order order) {
        String errorMsg = "Không thể hủy đơn hàng";
        if (response.code() == 401) {
            errorMsg = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại";
            prefsHelper.clear();
            navigateToLogin();
        } else if (response.code() == 403) {
            // Try DELETE endpoint as last resort if order ID is available
            if (order != null && order.getId() != null) {
                tryDeleteOrder(order.getId());
                return;
            }
            errorMsg = "Bạn không có quyền hủy đơn hàng này. Vui lòng liên hệ hỗ trợ để được hỗ trợ.";
        } else if (response.code() == 400) {
            // Try to get error message from response body
            try {
                okhttp3.ResponseBody errorBody = response.errorBody();
                if (errorBody != null) {
                    String errorBodyString = errorBody.string();
                    if (errorBodyString != null) {
                        if (errorBodyString.contains("cannot") || errorBodyString.contains("not allowed")) {
                            errorMsg = "Không thể hủy đơn hàng ở trạng thái hiện tại. Vui lòng liên hệ hỗ trợ.";
                        } else if (errorBodyString.contains("permission") || errorBodyString.contains("quyền") || 
                                   errorBodyString.contains("không có quyền")) {
                            // Try DELETE as fallback
                            if (order != null && order.getId() != null) {
                                tryDeleteOrder(order.getId());
                                return;
                            }
                            errorMsg = "Bạn không có quyền hủy đơn hàng này. Vui lòng liên hệ hỗ trợ.";
                        } else {
                            errorMsg = "Không thể hủy đơn hàng: " + errorBodyString;
                        }
                    } else {
                        errorMsg = "Không thể hủy đơn hàng ở trạng thái hiện tại";
                    }
                } else {
                    errorMsg = "Không thể hủy đơn hàng ở trạng thái hiện tại";
                }
            } catch (Exception e) {
                errorMsg = "Không thể hủy đơn hàng ở trạng thái hiện tại";
            }
        } else {
            errorMsg += " (Mã lỗi: " + response.code() + ")";
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }
    
    private void tryDeleteOrder(String orderId) {
        // This is a fallback method - may not be available on backend
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không thể hủy đơn hàng. Vui lòng liên hệ hỗ trợ.", Toast.LENGTH_LONG).show();
            return;
        }
        
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        
        deleteOrderCall = apiService.deleteOrder(token, orderId);
        deleteOrderCall.enqueue(new retrofit2.Callback<Map<String, String>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (response.isSuccessful()) {
                        Toast.makeText(CustomerOrderHistoryActivity.this, 
                            "Đã hủy đơn hàng thành công. Bạn có thể đặt đơn hàng mới hoặc thay đổi thông tin.", 
                            Toast.LENGTH_LONG).show();
                        loadOrders();
                    } else {
                        Toast.makeText(CustomerOrderHistoryActivity.this, 
                            "Không thể hủy đơn hàng. Vui lòng liên hệ hỗ trợ để được hỗ trợ.", 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    Toast.makeText(CustomerOrderHistoryActivity.this, 
                        "Không thể hủy đơn hàng. Vui lòng liên hệ hỗ trợ để được hỗ trợ.", 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        
        // Cancel ongoing network calls
        if (ordersCall != null && !ordersCall.isCanceled()) {
            ordersCall.cancel();
            ordersCall = null;
        }
        if (cancelOrderCall != null && !cancelOrderCall.isCanceled()) {
            cancelOrderCall.cancel();
            cancelOrderCall = null;
        }
        if (cancelOrderDirectCall != null && !cancelOrderDirectCall.isCanceled()) {
            cancelOrderDirectCall.cancel();
            cancelOrderDirectCall = null;
        }
        if (deleteOrderCall != null && !deleteOrderCall.isCanceled()) {
            deleteOrderCall.cancel();
            deleteOrderCall = null;
        }
        
        // Clear references
        adapter = null;
        recyclerView = null;
        emptyState = null;
        progressBar = null;
        
        super.onDestroy();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Cancel call if activity is paused and user might not come back
        // This prevents UI updates when activity is in background
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Don't cancel here - user might come back
    }
}

