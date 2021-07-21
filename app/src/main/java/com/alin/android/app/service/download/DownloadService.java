package com.alin.android.app.service.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @Description 下载服务
 * @Author zhangwl
 * @Date 2021/7/21 15:58
 */
public interface DownloadService {

    @Streaming
    @GET
    Observable<ResponseBody> byUrl(@Url String url);
}
