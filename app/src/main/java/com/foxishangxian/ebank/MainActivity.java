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

// MainActivity：应用主界面Activity
// 负责主页面的导航、自动登录判断、主题切换、侧边栏和底部导航栏的功能实现
public class MainActivity extends AppCompatActivity {

    // AppBar配置对象，用于管理导航栏和侧边栏的联动
    private AppBarConfiguration mAppBarConfiguration;
    // ViewBinding对象，绑定主界面布局
    private ActivityMainBinding binding;
    // 上次返回键按下时间，用于实现双击退出
    private long lastBackPressedTime = 0;

    /**
     * Activity创建时的回调方法，初始化界面、主题、导航等
     * @param savedInstanceState Activity保存的状态
     */
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
                // 判断两次返回键按下的时间间隔
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
                    // 未登录则跳转到登录界面
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    // 只有已登录用户时才初始化主界面
                    binding = ActivityMainBinding.inflate(getLayoutInflater());
                    setContentView(binding.getRoot());

                    // 设置顶部工具栏
                    setSupportActionBar(binding.appBarMain.toolbar);
                    DrawerLayout drawer = binding.drawerLayout;
                    NavigationView navigationView = binding.navView;
                    // 配置AppBar与DrawerLayout联动
                    mAppBarConfiguration = new AppBarConfiguration.Builder(
                            R.id.nav_home, R.id.nav_wealth, R.id.nav_mine)
                            .setOpenableLayout(drawer)
                            .build();
                    // 获取NavController用于Fragment导航
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
                    NavigationUI.setupWithNavController(navigationView, navController);

                    // 设置底部导航栏与NavController联动
                    BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
                    NavigationUI.setupWithNavController(bottomNav, navController);

                    // 设置侧边栏点击事件
                    navigationView.setNavigationItemSelectedListener(item -> {
                        if (item.getItemId() == R.id.nav_settings) {
                            // 跳转到设置界面
                            startActivity(new Intent(this, SettingsActivity.class));
                            drawer.closeDrawers();
                            return true;
                        }
                        if (item.getItemId() == R.id.nav_debug_add_500_money) {
                            // 调试功能：为所有银行卡加500元
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
                            // 调试功能：添加500条随机转账记录
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
                        // 加载用户头像
                        Glide.with(this).load(Uri.parse(loggedInUser.avatarUri)).circleCrop().into(ivNavAvatar);
                    } else {
                        // 默认头像
                        Glide.with(this).load(R.drawable.ic_avatar_circle_bg).circleCrop().into(ivNavAvatar);
                    }
                    tvNavUsername.setText(loggedInUser.username);
                    tvNavEmail.setText(loggedInUser.userCode == null ? "" : loggedInUser.userCode);
                }
            });
        });
    }

    /**
     * 创建菜单栏（右上角菜单）
     * @param menu 菜单对象
     * @return 是否显示菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 菜单项点击事件处理
     * @param item 被点击的菜单项
     * @return 是否消费该事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            showThemeDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 主题切换对话框
     */
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

    /**
     * 导航栏返回按钮处理
     * @return 是否成功处理
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}