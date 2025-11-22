package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Promotion {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("discountType")
    private String discountType; // "percentage" or "fixed"
    
    @SerializedName("discountValue")
    private double discountValue;
    
    @SerializedName("minPurchaseAmount")
    private double minPurchaseAmount;
    
    @SerializedName("maxDiscountAmount")
    private Double maxDiscountAmount;
    
    @SerializedName("startDate")
    private Date startDate;
    
    @SerializedName("endDate")
    private Date endDate;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("usageLimit")
    private Integer usageLimit;
    
    @SerializedName("usedCount")
    private int usedCount;

    @SerializedName("sellerId")
    private String sellerId; // null for admin promotions

    public Promotion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinPurchaseAmount() { return minPurchaseAmount; }
    public void setMinPurchaseAmount(double minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}

