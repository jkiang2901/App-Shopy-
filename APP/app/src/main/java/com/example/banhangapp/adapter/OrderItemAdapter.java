package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Order;
import com.example.banhangapp.models.Product;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<Order.OrderItem> items;

    public OrderItemAdapter() {
        this.items = new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        if (items != null && position < items.size()) {
            holder.bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<Order.OrderItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private TextView tvSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }

        public void bind(Order.OrderItem item) {
            if (item == null) {
                return;
            }
            
            try {
                Product product = item.getProductId();
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                // Handle product name - check if product has name or only ID
                if (tvProductName != null) {
                    if (product != null && product.getName() != null && !product.getName().isEmpty()) {
                        tvProductName.setText(product.getName());
                    } else if (product != null && product.getId() != null) {
                        // Product only has ID, show ID or generic name
                        tvProductName.setText("Sản phẩm #" + product.getId().substring(0, Math.min(8, product.getId().length())));
                    } else {
                        tvProductName.setText("Sản phẩm");
                    }
                }

                // Handle product image
                if (ivProductImage != null) {
                    if (product != null && product.getImages() != null && product.getImages().length > 0 && 
                        product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                        try {
                            Glide.with(itemView.getContext())
                                .load(product.getImages()[0])
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .centerCrop()
                                .into(ivProductImage);
                        } catch (Exception e) {
                            android.util.Log.e("OrderItemAdapter", "Error loading image", e);
                            ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }

                // Price, quantity, and subtotal - these are always available in OrderItem
                if (tvProductPrice != null) {
                    tvProductPrice.setText(format.format(item.getPrice()));
                }

                if (tvQuantity != null) {
                    tvQuantity.setText("Số lượng: x" + item.getQuantity());
                }

                if (tvSubtotal != null) {
                    double subtotal = item.getPrice() * item.getQuantity();
                    tvSubtotal.setText(format.format(subtotal));
                }
            } catch (Exception e) {
                android.util.Log.e("OrderItemAdapter", "Error binding order item", e);
                // Set default values on error
                if (tvProductName != null) {
                    tvProductName.setText("Sản phẩm");
                }
                if (tvProductPrice != null) {
                    tvProductPrice.setText("0 đ");
                }
                if (tvQuantity != null) {
                    tvQuantity.setText("x0");
                }
                if (tvSubtotal != null) {
                    tvSubtotal.setText("0 đ");
                }
            }
        }
    }
}

