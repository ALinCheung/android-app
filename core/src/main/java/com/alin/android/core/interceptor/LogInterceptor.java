package com.alin.android.core.interceptor;

import android.util.Log;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @Description 日志拦截器
 * @Author zhangwl
 * @Date 2021/7/9 15:39
 */
public class LogInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String LOG_TAG = "LogIntercepter";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        String body = null;
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.readString(charset);
        }
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        String rBody;
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        rBody = buffer.clone().readString(charset);
        Log.d(LOG_TAG, "├─────────────────────────────────────────────────────────────────");
        Log.d(LOG_TAG, "│【请求响应码】" + response.code());
        Log.d(LOG_TAG, "│【请求头】：" + request.headers());
        Log.d(LOG_TAG, "│【请求方法】：" + request.method());
        Log.d(LOG_TAG, "│【请求参数】：" + body);
        Log.d(LOG_TAG, "│【请求路径】：" + response.request().url());
        Log.d(LOG_TAG, "│【请求回调】：" + rBody);
        Log.d(LOG_TAG, "├─────────────────────────────────────────────────────────────────");
        return response;
    }
}
