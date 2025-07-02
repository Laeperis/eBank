package com.foxishangxian.ebank;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.BankCardDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.ui.AdminCardAdapter;
import com.foxishangxian.ebank.ui.ToastUtil;
import android.os.AsyncTask;
import java.util.List;
import java.util.UUID;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.User;

public class AdminCardManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminCardAdapter adapter;
    private BankCardDao bankCardDao;
    private List<BankCard> cardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_EBank_Admin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_card_manage);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bankCardDao = UserDatabase.getInstance(this).bankCardDao();
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCardAdapter(this, cardList);
        recyclerView.setAdapter(adapter);

        // 设置适配器回调
        adapter.setOnItemClickListener(new AdminCardAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(BankCard card) {
                showEditCardDialog(card);
            }

            @Override
            public void onDeleteClick(BankCard card) {
                showDeleteCardDialog(card);
            }
        });

        fabAdd.setOnClickListener(v -> showAddCardDialog());

        loadCards();
    }

    private void loadCards() {
        AsyncTask.execute(() -> {
            cardList = bankCardDao.getAllCards();
            runOnUiThread(() -> {
                adapter.updateData(cardList);
            });
        });
    }

    private void showAddCardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_card_edit, null);
        TextInputLayout tilUserId = dialogView.findViewById(R.id.til_user_id);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.til_phone);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.til_password);
        TextInputLayout tilCardType = dialogView.findViewById(R.id.til_card_type);
        TextInputLayout tilCardNumber = dialogView.findViewById(R.id.til_card_number);
        TextInputLayout tilBalance = dialogView.findViewById(R.id.til_balance);
        TextInputLayout tilStartDate = dialogView.findViewById(R.id.til_start_date);
        TextInputLayout tilEndDate = dialogView.findViewById(R.id.til_end_date);
        TextInputLayout tilLimitPerDay = dialogView.findViewById(R.id.til_limit_per_day);
        
        TextInputEditText etUserId = dialogView.findViewById(R.id.et_user_id);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);
        TextInputEditText etCardType = dialogView.findViewById(R.id.et_card_type);
        TextInputEditText etCardNumber = dialogView.findViewById(R.id.et_card_number);
        TextInputEditText etBalance = dialogView.findViewById(R.id.et_balance);
        TextInputEditText etStartDate = dialogView.findViewById(R.id.et_start_date);
        TextInputEditText etEndDate = dialogView.findViewById(R.id.et_end_date);
        TextInputEditText etLimitPerDay = dialogView.findViewById(R.id.et_limit_per_day);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("添加银行卡")
            .setView(dialogView)
            .setNeutralButton("查看用户ID", (dialog, which) -> {
                // 显示所有用户的UID列表
                AsyncTask.execute(() -> {
                    UserDao userDao = UserDatabase.getInstance(this).userDao();
                    List<User> allUsers = userDao.getAllUsers();
                    StringBuilder userList = new StringBuilder();
                    userList.append("所有用户ID列表：\n\n");
                    for (User user : allUsers) {
                        userList.append("用户名: ").append(user.username)
                               .append(" | UID: ").append(user.uid)
                               .append(" | 手机: ").append(user.phone != null ? user.phone : "无")
                               .append("\n");
                    }
                    final String userListStr = userList.toString();
                    runOnUiThread(() -> {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("用户ID列表")
                            .setMessage(userListStr)
                            .setPositiveButton("确定", null)
                            .show();
                    });
                });
            })
            .setPositiveButton("确定", (dialog, which) -> {
                String userId = etUserId.getText() == null ? "" : etUserId.getText().toString().trim();
                String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                String cardType = etCardType.getText() == null ? "" : etCardType.getText().toString().trim();
                String cardNumber = etCardNumber.getText() == null ? "" : etCardNumber.getText().toString().trim();
                String balanceStr = etBalance.getText() == null ? "" : etBalance.getText().toString().trim();
                String startDate = etStartDate.getText() == null ? "" : etStartDate.getText().toString().trim();
                String endDate = etEndDate.getText() == null ? "" : etEndDate.getText().toString().trim();
                String limitPerDayStr = etLimitPerDay.getText() == null ? "" : etLimitPerDay.getText().toString().trim();

                if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(cardNumber)) {
                    ToastUtil.show(this, "用户ID和卡号不能为空");
                    return;
                }

                final double balance;
                final double limitPerDay;
                try {
                    balance = TextUtils.isEmpty(balanceStr) ? 0 : Double.parseDouble(balanceStr);
                    limitPerDay = TextUtils.isEmpty(limitPerDayStr) ? 0 : Double.parseDouble(limitPerDayStr);
                } catch (NumberFormatException e) {
                    ToastUtil.show(this, "余额或限额格式不正确");
                    return;
                }

                AsyncTask.execute(() -> {
                    // 验证用户ID是否存在
                    UserDao userDao = UserDatabase.getInstance(this).userDao();
                    User user = userDao.getUserByUid(userId);
                    if (user == null) {
                        runOnUiThread(() -> {
                            ToastUtil.show(this, "用户ID不存在，请检查用户ID是否正确");
                        });
                        return;
                    }
                    
                    BankCard newCard = new BankCard();
                    newCard.id = UUID.randomUUID().toString();
                    newCard.userId = userId;
                    newCard.phone = phone;
                    newCard.password = password;
                    newCard.cardType = cardType;
                    newCard.cardNumber = cardNumber;
                    newCard.balance = balance;
                    newCard.startDate = startDate;
                    newCard.endDate = endDate;
                    newCard.limitPerDay = limitPerDay;
                    
                    bankCardDao.insert(newCard);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "银行卡添加成功");
                        loadCards();
                    });
                });
            })
            .setNegativeButton("取消", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.purple_500));
    }

    private void showEditCardDialog(BankCard card) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_card_edit, null);
        TextInputEditText etUserId = dialogView.findViewById(R.id.et_user_id);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);
        TextInputEditText etCardType = dialogView.findViewById(R.id.et_card_type);
        TextInputEditText etCardNumber = dialogView.findViewById(R.id.et_card_number);
        TextInputEditText etBalance = dialogView.findViewById(R.id.et_balance);
        TextInputEditText etStartDate = dialogView.findViewById(R.id.et_start_date);
        TextInputEditText etEndDate = dialogView.findViewById(R.id.et_end_date);
        TextInputEditText etLimitPerDay = dialogView.findViewById(R.id.et_limit_per_day);

        // 填充现有数据
        etUserId.setText(card.userId);
        etPhone.setText(card.phone);
        etPassword.setText(card.password);
        etCardType.setText(card.cardType);
        etCardNumber.setText(card.cardNumber);
        etBalance.setText(String.valueOf(card.balance));
        etStartDate.setText(card.startDate);
        etEndDate.setText(card.endDate);
        etLimitPerDay.setText(String.valueOf(card.limitPerDay));

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("编辑银行卡")
            .setView(dialogView)
            .setNeutralButton("查看用户ID", (dialog, which) -> {
                // 显示所有用户的UID列表
                AsyncTask.execute(() -> {
                    UserDao userDao = UserDatabase.getInstance(this).userDao();
                    List<User> allUsers = userDao.getAllUsers();
                    StringBuilder userList = new StringBuilder();
                    userList.append("所有用户ID列表：\n\n");
                    for (User user : allUsers) {
                        userList.append("用户名: ").append(user.username)
                               .append(" | UID: ").append(user.uid)
                               .append(" | 手机: ").append(user.phone != null ? user.phone : "无")
                               .append("\n");
                    }
                    final String userListStr = userList.toString();
                    runOnUiThread(() -> {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("用户ID列表")
                            .setMessage(userListStr)
                            .setPositiveButton("确定", null)
                            .show();
                    });
                });
            })
            .setPositiveButton("确定", (dialog, which) -> {
                String userId = etUserId.getText() == null ? "" : etUserId.getText().toString().trim();
                String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();
                String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                String cardType = etCardType.getText() == null ? "" : etCardType.getText().toString().trim();
                String cardNumber = etCardNumber.getText() == null ? "" : etCardNumber.getText().toString().trim();
                String balanceStr = etBalance.getText() == null ? "" : etBalance.getText().toString().trim();
                String startDate = etStartDate.getText() == null ? "" : etStartDate.getText().toString().trim();
                String endDate = etEndDate.getText() == null ? "" : etEndDate.getText().toString().trim();
                String limitPerDayStr = etLimitPerDay.getText() == null ? "" : etLimitPerDay.getText().toString().trim();

                if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(cardNumber)) {
                    ToastUtil.show(this, "用户ID和卡号不能为空");
                    return;
                }

                final double balance;
                final double limitPerDay;
                try {
                    balance = TextUtils.isEmpty(balanceStr) ? 0 : Double.parseDouble(balanceStr);
                    limitPerDay = TextUtils.isEmpty(limitPerDayStr) ? 0 : Double.parseDouble(limitPerDayStr);
                } catch (NumberFormatException e) {
                    ToastUtil.show(this, "余额或限额格式不正确");
                    return;
                }

                AsyncTask.execute(() -> {
                    // 验证用户ID是否存在
                    UserDao userDao = UserDatabase.getInstance(this).userDao();
                    User user = userDao.getUserByUid(userId);
                    if (user == null) {
                        runOnUiThread(() -> {
                            ToastUtil.show(this, "用户ID不存在，请检查用户ID是否正确");
                        });
                        return;
                    }
                    
                    card.userId = userId;
                    card.phone = phone;
                    card.password = password;
                    card.cardType = cardType;
                    card.cardNumber = cardNumber;
                    card.balance = balance;
                    card.startDate = startDate;
                    card.endDate = endDate;
                    card.limitPerDay = limitPerDay;
                    
                    bankCardDao.update(card);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "银行卡更新成功");
                        loadCards();
                    });
                });
            })
            .setNegativeButton("取消", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
    }

    private void showDeleteCardDialog(BankCard card) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("删除银行卡")
            .setMessage("确定要删除银行卡 " + card.cardNumber + " 吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                AsyncTask.execute(() -> {
                    bankCardDao.delete(card);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "银行卡删除成功");
                        loadCards();
                    });
                });
            })
            .setNegativeButton("取消", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
    }
} 