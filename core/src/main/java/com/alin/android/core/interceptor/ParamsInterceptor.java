package com.alin.android.core.interceptor;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

/**
 * @Description 请求参数拦截器
 * @Author zhangwl
 * @Date 2021/7/9 18:03
 */
public class ParamsInterceptor implements Interceptor {

    private Map<String, String> params;

    public ParamsInterceptor(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl.Builder url = request.url().newBuilder();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                url.addQueryParameter(key, params.get(key));
            }
        }
        return chain.proceed(request.newBuilder().url(url.build()).build());
    }
}