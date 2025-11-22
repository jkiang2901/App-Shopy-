package com.example.banhangapp.api;

import com.example.banhangapp.models.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    
    // ========== Authentication ==========
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
    
    // ========== Users ==========
    @GET("api/users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);
    
    @PUT("api/users/me")
    Call<User> updateCurrentUser(@Header("Authorization") String token, @Body User user);
    
    @GET("api/users")
    Call<List<User>> getUsers(@Header("Authorization") String token, @QueryMap Map<String, String> params);
    
    @GET("api/users/customers")
    Call<List<User>> getCustomers(@Header("Authorization") String token, @Query("search") String search);
    
    @GET("api/users/{id}")
    Call<User> getUserById(@Header("Authorization") String token, @Path("id") String id);
    
    @PUT("api/users/{id}")
    Call<User> updateUser(@Header("Authorization") String token, @Path("id") String id, @Body User user);
    
    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Header("Authorization") String token, @Path("id") String id);
    
    @POST("api/users")
    Call<User> createUser(@Header("Authorization") String token, @Body User user);
    
    // ========== Products ==========
    @GET("api/products")
    Call<List<Product>> getProducts(@QueryMap Map<String, String> params);
    
    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") String id);
    
    @POST("api/products")
    Call<Product> createProduct(@Header("Authorization") String token, @Body Product product);
    
    @PUT("api/products/{id}")
    Call<Product> updateProduct(@Header("Authorization") String token, @Path("id") String id, @Body Product product);
    
    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Header("Authorization") String token, @Path("id") String id);
    
    // ========== Cart ==========
    @GET("api/cart")
    Call<Cart> getCart(@Header("Authorization") String token);
    
    @POST("api/cart/items")
    Call<Cart> addToCart(@Header("Authorization") String token, @Body CartItemRequest request);
    
    @PUT("api/cart/items/{productId}")
    Call<Cart> updateCartItem(@Header("Authorization") String token, @Path("productId") String productId, @Body Map<String, Integer> quantity);
    
    @DELETE("api/cart/items/{productId}")
    Call<Cart> removeFromCart(@Header("Authorization") String token, @Path("productId") String productId);
    
    @DELETE("api/cart")
    Call<Map<String, String>> clearCart(@Header("Authorization") String token);
    
    // ========== Orders ==========
    @POST("api/orders")
    Call<Order> createOrder(@Header("Authorization") String token, @Body OrderRequest request);
    
    @GET("api/orders")
    Call<List<Order>> getOrders(@Header("Authorization") String token);
    
    @GET("api/orders/{id}")
    Call<Order> getOrderById(@Header("Authorization") String token, @Path("id") String id);
    
    @PUT("api/orders/{id}/status")
    Call<Order> updateOrderStatus(@Header("Authorization") String token, @Path("id") String id, @Body Map<String, String> status);
    
    // ========== Promotions ==========
    @GET("api/promotions")
    Call<List<Promotion>> getPromotions(@Header("Authorization") String token);
    
    @GET("api/promotions/code/{code}")
    Call<Promotion> getPromotionByCode(@Header("Authorization") String token, @Path("code") String code);
    
    @POST("api/promotions")
    Call<Promotion> createPromotion(@Header("Authorization") String token, @Body Promotion promotion);
    
    @PUT("api/promotions/{id}")
    Call<Promotion> updatePromotion(@Header("Authorization") String token, @Path("id") String id, @Body Promotion promotion);
    
    @DELETE("api/promotions/{id}")
    Call<Map<String, String>> deletePromotion(@Header("Authorization") String token, @Path("id") String id);
    
    // ========== Reports ==========
    @POST("api/reports")
    Call<Report> createReport(@Header("Authorization") String token, @Body Report report);
    
    @GET("api/reports")
    Call<List<Report>> getReports(@Header("Authorization") String token, @Query("status") String status);
    
    @GET("api/reports/{id}")
    Call<Report> getReportById(@Header("Authorization") String token, @Path("id") String id);
    
    @PUT("api/reports/{id}/status")
    Call<Report> updateReportStatus(@Header("Authorization") String token, @Path("id") String id, @Body Map<String, String> data);
    
    // ========== Inventory ==========
    @GET("api/inventory")
    Call<List<Inventory>> getInventory(@Header("Authorization") String token, @Query("productId") String productId);
    
    @POST("api/inventory")
    Call<Inventory> createInventoryRecord(@Header("Authorization") String token, @Body InventoryRequest request);
    
    // ========== Statistics ==========
    @GET("api/statistics/revenue")
    Call<RevenueResponse> getRevenue(@Header("Authorization") String token, @Query("period") String period);
    
    @GET("api/statistics/bestsellers")
    Call<List<BestSellerResponse>> getBestSellers(@Header("Authorization") String token, @Query("limit") int limit);
    
    // ========== Request/Response Models ==========
    class LoginRequest {
        String email;
        String password;
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    
    class RegisterRequest {
        String name;
        String email;
        String password;
        String phone;
        String address;
        String role;
        
        public RegisterRequest(String name, String email, String password, String phone, String address, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.address = address;
            this.role = role;
        }
    }
    
    class AuthResponse {
        public String token;
        public User user;
    }
    
    class CartItemRequest {
        String productId;
        int quantity;
        
        public CartItemRequest(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
    
    class OrderRequest {
        String paymentMethod;
        String deliveryAddress;
        String promotionCode;
        
        public OrderRequest(String paymentMethod, String deliveryAddress, String promotionCode) {
            this.paymentMethod = paymentMethod;
            this.deliveryAddress = deliveryAddress;
            this.promotionCode = promotionCode;
        }
    }
    
    class InventoryRequest {
        String productId;
        int quantity;
        String type;
        String note;
        
        public InventoryRequest(String productId, int quantity, String type, String note) {
            this.productId = productId;
            this.quantity = quantity;
            this.type = type;
            this.note = note;
        }
    }
    
    class RevenueResponse {
        public String period;
        public double totalRevenue;
        public Map<String, Double> revenueByPeriod;
        public int orderCount;
    }
    
    class BestSellerResponse {
        Product product;
        int totalQuantity;
        double totalRevenue;
    }
}
