package com.example.banhangapp;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.banhangapp.adapter.PromotionAdapter;
import com.example.banhangapp.api.ApiService;
import com.example.banhangapp.api.RetrofitClient;
import com.example.banhangapp.models.Promotion;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerManagePromotionsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SharedPreferencesHelper prefsHelper;
    private ApiService apiService;
    private PromotionAdapter adapter;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_manage_promotions);

        prefsHelper = new SharedPreferencesHelper(this);
        apiService = RetrofitClient.getApiService();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddEditPromotionDialog(null));

        String currentSellerId = prefsHelper.getUserId();
        adapter = new PromotionAdapter(new PromotionAdapter.OnPromotionClickListener() {
            @Override
            public void onPromotionClick(Promotion promotion) {
                // Show promotion details
            }

            @Override
            public void onPromotionEdit(Promotion promotion) {
                // Check if promotion belongs to current seller
                String promotionSellerId = promotion.getSellerId();
                String currentSellerId = prefsHelper.getUserId();
                if (promotionSellerId == null || !promotionSellerId.equals(currentSellerId)) {
                    Toast.makeText(SellerManagePromotionsActivity.this, 
                        "Bạn không có quyền chỉnh sửa khuyến mãi này", Toast.LENGTH_SHORT).show();
                    return;
                }
                showAddEditPromotionDialog(promotion);
            }

            @Override
            public void onPromotionDelete(Promotion promotion) {
                // Check if promotion belongs to current seller
                String promotionSellerId = promotion.getSellerId();
                String currentSellerId = prefsHelper.getUserId();
                if (promotionSellerId == null || !promotionSellerId.equals(currentSellerId)) {
                    Toast.makeText(SellerManagePromotionsActivity.this, 
                        "Bạn không có quyền xóa khuyến mãi này", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(SellerManagePromotionsActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa khuyến mãi " + promotion.getCode() + "?")
                    .setPositiveButton("Xóa", (dialog, which) -> deletePromotion(promotion))
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        }, currentSellerId);
        recyclerView.setAdapter(adapter);

        loadPromotions();
    }

    private void loadPromotions() {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Promotion>> call = apiService.getPromotions(token);
        
        call.enqueue(new Callback<List<Promotion>>() {
            @Override
            public void onResponse(Call<List<Promotion>> call, Response<List<Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Promotion> promotions = response.body();
                    android.util.Log.d("SellerPromotions", "Loaded " + promotions.size() + " promotions");
                    adapter.updatePromotions(promotions);
                    Toast.makeText(SellerManagePromotionsActivity.this, "Đã tải " + promotions.size() + " khuyến mãi", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi tải dữ liệu: Code " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " - " + response.message();
                    }
                    android.util.Log.e("SellerPromotions", errorMsg);
                    Toast.makeText(SellerManagePromotionsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Promotion>> call, Throwable t) {
                android.util.Log.e("SellerPromotions", "Network error", t);
                Toast.makeText(SellerManagePromotionsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void showAddEditPromotionDialog(Promotion promotion) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_promotion, null);
        
        TextInputEditText etCode = dialogView.findViewById(R.id.etCode);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        RadioGroup rgDiscountType = dialogView.findViewById(R.id.rgDiscountType);
        RadioButton rbPercentage = dialogView.findViewById(R.id.rbPercentage);
        RadioButton rbFixed = dialogView.findViewById(R.id.rbFixed);
        TextInputEditText etDiscountValue = dialogView.findViewById(R.id.etDiscountValue);
        TextInputEditText etMinPurchaseAmount = dialogView.findViewById(R.id.etMinPurchaseAmount);
        TextInputEditText etMaxDiscountAmount = dialogView.findViewById(R.id.etMaxDiscountAmount);
        TextInputEditText etUsageLimit = dialogView.findViewById(R.id.etUsageLimit);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);
        RadioButton rbActive = dialogView.findViewById(R.id.rbActive);
        RadioButton rbInactive = dialogView.findViewById(R.id.rbInactive);

        boolean isEdit = promotion != null;
        String dialogTitle = isEdit ? "Sửa khuyến mãi" : "Thêm khuyến mãi";

        if (isEdit) {
            etCode.setText(promotion.getCode());
            etCode.setEnabled(false); // Cannot change code
            etName.setText(promotion.getName());
            etDescription.setText(promotion.getDescription());
            if ("percentage".equals(promotion.getDiscountType())) {
                rbPercentage.setChecked(true);
            } else {
                rbFixed.setChecked(true);
            }
            etDiscountValue.setText(String.valueOf(promotion.getDiscountValue()));
            etMinPurchaseAmount.setText(String.valueOf(promotion.getMinPurchaseAmount()));
            if (promotion.getMaxDiscountAmount() != null) {
                etMaxDiscountAmount.setText(String.valueOf(promotion.getMaxDiscountAmount()));
            }
            if (promotion.getUsageLimit() != null) {
                etUsageLimit.setText(String.valueOf(promotion.getUsageLimit()));
            }
            if (promotion.isActive()) {
                rbActive.setChecked(true);
            } else {
                rbInactive.setChecked(true);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String code = etCode.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String discountType = rbPercentage.isChecked() ? "percentage" : "fixed";
                String discountValueStr = etDiscountValue.getText().toString().trim();
                String minPurchaseStr = etMinPurchaseAmount.getText().toString().trim();
                String maxDiscountStr = etMaxDiscountAmount.getText().toString().trim();
                String usageLimitStr = etUsageLimit.getText().toString().trim();
                boolean isActive = rbActive.isChecked();

                if (code.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mã khuyến mãi", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên khuyến mãi", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (discountValueStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập giá trị giảm", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double discountValue = Double.parseDouble(discountValueStr);
                    double minPurchaseAmount = minPurchaseStr.isEmpty() ? 0 : Double.parseDouble(minPurchaseStr);
                    Double maxDiscountAmount = maxDiscountStr.isEmpty() ? null : Double.parseDouble(maxDiscountStr);
                    Integer usageLimit = usageLimitStr.isEmpty() ? null : Integer.parseInt(usageLimitStr);

                    if (isEdit) {
                        updatePromotion(promotion.getId(), code, name, description, discountType, discountValue, 
                            minPurchaseAmount, maxDiscountAmount, usageLimit, isActive);
                    } else {
                        createPromotion(code, name, description, discountType, discountValue, 
                            minPurchaseAmount, maxDiscountAmount, usageLimit, isActive);
                    }
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void createPromotion(String code, String name, String description, String discountType, 
            double discountValue, double minPurchaseAmount, Double maxDiscountAmount, 
            Integer usageLimit, boolean isActive) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Promotion newPromotion = new Promotion();
        newPromotion.setCode(code);
        newPromotion.setName(name);
        newPromotion.setDescription(description);
        newPromotion.setDiscountType(discountType);
        newPromotion.setDiscountValue(discountValue);
        newPromotion.setMinPurchaseAmount(minPurchaseAmount);
        newPromotion.setMaxDiscountAmount(maxDiscountAmount);
        newPromotion.setUsageLimit(usageLimit);
        newPromotion.setActive(isActive);
        // Start date and end date will be set by API or need to be added to dialog
        newPromotion.setStartDate(new java.util.Date());
        newPromotion.setEndDate(new java.util.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)); // 30 days from now

        Call<Promotion> call = apiService.createPromotion(token, newPromotion);
        call.enqueue(new Callback<Promotion>() {
            @Override
            public void onResponse(Call<Promotion> call, Response<Promotion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SellerManagePromotionsActivity.this, "Đã thêm khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                    loadPromotions();
                } else {
                    String errorMsg = "Lỗi thêm khuyến mãi";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ": " + response.message();
                    }
                    Toast.makeText(SellerManagePromotionsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Promotion> call, Throwable t) {
                android.util.Log.e("SellerPromotions", "Network error creating promotion", t);
                Toast.makeText(SellerManagePromotionsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePromotion(String promotionId, String code, String name, String description, 
            String discountType, double discountValue, double minPurchaseAmount, 
            Double maxDiscountAmount, Integer usageLimit, boolean isActive) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Promotion updatePromotion = new Promotion();
        updatePromotion.setCode(code);
        updatePromotion.setName(name);
        updatePromotion.setDescription(description);
        updatePromotion.setDiscountType(discountType);
        updatePromotion.setDiscountValue(discountValue);
        updatePromotion.setMinPurchaseAmount(minPurchaseAmount);
        updatePromotion.setMaxDiscountAmount(maxDiscountAmount);
        updatePromotion.setUsageLimit(usageLimit);
        updatePromotion.setActive(isActive);

        Call<Promotion> call = apiService.updatePromotion(token, promotionId, updatePromotion);
        call.enqueue(new Callback<Promotion>() {
            @Override
            public void onResponse(Call<Promotion> call, Response<Promotion> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SellerManagePromotionsActivity.this, "Đã cập nhật khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                    loadPromotions();
                } else {
                    String errorMsg = "Lỗi cập nhật khuyến mãi";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ": " + response.message();
                    }
                    Toast.makeText(SellerManagePromotionsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Promotion> call, Throwable t) {
                android.util.Log.e("SellerPromotions", "Network error updating promotion", t);
                Toast.makeText(SellerManagePromotionsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deletePromotion(Promotion promotion) {
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Map<String, String>> call = apiService.deletePromotion(token, promotion.getId());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SellerManagePromotionsActivity.this, "Đã xóa khuyến mãi", Toast.LENGTH_SHORT).show();
                    loadPromotions();
                } else {
                    Toast.makeText(SellerManagePromotionsActivity.this, "Lỗi xóa khuyến mãi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(SellerManagePromotionsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

