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
        
        // Check if RecyclerView is found
        if (recyclerView == null) {
            android.util.Log.e("ProductList", "RecyclerView not found!");
            Toast.makeText(this, "Lỗi: RecyclerView không tìm thấy", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Log RecyclerView dimensions
        recyclerView.post(() -> {
            android.util.Log.d("ProductList", "RecyclerView dimensions: " + 
                recyclerView.getWidth() + "x" + recyclerView.getHeight());
            android.util.Log.d("ProductList", "RecyclerView visibility: " + recyclerView.getVisibility());
        });
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        
        // Force RecyclerView to measure
        recyclerView.post(() -> {
            int width = recyclerView.getWidth();
            int height = recyclerView.getHeight();
            android.util.Log.d("ProductList", "RecyclerView dimensions after layout: " + width + "x" + height);
            if (width == 0 || height == 0) {
                android.util.Log.e("ProductList", "RecyclerView has zero dimensions! This is the problem.");
            }
        });
        
        android.util.Log.d("ProductList", "LayoutManager set, hasFixedSize=false");
        
        // Initialize adapter first
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
        
        // Set adapter to RecyclerView
        recyclerView.setAdapter(adapter);
        android.util.Log.d("ProductList", "Adapter set to RecyclerView. Adapter item count: " + adapter.getItemCount());
        
        // Initialize with empty list
        adapter.updateProducts(new ArrayList<>());
        
        android.util.Log.d("ProductList", "RecyclerView and adapter initialized. RecyclerView visibility: " + recyclerView.getVisibility());

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
                            android.util.Log.d("ProductList", "Received " + (allProducts != null ? allProducts.size() : 0) + " products");
                            
                            if (allProducts != null && !allProducts.isEmpty()) {
                                // Log first product to check parsing
                                if (allProducts.size() > 0) {
                                    Product first = allProducts.get(0);
                                    android.util.Log.d("ProductList", "First product: " + first.getName() + ", ID: " + first.getId() + ", Price: " + first.getPrice());
                                }
                                
                                // Make sure adapter is set
                                if (adapter == null) {
                                    android.util.Log.e("ProductList", "Adapter is null! Recreating...");
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
                                android.util.Log.d("ProductList", "Adapter updated with " + allProducts.size() + " products. Item count: " + adapter.getItemCount());
                                
                                // Force RecyclerView to refresh
                                recyclerView.post(() -> {
                                    android.util.Log.d("ProductList", "Before refresh - RecyclerView dimensions: " + 
                                        recyclerView.getWidth() + "x" + recyclerView.getHeight());
                                    recyclerView.invalidate();
                                    recyclerView.requestLayout();
                                    
                                    // Check if adapter is attached
                                    if (recyclerView.getAdapter() == null) {
                                        android.util.Log.e("ProductList", "Adapter is null! Re-attaching...");
                                        recyclerView.setAdapter(adapter);
                                    }
                                    
                                    // Force measure and layout
                                    recyclerView.measure(
                                        View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.EXACTLY)
                                    );
                                    recyclerView.layout(recyclerView.getLeft(), recyclerView.getTop(), 
                                        recyclerView.getRight(), recyclerView.getBottom());
                                    
                                    android.util.Log.d("ProductList", "After refresh - RecyclerView dimensions: " + 
                                        recyclerView.getWidth() + "x" + recyclerView.getHeight());
                                    android.util.Log.d("ProductList", "Adapter item count: " + adapter.getItemCount());
                                });
                                
                                Toast.makeText(CustomerProductListActivity.this, "Đã tải " + allProducts.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                            } else {
                                android.util.Log.w("ProductList", "Product list is empty or null");
                                Toast.makeText(CustomerProductListActivity.this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ProductList", "Error processing products", e);
                            Toast.makeText(CustomerProductListActivity.this, "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    String errorMsg = "Lỗi thêm vào giỏ hàng: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            android.util.Log.e("ProductList", "Add to cart error: " + errorBody);
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                        android.util.Log.e("ProductList", "Error reading error body", e);
                    }
                    android.util.Log.e("ProductList", errorMsg);
                    Toast.makeText(CustomerProductListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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
}

