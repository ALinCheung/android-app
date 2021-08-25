package com.alin.android.app.common;

import com.alin.android.app.constant.Constant;
import com.alin.android.core.base.BaseService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseAppService extends BaseService {

    /**
     * 获取环境配置字符串
     */
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
}
