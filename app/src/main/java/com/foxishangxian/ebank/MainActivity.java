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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.foxishangxian.ebank.databinding.ActivityMainBinding;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDatabase;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null)
                                    .setAnchorView(R.id.fab).show();
                        }
                    });
                    DrawerLayout drawer = binding.drawerLayout;
                    NavigationView navigationView = binding.navView;
                    mAppBarConfiguration = new AppBarConfiguration.Builder(
                            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                            .setOpenableLayout(drawer)
                            .build();
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
                    NavigationUI.setupWithNavController(navigationView, navController);

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
                    tvNavEmail.setText(loggedInUser.email == null ? "" : loggedInUser.email);
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