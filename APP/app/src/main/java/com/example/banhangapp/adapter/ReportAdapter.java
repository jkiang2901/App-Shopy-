package com.example.banhangapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.banhangapp.R;
import com.example.banhangapp.models.Report;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reports;
    private OnReportClickListener onReportClickListener;

    public interface OnReportClickListener {
        void onReportClick(Report report);
        void onReportUpdateStatus(Report report, String status);
    }

    public ReportAdapter(OnReportClickListener onReportClickListener) {
        this.onReportClickListener = onReportClickListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        if (reports != null && position < reports.size()) {
            holder.bind(reports.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public void updateReports(List<Report> newReports) {
        this.reports = newReports != null ? newReports : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvType;
        private TextView tvReason;
        private TextView tvStatus;
        private TextView tvCreatedAt;
        private TextView btnResolve;
        private TextView btnReject;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(Report report) {
            if (tvType != null) {
                String type = report.getReportType() != null ? report.getReportType() : "N/A";
                tvType.setText("Loại: " + type);
            }
            
            if (tvReason != null) {
                tvReason.setText(report.getReason() != null ? report.getReason() : "N/A");
            }
            
            if (tvStatus != null) {
                String status = report.getStatus() != null ? report.getStatus() : "pending";
                tvStatus.setText("Trạng thái: " + status);
                if ("resolved".equals(status)) {
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                } else if ("rejected".equals(status)) {
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                } else {
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                }
            }
            
            if (tvCreatedAt != null && report.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvCreatedAt.setText("Ngày: " + sdf.format(report.getCreatedAt()));
            }

            if (btnResolve != null) {
                btnResolve.setOnClickListener(v -> {
                    if (onReportClickListener != null) {
                        onReportClickListener.onReportUpdateStatus(report, "resolved");
                    }
                });
            }

            if (btnReject != null) {
                btnReject.setOnClickListener(v -> {
                    if (onReportClickListener != null) {
                        onReportClickListener.onReportUpdateStatus(report, "rejected");
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (onReportClickListener != null) {
                    onReportClickListener.onReportClick(report);
                }
            });
        }
    }
}

