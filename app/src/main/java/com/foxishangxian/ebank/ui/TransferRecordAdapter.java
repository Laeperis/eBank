package com.foxishangxian.ebank.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import java.util.*;

public class TransferRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SECTION = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOAD_MORE = 2;
    private static final int TYPE_NO_MORE = 3;
    private List<RecordDisplayItem> data = new ArrayList<>();
    private Map<String, String> cardUserNameMap = new HashMap<>();
    private boolean hasMore = true;

    public void setData(List<RecordDisplayItem> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void setCardUserNameMap(Map<String, String> map) { this.cardUserNameMap = map; }

    public void setHasMore(boolean more) { hasMore = more; }

    public void appendData(List<RecordDisplayItem> list) {
        int pos = data.size();
        if (list != null) data.addAll(list);
        notifyItemRangeInserted(pos, list == null ? 0 : list.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= data.size()) {
            return hasMore ? TYPE_LOAD_MORE : TYPE_NO_MORE;
        }
        return data.get(position).isSection ? TYPE_SECTION : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_record_section, parent, false);
            return new SectionHolder(v);
        } else if (viewType == TYPE_LOAD_MORE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_more, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        } else if (viewType == TYPE_NO_MORE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_more, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transfer_record, parent, false);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_LOAD_MORE || viewType == TYPE_NO_MORE) return;
        
        if (position >= data.size()) return; // 防止数组越界
        
        RecordDisplayItem item = data.get(position);
        if (item.isSection) {
            ((SectionHolder) holder).tvSection.setText(item.sectionTitle);
        } else {
            ItemHolder h = (ItemHolder) holder;
            h.tvAmount.setText((item.isIncome ? "+" : "-") + String.format("%.2f", item.amount));
            h.tvAmount.setTextColor(item.isIncome ? Color.parseColor("#E53935") : Color.parseColor("#388E3C"));
            h.tvTime.setText(item.time);
            String userName = cardUserNameMap.getOrDefault(item.targetCard, item.targetCard);
            h.tvTarget.setText(item.isIncome ? ("来自：" + userName) : ("转给：" + userName));
            h.ivIcon.setImageResource(item.isIncome ? R.drawable.ic_recharge : R.drawable.ic_log);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1; // 总是显示一个额外的item（加载中或没有更多）
    }

    static class SectionHolder extends RecyclerView.ViewHolder {
        TextView tvSection;
        SectionHolder(View v) {
            super(v);
            tvSection = v.findViewById(R.id.tv_section);
        }
    }
    static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvAmount, tvTime, tvTarget;
        ItemHolder(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvTime = v.findViewById(R.id.tv_time);
            tvTarget = v.findViewById(R.id.tv_target);
        }
    }

    // 展示用数据结构
    public static class RecordDisplayItem {
        public boolean isSection;
        public String sectionTitle;
        public boolean isIncome;
        public double amount;
        public String time;
        public String targetName;
        public String targetCard;
        public RecordDisplayItem(String sectionTitle) {
            this.isSection = true;
            this.sectionTitle = sectionTitle;
        }
        public RecordDisplayItem(boolean isIncome, double amount, String time, String targetName, String targetCard) {
            this.isSection = false;
            this.isIncome = isIncome;
            this.amount = amount;
            this.time = time;
            this.targetName = targetName;
            this.targetCard = targetCard;
        }
    }
} 