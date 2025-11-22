package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Cart {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("customerId")
    private String customerId;
    
    @SerializedName("items")
    private List<CartItem> items;
    
    @SerializedName("updatedAt")
    private Date updatedAt;
    
    @SerializedName("__v")
    private int version;

    public Cart() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}

