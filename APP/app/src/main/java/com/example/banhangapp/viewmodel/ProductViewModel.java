package com.example.banhangapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.repository.ProductRepository;
import java.util.List;

public class ProductViewModel extends ViewModel {

    private ProductRepository repository;
    private MutableLiveData<List<Product>> products;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> error;

    public ProductViewModel() {
        repository = new ProductRepository();
        products = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        error = new MutableLiveData<>();
        
        loadProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadProducts() {
        isLoading.setValue(true);
        error.setValue(null);

        repository.getAllProducts(null, new ProductRepository.ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                products.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage != null ? errorMessage : "Unknown error");
                isLoading.setValue(false);
            }
        });
    }

    public void refreshProducts() {
        loadProducts();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ProductViewModel.class)) {
                return (T) new ProductViewModel();
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
