package com.alin.android.app.model;

/**
 * @Description 应用版本实体类
 * @Author zhangwl
 * @Date 2021/7/21 14:54
 */
public class AppVersion {
    private String version;
    private String apk_url;
    private String description;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
