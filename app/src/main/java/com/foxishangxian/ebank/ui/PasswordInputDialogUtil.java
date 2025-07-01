package com.foxishangxian.ebank.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;
import android.content.DialogInterface;

public class PasswordInputDialogUtil {
    public interface OnPasswordInputListener {
        void onPasswordInput(String password);
    }

    public static void showPasswordInputDialog(Context context, String title, final OnPasswordInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int dp12 = (int) (context.getResources().getDisplayMetrics().density * 12);
        int dp32 = (int) (context.getResources().getDisplayMetrics().density * 32);
        int dp24 = (int) (context.getResources().getDisplayMetrics().density * 24);
        layout.setPadding(dp32, dp32, dp32, dp24);
        final EditText[] edits = new EditText[6];
        for (int i = 0; i < 6; i++) {
            EditText et = new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lp.leftMargin = (i == 0) ? 0 : dp12/2;
            lp.rightMargin = (i == 5) ? 0 : dp12/2;
            et.setLayoutParams(lp);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            et.setBackgroundResource(com.foxishangxian.ebank.R.drawable.edittext_box_bg);
            edits[i] = et;
            layout.addView(et);
        }
        // 自动跳转/回退逻辑
        View.OnKeyListener keyListener = (v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                EditText et = (EditText) v;
                for (int i = 0; i < 6; i++) {
                    if (et == edits[i]) {
                        if (keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                            if (et.getText().length() == 0 && i > 0) {
                                edits[i-1].requestFocus();
                                edits[i-1].setText("");
                                edits[i-1].setSelection(0);
                            } else {
                                et.setText("");
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        };
        for (EditText et : edits) et.setOnKeyListener(keyListener);
        // 输入后自动跳转
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            edits[i].addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (s.length() == 1 && idx < 5) edits[idx+1].requestFocus();
                }
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
        builder.setView(layout);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", null); // 先不设置回调，后面手动处理
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            final android.widget.Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setEnabled(false);
            // 监听输入变化，动态设置按钮可用
            android.text.TextWatcher watcher = new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int b, int c) {
                    boolean full = true;
                    for (EditText et : edits) {
                        if (et.getText().toString().isEmpty()) {
                            full = false;
                            break;
                        }
                    }
                    positiveBtn.setEnabled(full);
                }
                public void afterTextChanged(android.text.Editable s) {}
            };
            for (EditText et : edits) et.addTextChangedListener(watcher);
            // 设置点击事件
            positiveBtn.setOnClickListener(v -> {
                StringBuilder pwd = new StringBuilder();
                for (EditText et : edits) pwd.append(et.getText().toString());
                if (listener != null) listener.onPasswordInput(pwd.toString());
                dialog.dismiss();
            });
        });
        dialog.show();
        // 自动弹出输入法
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            edits[0].requestFocus();
        }, 300);
    }
} 