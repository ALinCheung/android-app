package com.alin.android.core.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;


/**
 * @Description 添加请求头需要携带的参数
 * @Author zhangwl
 * @Date 2021/7/12 11:39
 */
public class HeaderInterceptor implements Interceptor {

    private Map<String, String> headers;

    public HeaderInterceptor(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        if (headers != null && !headers.isEmpty()) {
            for (String key : headers.keySet()) {
                builder.addHeader(key, headers.get(key));
            }
        }
        return chain.proceed(builder.method(request.method(), request.body()).build());
    }
}
