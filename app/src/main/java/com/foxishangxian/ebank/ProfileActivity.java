package com.foxishangxian.ebank;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.data.BankCardDao;
import com.foxishangxian.ebank.databinding.ActivityProfileBinding;
import com.foxishangxian.ebank.databinding.ContentProfileBinding;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import com.foxishangxian.ebank.databinding.AppBarProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private ContentProfileBinding contentBinding;
    private AppBarProfileBinding appBarProfileBinding;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置状态栏颜色与主页一致
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.purple_700));
        }
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定 app_bar_profile
        appBarProfileBinding = AppBarProfileBinding.bind(findViewById(R.id.app_bar_profile));
        // 绑定内容
        contentBinding = ContentProfileBinding.bind(appBarProfileBinding.getRoot().findViewById(R.id.content_profile_root));

        setSupportActionBar(appBarProfileBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("个人资料");
        }
        appBarProfileBinding.toolbar.setNavigationOnClickListener(v -> finish());

        contentBinding.layoutAvatar.setOnClickListener(v -> pickAvatar());
        contentBinding.layoutUsername.setOnClickListener(v -> showEditUsernameDialog());

        loadProfile();
    }

    private void loadProfile() {
        AsyncTask.execute(() -> {
            UserDao userDao = UserDatabase.getInstance(this).userDao();
            BankCardDao cardDao = UserDatabase.getInstance(this).bankCardDao();
            currentUser = userDao.getLoggedInUser();
            int cardCount = currentUser == null ? 0 : cardDao.getCardsByUserId(currentUser.uid).size();
            runOnUiThread(() -> {
                if (currentUser != null) {
                    // 头像
                    if (!TextUtils.isEmpty(currentUser.avatarUri)) {
                        Glide.with(this).load(Uri.parse(currentUser.avatarUri)).circleCrop().into(contentBinding.ivAvatar);
                    } else {
                        contentBinding.ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                    }
                    // 用户名
                    contentBinding.tvUsername.setText(currentUser.username);
                    // 用户代码
                    contentBinding.tvUserCode.setText(currentUser.userCode);
                    // 银行卡数量
                    contentBinding.tvCardCount.setText(String.valueOf(cardCount));
                }
            });
        });
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && currentUser != null) {
                currentUser.avatarUri = uri.toString();
                AsyncTask.execute(() -> {
                    UserDatabase.getInstance(this).userDao().update(currentUser);
                    runOnUiThread(this::loadProfile);
                });
            }
        }
    }

    private void showEditUsernameDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_single, null);
        TextInputLayout til = dialogView.findViewById(R.id.til_single);
        TextInputEditText et = dialogView.findViewById(R.id.et_single);
        til.setHint("请输入新用户名");
        if (currentUser != null) et.setText(currentUser.username);
        new MaterialAlertDialogBuilder(this)
                .setTitle("修改用户名")
                .setView(dialogView)
                .setPositiveButton("确定", (d, w) -> {
                    String value = et.getText() == null ? "" : et.getText().toString().trim();
                    if (value.isEmpty()) {
                        ToastUtil.show(this, "用户名不能为空"); return;
                    }
                    AsyncTask.execute(() -> {
                        currentUser.username = value;
                        UserDatabase.getInstance(this).userDao().update(currentUser);
                        runOnUiThread(this::loadProfile);
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }
} 