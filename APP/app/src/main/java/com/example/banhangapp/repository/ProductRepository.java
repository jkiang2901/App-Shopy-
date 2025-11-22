package com.example.banhangapp.repository;

import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    
    private ApiService apiService;

    public ProductRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public interface ProductCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public void getAllProducts(String token, final ProductCallback<List<Product>> callback) {
        java.util.HashMap<String, String> params = new java.util.HashMap<>();
        apiService.getProducts(params).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getProductById(String id, final ProductCallback<Product> callback) {
        apiService.getProductById(id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void createProduct(String token, Product product, final ProductCallback<Product> callback) {
        apiService.createProduct(token, product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void updateProduct(String token, String id, Product product, final ProductCallback<Product> callback) {
        apiService.updateProduct(token, id, product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void deleteProduct(String token, String id, final ProductCallback<Void> callback) {
        apiService.deleteProduct(token, id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
