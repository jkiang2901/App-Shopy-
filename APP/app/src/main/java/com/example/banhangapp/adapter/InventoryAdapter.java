package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Inventory;
import com.example.banhangapp.models.Product;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<Inventory> inventories;

    public InventoryAdapter() {
        this.inventories = new ArrayList<>();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        if (inventories != null && position < inventories.size()) {
            holder.bind(inventories.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return inventories != null ? inventories.size() : 0;
    }

    public void updateInventories(List<Inventory> newInventories) {
        this.inventories = newInventories != null ? newInventories : new ArrayList<>();
        notifyDataSetChanged();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvQuantity;
        private TextView tvType;
        private TextView tvNote;
        private TextView tvDate;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvType = itemView.findViewById(R.id.tvType);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(Inventory inventory) {
            Product product = inventory.getProductId();
            if (product != null && tvProductName != null) {
                tvProductName.setText(product.getName() != null ? product.getName() : "N/A");
            }

            if (tvQuantity != null) {
                String quantityText = String.valueOf(inventory.getQuantity());
                if ("import".equals(inventory.getType())) {
                    tvQuantity.setText("+" + quantityText);
                    tvQuantity.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                } else if ("export".equals(inventory.getType())) {
                    tvQuantity.setText("-" + quantityText);
                    tvQuantity.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                } else {
                    tvQuantity.setText(quantityText);
                    tvQuantity.setTextColor(itemView.getContext().getColor(android.R.color.black));
                }
            }

            if (tvType != null) {
                String type = inventory.getType();
                if ("import".equals(type)) {
                    tvType.setText("Nhập kho");
                } else if ("export".equals(type)) {
                    tvType.setText("Xuất kho");
                } else {
                    tvType.setText("Điều chỉnh");
                }
            }

            if (tvNote != null) {
                tvNote.setText(inventory.getNote() != null ? inventory.getNote() : "Không có ghi chú");
            }

            if (tvDate != null && inventory.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvDate.setText(sdf.format(inventory.getCreatedAt()));
            }
        }
    }
}

