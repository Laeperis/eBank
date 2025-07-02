package com.foxishangxian.ebank.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AdminCardAdapter extends RecyclerView.Adapter<AdminCardAdapter.ViewHolder> {
    private Context context;
    private List<BankCard> cardList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(BankCard card);
        void onDeleteClick(BankCard card);
    }

    public AdminCardAdapter(Context context, List<BankCard> cardList) {
        this.context = context;
        this.cardList = cardList != null ? cardList : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<BankCard> newCardList) {
        this.cardList = newCardList != null ? newCardList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BankCard card = cardList.get(position);
        
        // 显示卡号（只显示后4位）
        String cardNumber = card.cardNumber != null ? card.cardNumber : "";
        String maskedCardNumber = cardNumber.length() > 4 ? 
            "****" + cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        holder.tvCardNumber.setText("卡号: " + maskedCardNumber);
        
        holder.tvUserId.setText("用户ID: " + (card.userId != null ? card.userId : ""));
        holder.tvCardType.setText("卡类型: " + (card.cardType != null ? card.cardType : ""));
        holder.tvBalance.setText("余额: ¥" + String.format("%.2f", card.balance));
        holder.tvPhone.setText("手机号: " + (card.phone != null ? card.phone : ""));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(card);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNumber, tvUserId, tvCardType, tvBalance, tvPhone;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            tvCardType = itemView.findViewById(R.id.tv_card_type);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
} 