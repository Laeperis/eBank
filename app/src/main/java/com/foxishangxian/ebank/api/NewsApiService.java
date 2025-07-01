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
    
    // 备用方案：使用模拟数据
    private static final boolean USE_MOCK_DATA = false; // 改为false以使用真实API

    public interface NewsCallback {
        void onSuccess(List<NewsItem> newsList);
        void onError(String error);
    }

    public static void fetchNews(String category, NewsCallback callback) {
        fetchNews(category, 0, callback);
    }

    public static void fetchNews(String category, int offset, NewsCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            StringBuilder response = new StringBuilder();
            String urlString = null;
            try {
                // 华尔街见闻API的URL构建方式
                // 注意：如果API不支持offset参数，我们可能需要使用其他方式实现分页
                if (offset == 0) {
                    urlString = API_URL + "?key=" + API_KEY + "&limit=10";
                } else {
                    // 如果API支持offset，使用offset参数
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

                // 解析华尔街见闻API的JSON响应
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
                
                // 检查是否是第一次加载（offset=0），如果是，使用真实API数据
                // 如果不是第一次加载，使用模拟数据来演示分页功能
                if (offset == 0) {
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

    public static List<NewsItem> getMockNewsDataForPage(int page) {
        List<NewsItem> newsList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        // 根据页码生成不同的模拟数据
        String[][] mockData = {
            {
                "央行降准0.25个百分点，释放长期资金约5000亿元",
                "中国人民银行决定于2024年1月15日下调金融机构存款准备金率0.25个百分点，释放长期资金约5000亿元。",
                "https://example.com/news1",
                "财经网"
            },
            {
                "A股三大指数集体上涨，创业板指涨超2%",
                "今日A股市场表现强劲，三大指数集体上涨，其中创业板指涨幅超过2%，科技股表现活跃。",
                "https://example.com/news2",
                "证券时报"
            },
            {
                "人民币汇率保持稳定，外汇储备充足",
                "近期人民币汇率保持稳定，外汇储备充足，为经济稳定发展提供了有力支撑。",
                "https://example.com/news3",
                "金融时报"
            },
            {
                "数字人民币试点范围进一步扩大",
                "数字人民币试点范围进一步扩大，更多城市和场景加入试点，推动数字经济发展。",
                "https://example.com/news4",
                "经济日报"
            },
            {
                "银行理财产品收益率回升",
                "随着市场利率调整，银行理财产品收益率出现回升，投资者可关注相关投资机会。",
                "https://example.com/news5",
                "银行家"
            },
            {
                "房地产市场调控政策持续优化",
                "各地房地产调控政策持续优化，支持刚需和改善性住房需求，促进房地产市场平稳健康发展。",
                "https://example.com/news6",
                "房地产报"
            },
            {
                "新能源汽车销量再创新高",
                "新能源汽车市场持续火爆，销量再创新高，产业链相关企业受益明显。",
                "https://example.com/news7",
                "汽车周刊"
            },
            {
                "5G网络建设加速推进",
                "5G网络建设加速推进，覆盖范围不断扩大，为数字经济发展提供有力支撑。",
                "https://example.com/news8",
                "通信世界"
            },
            {
                "人工智能技术应用日益广泛",
                "人工智能技术在各个领域的应用日益广泛，推动产业升级和数字化转型。",
                "https://example.com/news9",
                "科技日报"
            },
            {
                "绿色金融发展势头良好",
                "绿色金融发展势头良好，为可持续发展提供重要资金支持。",
                "https://example.com/news10",
                "金融时报"
            }
        };

        // 根据页码选择不同的数据
        int startIndex = (page - 1) * 10;
        for (int i = 0; i < 10 && startIndex + i < mockData.length; i++) {
            String[] data = mockData[startIndex + i];
            
            // 根据页码计算时间偏移
            long timeOffset = (page * 10 + i) * 60000; // 每分钟偏移
            long adjustedTime = System.currentTimeMillis() - timeOffset;
            String publishedAt = sdf.format(new Date(adjustedTime));
            
            newsList.add(new NewsItem(data[0], data[1], data[2], publishedAt, data[3]));
        }

        return newsList;
    }
} 