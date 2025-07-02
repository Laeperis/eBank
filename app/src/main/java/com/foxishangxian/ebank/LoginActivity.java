package com.foxishangxian.ebank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.os.AsyncTask;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import java.util.List;
import java.util.UUID;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.foxishangxian.ebank.ui.AccountDropdownAdapter;
import com.foxishangxian.ebank.ui.ToastUtil;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.activity.OnBackPressedCallback;

public class LoginActivity extends AppCompatActivity {
    private UserDao userDao;
    private EditText etAccount, etPassword;
    private Button btnLogin, btnToRegister;
    private Spinner spinnerAccountList;
    private long lastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = UserDatabase.getInstance(this).userDao();
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);
        spinnerAccountList = findViewById(R.id.spinnerAccountList);

        // 创建默认管理员账户
        createDefaultAdmin();

        // 加载历史账号
        AsyncTask.execute(() -> {
            List<User> allUsers = userDao.getAllUsers();
            // 过滤掉管理员账户，只显示普通用户
            List<User> normalUsers = allUsers.stream()
                .filter(user -> !user.isAdmin)
                .collect(java.util.stream.Collectors.toList());
            runOnUiThread(() -> {
                AccountDropdownAdapter adapter = new AccountDropdownAdapter(this, normalUsers);
                spinnerAccountList.setAdapter(adapter);
                spinnerAccountList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        User user = adapter.getItem(position);
                        if (user.username != null && !user.username.isEmpty()) {
                            etAccount.setText(user.username);
                        } else if (user.phone != null && !user.phone.isEmpty()) {
                            etAccount.setText(user.phone);
                        } else if (user.email != null && !user.email.isEmpty()) {
                            etAccount.setText(user.email);
                        } else {
                            etAccount.setText("");
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });

        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (account.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, getString(R.string.empty_account_or_password));
                return;
            }
            AsyncTask.execute(() -> {
                User user = null;
                if (account.contains("@")) {
                    user = userDao.getUserByEmail(account);
                } else if (account.matches("^1[3-9]\\d{9}$")) {
                    user = userDao.getUserByPhone(account);
                } else {
                    user = userDao.getUserByUsername(account);
                }
                if (user != null && user.password.equals(password)) {
                    userDao.logoutAll();
                    user.isLoggedIn = true;
                    userDao.update(user);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, getString(R.string.login_success));
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> ToastUtil.show(this, getString(R.string.login_failed)));
                }
            });
        });

        btnToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        // 设置双击返回键退出
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long now = System.currentTimeMillis();
                if (now - lastBackPressedTime < 2000) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    ToastUtil.show(LoginActivity.this, getString(R.string.press_again_exit));
                    lastBackPressedTime = now;
                }
            }
        });
    }

    private void createDefaultAdmin() {
        AsyncTask.execute(() -> {
            // 检查是否已存在管理员账户
            User adminUser = userDao.getUserByUsername("root");
            if (adminUser == null) {
                // 创建默认管理员账户
                User admin = new User(
                    UUID.randomUUID().toString(),
                    "root",
                    "root",
                    "admin@ebank.com",
                    "13800000000",
                    "ADMIN001"
                );
                admin.isAdmin = true;
                userDao.insert(admin);
            }
        });
    }
} 