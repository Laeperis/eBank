package com.foxishangxian.ebank;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.foxishangxian.ebank.data.TransferRecord;
import com.foxishangxian.ebank.data.TransferRecordDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.ui.AdminTransferAdapter;
import com.foxishangxian.ebank.ui.ToastUtil;
import android.os.AsyncTask;
import java.util.List;
import java.util.UUID;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminTransferManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminTransferAdapter adapter;
    private TransferRecordDao transferRecordDao;
    private List<TransferRecord> transferList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_EBank_Admin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_transfer_manage);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        transferRecordDao = UserDatabase.getInstance(this).transferRecordDao();
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminTransferAdapter(this, transferList);
        recyclerView.setAdapter(adapter);

        // 设置适配器回调
        adapter.setOnItemClickListener(new AdminTransferAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(TransferRecord record) {
                showEditTransferDialog(record);
            }

            @Override
            public void onDeleteClick(TransferRecord record) {
                showDeleteTransferDialog(record);
            }
        });

        fabAdd.setOnClickListener(v -> showAddTransferDialog());

        loadTransfers();
    }

    private void loadTransfers() {
        AsyncTask.execute(() -> {
            transferList = transferRecordDao.getAllTransferRecords();
            runOnUiThread(() -> {
                adapter.updateData(transferList);
            });
        });
    }

    private void showAddTransferDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_transfer_edit, null);
        TextInputEditText etFromUserId = dialogView.findViewById(R.id.et_from_user_id);
        TextInputEditText etToUserId = dialogView.findViewById(R.id.et_to_user_id);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);
        TextInputEditText etStatus = dialogView.findViewById(R.id.et_status);
        TextInputEditText etTransferTime = dialogView.findViewById(R.id.et_transfer_time);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("添加转账记录")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String fromUserId = etFromUserId.getText() == null ? "" : etFromUserId.getText().toString().trim();
                String toUserId = etToUserId.getText() == null ? "" : etToUserId.getText().toString().trim();
                String amountStr = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
                String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
                String status = etStatus.getText() == null ? "" : etStatus.getText().toString().trim();
                String transferTime = etTransferTime.getText() == null ? "" : etTransferTime.getText().toString().trim();

                if (TextUtils.isEmpty(fromUserId) || TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(amountStr)) {
                    ToastUtil.show(this, "转出用户、转入用户和金额不能为空");
                    return;
                }

                final double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    ToastUtil.show(this, "金额格式不正确");
                    return;
                }

                AsyncTask.execute(() -> {
                    TransferRecord newRecord = new TransferRecord();
                    newRecord.id = UUID.randomUUID().toString();
                    newRecord.fromUid = fromUserId;
                    newRecord.toUid = toUserId;
                    newRecord.amount = amount;
                    newRecord.description = description;
                    newRecord.status = status;
                    if (!transferTime.isEmpty()) {
                        newRecord.transferTime = transferTime;
                    } else {
                        newRecord.transferTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    }
                    newRecord.time = System.currentTimeMillis();
                    
                    transferRecordDao.insert(newRecord);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "转账记录添加成功");
                        loadTransfers();
                    });
                });
            })
            .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
    }

    private void showEditTransferDialog(TransferRecord record) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_transfer_edit, null);
        TextInputEditText etFromUserId = dialogView.findViewById(R.id.et_from_user_id);
        TextInputEditText etToUserId = dialogView.findViewById(R.id.et_to_user_id);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);
        TextInputEditText etStatus = dialogView.findViewById(R.id.et_status);
        TextInputEditText etTransferTime = dialogView.findViewById(R.id.et_transfer_time);

        // 填充现有数据
        etFromUserId.setText(record.fromUid != null ? record.fromUid : record.fromUserId);
        etToUserId.setText(record.toUid != null ? record.toUid : record.toUserId);
        etAmount.setText(String.valueOf(record.amount));
        etDescription.setText(record.description);
        etStatus.setText(record.status);
        etTransferTime.setText(record.transferTime != null ? record.transferTime : "");

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("编辑转账记录")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String fromUserId = etFromUserId.getText() == null ? "" : etFromUserId.getText().toString().trim();
                String toUserId = etToUserId.getText() == null ? "" : etToUserId.getText().toString().trim();
                String amountStr = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
                String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
                String status = etStatus.getText() == null ? "" : etStatus.getText().toString().trim();
                String transferTime = etTransferTime.getText() == null ? "" : etTransferTime.getText().toString().trim();

                if (TextUtils.isEmpty(fromUserId) || TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(amountStr)) {
                    ToastUtil.show(this, "转出用户、转入用户和金额不能为空");
                    return;
                }

                final double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    ToastUtil.show(this, "金额格式不正确");
                    return;
                }

                AsyncTask.execute(() -> {
                    record.fromUid = fromUserId;
                    record.toUid = toUserId;
                    record.amount = amount;
                    record.description = description;
                    record.status = status;
                    if (!transferTime.isEmpty()) {
                        record.transferTime = transferTime;
                    } else {
                        record.transferTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    }
                    record.time = System.currentTimeMillis();
                    
                    transferRecordDao.update(record);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "转账记录更新成功");
                        loadTransfers();
                    });
                });
            })
            .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
    }

    private void showDeleteTransferDialog(TransferRecord record) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("删除转账记录")
            .setMessage("确定要删除这条转账记录吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                AsyncTask.execute(() -> {
                    transferRecordDao.delete(record);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "转账记录删除成功");
                        loadTransfers();
                    });
                });
            })
            .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        
        // 设置按钮颜色
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple_500));
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple_500));
    }
} 