package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
        void onUserEdit(User user);
        void onUserDelete(User user);
    }

    public UserAdapter(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (users != null && position < users.size()) {
            holder.bind(users.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers != null ? newUsers : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvEmail;
        private TextView tvPhone;
        private TextView tvAddress;
        private TextView tvRole;
        private TextView tvStatus;
        private TextView btnEdit;
        private TextView btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(User user) {
            if (tvName != null) tvName.setText(user.getName() != null ? user.getName() : "N/A");
            if (tvEmail != null) tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            if (tvPhone != null) tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
            if (tvAddress != null) tvAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");
            if (tvRole != null) tvRole.setText(user.getRole() != null ? user.getRole() : "N/A");
            if (tvStatus != null) {
                tvStatus.setText(user.isActive() ? "Hoạt động" : "Không hoạt động");
                tvStatus.setTextColor(user.isActive() ? 
                    itemView.getContext().getColor(android.R.color.holo_green_dark) : 
                    itemView.getContext().getColor(android.R.color.holo_red_dark));
            }

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (onUserClickListener != null) {
                        onUserClickListener.onUserEdit(user);
                    }
                });
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (onUserClickListener != null) {
                        onUserClickListener.onUserDelete(user);
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onUserClick(user);
                }
            });
        }
    }
}

