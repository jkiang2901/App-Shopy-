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
import com.example.banhangapp.models.Product;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener onProductClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(OnProductClickListener onProductClickListener, 
                        OnAddToCartClickListener onAddToCartClickListener) {
        this.onProductClickListener = onProductClickListener;
        this.onAddToCartClickListener = onAddToCartClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ProductAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        ProductViewHolder holder = new ProductViewHolder(view);
        android.util.Log.d("ProductAdapter", "ViewHolder created");
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        android.util.Log.d("ProductAdapter", "onBindViewHolder called for position: " + position);
        if (products != null && position < products.size()) {
            try {
                Product product = products.get(position);
                android.util.Log.d("ProductAdapter", "Binding product: " + product.getName() + " at position " + position);
                holder.bind(product);
                
                // Add entrance animation (only if position is valid)
                if (position >= 0) {
                    holder.itemView.setAlpha(0f);
                    holder.itemView.setTranslationX(holder.itemView.getWidth());
                    holder.itemView.animate()
                        .alpha(1f)
                        .translationX(0)
                        .setDuration(400)
                        .setStartDelay(Math.max(0, position * 50))
                        .start();
                }
            } catch (Exception e) {
                android.util.Log.e("ProductAdapter", "Error binding product at position " + position, e);
                e.printStackTrace();
            }
        } else {
            android.util.Log.w("ProductAdapter", "Cannot bind: products=" + (products != null ? products.size() : "null") + ", position=" + position);
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new java.util.ArrayList<>();
        android.util.Log.d("ProductAdapter", "Updating products: " + this.products.size() + " items");
        notifyDataSetChanged();
        android.util.Log.d("ProductAdapter", "notifyDataSetChanged called. Item count: " + getItemCount());
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        private TextView productPrice;
        private TextView productDescription;
        private com.google.android.material.button.MaterialButton addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }

        public void bind(Product product) {
            try {
                android.util.Log.d("ProductAdapter", "Binding product: " + product.getName());
                
                if (productName != null) {
                    productName.setText(product.getName() != null ? product.getName() : "N/A");
                } else {
                    android.util.Log.e("ProductAdapter", "productName TextView is null!");
                }
                
                if (productPrice != null) {
                    productPrice.setText(formatPrice(product.getPrice()));
                } else {
                    android.util.Log.e("ProductAdapter", "productPrice TextView is null!");
                }
                
                // Handle description
                if (productDescription != null) {
                    if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                        productDescription.setText(product.getDescription());
                    } else if (product.isInStock()) {
                        productDescription.setText("Còn hàng");
                    } else {
                        productDescription.setText("Hết hàng");
                    }
                } else {
                    android.util.Log.e("ProductAdapter", "productDescription TextView is null!");
                }

                // Handle image
                if (productImage != null) {
                    if (product.getImages() != null && product.getImages().length > 0 && 
                        product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                        // Load image from URL if available
                        Glide.with(itemView.getContext())
                                .load(product.getImages()[0])
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(productImage);
                    } else {
                        // Use placeholder if no image URL
                        productImage.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    android.util.Log.e("ProductAdapter", "productImage ImageView is null!");
                }
                
                // Set click listeners
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onProductClickListener != null) {
                            onProductClickListener.onProductClick(product);
                        }
                    }
                });

                if (addToCartButton != null) {
                    addToCartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onAddToCartClickListener != null) {
                                onAddToCartClickListener.onAddToCartClick(product);
                            }
                        }
                    });
                }
                
                android.util.Log.d("ProductAdapter", "Product bound successfully: " + product.getName());
            } catch (Exception e) {
                android.util.Log.e("ProductAdapter", "Error in bind()", e);
                e.printStackTrace();
            }
        }

        private String formatPrice(double price) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return format.format(price);
        }
    }
}
