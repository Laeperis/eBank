package com.foxishangxian.ebank;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.AsyncTask;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.ui.ToastUtil;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.foxishangxian.ebank.databinding.ActivitySettingsBinding;
import android.app.AlertDialog;
import android.widget.EditText;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import android.view.View;

// SettingsActivity：设置界面Activity
// 负责展示和管理用户的设置，包括手机号、邮箱、密码修改、退出登录、管理员功能入口等
// 支持普通用户和管理员用户的不同设置项显示，涉及权限判断、信息脱敏、弹窗输入等常见场景
public class SettingsActivity extends AppCompatActivity {
    // 视图绑定对象，绑定activity_settings.xml布局
    private ActivitySettingsBinding binding;
    // 当前用户是否为管理员
    private boolean isAdmin = false;

    /**
     * Activity创建时回调，初始化界面、事件、加载用户信息
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置Toolbar
        setSupportActionBar(binding.appBarSettings.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }

        // 设置侧边栏点击事件，防止重复跳转
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                // 已经在设置页面，无需跳转
                binding.drawerLayout.closeDrawers();
                return true;
            }
            return false;
        });

        // 延迟加载用户信息，避免界面未初始化完成
        new android.os.Handler().postDelayed(this::refreshUserInfo, 100);
        // 设置手机号、邮箱、密码修改点击事件
        findViewById(R.id.layout_change_phone).setOnClickListener(v -> showChangeDialog("修改手机号", "请输入新手机号", "phone"));
        findViewById(R.id.layout_change_email).setOnClickListener(v -> showChangeDialog("修改邮箱", "请输入新邮箱", "email"));
        findViewById(R.id.layout_change_password).setOnClickListener(v -> showChangeDialog("修改密码", "请输入新密码", "password"));
        // 退出登录按钮
        findViewById(R.id.btn_logout).setOnClickListener(v -> logout());

        // 管理员功能入口点击事件
        findViewById(R.id.layout_manage_users).setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(this, AdminUserManageActivity.class));
            }
        });
        findViewById(R.id.layout_manage_cards).setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(this, AdminCardManageActivity.class));
            }
        });
        findViewById(R.id.layout_manage_transfers).setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(this, AdminTransferManageActivity.class));
            }
        });
    }

    /**
     * 刷新并显示当前用户信息，包括手机号、邮箱、密码（脱敏）、管理员选项
     */
    private void refreshUserInfo() {
        AsyncTask.execute(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            UserDao userDao = db.userDao();
            com.foxishangxian.ebank.data.User user = userDao.getLoggedInUser();
            if (user == null) return;
            String phone = user.phone;
            String email = user.email;
            String phoneMasked = maskPhone(phone);
            String emailMasked = maskEmail(email);
            boolean admin = user.isAdmin;
            runOnUiThread(() -> {
                ((android.widget.TextView)findViewById(R.id.tv_value_phone)).setText(phoneMasked);
                ((android.widget.TextView)findViewById(R.id.tv_value_email)).setText(emailMasked);
                ((android.widget.TextView)findViewById(R.id.tv_value_password)).setText("******");
                
                // 显示或隐藏管理员选项
                View adminSection = findViewById(R.id.admin_section);
                if (admin) {
                    adminSection.setVisibility(View.VISIBLE);
                    isAdmin = true;
                } else {
                    adminSection.setVisibility(View.GONE);
                    isAdmin = false;
                }
            });
        });
    }

    /**
     * 手机号脱敏显示，中间四位用*号代替
     */
    private String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 7) return phone;
        return phone.substring(0,3) + "****" + phone.substring(phone.length()-4);
    }

    /**
     * 邮箱脱敏显示，@前只保留前两位
     */
    private String maskEmail(String email) {
        if (TextUtils.isEmpty(email) || !email.contains("@")) return email;
        int at = email.indexOf('@');
        if (at <= 2) return "****" + email.substring(at);
        return email.substring(0,2) + "****" + email.substring(at);
    }

    /**
     * 退出登录，清除本地登录状态并跳转到登录页
     */
    private void logout() {
        AsyncTask.execute(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            UserDao userDao = db.userDao();
            userDao.logoutAll();
            runOnUiThread(() -> {
                ToastUtil.show(this, "已退出账号");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        });
    }

    /**
     * 弹出信息修改对话框，支持手机号、邮箱、密码修改
     * @param title 对话框标题
     * @param hint 输入框提示
     * @param type 修改类型（phone/email/password）
     */
    private void showChangeDialog(String title, String hint, String type) {
        if ("password".equals(type)) {
            // 密码两次确认对话框
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
            android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
            TextInputLayout til1 = dialogView.findViewById(R.id.til_password1);
            TextInputLayout til2 = dialogView.findViewById(R.id.til_password2);
            TextInputEditText et1 = dialogView.findViewById(R.id.et_password1);
            TextInputEditText et2 = dialogView.findViewById(R.id.et_password2);
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("确定", (d, w) -> {
                    // 获取两次输入的密码
                    String p1 = et1.getText() == null ? "" : et1.getText().toString().trim();
                    String p2 = et2.getText() == null ? "" : et2.getText().toString().trim();
                    // 校验输入不能为空
                    if (p1.isEmpty() || p2.isEmpty()) {
                        ToastUtil.show(this, "输入不能为空"); return;
                    }
                    // 校验两次输入一致
                    if (!p1.equals(p2)) {
                        ToastUtil.show(this, "两次密码不一致"); return;
                    }
                    // 异步保存新密码到数据库
                    AsyncTask.execute(() -> {
                        UserDatabase db = UserDatabase.getInstance(this);
                        UserDao userDao = db.userDao();
                        com.foxishangxian.ebank.data.User user = userDao.getLoggedInUser();
                        if (user == null) return;
                        user.password = p1;
                        userDao.update(user);
                        runOnUiThread(() -> { ToastUtil.show(this, "修改成功"); refreshUserInfo(); });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
        } else {
            // 手机号/邮箱单项修改对话框
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
            android.view.View dialogView = inflater.inflate(R.layout.dialog_change_single, null);
            com.google.android.material.textfield.TextInputLayout til = dialogView.findViewById(R.id.til_single);
            com.google.android.material.textfield.TextInputEditText et = dialogView.findViewById(R.id.et_single);
            til.setHint(hint);
            // 设置输入类型
            if ("phone".equals(type)) et.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            if ("email".equals(type)) et.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("确定", (d, w) -> {
                    // 获取输入内容
                    String value = et.getText() == null ? "" : et.getText().toString().trim();
                    // 校验输入不能为空
                    if (value.isEmpty()) {
                        ToastUtil.show(this, "输入不能为空"); return;
                    }
                    // 手机号格式校验
                    if ("phone".equals(type) && !value.matches("^1[3-9]\\d{9}$")) {
                        ToastUtil.show(this, "手机号格式不正确"); return;
                    }
                    // 邮箱格式校验
                    if ("email".equals(type) && !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                        ToastUtil.show(this, "邮箱格式不正确"); return;
                    }
                    // 异步保存新手机号/邮箱到数据库
                    AsyncTask.execute(() -> {
                        UserDatabase db = UserDatabase.getInstance(this);
                        UserDao userDao = db.userDao();
                        com.foxishangxian.ebank.data.User user = userDao.getLoggedInUser();
                        if (user == null) return;
                        if ("phone".equals(type)) user.phone = value;
                        else if ("email".equals(type)) user.email = value;
                        userDao.update(user);
                        runOnUiThread(() -> { ToastUtil.show(this, "修改成功"); refreshUserInfo(); });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
        }
    }

    /**
     * 处理Toolbar返回按钮点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 