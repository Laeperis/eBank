package com.foxishangxian.ebank.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.os.Handler;
import android.os.Looper;

import com.foxishangxian.ebank.databinding.FragmentHomeBinding;
import com.foxishangxian.ebank.ui.ToastUtil;
import com.foxishangxian.ebank.ui.NewsAdapter;
import com.foxishangxian.ebank.api.NewsApiService;
import com.foxishangxian.ebank.data.NewsItem;
import com.foxishangxian.ebank.ui.home.BannerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class HomeFragment extends Fragment implements NewsAdapter.OnNewsClickListener {

    private FragmentHomeBinding binding;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList = new ArrayList<>();
    private int currentOffset = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    // Banner自动轮播相关
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private final int BANNER_INTERVAL = 3000; // 3秒

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置 Banner 轮播图
        BannerAdapter bannerAdapter = new BannerAdapter(getContext());
        binding.bannerViewPager.setAdapter(bannerAdapter);

        // 自动轮播逻辑
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int count = bannerAdapter.getItemCount();
                int nextItem = (binding.bannerViewPager.getCurrentItem() + 1) % count;
                binding.bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, BANNER_INTERVAL);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL);

        // 四大功能按钮点击事件
        binding.llCardManage.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到卡片管理")
        );
        binding.llTransfer.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到转账汇款")
        );
        binding.llIncomeExpense.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到收支分析")
        );
        binding.llTransferRecord.setOnClickListener(v ->
            ToastUtil.show(getContext(), "跳转到转账记录")
        );

        // 设置下拉刷新
        setupSwipeRefresh();

        // 设置新闻RecyclerView
        setupNewsRecyclerView();

        // 设置加载更多按钮
        setupLoadMoreButton();

        // 加载新闻数据
        loadNews(true);

        return root;
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // 下拉刷新，重置分页
            currentOffset = 0;
            hasMoreData = true;
            loadNews(true);
        });
    }

    private void setupNewsRecyclerView() {
        newsAdapter = new NewsAdapter(newsList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvNews.setLayoutManager(layoutManager);
        binding.rvNews.setAdapter(newsAdapter);

        // 由于现在整个页面都在ScrollView中，我们需要手动检测滚动到底部
        // 这里可以添加一个按钮或者使用其他方式触发加载更多
        // 暂时移除滚动监听器，因为RecyclerView现在在ScrollView中
    }

    private void setupLoadMoreButton() {
        binding.btnLoadMore.setOnClickListener(v -> {
            if (!isLoading && hasMoreData) {
                loadMoreNews();
            }
        });
    }

    private void updateLoadMoreButton() {
        if (hasMoreData && !isLoading) {
            binding.btnLoadMore.setVisibility(View.VISIBLE);
        } else {
            binding.btnLoadMore.setVisibility(View.GONE);
        }
    }

    private void loadNews(boolean isRefresh) {
        if (isLoading) return;
        
        isLoading = true;
        if (isRefresh) {
            currentOffset = 0;
            hasMoreData = true;
        } else {
            // 显示加载更多状态
            newsAdapter.setLoadingMore(true);
        }

        // 获取财经新闻
        NewsApiService.fetchNews("", currentOffset, new NewsApiService.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> newsList) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 隐藏加载更多状态
                        newsAdapter.setLoadingMore(false);
                        
                        if (isRefresh) {
                            // 刷新时清空列表
                            HomeFragment.this.newsList.clear();
                        }
                        
                        if (newsList != null && !newsList.isEmpty()) {
                            HomeFragment.this.newsList.addAll(newsList);
                            currentOffset += newsList.size();
                            
                            // 如果返回的数据少于10条，说明没有更多数据了
                            if (newsList.size() < 10) {
                                hasMoreData = false;
                                newsAdapter.setNoMore(true);
                            }
                        } else {
                            hasMoreData = false;
                            newsAdapter.setNoMore(true);
                        }
                        
                        newsAdapter.notifyDataSetChanged();
                        isLoading = false;
                        
                        // 更新加载更多按钮状态
                        updateLoadMoreButton();
                        
                        if (isRefresh) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                            ToastUtil.show(getContext(), "刷新成功");
                        } else {
                            ToastUtil.show(getContext(), "加载更多成功");
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 隐藏加载更多状态
                        newsAdapter.setLoadingMore(false);
                        isLoading = false;
                        
                        // 更新加载更多按钮状态
                        updateLoadMoreButton();
                        
                        if (isRefresh) {
                            binding.swipeRefreshLayout.setRefreshing(false);
                        }
                        
                        // 显示详细的错误信息
                        String errorMessage = "新闻加载失败\n\n调试信息:\n" + error;
                        ToastUtil.show(getContext(), errorMessage);
                        
                        // 同时在控制台打印错误信息
                        System.out.println("HomeFragment - API Error: " + error);
                        
                        // 如果是刷新且没有数据，加载模拟数据作为备用
                        if (isRefresh && HomeFragment.this.newsList.isEmpty()) {
                            loadMockNews();
                        }
                    });
                }
            }
        });
    }

    private void loadMoreNews() {
        System.out.println("loadMoreNews被调用 - hasMoreData: " + hasMoreData + ", isLoading: " + isLoading);
        if (!hasMoreData || isLoading) {
            System.out.println("loadMoreNews被阻止 - hasMoreData: " + hasMoreData + ", isLoading: " + isLoading);
            return;
        }
        System.out.println("开始加载更多新闻，当前offset: " + currentOffset);
        loadNews(false);
    }

    private void loadMockNews() {
        // 使用模拟新闻数据
        List<NewsItem> mockNewsList = NewsApiService.getMockNewsDataForPage(1);

        // 更新UI显示模拟数据
        newsList.clear();
        newsList.addAll(mockNewsList);
        newsAdapter.notifyDataSetChanged();
        
        ToastUtil.show(getContext(), "已加载模拟数据");
    }

    @Override
    public void onNewsClick(NewsItem newsItem) {
        // 点击新闻项时使用内置浏览器打开
        try {
            System.out.println("点击新闻: " + newsItem.getTitle());
            System.out.println("新闻URL: " + newsItem.getUrl());
            
            // 检查URL是否有效
            if (newsItem.getUrl() == null || newsItem.getUrl().isEmpty()) {
                ToastUtil.show(getContext(), "新闻链接无效");
                return;
            }
            
            // 确保URL有协议前缀
            String url = newsItem.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            // 创建内置浏览器对话框
            showWebViewDialog(newsItem.getTitle(), url);
            
        } catch (Exception e) {
            System.out.println("打开新闻链接失败: " + e.getMessage());
            ToastUtil.show(getContext(), "无法打开新闻链接: " + e.getMessage());
        }
    }

    private void showWebViewDialog(String title, String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        
        // 创建WebView
        WebView webView = new WebView(getContext());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        
        webView.loadUrl(url);
        
        builder.setView(webView);
        builder.setPositiveButton("关闭", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除Banner自动轮播回调，防止内存泄漏
        bannerHandler.removeCallbacks(bannerRunnable);
        binding = null;
    }
}