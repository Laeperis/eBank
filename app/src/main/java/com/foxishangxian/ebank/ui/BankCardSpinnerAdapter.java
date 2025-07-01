package com.foxishangxian.ebank.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import java.util.List;

public class BankCardSpinnerAdapter extends ArrayAdapter<BankCard> {
    private final List<BankCard> cards;
    private final Context context;
    public BankCardSpinnerAdapter(@NonNull Context context, List<BankCard> cards) {
        super(context, 0, cards);
        this.cards = cards;
        this.context = context;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }
    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bank_card_spinner, parent, false);
        }
        BankCard card = cards.get(position);
        ImageView iv = convertView.findViewById(R.id.iv_card_icon);
        TextView tvName = convertView.findViewById(R.id.tv_card_name);
        TextView tvNum = convertView.findViewById(R.id.tv_card_number);
        iv.setImageResource(R.drawable.ic_card);
        tvName.setText(card.cardType);
        tvNum.setText(maskCardNumber(card.cardNumber));
        return convertView;
    }
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return cardNumber;
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
} 