package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Promotion;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private List<Promotion> promotions;
    private OnPromotionClickListener listener;
    private String currentSellerId; // To check if promotion belongs to current seller

    public interface OnPromotionClickListener {
        void onPromotionClick(Promotion promotion);
        void onPromotionEdit(Promotion promotion);
        void onPromotionDelete(Promotion promotion);
    }

    public PromotionAdapter(OnPromotionClickListener listener, String currentSellerId) {
        this.promotions = new ArrayList<>();
        this.listener = listener;
        this.currentSellerId = currentSellerId;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        if (promotions != null && position < promotions.size()) {
            holder.bind(promotions.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return promotions != null ? promotions.size() : 0;
    }

    public void updatePromotions(List<Promotion> newPromotions) {
        this.promotions = newPromotions != null ? newPromotions : new ArrayList<>();
        notifyDataSetChanged();
    }

    private boolean canEditPromotion(Promotion promotion) {
        // Seller can only edit their own promotions (not admin promotions with sellerId = null)
        if (currentSellerId == null || currentSellerId.isEmpty()) {
            return false;
        }
        String promotionSellerId = promotion.getSellerId();
        return promotionSellerId != null && promotionSellerId.equals(currentSellerId);
    }

    class PromotionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCode;
        private TextView tvName;
        private TextView tvDiscount;
        private TextView tvStatus;
        private TextView tvDateRange;
        private TextView tvUsage;
        private TextView btnEdit;
        private TextView btnDelete;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvUsage = itemView.findViewById(R.id.tvUsage);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Promotion promotion) {
            if (tvCode != null) {
                tvCode.setText(promotion.getCode() != null ? promotion.getCode() : "N/A");
            }

            if (tvName != null) {
                tvName.setText(promotion.getName() != null ? promotion.getName() : "N/A");
            }

            if (tvDiscount != null) {
                String discountText;
                if ("percentage".equals(promotion.getDiscountType())) {
                    discountText = String.format("%.0f%%", promotion.getDiscountValue());
                } else {
                    discountText = String.format("%,.0f VNĐ", promotion.getDiscountValue());
                }
                tvDiscount.setText("Giảm: " + discountText);
            }

            if (tvStatus != null) {
                if (promotion.isActive()) {
                    tvStatus.setText("Đang hoạt động");
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                } else {
                    tvStatus.setText("Đã tắt");
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                }
            }

            if (tvDateRange != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateRange = "";
                if (promotion.getStartDate() != null) {
                    dateRange = sdf.format(promotion.getStartDate());
                }
                if (promotion.getEndDate() != null) {
                    dateRange += " - " + sdf.format(promotion.getEndDate());
                }
                tvDateRange.setText(dateRange);
            }

            if (tvUsage != null) {
                String usageText = promotion.getUsedCount() + " / ";
                if (promotion.getUsageLimit() != null) {
                    usageText += promotion.getUsageLimit();
                } else {
                    usageText += "∞";
                }
                tvUsage.setText("Đã dùng: " + usageText);
            }

            if (btnEdit != null) {
                // Only show edit button if promotion belongs to current seller
                boolean canEdit = canEditPromotion(promotion);
                btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
                btnEdit.setOnClickListener(v -> {
                    if (listener != null && canEdit) {
                        listener.onPromotionEdit(promotion);
                    } else {
                        android.widget.Toast.makeText(itemView.getContext(), 
                            "Bạn không có quyền chỉnh sửa khuyến mãi này", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnDelete != null) {
                // Only show delete button if promotion belongs to current seller
                boolean canDelete = canEditPromotion(promotion);
                btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
                btnDelete.setOnClickListener(v -> {
                    if (listener != null && canDelete) {
                        listener.onPromotionDelete(promotion);
                    } else {
                        android.widget.Toast.makeText(itemView.getContext(), 
                            "Bạn không có quyền xóa khuyến mãi này", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPromotionClick(promotion);
                }
            });
        }
    }
}

