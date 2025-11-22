package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Product {
    
    @SerializedName("_id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("price")
    private double price;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("images")
    private String[] images;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("brand")
    private String brand;
    
    @SerializedName("color")
    private String color;
    
    @SerializedName("size")
    private String size;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("inStock")
    private boolean inStock;
    
    @SerializedName("sellerId")
    private String sellerId;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    @SerializedName("updatedAt")
    private Date updatedAt;

    public Product() {
        this.inStock = true;
    }

    public Product(String id, String name, double price, boolean inStock, Date createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.inStock = inStock;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isInStock() {
        return inStock;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String[] getImages() { return images; }
    public void setImages(String[] images) { this.images = images; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", inStock=" + inStock +
                ", createdAt=" + createdAt +
                '}';
    }
}
