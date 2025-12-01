package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.ProductAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProductListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerViewProducts);
        
        if (recyclerView == null) {
            Toast.makeText(this, "Lỗi: RecyclerView không tìm thấy", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        
        // Initialize adapter
        adapter = new ProductAdapter(
            product -> {
                Intent intent = new Intent(this, CustomerProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            },
            product -> {
                addToCart(product);
            }
        );
        
        recyclerView.setAdapter(adapter);

        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_menu, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    adapter.updateProducts(allProducts);
                } else {
                    filterProducts(newText);
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            startActivity(new Intent(this, CustomerCartActivity.class));
            return true;
        } else if (id == R.id.action_orders) {
            startActivity(new Intent(this, CustomerOrderHistoryActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, CustomerProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            prefsHelper.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProducts() {
        Map<String, String> params = new HashMap<>();
        Call<List<Product>> call = apiService.getProducts(params);
        
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                // Run on main thread
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            allProducts = response.body();
                            
                            if (allProducts != null && !allProducts.isEmpty()) {
                                if (adapter == null) {
                                    adapter = new ProductAdapter(
                                        product -> {
                                            Intent intent = new Intent(CustomerProductListActivity.this, CustomerProductDetailActivity.class);
                                            intent.putExtra("productId", product.getId());
                                            startActivity(intent);
                                        },
                                        product -> {
                                            addToCart(product);
                                        }
                                    );
                                    recyclerView.setAdapter(adapter);
                                }
                                
                                adapter.updateProducts(allProducts);
                            } else {
                                Toast.makeText(CustomerProductListActivity.this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(CustomerProductListActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        String errorMsg = "Lỗi tải sản phẩm: Code " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            errorMsg += " - " + response.message();
                        }
                        android.util.Log.e("ProductList", errorMsg);
                        Toast.makeText(CustomerProductListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: ";
                if (t.getMessage() != null) {
                    errorMsg += t.getMessage();
                } else {
                    errorMsg += "Không thể kết nối đến server";
                }
                android.util.Log.e("ProductList", errorMsg, t);
                Toast.makeText(CustomerProductListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filtered = new ArrayList<>();
        String searchQuery = query.toLowerCase().trim();
        for (Product product : allProducts) {
            if (product.getName() != null && product.getName().toLowerCase().contains(searchQuery)) {
                filtered.add(product);
            }
        }
        adapter.updateProducts(filtered);
    }

    private void addToCart(Product product) {
        String token = prefsHelper.getToken();
        
        if (token == null || token.isEmpty()) {
            android.util.Log.e("ProductList", "Token is null or empty");
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (product == null || product.getId() == null) {
            android.util.Log.e("ProductList", "Product or product ID is null");
            Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("ProductList", "Adding product to cart: " + product.getName() + " (ID: " + product.getId() + ")");
        
        ApiService.CartItemRequest request = new ApiService.CartItemRequest(product.getId(), 1);
        Call<com.example.banhangapp.models.Cart> call = apiService.addToCart(token, request);
        
        call.enqueue(new Callback<com.example.banhangapp.models.Cart>() {
            @Override
            public void onResponse(Call<com.example.banhangapp.models.Cart> call, Response<com.example.banhangapp.models.Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.banhangapp.models.Cart cart = response.body();
                    android.util.Log.d("ProductList", "Product added to cart successfully. Cart has " + 
                        (cart.getItems() != null ? cart.getItems().size() : 0) + " items");
                    Toast.makeText(CustomerProductListActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<com.example.banhangapp.models.Cart> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: ";
                if (t.getMessage() != null) {
                    errorMsg += t.getMessage();
                } else {
                    errorMsg += "Không thể kết nối đến server";
                }
                android.util.Log.e("ProductList", "Add to cart network error", t);
                Toast.makeText(CustomerProductListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                t.printStackTrace();
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
                
                // Navigate to login screen
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
            
            String errorMsg = "Không thể thêm vào giỏ hàng";
            String message = response.message();
            
            okhttp3.ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                try {
                    String errorBodyString = errorBody.string();
                    // Parse JSON error message if possible
                    if (errorBodyString.contains("\"error\"") || errorBodyString.contains("\"message\"")) {
                        // Try to extract error message from JSON
                        if (errorBodyString.contains("User not found or inactive")) {
                            errorMsg = "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa. Vui lòng đăng nhập lại";
                            prefsHelper.clear();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        } else {
                            errorMsg = "Lỗi: " + errorBodyString;
                        }
                    } else {
                        errorMsg = "Lỗi " + code + ": " + errorBodyString;
                    }
                } catch (Exception e) {
                    errorMsg = "Lỗi " + code + ": " + (message != null ? message : "Không thể đọc thông báo lỗi");
                }
            } else {
                switch (code) {
                    case 403:
                        errorMsg = "Bạn không có quyền thực hiện thao tác này";
                        break;
                    case 404:
                        errorMsg = "Sản phẩm không tồn tại";
                        break;
                    case 400:
                        errorMsg = "Dữ liệu không hợp lệ";
                        break;
                    case 500:
                        errorMsg = "Lỗi server. Vui lòng thử lại sau";
                        break;
                    default:
                        errorMsg = "Lỗi " + code + ": " + (message != null ? message : "Đã xảy ra lỗi");
                }
            }
            
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Đã xảy ra lỗi không xác định", Toast.LENGTH_SHORT).show();
        }
    }
}

