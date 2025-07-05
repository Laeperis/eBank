package com.foxishangxian.ebank.ui.wealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.foxishangxian.ebank.databinding.FragmentWealthBinding;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.foxishangxian.ebank.api.GoldPriceApiService;
import com.foxishangxian.ebank.ui.GoldPriceAdapter;
import com.foxishangxian.ebank.data.GoldPrice;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.foxishangxian.ebank.data.UserDatabase;
import com.foxishangxian.ebank.data.User;
import com.foxishangxian.ebank.data.BankCard;
import java.util.concurrent.Executors;

public class WealthFragment extends Fragment {

    private FragmentWealthBinding binding;
    private GoldPriceAdapter goldPriceAdapter;
    private List<GoldPrice> goldPrices = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWealthBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 查询数据库，统计总资产和收益
        Executors.newSingleThreadExecutor().execute(() -> {
            UserDatabase db = UserDatabase.getInstance(getContext());
            User user = db.userDao().getLoggedInUser();
            double total = 0;
            double todayEarnings = 0;
            double totalEarnings = 0;
            
            if (user != null) {
                // 计算总资产
                java.util.List<BankCard> cards = db.bankCardDao().getCardsByUserId(user.uid);
                for (BankCard card : cards) {
                    total += card.balance;
                }
                
                // 获取今日收益和累计收益
                todayEarnings = db.transferRecordDao().getTodayEarnings(user.uid);
                totalEarnings = db.transferRecordDao().getTotalEarnings(user.uid);
            }
            
            double finalTotal = total;
            double finalTodayEarnings = todayEarnings;
            double finalTotalEarnings = totalEarnings;
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.tvTotalAssets.setText("总资产：￥" + String.format("%,.2f", finalTotal));
                    binding.tvTodayEarnings.setText("今日收益：" + (finalTodayEarnings >= 0 ? "+" : "") + "￥" + String.format("%,.2f", finalTodayEarnings));
                    binding.tvTotalEarnings.setText("累计收益：" + (finalTotalEarnings >= 0 ? "+" : "") + "￥" + String.format("%,.2f", finalTotalEarnings));
                });
            }
        });

        // 初始化今日金价列表
        initGoldPriceList();
        
        // 加载今日金价数据
        loadGoldPrices();

        return root;
    }

    private void initGoldPriceList() {
        goldPriceAdapter = new GoldPriceAdapter(goldPrices);
        binding.rvGoldPrices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGoldPrices.setAdapter(goldPriceAdapter);
    }

    private void loadGoldPrices() {
        GoldPriceApiService.getGoldPrices(new GoldPriceApiService.GoldPriceCallback() {
            @Override
            public void onSuccess(List<GoldPrice> prices) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        goldPrices.clear();
                        goldPrices.addAll(prices);
                        goldPriceAdapter.updateData(goldPrices);
                        
                        // 更新刷新时间
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String currentTime = sdf.format(new Date());
                        binding.tvRefreshTime.setText("更新时间：" + currentTime);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ToastUtil.show(getContext(), "获取金价数据失败：" + error);
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 