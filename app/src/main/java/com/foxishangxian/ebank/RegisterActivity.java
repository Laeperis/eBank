// RegisterActivity：注册界面Activity
// 主要负责新用户注册、头像选择、跳转登录、注册成功后自动登录等功能
package com.foxishangxian.ebank;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import java.util.UUID;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.foxishangxian.ebank.ui.ToastUtil;
import android.view.View;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import com.yalantis.ucrop.UCrop;
import android.graphics.drawable.GradientDrawable;
import com.bumptech.glide.Glide;
import com.foxishangxian.ebank.data.BankCard;
import com.foxishangxian.ebank.data.BankCardDao;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    // 头像选择相关常量
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final int REQUEST_CODE_CROP_IMAGE = 1003;
    private static final int REQUEST_CODE_UCROP = 69;
    // 头像Uri
    private Uri avatarUri = null;
    // 头像ImageView
    private ImageView ivAvatar;
    // 相机按钮
    private ImageView ivCamera;
    // 临时拍照文件
    private File tempPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 初始化控件
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnToLogin = findViewById(R.id.btnToLogin);
        UserDao userDao = UserDatabase.getInstance(this).userDao();
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCamera = findViewById(R.id.ivCamera);
        // 头像点击事件，弹出选择对话框
        View.OnClickListener avatarClickListener = v -> showAvatarDialog();
        ivAvatar.setOnClickListener(avatarClickListener);
        ivCamera.setOnClickListener(avatarClickListener);
        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            // 校验输入
            if (username.isEmpty() || password.isEmpty()) {
                ToastUtil.show(this, getString(R.string.empty_username_or_password));
                return;
            }
            if (email.isEmpty()) {
                ToastUtil.show(this, getString(R.string.empty_email));
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                ToastUtil.show(this, getString(R.string.invalid_email));
                return;
            }
            if (phone.isEmpty()) {
                ToastUtil.show(this, getString(R.string.empty_phone));
                return;
            }
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                ToastUtil.show(this, getString(R.string.invalid_phone));
                return;
            }
            // 异步注册逻辑
            AsyncTask.execute(() -> {
                if (userDao.getUserByUsername(username) != null) {
                    runOnUiThread(() -> ToastUtil.show(this, getString(R.string.register_failed)));
                } else {
                    userDao.logoutAll();
                    String userCode = generateUserCode(userDao);
                    // 创建新用户对象
                    User user = new User(UUID.randomUUID().toString(), username, password, email, phone, userCode);
                    user.isLoggedIn = true;
                    user.avatarUri = avatarUri == null ? null : avatarUri.toString();
                    userDao.insert(user);
                    // 自动为新用户生成一张银行卡
                    BankCardDao bankCardDao = UserDatabase.getInstance(this).bankCardDao();
                    String cardType = "储蓄卡";
                    String cardNumber = generateCardNumber();
                    double balance = 100.0;
                    String startDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String endDate = "2029-12-31";
                    double limitPerDay = 5000.0;
                    BankCard card = new BankCard(user.uid, cardType, cardNumber, balance, startDate, endDate, limitPerDay, phone, "");
                    bankCardDao.insert(card);
                    runOnUiThread(() -> {
                        ToastUtil.show(this, getString(R.string.register_success));
                        // 注册成功跳转主界面
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });
                }
            });
        });
        // 跳转登录按钮点击事件
        btnToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // 头像选择弹窗
    private void showAvatarDialog() {
        String[] options = {"拍照", "从相册选择", "使用默认头像"};
        new AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        takePhoto();
                    } else if (which == 1) {
                        // 相册
                        pickImage();
                    } else {
                        // 默认头像
                        avatarUri = null;
                        ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                    }
                })
                .show();
    }

    // 从相册选择图片
    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PICK_IMAGE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PICK_IMAGE);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    // 拍照获取图片
    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_TAKE_PHOTO);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tempPhotoFile = new File(getExternalFilesDir(null), "avatar_temp_" + System.currentTimeMillis() + ".jpg");
        Uri photoUri = FileProvider.getUriForFile(this, "com.foxishangxian.ebank.fileprovider", tempPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
                Uri sourceUri = data.getData();
                if (sourceUri != null) {
                    startUCrop(sourceUri);
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                if (tempPhotoFile != null && tempPhotoFile.exists()) {
                    Uri uri = FileProvider.getUriForFile(this, "com.foxishangxian.ebank.fileprovider", tempPhotoFile);
                    startUCrop(uri);
                }
            } else if (requestCode == REQUEST_CODE_UCROP) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    avatarUri = resultUri;
                    Glide.with(this).load(avatarUri).circleCrop().into(ivAvatar);
                }
            }
        }
    }

    // 启动UCrop裁剪图片
    private void startUCrop(Uri sourceUri) {
        File cropFile = new File(getExternalFilesDir(null), "avatar_crop_" + System.currentTimeMillis() + ".jpg");
        Uri cropUri = FileProvider.getUriForFile(this, "com.foxishangxian.ebank.fileprovider", cropFile);
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(true);
        options.setStatusBarColor(getResources().getColor(R.color.white));
        options.setActiveControlsWidgetColor(getResources().getColor(R.color.purple_500));
        options.setToolbarWidgetColor(getResources().getColor(R.color.black));
        UCrop.of(sourceUri, cropUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300)
                .withOptions(options)
                .start(this, REQUEST_CODE_UCROP);
        avatarUri = cropUri;
    }

    @Override
    public void onBackPressed() {
        // 返回登录页
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 保证头像始终圆形显示
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(0xFFE3F2FD); // 浅蓝色背景
        ivAvatar.setBackground(shape);
    }

    // 生成唯一用户编码
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

    // 生成银行卡号
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append("6222");
        for (int i = 0; i < 12; i++) {
            sb.append((int)(Math.random() * 10));
        }
        return sb.toString();
    }
} 