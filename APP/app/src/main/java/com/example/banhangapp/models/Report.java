package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Report {
    @SerializedName("_id")
    private String id;
    
    @SerializedName("reporterId")
    private User reporterId;
    
    @SerializedName("reportedUserId")
    private User reportedUserId;
    
    @SerializedName("reportType")
    private String reportType; // "seller" or "buyer"
    
    @SerializedName("reason")
    private String reason;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("status")
    private String status; // "pending", "reviewing", "resolved", "dismissed"
    
    @SerializedName("adminNotes")
    private String adminNotes;
    
    @SerializedName("createdAt")
    private Date createdAt;

    public Report() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getReporterId() { return reporterId; }
    public void setReporterId(User reporterId) { this.reporterId = reporterId; }

    public User getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(User reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

