package com.foxishangxian.ebank.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDatabase;
import java.util.List;
import android.os.AsyncTask;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

public class TransferActivity extends AppCompatActivity {
    private Spinner spinnerCard;
    private EditText etTargetCard;
    private EditText etAmount;
    private Button btnConfirm;
    private List<BankCard> cardList;
    private BankCard selectedCard;
    private TextView tvCardBalance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        spinnerCard = findViewById(R.id.spinner_card);
        etTargetCard = findViewById(R.id.et_target_card);
        etAmount = findViewById(R.id.et_amount);
        btnConfirm = findViewById(R.id.btn_confirm_transfer);
        tvCardBalance = findViewById(R.id.tv_card_balance);

        AsyncTask.execute(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            User user = db.userDao().getLoggedInUser();
            if (user != null) {
                List<BankCard> cards = db.bankCardDao().getCardsByUserId(user.uid);
                if (cards == null || cards.isEmpty()) {
                    cards = db.bankCardDao().getCardsByPhone(user.phone);
                }
                cardList = cards;
                runOnUiThread(() -> {
                    spinnerCard.setAdapter(new BankCardSpinnerAdapter(this, cardList));
                    if (!cardList.isEmpty()) {
                        selectedCard = cardList.get(0);
                        updateCardBalance();
                    }
                });
            }
        });

        spinnerCard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCard = cardList.get(position);
                updateCardBalance();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnConfirm.setOnClickListener(v -> {
            String targetCard = etTargetCard.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            if (selectedCard == null) {
                ToastUtil.show(this, "请选择银行卡");
                return;
            }
            if (targetCard.isEmpty()) {
                ToastUtil.show(this, "请输入目标银行卡号");
                return;
            }
            if (amountStr.isEmpty()) {
                ToastUtil.show(this, "请输入转账金额");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (Exception e) {
                ToastUtil.show(this, "金额格式错误");
                return;
            }
            if (amount <= 0) {
                ToastUtil.show(this, "金额必须大于0");
                return;
            }
            if (selectedCard.balance < amount) {
                ToastUtil.show(this, "余额不足");
                return;
            }
            AsyncTask.execute(() -> {
                UserDatabase db = UserDatabase.getInstance(this);
                com.foxishangxian.ebank.data.BankCard toCard = db.bankCardDao().getCardByNumber(targetCard);
                runOnUiThread(() -> {
                    if (toCard == null) {
                        ToastUtil.show(this, "目标银行卡号不存在");
                        return;
                    }
                    if (selectedCard.password == null || selectedCard.password.isEmpty()) {
                        ToastUtil.show(this, "请先去卡片管理页面设置密码");
                        return;
                    }
                    PasswordInputDialogUtil.showPasswordInputDialog(this, "请输入支付密码", pwd1 -> {
                        PasswordInputDialogUtil.showPasswordInputDialog(this, "请再次输入支付密码", pwd2 -> {
                            if (!pwd1.equals(pwd2)) {
                                ToastUtil.show(this, "两次密码输入不一致");
                                return;
                            }
                            if (!selectedCard.password.equals(pwd1)) {
                                ToastUtil.show(this, "密码错误");
                                return;
                            }
                            AsyncTask.execute(() -> {
                                selectedCard.balance -= amount;
                                db.bankCardDao().update(selectedCard);
                                toCard.balance += amount;
                                db.bankCardDao().update(toCard);
                                com.foxishangxian.ebank.data.TransferRecord record = new com.foxishangxian.ebank.data.TransferRecord();
                                record.fromCard = selectedCard.cardNumber;
                                record.toCard = targetCard;
                                record.time = System.currentTimeMillis();
                                record.fromUid = selectedCard.userId;
                                record.toUid = toCard.userId;
                                record.amount = amount;
                                db.transferRecordDao().insert(record);
                                runOnUiThread(() -> {
                                    ToastUtil.show(this, "转账成功");
                                    finish();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private void updateCardBalance() {
        if (selectedCard != null) {
            tvCardBalance.setText("余额：￥" + String.format("%.2f", selectedCard.balance));
        }
    }
} 