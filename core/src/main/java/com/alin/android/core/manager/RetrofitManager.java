package com.alin.android.core.manager;

import com.alin.android.core.intercepter.LogIntercepter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description 请求管理器
 * @Author zhangwl
 * @Date 2021/7/9 15:33
 */
public class RetrofitManager {

    private static Map<String, Retrofit> retrofits = new LinkedHashMap<>(0);

    public static Retrofit getInstance() {
        if (retrofits.size() == 1) {
            return retrofits.get(retrofits.keySet().iterator().next());
        } else {
            return null;
        }
    }

    public static Retrofit getInstance(String baseUrl) {
        if (!retrofits.containsKey(baseUrl)) {
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.addInterceptor(new LogIntercepter());
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okBuilder.build())
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            retrofits.put(baseUrl, retrofit);
        }
        return retrofits.get(baseUrl);
    }
}
