package com.foxishangxian.ebank;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.MenuItem;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import android.Manifest;
import android.os.Build;
import java.io.File;
import java.io.FileOutputStream;
import com.yalantis.ucrop.UCrop;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.data.BankCardDao;
import com.foxishangxian.ebank.databinding.ActivityProfileBinding;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

// ProfileActivity：用户个人信息界面Activity
// 负责展示和编辑用户的个人资料，包括头像、用户名、用户代码、银行卡数量等信息
// 支持头像更换（拍照、相册、默认）、用户名修改、头像保存到相册等功能
// 涉及权限申请、图片裁剪、数据库操作等常见Android开发场景
public class ProfileActivity extends AppCompatActivity {
    // 视图绑定对象，绑定activity_profile.xml布局
    private ActivityProfileBinding binding;
    // 选择图片请求码
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    // 拍照请求码
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    // 裁剪图片请求码（未使用）
    private static final int REQUEST_CODE_CROP_IMAGE = 1003;
    // uCrop库裁剪请求码
    private static final int REQUEST_CODE_UCROP = 69;
    // 当前登录用户对象
    private User currentUser;
    // 头像ImageView
    private ImageView ivAvatar;
    // 相机按钮ImageView
    private ImageView ivCamera;
    // 拍照临时文件
    private File tempPhotoFile;

    /**
     * Activity创建时回调，初始化界面、事件、加载用户信息
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置Toolbar
        setSupportActionBar(binding.appBarProfile.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("个人资料");
        }

        // 初始化头像相关视图
        ivAvatar = findViewById(R.id.iv_avatar);
        ivCamera = findViewById(R.id.iv_camera);
        
        // 设置头像和相机按钮点击事件，弹出头像操作对话框
        View.OnClickListener avatarClickListener = v -> showAvatarDialog();
        ivAvatar.setOnClickListener(avatarClickListener);
        ivCamera.setOnClickListener(avatarClickListener);
        
        // 设置用户名区域点击事件，弹出修改用户名对话框
        findViewById(R.id.layout_username).setOnClickListener(v -> showEditUsernameDialog());

        // 延迟加载用户信息，避免界面未初始化完成
        new android.os.Handler().postDelayed(this::loadProfile, 100);
    }

    /**
     * 加载当前用户信息并显示到界面
     * 包括头像、用户名、用户代码、银行卡数量
     */
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
                        Glide.with(this).load(Uri.parse(currentUser.avatarUri)).circleCrop().into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                    }
                    // 用户名
                    ((android.widget.TextView)findViewById(R.id.tv_username)).setText(currentUser.username);
                    // 用户代码
                    ((android.widget.TextView)findViewById(R.id.tv_user_code)).setText(currentUser.userCode);
                    // 银行卡数量
                    ((android.widget.TextView)findViewById(R.id.tv_card_count)).setText(String.valueOf(cardCount));
                }
            });
        });
    }

    /**
     * 弹出头像操作对话框，支持拍照、相册、查看、恢复默认
     */
    private void showAvatarDialog() {
        String[] options = {"拍照", "从相册选择", "查看头像", "使用默认头像"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("选择头像")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        takePhoto();
                    } else if (which == 1) {
                        // 相册
                        pickImage();
                    } else if (which == 2) {
                        // 查看头像
                        showCurrentAvatar();
                    } else {
                        // 默认头像
                        currentUser.avatarUri = null;
                        ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                        // 保存到数据库
                        AsyncTask.execute(() -> {
                            UserDatabase.getInstance(this).userDao().update(currentUser);
                        });
                    }
                })
                .show();
    }

    /**
     * 打开系统相册选择图片，动态申请权限
     */
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

    /**
     * 打开系统相机拍照，动态申请权限，保存临时图片
     */
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

    /**
     * 查看当前头像大图，并可保存到相册
     */
    private void showCurrentAvatar() {
        // 创建ImageView用于显示头像
        ImageView imageView = new ImageView(this);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imageSize = (int) (screenWidth * 0.6); // 使用屏幕宽度的60%
        
        android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(
                imageSize, imageSize);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // 设置图片
        if (currentUser != null && !TextUtils.isEmpty(currentUser.avatarUri)) {
            Glide.with(this)
                    .load(Uri.parse(currentUser.avatarUri))
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_avatar_circle_bg);
        }

        // 创建对话框
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("当前头像")
                .setView(imageView)
                .setPositiveButton("保存到相册", (dialogInterface, which) -> {
                    if (currentUser != null && !TextUtils.isEmpty(currentUser.avatarUri)) {
                        saveAvatarToGallery(Uri.parse(currentUser.avatarUri));
                    } else {
                        ToastUtil.show(this, "当前使用的是默认头像，无法保存");
                    }
                })
                .setNegativeButton("关闭", null)
                .create();
        
        dialog.show();
        
        // 调整对话框大小，确保图片完全显示
        dialog.getWindow().setLayout(
                imageSize + 100, // 图片宽度 + 边距
                imageSize + 200); // 图片高度 + 标题和按钮高度
    }

    /**
     * 将头像图片保存到本地相册
     * @param avatarUri 头像图片的Uri
     */
    private void saveAvatarToGallery(Uri avatarUri) {
        AsyncTask.execute(() -> {
            try {
                // 创建保存文件
                String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
                File galleryFile = new File(getExternalFilesDir(null), fileName);
                
                // 复制文件
                java.io.InputStream inputStream = getContentResolver().openInputStream(avatarUri);
                java.io.FileOutputStream outputStream = new FileOutputStream(galleryFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();

                // 通知媒体扫描器
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(galleryFile));
                sendBroadcast(mediaScanIntent);

                runOnUiThread(() -> {
                    ToastUtil.show(this, "头像已保存到相册");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    ToastUtil.show(this, "保存失败：" + e.getMessage());
                });
            }
        });
    }

    /**
     * 处理拍照、相册、裁剪等Activity返回结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                if (resultUri != null && currentUser != null) {
                    currentUser.avatarUri = resultUri.toString();
                    Glide.with(this).load(resultUri).circleCrop().into(ivAvatar);
                    // 保存到数据库
                    AsyncTask.execute(() -> {
                        UserDatabase.getInstance(this).userDao().update(currentUser);
                    });
                }
            }
        }
    }

    /**
     * 弹出修改用户名对话框，输入新用户名并保存
     */
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

    /**
     * 启动uCrop库进行头像裁剪，裁剪为圆形并压缩
     * @param sourceUri 原始图片Uri
     */
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

    /**
     * onResume时保证头像始终为圆形背景
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 保证头像始终圆形显示
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(0xFFE3F2FD); // 浅蓝色背景
        ivAvatar.setBackground(shape);
    }
} 