package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvBrand, tvColor, tvSize, tvQuantity;
    private ImageView ivProduct;
    private Button btnAddToCart;
    private String productId;
    private ApiService apiService;
    private SharedPreferencesHelper prefsHelper;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvBrand = findViewById(R.id.tvBrand);
        tvColor = findViewById(R.id.tvColor);
        tvSize = findViewById(R.id.tvSize);
        tvQuantity = findViewById(R.id.tvQuantity);
        ivProduct = findViewById(R.id.ivProduct);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnAddToCart.setOnClickListener(v -> addToCart());

        loadProduct();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProduct() {
        Call<Product> call = apiService.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    displayProduct(product);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(CustomerProductDetailActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(Product product) {
        // Save product for addToCart
        currentProduct = product;
        
        if (tvName != null) {
            tvName.setText(product.getName() != null ? product.getName() : "N/A");
        }
        
        if (tvPrice != null) {
        tvPrice.setText(String.format("%,.0f VNĐ", product.getPrice()));
        }
        
        if (tvDescription != null) {
            tvDescription.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả");
        }
        
        if (tvCategory != null) {
        tvCategory.setText("Danh mục: " + (product.getCategory() != null ? product.getCategory() : "N/A"));
        }
        
        if (tvBrand != null) {
        tvBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "N/A"));
        }
        
        if (tvColor != null) {
        tvColor.setText("Màu sắc: " + (product.getColor() != null ? product.getColor() : "N/A"));
        }
        
        if (tvSize != null) {
        tvSize.setText("Kích cỡ: " + (product.getSize() != null ? product.getSize() : "N/A"));
        }
        
        if (tvQuantity != null) {
        tvQuantity.setText("Số lượng: " + product.getQuantity());
        }
        
        // Load product image
        if (ivProduct != null) {
            if (product.getImages() != null && product.getImages().length > 0 && 
                product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                // Load image from URL using Glide
                Glide.with(this)
                    .load(product.getImages()[0])
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(ivProduct);
            } else {
                // Use placeholder if no image URL
                ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        
        if (btnAddToCart != null) {
        btnAddToCart.setEnabled(product.isInStock() && product.getQuantity() > 0);
        }
    }

    private void addToCart() {
        if (currentProduct == null) {
            Toast.makeText(this, "Sản phẩm chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String token = prefsHelper.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentProduct.getId() == null || currentProduct.getId().isEmpty()) {
            Toast.makeText(this, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button while adding to cart
        if (btnAddToCart != null) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Đang thêm...");
        }
        
        ApiService.CartItemRequest request = new ApiService.CartItemRequest(currentProduct.getId(), 1);
        
        Call<com.example.banhangapp.models.Cart> call = apiService.addToCart(token, request);
        
        call.enqueue(new Callback<com.example.banhangapp.models.Cart>() {
            @Override
            public void onResponse(Call<com.example.banhangapp.models.Cart> call, Response<com.example.banhangapp.models.Cart> response) {
                // Run on main thread
                runOnUiThread(() -> {
                    // Re-enable button
                    if (btnAddToCart != null) {
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setText("🛒 Thêm vào giỏ hàng");
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(CustomerProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        handleErrorResponse(response);
                    }
                });
            }

            @Override
            public void onFailure(Call<com.example.banhangapp.models.Cart> call, Throwable t) {
                // Run on main thread
                runOnUiThread(() -> {
                    // Re-enable button
                    if (btnAddToCart != null) {
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setText("🛒 Thêm vào giỏ hàng");
                    }
                    
                    String errorMsg = "Lỗi kết nối: ";
                    if (t.getMessage() != null) {
                        errorMsg += t.getMessage();
                    } else {
                        errorMsg += "Không thể kết nối đến server";
                    }
                    Toast.makeText(CustomerProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

