package com.foxishangxian.ebank.ui;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.User;
import android.graphics.drawable.GradientDrawable;
import java.util.List;
import com.bumptech.glide.Glide;

public class AccountDropdownAdapter extends ArrayAdapter<User> {
    private final LayoutInflater inflater;
    public AccountDropdownAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, 0, users);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.account_dropdown_item, parent, false);
        }
        User user = getItem(position);
        ImageView ivAvatar = convertView.findViewById(R.id.ivAvatar);
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvPhone = convertView.findViewById(R.id.tvPhone);
        if (user.avatarUri != null) {
            Glide.with(getContext()).load(Uri.parse(user.avatarUri)).circleCrop().into(ivAvatar);
        } else {
            Glide.with(getContext()).load(R.drawable.ic_avatar_circle_bg).circleCrop().into(ivAvatar);
        }
        // 动态设置圆形背景
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(0xFFE3F2FD);
        ivAvatar.setBackground(shape);
        tvUsername.setText(user.username);
        tvPhone.setText(user.userCode == null ? "" : user.userCode);
        return convertView;
    }
} 