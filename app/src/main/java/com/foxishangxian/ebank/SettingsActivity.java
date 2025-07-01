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

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

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

        // 设置侧边栏点击事件
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                // 已经在设置页面，无需跳转
                binding.drawerLayout.closeDrawers();
                return true;
            }
            return false;
        });

        // 退出登录按钮
        findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 