package com.foxishangxian.ebank.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.foxishangxian.ebank.LoginActivity;
import com.foxishangxian.ebank.R;
import com.foxishangxian.ebank.data.UserDao;
import com.foxishangxian.ebank.data.UserDatabase;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            AsyncTask.execute(() -> {
                UserDao userDao = UserDatabase.getInstance(requireContext()).userDao();
                userDao.logoutAll();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "已退出账号", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                });
            });
        });
        return view;
    }
} 