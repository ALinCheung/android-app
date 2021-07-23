package com.alin.android.app.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import androidx.annotation.Nullable;
import com.alin.android.app.activity.SplashActivity;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.constant.InstallType;
import com.alin.android.core.constant.ReturnCode;
import com.alin.android.app.model.AppVersion;
import com.alin.android.app.service.app.AppService;
import com.alin.android.app.service.download.DownloadService;
import com.alin.android.core.base.BaseActivity;
import com.alin.android.core.constant.AppStatus;
import com.alin.android.core.listener.DownloadListener;
import com.alin.android.core.manager.AppStatusManager;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.model.Result;
import com.alin.android.core.utils.DownloadUtil;
import com.alin.android.core.utils.FileUtil;
import com.alin.android.core.utils.MartketUtil;
import com.mylhyl.circledialog.BaseCircleDialog;
import com.mylhyl.circledialog.CircleDialog;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * @description: 基础活动
 * @author: Create By ZhangWenLin
 * @create: 2021年7月9日12:52:03
 **/
public abstract class BaseAppActivity extends BaseActivity {

    protected Retrofit retrofit;
    protected Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        // 初始化retrofit
        String apiUrl = getEnvString(Constant.KEY_API_URL);
        retrofit = RetrofitManager.getInstance(this, apiUrl != null ? apiUrl : Constant.DEFAULT_URL);
        // 检测app版本
        // TODO 设置版本检测弹出次数
        appVersionCheck(true);
    }

    /**
     * app版本检测
     * @param isStartCheck 是否启动检测
     */
    protected void appVersionCheck(boolean isStartCheck) {
        if (AppStatusManager.getInstance().appStatus == AppStatus.STATUS_VERSION_CHECK) {
            try {
                if (!isStartCheck) {
                    showLoadingDialog("检测中");
                }
                // 当前app版本
                final PackageInfo pkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                Log.i(TAG, "APP当前版本: "+pkInfo.versionName);
                // 远程版本
                retrofit.create(AppService.class).getAppVersion()
                        .compose(RetrofitManager.<Result<AppVersion>>ioMain())
                        .subscribe(new BaseAppObserver<Result<AppVersion>>(this, false) {
                            @Override
                            public void onAccept(Result<AppVersion> o, String error) {
                                if (!isStartCheck) {
                                    dismissDialog();
                                }
                                if (StringUtils.isBlank(error) && ReturnCode.SUCCESS == o.getCode()) {
                                    AppVersion version = o.getData();
                                    if (!StringUtils.equalsIgnoreCase(pkInfo.versionName, version.getVersion()) && ".apk".equals(version.getApk_url().replaceAll(".*?(\\.apk)$", "$1"))) {
                                        // 弹出询问框
                                        showUpdateDialog(version);
                                        // APP正常状态
                                        AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_NORMAL);
                                    } else {
                                        if (!isStartCheck) {
                                            showInfoDialog("已是最新版本");
                                        }
                                    }
                                } else {
                                    if (!isStartCheck) {
                                        super.onAccept(o, error);
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 弹出对话框通知用户更新程序
     * @param version
     */
    protected void showUpdateDialog(final AppVersion version) {
        // 对话框
        new CircleDialog.Builder()
                .setWidth(0.7f)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .setTitle("版本升级")
                .setText(version.getDescription())
                .configText(params -> {
                    params.gravity = Gravity.LEFT | Gravity.TOP;
                })
                .setNegative("取消", v -> {
                    Log.i(TAG, "取消版本升级");
                    return true;
                })
                .setPositive("确定", v -> {
                    boolean isMarketInstall = false;
                    String marketPkg = null;
                    if (InstallType.MARKET.equals(version.getInstall_type()) && MartketUtil.isAvilible(context)) {
                        marketPkg = MartketUtil.getMarketPkg(context);
                        isMarketInstall = StringUtils.isNotBlank(marketPkg);
                    }
                    if (isMarketInstall) {
                        // 应用商店下载
                        MartketUtil.install(context, getPackageName(), marketPkg);
                    } else {
                        // 手动下载应用
                        manualDownloadApp(version.getApk_url());
                    }
                    return true;
                })
                .show(getSupportFragmentManager());
    }

    /**
     * 手动下载应用
     * @param url
     */
    private void manualDownloadApp(final String url) {
        Log.i(TAG,"下载apk, 版本升级");
        showLoadingDialog("加载中");
        // 进度条对话框
        retrofit.create(DownloadService.class).byUrl(url)
                .compose(RetrofitManager.<ResponseBody>ioMain())
                .subscribe(new BaseAppObserver<ResponseBody>() {
                    @Override
                    public void onAccept(ResponseBody o, String error) {
                        dismissDialog();
                        super.onAccept(o, error);
                        // 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            final String fileName = url.replaceAll(".*?(\\/((?!\\/).)+\\.apk)$", "$1");
                            final File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
                            // 进度条
                            CircleDialog.Builder builder = new CircleDialog.Builder();
                            BaseCircleDialog dialog = builder.setCancelable(false)
                                    .setCanceledOnTouchOutside(false)
                                    .setTitle("下载")
                                    .setProgressText("已经下载")
                                    .setNegative("取消", v -> {
                                        Log.i(TAG, "取消下载: " + file.getPath());
                                        return true;
                                    })
                                    .show(getSupportFragmentManager());
                            boolean isSuccess = DownloadUtil.writeFileFromIs(file, o.byteStream(), o.contentLength(), new DownloadListener() {
                                @Override
                                public void onStart() {
                                    Log.i(TAG, "开始下载" + fileName + ": " + file.getPath());
                                }

                                @Override
                                public void onProgress(final int progress) {
                                    Log.i(TAG, fileName + "当前下载进度: " + progress);
                                    BaseAppActivity.this.runOnUiThread(() -> {
                                        builder.setProgressText("已经下载"+progress+"%")
                                                .setProgress(100, progress)
                                                .refresh();
                                    });
                                }

                                @Override
                                public void onFinish(String path) {
                                    dialog.dialogDismiss();
                                    showSuccessDialog("下载完成");
                                    Log.i(TAG, "下载完成: " + path);
                                }

                                @Override
                                public void onFail(String errorInfo) {
                                    dialog.dialogDismiss();
                                    showErrorDialog("下载失败");
                                    Log.i(TAG, "下载失败: " + errorInfo);
                                }
                            });
                            // 安装apk
                            if (isSuccess) {
                                FileUtil.install(context, file);
                            }
                        }
                    }
                });
    }

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

    @Override
    protected Class getStartActivity() {
        // 版本检测准备
        AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_VERSION_CHECK_READY);
        return SplashActivity.class;
    }
}
