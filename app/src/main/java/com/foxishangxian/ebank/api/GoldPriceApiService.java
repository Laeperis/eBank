// GoldPriceApiService：黄金价格API服务类
// 负责从网络获取黄金价格数据，支持真实API和模拟数据两种模式
package com.foxishangxian.ebank.api;

import android.os.AsyncTask;
import android.util.Log;
import com.foxishangxian.ebank.data.GoldPrice;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoldPriceApiService {
    // 日志标签
    private static final String TAG = "GoldPriceApiService";
    // API接口地址
    private static final String API_URL = "https://api.jisuapi.com/gold/shgold";
    // API密钥
    private static final String API_KEY = "27244af53cc3d211";

    // 黄金价格回调接口
    public interface GoldPriceCallback {
        void onSuccess(List<GoldPrice> goldPrices); // 成功回调，返回黄金价格列表
        void onError(String error); // 失败回调，返回错误信息
    }

    // 获取黄金价格的主方法，异步请求API
    public static void getGoldPrices(GoldPriceCallback callback) {
        new AsyncTask<Void, Void, String>() {
            // 后台线程执行网络请求
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // 构建请求URL
                    String urlString = API_URL + "?appkey=" + API_KEY;
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    // 获取响应码，判断是否成功
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 读取响应内容
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        return response.toString();
                    } else {
                        // 响应码非200，返回错误信息
                        return "HTTP Error: " + responseCode;
                    }
                } catch (Exception e) {
                    // 捕获异常，记录日志并返回错误信息
                    Log.e(TAG, "Error fetching gold prices", e);
                    return "Error: " + e.getMessage();
                }
            }

            // 主线程处理结果
            @Override
            protected void onPostExecute(String result) {
                try {
                    // 解析JSON响应
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.optInt("status", -1);
                    
                    if (status == 0) {
                        // 新接口：result为JSONArray
                        JSONArray resultArr = jsonObject.optJSONArray("result");
                        if (resultArr != null) {
                            List<GoldPrice> goldPrices = new ArrayList<>();
                            for (int i = 0; i < resultArr.length(); i++) {
                                JSONObject item = resultArr.getJSONObject(i);
                                // 构建GoldPrice对象
                                GoldPrice goldPrice = new GoldPrice(
                                    item.optString("type", ""),
                                    item.optString("typename", ""),
                                    item.optString("price", ""),
                                    item.optString("openingprice", ""),
                                    item.optString("maxprice", ""),
                                    item.optString("minprice", ""),
                                    item.optString("changepercent", ""),
                                    item.optString("lastclosingprice", ""),
                                    item.optString("tradeamount", ""),
                                    item.optString("updatetime", "")
                                );
                                goldPrices.add(goldPrice);
                            }
                            // 成功回调
                            callback.onSuccess(goldPrices);
                            return;
                        }
                    }
                    
                    // 如果API调用失败，返回模拟数据
                    Log.w(TAG, "API call failed, using mock data. Response: " + result);
                    List<GoldPrice> mockData = getMockGoldPrices();
                    callback.onSuccess(mockData);
                    
                } catch (Exception e) {
                    // 解析异常，返回模拟数据
                    Log.e(TAG, "Error parsing gold prices", e);
                    List<GoldPrice> mockData = getMockGoldPrices();
                    callback.onSuccess(mockData);
                }
            }
        }.execute();
    }

    // 获取模拟黄金价格数据
    private static List<GoldPrice> getMockGoldPrices() {
        List<GoldPrice> mockData = new ArrayList<>();
        // 添加模拟数据1
        mockData.add(new GoldPrice(
                "Au(T+D)", "黄金延期", "238.05", "241.00", "241.50", "237.50", "-0.90%", "240.22", "45998.0000", "2015-10-26 15:29:13"
        ));
        // 添加模拟数据2
        mockData.add(new GoldPrice(
                "Au99.99", "沪金99", "238.30", "241.48", "241.48", "238.00", "-0.91%", "240.49", "25400.9800", "2015-10-26 15:29:15"
        ));
        return mockData;
    }
} 