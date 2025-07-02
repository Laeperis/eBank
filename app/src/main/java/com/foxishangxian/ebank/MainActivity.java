package com.foxishangxian.ebank;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.foxishangxian.ebank.databinding.ActivityMainBinding;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDatabase;
import com.bumptech.glide.Glide;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.TransferRecord;
import com.foxishangxian.ebank.data.TransferRecordDao;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private long lastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 主题切换：必须在super.onCreate前
        SharedPreferences sp = getSharedPreferences("theme", MODE_PRIVATE);
        String theme = sp.getString("theme", "default");
        if ("red".equals(theme)) setTheme(R.style.Theme_EBank_Red);
        super.onCreate(savedInstanceState);

        // 设置双击返回键退出
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    ToastUtil.show(MainActivity.this, "再按一次退出应用");
                    lastBackPressedTime = System.currentTimeMillis();
                }
            }
        });

        // 自动登录判断（异步）
        AsyncTask.execute(() -> {
            UserDatabase db = UserDatabase.getInstance(this);
            User loggedInUser = db.userDao().getLoggedInUser();
            runOnUiThread(() -> {
                if (loggedInUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    // 只有已登录用户时才初始化主界面
                    binding = ActivityMainBinding.inflate(getLayoutInflater());
                    setContentView(binding.getRoot());

                    setSupportActionBar(binding.appBarMain.toolbar);
                    DrawerLayout drawer = binding.drawerLayout;
                    NavigationView navigationView = binding.navView;
                    mAppBarConfiguration = new AppBarConfiguration.Builder(
                            R.id.nav_home, R.id.nav_wealth, R.id.nav_mine)
                            .setOpenableLayout(drawer)
                            .build();
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
                    NavigationUI.setupWithNavController(navigationView, navController);

                    // 设置底部导航栏
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
                    NavigationUI.setupWithNavController(bottomNav, navController);

                    // 设置侧边栏点击事件
                    navigationView.setNavigationItemSelectedListener(item -> {
                        if (item.getItemId() == R.id.nav_settings) {
                            startActivity(new Intent(this, SettingsActivity.class));
                            drawer.closeDrawers();
                            return true;
                        }
                        if (item.getItemId() == R.id.nav_debug_add_500_money) {
                            AsyncTask.execute(() -> {
                                User user = db.userDao().getLoggedInUser();
                                if (user != null) {
                                    java.util.List<BankCard> cards = db.bankCardDao().getCardsByUserId(user.uid);
                                    if (cards == null || cards.isEmpty()) {
                                        cards = db.bankCardDao().getCardsByPhone(user.phone);
                                    }
                                    if (cards != null && !cards.isEmpty()) {
                                        for (BankCard card : cards) {
                                            card.balance += 500;
                                            db.bankCardDao().update(card);
                                        }
                                        runOnUiThread(() -> {
                                            ToastUtil.show(this, "已为你所有银行卡加500元！");
                                            drawer.closeDrawers();
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            ToastUtil.show(this, "未找到银行卡！");
                                            drawer.closeDrawers();
                                        });
                                    }
                                } else {
                                    runOnUiThread(() -> {
                                        ToastUtil.show(this, "未登录用户！");
                                        drawer.closeDrawers();
                                    });
                                }
                            });
                            return true;
                        }
                        if (item.getItemId() == R.id.nav_debug_add_500_records) {
                            AsyncTask.execute(() -> {
                                java.util.List<BankCard> cards = db.bankCardDao().getAllCards();
                                if (cards != null && cards.size() >= 2) {
                                    java.util.Random random = new java.util.Random();
                                    TransferRecordDao recordDao = db.transferRecordDao();
                                    long startTime = 1672502400000L; // 2023-01-01 00:00:00
                                    long endTime = 1751990400000L;   // 2025-07-01 00:00:00
                                    for (int i = 0; i < 500; i++) {
                                        int fromIdx = random.nextInt(cards.size());
                                        int toIdx = random.nextInt(cards.size());
                                        while (toIdx == fromIdx) toIdx = random.nextInt(cards.size());
                                        BankCard fromCardObj = cards.get(fromIdx);
                                        BankCard toCardObj = cards.get(toIdx);
                                        String fromCard = fromCardObj.cardNumber;
                                        String toCard = toCardObj.cardNumber;
                                        double amount = 1 + (9999 - 1) * random.nextDouble();
                                        long time = startTime + (long) (random.nextDouble() * (endTime - startTime));
                                        TransferRecord record = new TransferRecord();
                                        record.id = UUID.randomUUID().toString();
                                        record.fromCard = fromCard;
                                        record.toCard = toCard;
                                        record.amount = Math.round(amount * 100.0) / 100.0;
                                        record.time = time;
                                        record.fromUid = fromCardObj.userId;
                                        record.toUid = toCardObj.userId;
                                        recordDao.insert(record);
                                    }
                                    runOnUiThread(() -> {
                                        ToastUtil.show(this, "已添加500条随机转账记录！");
                                        drawer.closeDrawers();
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        ToastUtil.show(this, "所有用户银行卡总数不足2张，无法生成记录！");
                                        drawer.closeDrawers();
                                    });
                                }
                            });
                            return true;
                        }
                        return false;
                    });

                    // 侧边栏显示当前用户信息
                    ImageView ivNavAvatar = navigationView.getHeaderView(0).findViewById(R.id.ivNavAvatar);
                    TextView tvNavUsername = navigationView.getHeaderView(0).findViewById(R.id.tvNavUsername);
                    TextView tvNavEmail = navigationView.getHeaderView(0).findViewById(R.id.tvNavEmail);
                    if (loggedInUser.avatarUri != null) {
                        Glide.with(this).load(Uri.parse(loggedInUser.avatarUri)).circleCrop().into(ivNavAvatar);
                    } else {
                        Glide.with(this).load(R.drawable.ic_avatar_circle_bg).circleCrop().into(ivNavAvatar);
                    }
                    tvNavUsername.setText(loggedInUser.username);
                    tvNavEmail.setText(loggedInUser.userCode == null ? "" : loggedInUser.userCode);
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            showThemeDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showThemeDialog() {
        String[] themes = {"默认主题", "红色主题"};
        SharedPreferences sp = getSharedPreferences("theme", MODE_PRIVATE);
        String cur = sp.getString("theme", "default");
        int checked = "red".equals(cur) ? 1 : 0;
        new AlertDialog.Builder(this)
            .setTitle("选择主题")
            .setSingleChoiceItems(themes, checked, (d, which) -> {
                String sel = which == 1 ? "red" : "default";
                sp.edit().putString("theme", sel).apply();
                d.dismiss();
                recreate();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}