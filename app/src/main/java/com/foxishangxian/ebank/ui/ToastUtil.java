package com.foxishangxian.ebank.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.foxishangxian.ebank.R;
import android.view.Gravity;

public class ToastUtil {
    public static void show(Context context, String msg) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        TextView tv = view.findViewById(R.id.tvToast);
        tv.setText(msg);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, (int)(context.getResources().getDisplayMetrics().density * 120));
        toast.show();
    }
} 