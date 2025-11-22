package com.example.banhangapp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName(value = "_id", alternate = {"id"})
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("role")
    private String role; // "admin", "seller", "customer"
    
    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("password")
    private String password; // Only used when creating/updating user, not returned from API

    public User() {}

    public User(String id, String name, String email, String phone, String address, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

