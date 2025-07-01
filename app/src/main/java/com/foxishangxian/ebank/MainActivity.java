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

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private long lastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        if (item.getItemId() == R.id.nav_debug_add_500) {
                            AsyncTask.execute(() -> {
                                User user = db.userDao().getLoggedInUser();
                                if (user != null) {
                                    java.util.List<com.foxishangxian.ebank.data.BankCard> cards = db.bankCardDao().getCardsByUserId(user.uid);
                                    if (cards == null || cards.isEmpty()) {
                                        cards = db.bankCardDao().getCardsByPhone(user.phone);
                                    }
                                    if (cards != null && !cards.isEmpty()) {
                                        for (com.foxishangxian.ebank.data.BankCard card : cards) {
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}