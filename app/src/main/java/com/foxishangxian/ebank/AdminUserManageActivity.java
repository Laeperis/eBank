package com.foxishangxian.ebank;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.ui.AdminUserAdapter;
import com.foxishangxian.ebank.ui.ToastUtil;
import android.os.AsyncTask;
import java.util.List;
import java.util.UUID;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class AdminUserManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private UserDao userDao;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_EBank_Admin);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_manage);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        userDao = UserDatabase.getInstance(this).userDao();
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        // 设置适配器回调
        adapter.setOnItemClickListener(new AdminUserAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(User user) {
                showEditUserDialog(user);
            }

            @Override
            public void onDeleteClick(User user) {
                showDeleteUserDialog(user);
            }
        });

        fabAdd.setOnClickListener(v -> showAddUserDialog());

        loadUsers();
    }

    private void loadUsers() {
        AsyncTask.execute(() -> {
            List<User> allUsers = userDao.getAllUsers();
            // 过滤掉root账号，只显示普通用户
            List<User> normalUsers = allUsers.stream()
                .filter(user -> !"root".equals(user.username))
                .collect(java.util.stream.Collectors.toList());
            runOnUiThread(() -> {
                adapter.updateData(normalUsers);
            });
        });
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_user_edit, null);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.til_username);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.til_password);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.til_email);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.til_phone);
        TextInputEditText etUsername = dialogView.findViewById(R.id.et_username);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("添加用户")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    ToastUtil.show(this, "用户名和密码不能为空");
                    return;
                }

                AsyncTask.execute(() -> {
                    String userCode = generateUserCode(userDao);
                    User newUser = new User(
                        UUID.randomUUID().toString(),
                        username,
                        password,
                        email,
                        phone,
                        userCode
                    );
                    userDao.insert(newUser);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "用户添加成功");
                        loadUsers();
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

    private void showEditUserDialog(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_user_edit, null);
        TextInputLayout tilUsername = dialogView.findViewById(R.id.til_username);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.til_password);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.til_email);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.til_phone);
        TextInputEditText etUid = dialogView.findViewById(R.id.et_uid);
        TextInputEditText etUsername = dialogView.findViewById(R.id.et_username);
        TextInputEditText etPassword = dialogView.findViewById(R.id.et_password);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);

        // 填充现有数据
        etUid.setText(user.uid);
        etUsername.setText(user.username);
        etPassword.setText(user.password);
        etEmail.setText(user.email);
        etPhone.setText(user.phone);

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("编辑用户")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                String uid = etUid.getText() == null ? "" : etUid.getText().toString().trim();
                String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
                String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                String phone = etPhone.getText() == null ? "" : etPhone.getText().toString().trim();

                if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    ToastUtil.show(this, "UID、用户名和密码不能为空");
                    return;
                }

                AsyncTask.execute(() -> {
                    user.uid = uid;
                    user.username = username;
                    user.password = password;
                    user.email = email;
                    user.phone = phone;
                    userDao.update(user);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "用户更新成功");
                        loadUsers();
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

    private void showDeleteUserDialog(User user) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("删除用户")
            .setMessage("确定要删除用户 " + user.username + " 吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                AsyncTask.execute(() -> {
                    userDao.delete(user);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, "用户删除成功");
                        loadUsers();
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

    private String generateUserCode(UserDao userDao) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.util.Random random = new java.util.Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (userDao.getUserByUserCode(code) != null);
        return code;
    }
} 