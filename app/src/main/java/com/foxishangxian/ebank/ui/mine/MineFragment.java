package com.foxishangxian.ebank.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.foxishangxian.ebank.databinding.FragmentMineBinding;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;
import android.os.AsyncTask;
import com.bumptech.glide.Glide;
import android.net.Uri;
import com.foxishangxian.ebank.R;

public class MineFragment extends Fragment {

    private FragmentMineBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 加载用户信息
        loadUserInfo();

        // 功能按钮点击事件
        binding.btnProfile.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到个人资料")
        );
        binding.btnSecurity.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到安全设置")
        );
        binding.btnNotification.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到消息通知")
        );
        binding.btnHelp.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到帮助中心")
        );
        binding.btnAbout.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到关于我们")
        );

        return root;
    }

    private void loadUserInfo() {
        AsyncTask.execute(() -> {
            UserDao userDao = UserDatabase.getInstance(getContext()).userDao();
            User currentUser = userDao.getLoggedInUser();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (currentUser != null) {
                        // 设置用户名
                        binding.tvUsername.setText(currentUser.username);
                        
                        // 设置用户代码
                        binding.tvUserCode.setText("用户代码：" + currentUser.userCode);
                        
                        // 加载头像
                        if (currentUser.avatarUri != null && !currentUser.avatarUri.isEmpty()) {
                            try {
                                Uri avatarUri = Uri.parse(currentUser.avatarUri);
                                Glide.with(this)
                                    .load(avatarUri)
                                    .circleCrop()
                                    .into(binding.ivAvatar);
                            } catch (Exception e) {
                                // 如果头像加载失败，使用默认头像
                                binding.ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                            }
                        } else {
                            // 使用默认头像
                            binding.ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                        }
                    } else {
                        // 如果没有登录用户，显示默认信息
                        binding.tvUsername.setText("未登录");
                        binding.tvUserCode.setText("用户代码：--");
                        binding.ivAvatar.setImageResource(R.drawable.ic_avatar_circle_bg);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次回到页面时刷新用户信息
        loadUserInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 