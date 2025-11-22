package com.example.banhangapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.adapter.InventoryAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Inventory;
import com.example.banhangapp.models.Product;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerManageInventoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private InventoryAdapter adapter;
    private FloatingActionButton fabAdd;
    private List<Product> sellerProducts;
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_manage_inventory);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        adapter = new InventoryAdapter();
        recyclerView.setAdapter(adapter);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            loadSellerProducts(() -> showAddInventoryDialog());
        });

        loadInventory();
    }

    private void loadInventory() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Inventory>> call = apiService.getInventory(token, null);
        
        call.enqueue(new Callback<List<Inventory>>() {
            @Override
            public void onResponse(Call<List<Inventory>> call, Response<List<Inventory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Inventory> inventories = response.body();
                    android.util.Log.d("SellerInventory", "Loaded " + inventories.size() + " inventory records");
                    adapter.updateInventories(inventories);
                    Toast.makeText(SellerManageInventoryActivity.this, "Đã tải " + inventories.size() + " bản ghi kho", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi tải dữ liệu: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("SellerInventory", errorMsg);
                    Toast.makeText(SellerManageInventoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Inventory>> call, Throwable t) {
                android.util.Log.e("SellerInventory", "Network error", t);
                Toast.makeText(SellerManageInventoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void loadSellerProducts(Runnable onComplete) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentSellerId = prefsHelper.getUserId();
        Map<String, String> params = new HashMap<>();
        // Get all products
        Call<List<Product>> call = apiService.getProducts(params);
        
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body();
                    // Filter products: show products that belong to current seller OR have no sellerId (unassigned)
                    sellerProducts = new java.util.ArrayList<>();
                    for (Product product : allProducts) {
                        String productSellerId = product.getSellerId();
                        if (productSellerId == null || productSellerId.isEmpty() || productSellerId.equals(currentSellerId)) {
                            sellerProducts.add(product);
                        }
                    }
                    android.util.Log.d("SellerInventory", "Loaded " + sellerProducts.size() + " products (filtered from " + allProducts.size() + ")");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    Toast.makeText(SellerManageInventoryActivity.this, "Lỗi tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                android.util.Log.e("SellerInventory", "Network error loading products", t);
                Toast.makeText(SellerManageInventoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAddInventoryDialog() {
        if (sellerProducts == null || sellerProducts.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_inventory, null);
        
        TextView tvProductName = dialogView.findViewById(R.id.tvProductName);
        RadioGroup rgType = dialogView.findViewById(R.id.rgType);
        RadioButton rbImport = dialogView.findViewById(R.id.rbImport);
        RadioButton rbExport = dialogView.findViewById(R.id.rbExport);
        RadioButton rbAdjustment = dialogView.findViewById(R.id.rbAdjustment);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        selectedProduct = null;
        tvProductName.setOnClickListener(v -> showProductSelectionDialog(tvProductName));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Thêm bản ghi kho")
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (selectedProduct == null) {
                    Toast.makeText(this, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }

                String quantityStr = etQuantity.getText().toString().trim();
                if (quantityStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String type = "import";
                    if (rbExport.isChecked()) {
                        type = "export";
                    } else if (rbAdjustment.isChecked()) {
                        type = "adjustment";
                    }

                    String note = etNote.getText().toString().trim();

                    createInventoryRecord(selectedProduct.getId(), quantity, type, note);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showProductSelectionDialog(TextView tvProductName) {
        if (sellerProducts == null || sellerProducts.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] productNames = new String[sellerProducts.size()];
        for (int i = 0; i < sellerProducts.size(); i++) {
            productNames[i] = sellerProducts.get(i).getName() + " (Tồn: " + sellerProducts.get(i).getQuantity() + ")";
        }

        new AlertDialog.Builder(this)
            .setTitle("Chọn sản phẩm")
            .setItems(productNames, (dialog, which) -> {
                selectedProduct = sellerProducts.get(which);
                tvProductName.setText(selectedProduct.getName() + " (Tồn: " + selectedProduct.getQuantity() + ")");
            })
            .show();
    }

    private void createInventoryRecord(String productId, int quantity, String type, String note) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.InventoryRequest request = new ApiService.InventoryRequest(productId, quantity, type, note);
        Call<Inventory> call = apiService.createInventoryRecord(token, request);
        
        call.enqueue(new Callback<Inventory>() {
            @Override
            public void onResponse(Call<Inventory> call, Response<Inventory> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SellerManageInventoryActivity.this, "Đã thêm bản ghi kho thành công", Toast.LENGTH_SHORT).show();
                    loadInventory();
                } else {
                    String errorMsg = "Lỗi thêm bản ghi kho";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ": " + response.message();
                    }
                    Toast.makeText(SellerManageInventoryActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Inventory> call, Throwable t) {
                android.util.Log.e("SellerInventory", "Network error creating inventory", t);
                Toast.makeText(SellerManageInventoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

