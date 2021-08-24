package com.alin.android.core.manager;

import android.content.Context;
import com.alin.android.core.interceptor.HeaderInterceptor;
import com.alin.android.core.interceptor.LogInterceptor;
import com.alin.android.core.interceptor.ParamsInterceptor;
import com.google.gson.GsonBuilder;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ren.yale.android.retrofitcachelibrx2.RetrofitCache;
import ren.yale.android.retrofitcachelibrx2.intercept.CacheForceInterceptorNoNet;
import ren.yale.android.retrofitcachelibrx2.intercept.CacheInterceptorOnNet;
import ren.yale.android.retrofitcachelibrx2.intercept.MockInterceptor;
import ren.yale.android.retrofitcachelibrx2.transformer.CacheTransformer;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description 请求管理器
 * @Author zhangwl
 * @Date 2021/7/9 15:33
 */
public class RetrofitManager {

    private static Map<String, Retrofit> retrofits = new LinkedHashMap<>(0);

    public static Retrofit getInstance(Context context, String baseUrl) {
        return RetrofitManager.getInstance(context, baseUrl, null, null);
    }

    public static Retrofit getInstance(Context context, String baseUrl, Map<String, String> headers, Map<String, String> params) {
        if (!retrofits.containsKey(baseUrl)) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // 缓存
                    .cache(new Cache(new File(context.getCacheDir(), "httpcache"), 200 * 1024 * 1024))
                    .addInterceptor(new CacheForceInterceptorNoNet())
                    .addNetworkInterceptor(new CacheInterceptorOnNet())
                    // 添加mock数据
                    .addInterceptor(new MockInterceptor())
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
                    .addConverterFactory(ScalarsConverterFactory.create()) //important
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                            .setLenient()
                            .create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            retrofits.put(baseUrl, retrofit);
            RetrofitCache.getInstance().addRetrofit(retrofit);
        }
        return retrofits.get(baseUrl);
    }

    public static <T> ObservableTransformer<T, T> ioMain() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(io.reactivex.Observable<T> upstream) {
                return upstream.compose(CacheTransformer.<T>emptyTransformer()).
                        subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    static class CustomConverterFactory extends Converter.Factory {

        private GsonConverterFactory gsonConverterFactory;

        public CustomConverterFactory() {
            gsonConverterFactory = GsonConverterFactory.create(new GsonBuilder()
                    .setLenient()
                    .create());
        }

        public static CustomConverterFactory create() {
            return new CustomConverterFactory();
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            try {
                return gsonConverterFactory.responseBodyConverter(type, annotations, retrofit);
            } catch (Exception e) {
                return (Converter<ResponseBody, String>) value -> value.toString();
            }
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
            try {
                return gsonConverterFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
