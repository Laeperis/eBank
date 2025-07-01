package com.foxishangxian.ebank.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.GoldPrice;
import java.util.List;

public class GoldPriceAdapter extends RecyclerView.Adapter<GoldPriceAdapter.GoldPriceViewHolder> {
    private List<GoldPrice> goldPrices;

    public GoldPriceAdapter(List<GoldPrice> goldPrices) {
        this.goldPrices = goldPrices;
    }

    @NonNull
    @Override
    public GoldPriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gold_price, parent, false);
        return new GoldPriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoldPriceViewHolder holder, int position) {
        GoldPrice goldPrice = goldPrices.get(position);
        holder.bind(goldPrice);
    }

    @Override
    public int getItemCount() {
        return goldPrices.size();
    }

    public void updateData(List<GoldPrice> newGoldPrices) {
        this.goldPrices = newGoldPrices;
        notifyDataSetChanged();
    }

    static class GoldPriceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvPrice;
        private TextView tvChangePercent;
        private TextView tvTime;

        public GoldPriceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_gold_name);
            tvPrice = itemView.findViewById(R.id.tv_gold_price);
            tvChangePercent = itemView.findViewById(R.id.tv_gold_change_percent);
            tvTime = itemView.findViewById(R.id.tv_gold_time);
        }

        public void bind(GoldPrice goldPrice) {
            tvName.setText(goldPrice.typename);
            tvPrice.setText("￥" + goldPrice.price);
            tvChangePercent.setText(goldPrice.changepercent);
            tvTime.setText(goldPrice.updatetime);
            // 涨跌幅颜色
            if (goldPrice.changepercent.startsWith("+")) {
                tvChangePercent.setTextColor(itemView.getContext().getColor(R.color.teal_200));
            } else if (goldPrice.changepercent.startsWith("-")) {
                tvChangePercent.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_light));
            } else {
                tvChangePercent.setTextColor(itemView.getContext().getColor(R.color.black));
            }
        }
    }
} 