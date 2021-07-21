package com.alin.android.core.constant;

/**
 * File descripition:   APP状态跟踪器常量码
 * @Author zhangwl
 * @Date 2021/7/5 17:35
 */
public class AppStatusConstant {
    /**
     * 应用放在后台被强杀了
     */
    public static final int STATUS_FORCE_KILLED=0;
    /**
     * 应用版本检测准备
     */
    public static final int STATUS_VERSION_CHECK_READY=1;
    /**
     * 应用版本检测
     */
    public static final int STATUS_VERSION_CHECK=2;
    /**
     * APP正常态
     */
    public static final int STATUS_NORMAL=3;
}
