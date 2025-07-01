package com.foxishangxian.ebank.ui.card;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import java.util.List;

public class CardManageAdapter extends RecyclerView.Adapter<CardManageAdapter.CardViewHolder> {
    public interface OnCardClickListener {
        void onCardClick(BankCard card);
    }
    private List<BankCard> cardList;
    private OnCardClickListener listener;
    public CardManageAdapter(List<BankCard> cardList, OnCardClickListener listener) {
        this.cardList = cardList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_card, parent, false);
        return new CardViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        BankCard card = cardList.get(position);
        holder.tvType.setText(card.cardType);
        // 屏蔽银行卡号，只显示后4位
        String number = card.cardNumber;
        if (number != null && number.length() > 4) {
            String masked = "**** **** **** " + number.substring(number.length() - 4);
            holder.tvNumber.setText(masked);
        } else {
            holder.tvNumber.setText(number);
        }
        holder.tvBalance.setText(String.format("￥%.2f", card.balance));
        holder.ivCard.setImageResource(R.drawable.pic_card_holder); // 示例图片
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(card);
        });
    }
    @Override
    public int getItemCount() {
        return cardList.size();
    }
    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCard;
        TextView tvType, tvNumber, tvBalance;
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCard = itemView.findViewById(R.id.iv_card_img);
            tvType = itemView.findViewById(R.id.tv_card_type);
            tvNumber = itemView.findViewById(R.id.tv_card_number);
            tvBalance = itemView.findViewById(R.id.tv_card_balance);
        }
    }
} 