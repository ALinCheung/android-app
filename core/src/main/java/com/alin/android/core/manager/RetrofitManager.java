package com.alin.android.core.manager;

import com.alin.android.core.interceptor.HeaderInterceptor;
import com.alin.android.core.interceptor.LogInterceptor;
import com.alin.android.core.interceptor.ParamsInterceptor;
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

    public static Retrofit getInstance(String baseUrl) {
        return RetrofitManager.getInstance(baseUrl, null, null);
    }

    public static Retrofit getInstance(String baseUrl, Map<String, String> headers, Map<String, String> params) {
        if (!retrofits.containsKey(baseUrl)) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // 日志拦截
                    .addInterceptor(new LogInterceptor())
                    // 请求头拦截
                    .addInterceptor(new HeaderInterceptor(headers))
                    // 参数拦截
                    .addInterceptor(new ParamsInterceptor(params))
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            retrofits.put(baseUrl, retrofit);
        }
        return retrofits.get(baseUrl);
    }
}
