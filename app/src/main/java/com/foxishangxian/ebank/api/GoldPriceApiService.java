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
    private static final String TAG = "GoldPriceApiService";
    private static final String API_URL = "https://api.jisuapi.com/gold/shgold";
    private static final String API_KEY = "c812e10685414c68";

    public interface GoldPriceCallback {
        void onSuccess(List<GoldPrice> goldPrices);
        void onError(String error);
    }

    public static void getGoldPrices(GoldPriceCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String urlString = API_URL + "?appkey=" + API_KEY;
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
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
                        return "HTTP Error: " + responseCode;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching gold prices", e);
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.optInt("status", -1);
                    
                    if (status == 0) {
                        // 新接口：result为JSONArray
                        JSONArray resultArr = jsonObject.optJSONArray("result");
                        if (resultArr != null) {
                            List<GoldPrice> goldPrices = new ArrayList<>();
                            for (int i = 0; i < resultArr.length(); i++) {
                                JSONObject item = resultArr.getJSONObject(i);
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
                            callback.onSuccess(goldPrices);
                            return;
                        }
                    }
                    
                    // 如果API调用失败，返回模拟数据
                    Log.w(TAG, "API call failed, using mock data. Response: " + result);
                    List<GoldPrice> mockData = getMockGoldPrices();
                    callback.onSuccess(mockData);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing gold prices", e);
                    // 返回模拟数据
                    List<GoldPrice> mockData = getMockGoldPrices();
                    callback.onSuccess(mockData);
                }
            }
        }.execute();
    }

    private static List<GoldPrice> getMockGoldPrices() {
        List<GoldPrice> mockData = new ArrayList<>();
        mockData.add(new GoldPrice(
                "Au(T+D)", "黄金延期", "238.05", "241.00", "241.50", "237.50", "-0.90%", "240.22", "45998.0000", "2015-10-26 15:29:13"
        ));
        mockData.add(new GoldPrice(
                "Au99.99", "沪金99", "238.30", "241.48", "241.48", "238.00", "-0.91%", "240.49", "25400.9800", "2015-10-26 15:29:15"
        ));
        return mockData;
    }
} 