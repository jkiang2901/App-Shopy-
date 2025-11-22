package com.example.banhangapp;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.ProductAdapter;
import com.example.banhangapp.databinding.ActivityMainBinding;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.viewmodel.ProductViewModel;
import com.example.banhangapp.viewmodel.ProductViewModel.Factory;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(
                new ProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        showProductDetails(product);
                    }
                },
                new ProductAdapter.OnAddToCartClickListener() {
                    @Override
                    public void onAddToCartClick(Product product) {
                        addToCart(product);
                    }
                }
        );

        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSearchView() {
        // Simple client-side search since API doesn't have search endpoint
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    productAdapter.updateProducts(viewModel.getProducts().getValue());
                } else {
                    filterProducts(newText);
                }
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> allProducts = viewModel.getProducts().getValue();
        if (allProducts == null) return;

        String searchQuery = query.toLowerCase().trim();
        java.util.ArrayList<Product> filtered = new java.util.ArrayList<>();
        
        for (Product product : allProducts) {
            if (product.getName() != null && product.getName().toLowerCase().contains(searchQuery)) {
                filtered.add(product);
            }
        }
        
        productAdapter.updateProducts(filtered);
    }

    private void observeViewModel() {
        viewModel = new ViewModelProvider(this, new ProductViewModel.Factory()).get(ProductViewModel.class);

        viewModel.getProducts().observe(this, products -> {
            if (products != null) {
                productAdapter.updateProducts(products);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading != null && isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProductDetails(Product product) {
        // TODO: Navigate to product details screen
        Toast.makeText(this, "Chi tiết: " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    private void addToCart(Product product) {
        // TODO: Implement cart functionality
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}
