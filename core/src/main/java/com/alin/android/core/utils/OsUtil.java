package com.alin.android.core.utils;

import android.os.Build;

/**
 * @Description 系统工具类
 * @Author zhangwl
 * @Date 2021/7/23 17:23
 */
public class OsUtil {

    public static boolean isMIUI() {
        String manufacturer = Build.MANUFACTURER;
        //这个字符串可以自己定义,例如判断华为就填写huawei,魅族就填写meizu
        if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return false;
    }
}
