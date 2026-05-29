package com.example.safelink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

import com.example.safelink.R;
import com.example.safelink.models.BookmarkModel;
import com.example.safelink.models.ScanResult;

import com.bumptech.glide.Glide;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private List<BookmarkModel> data;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BookmarkModel item);
        void onMoreClick(BookmarkModel item, View anchor);
    }

    public BookmarkAdapter(List<BookmarkModel> data, OnItemClickListener listener) {
        this.data = data != null ? data : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    public BookmarkModel getItem(int position) { return data.get(position); }

    public void updateData(List<BookmarkModel> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle, tvUrl, tvCategory, tvRepBadge;
        private final ImageView btnMore, ivFavicon;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tv_title);
            tvUrl      = itemView.findViewById(R.id.tv_url);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnMore    = itemView.findViewById(R.id.btn_more);
            tvRepBadge = itemView.findViewById(R.id.tv_rep_badge);
            ivFavicon  = itemView.findViewById(R.id.iv_favicon);
            
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(data.get(pos));
                }
            });
            
            btnMore.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMoreClick(data.get(pos), v);
                }
            });
        }

        void bind(BookmarkModel item) {
            tvTitle.setText(item.getTitle() != null && !item.getTitle().isEmpty() ? item.getTitle() : "Saved Link");
            tvUrl.setText(item.getUrl());
            tvCategory.setText(item.getCategory() != null && !item.getCategory().isEmpty() ? item.getCategory() : "Uncategorized");

            int colorRes = R.color.accent_green;
            int bgRes = R.drawable.bg_badge_safe;
            String label = "SAFE";

            if (ScanResult.STATUS_SUSPICIOUS.equalsIgnoreCase(item.getStatus())) {
                colorRes = R.color.accent_orange;
                bgRes = R.drawable.bg_badge_suspicious;
                label = "SUSPICIOUS";
            } else if (ScanResult.STATUS_DANGEROUS.equalsIgnoreCase(item.getStatus())) {
                colorRes = R.color.accent_red;
                bgRes = R.drawable.bg_badge_dangerous;
                label = "DANGEROUS";
            } else if ("TRUSTED".equalsIgnoreCase(item.getStatus())) {
                colorRes = R.color.accent_green;
                bgRes = R.drawable.bg_badge_safe;
                label = "TRUSTED";
            }

            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            tvRepBadge.setText(label);
            tvRepBadge.setTextColor(color);
            tvRepBadge.setBackgroundResource(bgRes);

            try {
                String host = Uri.parse(item.getUrl()).getHost();
                if (host != null) {
                    String faviconUrl = "https://www.google.com/s2/favicons?domain=" + host + "&sz=128";
                    Glide.with(itemView.getContext())
                         .load(faviconUrl)
                         .error(R.drawable.ic_nav_bookmark)
                         .into(ivFavicon);
                }
            } catch (Exception e) {
                ivFavicon.setImageResource(R.drawable.ic_nav_bookmark);
            }
        }
    }
}
