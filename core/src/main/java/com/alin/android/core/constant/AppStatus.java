package com.alin.android.core.constant;

/**
 * File descripition:   APP状态跟踪器常量码
 * @Author zhangwl
 * @Date 2021/7/5 17:35
 */
public interface AppStatus {
    /**
     * 应用放在后台被强杀了
     */
    int STATUS_FORCE_KILLED = 0;
    /**
     * 应用版本检测准备
     */
    int STATUS_VERSION_CHECK_READY = 1;
    /**
     * 应用版本检测
     */
    int STATUS_VERSION_CHECK = 2;
    /**
     * APP正常态
     */
    int STATUS_NORMAL = 3;
}
