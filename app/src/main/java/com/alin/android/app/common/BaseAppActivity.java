package com.alin.android.app.common;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.alin.android.app.activity.SplashActivity;
import com.alin.android.core.base.BaseActivity;

/**
 * @description: 基础活动
 * @author: Create By ZhangWenLin
 * @create: 2021年7月9日12:52:03
 **/
public abstract class BaseAppActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Class getStartActivity() {
        return SplashActivity.class;
    }
}
