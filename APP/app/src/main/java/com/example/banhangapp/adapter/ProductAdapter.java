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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (products != null && position < products.size()) {
            try {
                Product product = products.get(position);
                holder.bind(product);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new java.util.ArrayList<>();
        notifyDataSetChanged();
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
            if (productName != null) {
                productName.setText(product.getName() != null ? product.getName() : "N/A");
            }
            
            if (productPrice != null) {
                productPrice.setText(formatPrice(product.getPrice()));
            }
            
            if (productDescription != null) {
                if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                    productDescription.setText(product.getDescription());
                } else if (product.isInStock()) {
                    productDescription.setText("Còn hàng");
                } else {
                    productDescription.setText("Hết hàng");
                }
            }

            if (productImage != null) {
                if (product.getImages() != null && product.getImages().length > 0 && 
                    product.getImages()[0] != null && !product.getImages()[0].isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(product.getImages()[0])
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .centerCrop()
                            .into(productImage);
                } else {
                    productImage.setImageResource(R.drawable.placeholder_image);
                }
            }
            
            itemView.setOnClickListener(v -> {
                if (onProductClickListener != null) {
                    onProductClickListener.onProductClick(product);
                }
            });

            if (addToCartButton != null) {
                addToCartButton.setOnClickListener(v -> {
                    if (onAddToCartClickListener != null) {
                        onAddToCartClickListener.onAddToCartClick(product);
                    }
                });
            }
        }

        private String formatPrice(double price) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return format.format(price);
        }
    }
}
