package com.alin.android.app.api.chat;

import com.alin.android.app.model.AppVersion;
import com.alin.android.core.model.Result;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 聊天室接口
 */
public interface ChatApi {

    @GET("/loginvalidate")
    Observable<String> loginValidate(@Query("username") String username, @Query("password") String password);
}
