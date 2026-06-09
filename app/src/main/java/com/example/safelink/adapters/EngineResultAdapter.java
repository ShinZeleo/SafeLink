package com.example.safelink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safelink.R;
import com.example.safelink.models.ApiResponse.EngineResultItem;

import java.util.List;

public class EngineResultAdapter extends RecyclerView.Adapter<EngineResultAdapter.ViewHolder> {

    private final List<EngineResultItem> items;

    public EngineResultAdapter(List<EngineResultItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_engine_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EngineResultItem item = items.get(position);
        holder.tvEngineName.setText(item.engineName);
        holder.tvEngineResult.setText(item.result != null ? item.result : "—");
        holder.tvCategory.setText(item.category != null ? item.category.toUpperCase() : "UNDETECTED");

        // Reset dot to default first (handles RecyclerView recycling)
        holder.viewDot.setBackgroundResource(R.drawable.bg_input);

        // Set colors based on category
        if (item.category == null) {
            holder.tvCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.charcoal, null));
            holder.tvCategory.setBackgroundResource(R.drawable.bg_input);
        } else {
            switch (item.category.toLowerCase()) {
                case "malicious":
                    holder.tvCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.accent_red, null));
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_badge_dangerous);
                    holder.viewDot.setBackgroundResource(R.drawable.bg_status_dangerous);
                    break;
                case "suspicious":
                    holder.tvCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.accent_orange, null));
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_badge_suspicious);
                    holder.viewDot.setBackgroundResource(R.drawable.bg_status_suspicious);
                    break;
                case "harmless":
                    holder.tvCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.accent_green, null));
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_badge_safe);
                    holder.viewDot.setBackgroundResource(R.drawable.bg_status_safe);
                    break;
                default:
                    holder.tvCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.charcoal, null));
                    holder.tvCategory.setBackgroundResource(R.drawable.bg_input);
                    holder.viewDot.setBackgroundResource(R.drawable.bg_input);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewDot;
        TextView tvEngineName, tvEngineResult, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot = itemView.findViewById(R.id.view_status_dot);
            tvEngineName = itemView.findViewById(R.id.tv_engine_name);
            tvEngineResult = itemView.findViewById(R.id.tv_engine_result);
            tvCategory = itemView.findViewById(R.id.tv_engine_category);
        }
    }
}
