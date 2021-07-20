package com.alin.android.app.common;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.alin.android.app.activity.SplashActivity;
import com.alin.android.app.constant.Constant;
import com.alin.android.core.base.BaseActivity;
import com.alin.android.core.manager.RetrofitManager;
import retrofit2.Retrofit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @description: 基础活动
 * @author: Create By ZhangWenLin
 * @create: 2021年7月9日12:52:03
 **/
public abstract class BaseAppActivity extends BaseActivity {

    protected Retrofit retrofit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String apiUrl = getEnvString(Constant.KEY_API_URL);
        retrofit = RetrofitManager.getInstance(this, apiUrl != null ? apiUrl : Constant.DEFAULT_URL);
    }

    protected String getEnvString(String key) {
        Properties properties = new Properties();
        try (InputStream is = getAssets().open(Constant.ENV_PROPERTIES)){
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object o = properties.get(key);
        return o != null?o.toString():null;
    }

    @Override
    protected Class getStartActivity() {
        return SplashActivity.class;
    }
}
