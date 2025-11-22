package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Inventory {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("sellerId")
    private String sellerId;
    
    @SerializedName("productId")
    private Product productId;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("type")
    private String type; // "import", "export", "adjustment"
    
    @SerializedName("note")
    private String note;
    
    @SerializedName("createdAt")
    private Date createdAt;

    public Inventory() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public Product getProductId() { return productId; }
    public void setProductId(Product productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

