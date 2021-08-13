package com.alin.android.app.api.app;

import com.alin.android.app.model.App;
import com.alin.android.app.model.AppVersion;
import com.alin.android.app.model.Banner;
import com.alin.android.core.model.Result;
import io.reactivex.Observable;
import ren.yale.android.retrofitcachelibrx2.anno.Mock;
import retrofit2.http.GET;

import java.util.List;

/**
 * @Description 应用接口
 * @Author zhangwl
 * @Date 2021/7/16 17:59
 */
public interface AppApi {

    @Mock(assets = "json/app_version.json")
    @GET("/app/version")
    Observable<Result<AppVersion>> getAppVersion();

    @Mock(assets = "json/app_list.json")
    @GET("/app/list")
    Observable<Result<List<App>>> getAppList();

    @Mock(assets = "json/banner_list.json")
    @GET("/banner/list")
    Observable<Result<List<Banner>>> getBannerList();
}
