package com.example.safelink.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.models.HistoryModel;
import com.example.safelink.models.ScanResult;
import com.example.safelink.utils.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryModel> data;

    public HistoryAdapter(List<HistoryModel> data) {
        this.data = data != null ? data : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    public HistoryModel getItem(int position) { return data.get(position); }

    public void updateData(List<HistoryModel> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View dotView;
        private final TextView tvUrl, tvTime, tvStatus;
        private final ImageView ivFavicon;

        ViewHolder(View itemView) {
            super(itemView);
            dotView   = itemView.findViewById(R.id.view_status_dot);
            tvUrl     = itemView.findViewById(R.id.tv_url);
            tvTime    = itemView.findViewById(R.id.tv_time);
            tvStatus  = itemView.findViewById(R.id.tv_status);
            ivFavicon = itemView.findViewById(R.id.iv_favicon);
        }

        void bind(HistoryModel item) {
            tvUrl.setText(item.getUrl());
            tvTime.setText(DateFormatter.formatForDisplay(item.getScannedAt()));

            int colorRes;
            int bgRes;
            String label;

            if (ScanResult.STATUS_SAFE.equalsIgnoreCase(item.getStatus())) {
                colorRes = R.color.accent_green;
                bgRes = R.drawable.bg_badge_safe;
                label = "SAFE";
            } else if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(item.getStatus())) {
                colorRes = R.color.accent_red;
                bgRes = R.drawable.bg_badge_dangerous;
                label = "DANGEROUS";
            } else {
                colorRes = R.color.accent_orange;
                bgRes = R.drawable.bg_badge_suspicious;
                label = "SUSPICIOUS";
            }

            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            tvStatus.setText(label);
            tvStatus.setTextColor(color);
            tvStatus.setBackgroundResource(bgRes);

            if (dotView != null) {
                // Set both background drawable (shape) and tint for correct color
                dotView.setBackgroundResource(bgRes);
                dotView.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            try {
                String host = Uri.parse(item.getUrl()).getHost();
                if (host != null) {
                    String faviconUrl = "https://www.google.com/s2/favicons?domain=" + host + "&sz=128";
                    Glide.with(itemView.getContext())
                         .load(faviconUrl)
                         .error(R.drawable.ic_status_safe)
                         .into(ivFavicon);
                }
            } catch (Exception e) {
                ivFavicon.setImageResource(R.drawable.ic_status_safe);
            }
        }
    }
}
