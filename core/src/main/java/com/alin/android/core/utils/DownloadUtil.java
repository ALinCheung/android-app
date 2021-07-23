package com.alin.android.core.utils;

import com.alin.android.core.listener.DownloadListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @Description 下载工具类
 * @Author zhangwl
 * @Date 2021/7/22 11:51
 */
public class DownloadUtil {

    public static boolean writeFileFromIs(File file, InputStream is, long totalLength, DownloadListener downloadListener) {
        boolean isSuccess = true;
        // 开始下载
        downloadListener.onStart();
        // 创建文件
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                downloadListener.onFail(e.getMessage());
                isSuccess = false;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {
            byte[] buffer = new byte[1024];
            int len;
            int currentLength = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                currentLength += len;
                //计算当前下载进度
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
            }
            //下载完成，并返回保存的文件路径
            downloadListener.onFinish(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            downloadListener.onFail(e.getMessage());
            isSuccess = false;
        }
        return isSuccess;
    }
}
