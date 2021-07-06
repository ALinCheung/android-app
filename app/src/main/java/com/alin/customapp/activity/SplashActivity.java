package com.alin.customapp.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.alin.customapp.R;
import com.alin.customapp.common.BaseActivity;

/**
 * @Description 闪屏页
 * @Author zhangwl
 * @Date 2021/7/6 9:37
 */
public class SplashActivity extends BaseActivity {

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.context = SplashActivity.this;

    }
}
