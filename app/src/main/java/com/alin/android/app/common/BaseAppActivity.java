package com.alin.android.app.common;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import com.alin.android.app.activity.SplashActivity;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.constant.ReturnCode;
import com.alin.android.app.model.AppVersion;
import com.alin.android.app.service.app.AppService;
import com.alin.android.app.service.download.DownloadService;
import com.alin.android.core.base.BaseActivity;
import com.alin.android.core.constant.AppStatusConstant;
import com.alin.android.core.manager.AppStatusManager;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.model.Result;
import com.alin.app.BuildConfig;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;

import java.io.*;
import java.util.Properties;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * @description: 基础活动
 * @author: Create By ZhangWenLin
 * @create: 2021年7月9日12:52:03
 **/
public abstract class BaseAppActivity extends BaseActivity {

    protected Retrofit retrofit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化retrofit
        String apiUrl = getEnvString(Constant.KEY_API_URL);
        retrofit = RetrofitManager.getInstance(this, apiUrl != null ? apiUrl : Constant.DEFAULT_URL);
        // 检测app版本
        appVersionCheck();
    }

    protected void appVersionCheck() {
        if (AppStatusManager.getInstance().appStatus == AppStatusConstant.STATUS_VERSION_CHECK) {
            try {
                // 当前app版本
                final PackageInfo pkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                Log.i(TAG, "APP当前版本: "+pkInfo.versionName);
                // 远程版本
                retrofit.create(AppService.class).getAppVersion()
                        .compose(RetrofitManager.<Result<AppVersion>>ioMain())
                        .subscribe(new BaseAppObserver<Result<AppVersion>>(this, false) {
                            @Override
                            public void onAccept(Result<AppVersion> o, String error) {
                                if (StringUtils.isBlank(error) && ReturnCode.SUCCESS == o.getCode()) {
                                    AppVersion version = o.getData();
                                    if (!StringUtils.equalsIgnoreCase(pkInfo.versionName, version.getVersion()) & ".apk".equals(version.getApk_url().replaceAll(".*?(\\.apk)$", "$1"))) {
                                        // 弹出询问框
                                        showUpdataDialog(version.getDescription(), version.getApk_url());
                                        // APP正常状态
                                        AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_NORMAL);
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     *
     * 弹出对话框通知用户更新程序
     *
     * 弹出对话框的步骤：
     * 	1.创建alertDialog的builder.
     *	2.要给builder设置属性, 对话框的内容,样式,按钮
     *	3.通过builder 创建一个对话框
     *	4.对话框show()出来
     */
    protected void showUpdataDialog(String description, final String url) {
        AlertDialog.Builder builer = new AlertDialog.Builder(this) ;
        builer.setTitle("版本升级");
        builer.setMessage(description);
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG,"下载apk, 版本升级");
                //进度条对话框
                retrofit.create(DownloadService.class).byUrl(url)
                        .compose(RetrofitManager.<ResponseBody>ioMain())
                        .subscribe(new BaseAppObserver<ResponseBody>() {
                            @Override
                            public void onAccept(ResponseBody o, String error) {
                                //如果相等的话表示当前的sdcard挂载在手机上并且是可用的
                                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                                    File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), url.replaceAll(".*?(\\/((?!\\/).)+\\.apk)$", "$1"));
                                    try (FileOutputStream fos = new FileOutputStream(file);
                                         BufferedInputStream bis = new BufferedInputStream(o.byteStream())){
                                        byte[] buffer = new byte[1024];
                                        int len ;
                                        int total=0;
                                        while((len =bis.read(buffer))!=-1){
                                            fos.write(buffer, 0, len);
                                            total+= len;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    Log.i(TAG,"开始安装"+file.getPath());
                                    // 安装apk
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    // 判断是否是AndroidN以及更高的版本
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", file);
                                        Log.i(TAG,"安装路径"+contentUri.getPath());
                                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                                    } else {
                                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                    }
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }
                        });
            }
        });
        //当点取消按钮时进行登录
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "取消版本升级");
            }
        });
        AlertDialog dialog = builer.create();
        dialog.show();
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
        AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_VERSION_CHECK_READY);
        return SplashActivity.class;
    }
}
