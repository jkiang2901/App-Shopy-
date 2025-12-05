package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Order;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private boolean allowStatusUpdate; // Flag to control if status update button should be shown

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onOrderStatusChange(Order order, String newStatus);
        void onOrderCancel(Order order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this(listener, false); // Default: don't allow status update
    }

    public OrderAdapter(OnOrderClickListener listener, boolean allowStatusUpdate) {
        this.orders = new ArrayList<>();
        this.listener = listener;
        this.allowStatusUpdate = allowStatusUpdate;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        try {
            if (orders != null && position < orders.size() && position >= 0) {
                holder.bind(orders.get(position));
            }
        } catch (Exception e) {
            android.util.Log.e("OrderAdapter", "Error binding order at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        try {
            android.util.Log.d("OrderAdapter", "Updating orders. New count: " + 
                (newOrders != null ? newOrders.size() : 0));
            
            // Filter out null orders
            List<Order> validOrders = new ArrayList<>();
            if (newOrders != null) {
                for (Order order : newOrders) {
                    if (order != null && order.getId() != null) {
                        validOrders.add(order);
                    } else {
                        android.util.Log.w("OrderAdapter", "Skipping null order or order with null ID");
                    }
                }
            }
            
            this.orders = validOrders;
            android.util.Log.d("OrderAdapter", "Valid orders count: " + this.orders.size());
            
            // Notify adapter of data change
            notifyDataSetChanged();
            android.util.Log.d("OrderAdapter", "Adapter notified of data change");
        } catch (IllegalStateException e) {
            // RecyclerView might be detached, log but don't crash
            android.util.Log.w("OrderAdapter", "Cannot notify adapter - RecyclerView may be detached", e);
        } catch (Exception e) {
            android.util.Log.e("OrderAdapter", "Error updating orders", e);
            e.printStackTrace();
        }
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId;
        private TextView tvTotalAmount;
        private com.google.android.material.chip.Chip tvStatus;
        private TextView tvDate;
        private TextView tvItemCount;
        private com.google.android.material.button.MaterialButton btnUpdateStatus;
        private com.google.android.material.button.MaterialButton btnViewDetail;
        private com.google.android.material.button.MaterialButton btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }

        public void bind(Order order) {
            if (order == null) {
                return;
            }
            
            try {
                if (tvOrderId != null) {
                    String orderId = order.getId();
                    if (orderId != null && orderId.length() > 8) {
                        tvOrderId.setText("Đơn hàng: " + orderId.substring(0, 8) + "...");
                    } else if (orderId != null) {
                        tvOrderId.setText("Đơn hàng: " + orderId);
                    } else {
                        tvOrderId.setText("Đơn hàng: N/A");
                    }
                }

                if (tvTotalAmount != null) {
                    try {
                        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                        tvTotalAmount.setText(format.format(order.getFinalAmount()));
                    } catch (Exception e) {
                        android.util.Log.e("OrderAdapter", "Error formatting total amount", e);
                        tvTotalAmount.setText(String.format("%,.0f đ", order.getFinalAmount()));
                    }
                }

                if (tvStatus != null) {
                    try {
                        String status = order.getStatus() != null ? order.getStatus() : "pending";
                        String statusText = "";
                        int chipBackgroundColor = R.color.info_light;
                        int chipStrokeColor = R.color.info;
                        
                        switch (status) {
                            case "pending":
                                statusText = "⏳ Chờ xử lý";
                                chipBackgroundColor = R.color.warning_light;
                                chipStrokeColor = R.color.warning;
                                break;
                            case "confirmed":
                                statusText = "✅ Đã xác nhận";
                                chipBackgroundColor = R.color.info_light;
                                chipStrokeColor = R.color.info;
                                break;
                            case "shipping":
                                statusText = "🚚 Đang giao";
                                chipBackgroundColor = R.color.info_light;
                                chipStrokeColor = R.color.info;
                                break;
                            case "delivered":
                                statusText = "🎉 Đã giao";
                                chipBackgroundColor = R.color.success_light;
                                chipStrokeColor = R.color.success;
                                break;
                            case "cancelled":
                                statusText = "❌ Đã hủy";
                                chipBackgroundColor = R.color.error_light;
                                chipStrokeColor = R.color.error;
                                break;
                        }
                        tvStatus.setText(statusText);
                        tvStatus.setChipBackgroundColorResource(chipBackgroundColor);
                        tvStatus.setChipStrokeColorResource(chipStrokeColor);
                    } catch (Exception e) {
                        android.util.Log.e("OrderAdapter", "Error setting status", e);
                    }
                }

                if (tvDate != null && order.getCreatedAt() != null) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        tvDate.setText(sdf.format(order.getCreatedAt()));
                    } catch (Exception e) {
                        android.util.Log.e("OrderAdapter", "Error formatting date", e);
                        tvDate.setText("N/A");
                    }
                }

                if (tvItemCount != null && order.getItems() != null) {
                    try {
                        int totalItems = 0;
                        for (Order.OrderItem item : order.getItems()) {
                            if (item != null) {
                                totalItems += item.getQuantity();
                            }
                        }
                        tvItemCount.setText(totalItems + " sản phẩm");
                    } catch (Exception e) {
                        android.util.Log.e("OrderAdapter", "Error counting items", e);
                        tvItemCount.setText("0 sản phẩm");
                    }
                }

                if (btnUpdateStatus != null) {
                    // Hide status update button for customers
                    if (!allowStatusUpdate) {
                        btnUpdateStatus.setVisibility(View.GONE);
                    } else {
                        // Only show for sellers/admins
                        String currentStatus = order.getStatus();
                        if ("pending".equals(currentStatus)) {
                            btnUpdateStatus.setText("Xác nhận");
                            btnUpdateStatus.setVisibility(View.VISIBLE);
                            btnUpdateStatus.setOnClickListener(v -> {
                                if (listener != null) {
                                    listener.onOrderStatusChange(order, "confirmed");
                                }
                            });
                        } else if ("confirmed".equals(currentStatus)) {
                            btnUpdateStatus.setText("Bắt đầu giao");
                            btnUpdateStatus.setVisibility(View.VISIBLE);
                            btnUpdateStatus.setOnClickListener(v -> {
                                if (listener != null) {
                                    listener.onOrderStatusChange(order, "shipping");
                                }
                            });
                        } else if ("shipping".equals(currentStatus)) {
                            btnUpdateStatus.setText("Hoàn thành");
                            btnUpdateStatus.setVisibility(View.VISIBLE);
                            btnUpdateStatus.setOnClickListener(v -> {
                                if (listener != null) {
                                    listener.onOrderStatusChange(order, "delivered");
                                }
                            });
                        } else {
                            btnUpdateStatus.setVisibility(View.GONE);
                        }
                    }
                }

                // Cancel order button - show for customers when order can be cancelled
                if (btnCancelOrder != null) {
                    String currentStatus = order.getStatus();
                    // Show cancel button for customers if order is not delivered or already cancelled
                    // Allow cancellation for: pending, confirmed, shipping (before delivery)
                    if (!allowStatusUpdate) {
                        if (currentStatus != null && 
                            !"delivered".equals(currentStatus) && 
                            !"cancelled".equals(currentStatus)) {
                            // Can cancel pending, confirmed, or shipping orders
                            btnCancelOrder.setVisibility(View.VISIBLE);
                            btnCancelOrder.setOnClickListener(v -> {
                                if (listener != null) {
                                    listener.onOrderCancel(order);
                                }
                            });
                        } else {
                            btnCancelOrder.setVisibility(View.GONE);
                        }
                    } else {
                        // For sellers/admins, don't show cancel button
                        btnCancelOrder.setVisibility(View.GONE);
                    }
                }

                // View detail button - always visible for customers
                if (btnViewDetail != null) {
                    btnViewDetail.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onOrderClick(order);
                        }
                    });
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onOrderClick(order);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("OrderAdapter", "Error binding order", e);
            }
        }
    }
}

