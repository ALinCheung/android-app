package com.alin.android.core.listener;

/**
 * @Description 下载监听器
 * @Author zhangwl
 * @Date 2021/7/22 14:43
 */
public interface DownloadListener {

    /**
     * 下载开始
     */
    void onStart();

    /**
     * 下载进度
     * @param progress
     */
    void onProgress(int progress);

    /**
     * 下载完成
     * @param path
     */
    void onFinish(String path);

    /**
     * 下载失败
     * @param errorInfo
     */
    void onFail(String errorInfo);
}
