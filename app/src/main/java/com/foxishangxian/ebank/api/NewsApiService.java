// NewsApiService：新闻API服务类
// 负责从网络获取财经新闻数据，支持真实API和模拟数据两种模式
package com.foxishangxian.ebank.api;

import android.os.AsyncTask;
import com.foxishangxian.ebank.data.NewsItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NewsApiService {

    // 使用免费的华尔街见闻API
    private static final String API_URL = "https://whyta.cn/api/wallstreetcn";
    private static final String API_KEY = "36de5db81215";
    
    // 是否使用模拟数据（true为模拟，false为真实API）
    private static final boolean USE_MOCK_DATA = false; // 改为false以使用真实API

    // 新闻回调接口
    public interface NewsCallback {
        void onSuccess(List<NewsItem> newsList); // 成功回调，返回新闻列表
        void onError(String error); // 失败回调，返回错误信息
    }

    // 获取新闻（默认从offset=0开始）
    public static void fetchNews(String category, NewsCallback callback) {
        fetchNews(category, 0, callback);
    }

    // 获取新闻（支持分页）
    public static void fetchNews(String category, int offset, NewsCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            StringBuilder response = new StringBuilder();
            String urlString = null;
            try {
                // 构建API请求URL，支持分页参数
                if (offset == 0) {
                    urlString = API_URL + "?key=" + API_KEY + "&limit=10";
                } else {
                    urlString = API_URL + "?key=" + API_KEY + "&limit=10&offset=" + offset;
                }
                
                System.out.println("请求URL: " + urlString);
                
                URL newsUrl = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) newsUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 打印API响应用于调试
                System.out.println("API Response: " + response.toString());
                System.out.println("API调用成功，开始解析数据...");

                // 解析API返回的JSON响应
                JSONObject jsonResponse = new JSONObject(response.toString());
                
                JSONArray articles;
                long updatedTime = 0;
                
                // 根据实际API返回格式解析
                if (jsonResponse.has("status") && jsonResponse.getString("status").equals("success")) {
                    // 正确的格式: {"status":"success","id":"wallstreetcn-hot","updatedTime":...,"items":[...]}
                    articles = jsonResponse.getJSONArray("items");
                    updatedTime = jsonResponse.optLong("updatedTime", System.currentTimeMillis());
                } else if (jsonResponse.has("data")) {
                    // 备用格式1: {"data": [...]}
                    articles = jsonResponse.getJSONArray("data");
                    updatedTime = System.currentTimeMillis();
                } else if (jsonResponse.has("code")) {
                    // 备用格式2: {"code": 200, "data": [...]}
                    if (jsonResponse.getInt("code") != 200) {
                        throw new IOException("API error: " + jsonResponse.optString("message", "Unknown error"));
                    }
                    articles = jsonResponse.getJSONArray("data");
                    updatedTime = System.currentTimeMillis();
                } else {
                    // 备用格式3: 直接是数组 [...]
                    articles = new JSONArray(response.toString());
                    updatedTime = System.currentTimeMillis();
                }

                List<NewsItem> newsList = new ArrayList<>();

                if (offset >= 0) {
                    // 使用真实API数据
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        
                        String title = article.optString("title", "");
                        String url = article.optString("url", "");
                        String description = title;
                        
                        // 使用API返回的updatedTime作为发布时间
                        String publishedAt;
                        if (updatedTime > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            publishedAt = sdf.format(new Date(updatedTime));
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            publishedAt = sdf.format(new Date());
                        }
                        
                        String source = "华尔街见闻";

                        if (!title.isEmpty()) {
                            newsList.add(new NewsItem(title, description, url, publishedAt, source));
                        }
                    }
                } else {
                    // 使用模拟数据来演示分页功能
                    newsList = getMockNewsDataForPage(offset);
                }

                if (callback != null) {
                    callback.onSuccess(newsList);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();

                // 构建详细的调试信息
                StringBuilder debugInfo = new StringBuilder();
                debugInfo.append("API调用失败:\n");
                debugInfo.append("URL: ").append(urlString).append("\n");
                debugInfo.append("API Key: ").append(API_KEY).append("\n");
                debugInfo.append("Offset: ").append(offset).append("\n");
                debugInfo.append("错误类型: ").append(e.getClass().getSimpleName()).append("\n");
                debugInfo.append("错误消息: ").append(e.getMessage()).append("\n");

                if (e instanceof IOException) {
                    debugInfo.append("网络连接错误\n");
                } else if (e instanceof JSONException) {
                    debugInfo.append("JSON解析错误\n");
                }

                // 打印调试信息到控制台
                System.out.println(debugInfo.toString());

                // 将错误信息传递给回调
                if (callback != null) {
                    callback.onError(debugInfo.toString());
                }
            }
        });
    }

    // 获取指定页码的模拟新闻数据
    public static List<NewsItem> getMockNewsDataForPage(int page) {
        List<NewsItem> newsList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // 根据页码生成不同的模拟数据
        String[][] mockData = {
            {
                "央行降准0.25个百分点，释放长期资金约5000亿元",
                "中国人民银行决定于2024年1月15日下调金融机构存款准备金率0.25个百分点，释放长期资金约5000亿元。",
                "https://example.com/news1",
                "华尔街见闻"
            },
            {
                "A股三大指数集体上涨，创业板指涨超2%",
                "今日A股市场表现强劲，三大指数集体上涨，其中创业板指涨幅超过2%，科技股表现活跃。",
                "https://example.com/news2",
                "华尔街见闻"
            },
            {
                "人民币汇率保持稳定，外汇储备充足",
                "近期人民币汇率保持稳定，外汇储备充足，为经济稳定发展提供了有力支撑。",
                "https://example.com/news3",
                "华尔街见闻"
            },
            {
                "数字人民币试点范围进一步扩大",
                "中国数字人民币试点城市再扩容，应用场景持续丰富，推动数字经济发展。",
                "https://example.com/news4",
                "华尔街见闻"
            }
        };
        // 生成10条模拟数据
        for (int i = 0; i < 10; i++) {
            int idx = (page * 10 + i) % mockData.length;
            String[] data = mockData[idx];
            newsList.add(new NewsItem(
                data[0],
                data[1],
                data[2],
                sdf.format(new Date(System.currentTimeMillis() - i * 3600_000)),
                data[3]
            ));
        }
        return newsList;
    }
} 