package com.foxishangxian.ebank.ui.card;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.UserDatabase;
import androidx.appcompat.widget.Toolbar;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;
import android.text.InputFilter;
import android.text.InputType;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.foxishangxian.ebank.ui.PasswordInputDialogUtil;
import android.os.Handler;
import android.os.Looper;

public class CardDetailActivity extends AppCompatActivity {
    private BankCard currentCard;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        int cardId = getIntent().getIntExtra("cardId", -1);
        if (cardId != -1) {
            AsyncTask.execute(() -> {
                BankCard card = UserDatabase.getInstance(this).bankCardDao().getCardById(cardId);
                runOnUiThread(() -> {
                    currentCard = card;
                    showCardDetail(card);
                });
            });
        }
        Button btnChangePwd = findViewById(R.id.btn_change_password);
        btnChangePwd.setOnClickListener(v -> {
            if (currentCard == null) return;
            if (currentCard.password == null || currentCard.password.isEmpty()) {
                // 首次设置密码
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("该银行卡还没有密码，请先设置新密码")
                        .setPositiveButton("确定", (d, w) -> showSetNewPasswordFlow())
                        .show();
            } else {
                // 修改密码
                showChangePasswordFlow();
            }
        });
    }
    private void showCardDetail(BankCard card) {
        if (card == null) return;
        ((TextView)findViewById(R.id.tv_card_type)).setText(card.cardType);
        ((TextView)findViewById(R.id.tv_card_number)).setText(card.cardNumber);
        ((TextView)findViewById(R.id.tv_card_balance)).setText(String.format("￥%.2f", card.balance));
        ((TextView)findViewById(R.id.tv_card_start)).setText(card.startDate);
        ((TextView)findViewById(R.id.tv_card_end)).setText(card.endDate);
        ((TextView)findViewById(R.id.tv_card_limit)).setText(String.format("￥%.2f", card.limitPerDay));
        ((ImageView)findViewById(R.id.iv_card_img)).setImageResource(R.drawable.pic_card_holder);
    }

    // 首次设置密码流程
    private void showSetNewPasswordFlow() {
        PasswordInputDialogUtil.showPasswordInputDialog(this, "请输入新密码", newPwd -> {
            if (newPwd.length() != 6) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ToastUtil.show(this, "新密码必须为6位数字");
                }, 300);
                return;
            }
            PasswordInputDialogUtil.showPasswordInputDialog(this, "请再次输入新密码", confirmPwd -> {
                if (!confirmPwd.equals(newPwd)) {
                    ToastUtil.show(this, "两次输入的新密码不一致");
                } else {
                    AsyncTask.execute(() -> {
                        currentCard.password = newPwd;
                        UserDatabase.getInstance(this).bankCardDao().update(currentCard);
                        runOnUiThread(() -> ToastUtil.show(this, "密码设置成功"));
                    });
                }
            });
        });
    }

    // 修改密码流程
    private void showChangePasswordFlow() {
        PasswordInputDialogUtil.showPasswordInputDialog(this, "请输入原密码", oldPwd -> {
            if (!oldPwd.equals(currentCard.password)) {
                ToastUtil.show(this, "原密码错误");
                return;
            }
            PasswordInputDialogUtil.showPasswordInputDialog(this, "请输入新密码", newPwd -> {
                if (newPwd.length() != 6) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        ToastUtil.show(this, "新密码必须为6位数字");
                    }, 300);
                    return;
                }
                PasswordInputDialogUtil.showPasswordInputDialog(this, "请再次输入新密码", confirmPwd -> {
                    if (!confirmPwd.equals(newPwd)) {
                        ToastUtil.show(this, "两次输入的新密码不一致");
                    } else {
                        AsyncTask.execute(() -> {
                            currentCard.password = newPwd;
                            UserDatabase.getInstance(this).bankCardDao().update(currentCard);
                            runOnUiThread(() -> ToastUtil.show(this, "密码设置成功"));
                        });
                    }
                });
            });
        });
    }

    // 工具方法：创建六格EditText横向布局
    private LinearLayout createSixEditTextLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int dp12 = (int) (getResources().getDisplayMetrics().density * 12);
        int dp32 = (int) (getResources().getDisplayMetrics().density * 32);
        int dp24 = (int) (getResources().getDisplayMetrics().density * 24);
        // 设置整体padding和与上方文字的距离
        layout.setPadding(dp32, dp32, dp32, dp24);
        final EditText[] edits = new EditText[6];
        for (int i = 0; i < 6; i++) {
            EditText et = new EditText(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lp.leftMargin = (i == 0) ? 0 : dp12/2;
            lp.rightMargin = (i == 5) ? 0 : dp12/2;
            et.setLayoutParams(lp);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            et.setBackgroundResource(R.drawable.edittext_box_bg);
            edits[i] = et;
            layout.addView(et);
        }
        // 自动跳转/回退逻辑
        View.OnKeyListener keyListener = (v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                EditText et = (EditText) v;
                for (int i = 0; i < 6; i++) {
                    if (et == edits[i]) {
                        if (keyCode == android.view.KeyEvent.KEYCODE_DEL && et.getText().length() == 0 && i > 0) {
                            edits[i-1].requestFocus();
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
        layout.setTag(edits);
        return layout;
    }
} 