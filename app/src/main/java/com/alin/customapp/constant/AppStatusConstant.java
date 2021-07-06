package com.alin.customapp.constant;

/**
 * File descripition:   APP状态跟踪器常量码
 * @Author zhangwl
 * @Date 2021/7/5 17:35
 */
public class AppStatusConstant {
    /**
     * 应用放在后台被强杀了
     */
    public static final int STATUS_FORCE_KILLED=-1;
    /**
     * APP正常态
     */
    public static final int STATUS_NORMAL=2;

    //intent到MainActivity 区分跳转目的
    /**
     * 返回到主页面
     */
    public static final String KEY_HOME_ACTION="key_home_action";
    /**
     * 默认值
     */
    public static final int ACTION_BACK_TO_HOME=6;
    /**
     * 被强杀
     */
    public static final int ACTION_RESTART_APP=9;
}
