package com.alin.android.core.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * @Description 文件工具类
 * @Author zhangwl
 * @Date 2021/7/22 11:06
 */
public class FileUtil {

    /**
     * 安装文件
     * @param context
     * @param file
     */
    public static void install(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, context
                    .getPackageName() + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
}
