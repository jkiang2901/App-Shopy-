package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("productId")
    private Product productId;
    
    @SerializedName("quantity")
    private int quantity;

    public CartItem() {}

    public CartItem(Product productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Product getProductId() { return productId; }
    public void setProductId(Product productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

