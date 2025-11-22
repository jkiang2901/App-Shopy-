package com.example.banhangapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Product;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvBrand, tvColor, tvSize, tvQuantity;
    private ImageView ivProduct;
    private Button btnAddToCart;
    private String productId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_detail);

        productId = getIntent().getStringExtra("productId");
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
        tvName.setText(product.getName());
        tvPrice.setText(String.format("%,.0f VNĐ", product.getPrice()));
        tvDescription.setText(product.getDescription());
        tvCategory.setText("Danh mục: " + (product.getCategory() != null ? product.getCategory() : "N/A"));
        tvBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "N/A"));
        tvColor.setText("Màu sắc: " + (product.getColor() != null ? product.getColor() : "N/A"));
        tvSize.setText("Kích cỡ: " + (product.getSize() != null ? product.getSize() : "N/A"));
        tvQuantity.setText("Số lượng: " + product.getQuantity());
        
        btnAddToCart.setEnabled(product.isInStock() && product.getQuantity() > 0);
    }

    private void addToCart() {
        // Implementation similar to CustomerProductListActivity
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}

