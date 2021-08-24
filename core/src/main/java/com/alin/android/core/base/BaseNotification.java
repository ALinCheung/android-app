package com.alin.android.core.base;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static android.provider.Settings.EXTRA_CHANNEL_ID;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * 通知基础类
 */
public interface BaseNotification {

    default int getNotificationImportance(){
        return NotificationManager.IMPORTANCE_HIGH;
    }

    default int getNotificationSmallIcon() {
        return android.R.mipmap.sym_def_app_icon;
    }

    default int getNotificationLargeIcon() {
        return android.R.mipmap.sym_def_app_icon;
    }

    /**
     * 判断是否有通知权限
     */
    default boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        return manager.areNotificationsEnabled();
    }

    /**
     * 跳转应用通知权限设置
     */
    default void redirectNotificationSetting(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            intent.putExtra(EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);

            //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);

            // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
            //  if ("MI 6".equals(Build.MODEL)) {
            //      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            //      Uri uri = Uri.fromParts("package", getPackageName(), null);
            //      intent.setData(uri);
            //      // intent.setAction("com.android.settings/.SubSettings");
            //  }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
            Intent intent = new Intent();

            //下面这种方案是直接跳转到当前应用的设置界面。
            //https://blog.csdn.net/ysy950803/article/details/71910806
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    /**
     * 初始化通知
     */
    default void initNotification(Context context, String channelId, String channelName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // 创建消息渠道
        int importance = getNotificationImportance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 发送通知
     */
    default void sendNotification(Context context, PendingIntent intent, String channelId, String title, String content, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        int notificationSmallIcon = getNotificationSmallIcon();
        int notificationLargeIcon = getNotificationLargeIcon();
        // 创建消息
        Notification notification = new NotificationCompat.Builder(context, channelId)
                //.setChannelId(channelId)  //关键！一定要set，不然就失效
                .setContentTitle(title)  //设置标题
                //.setContentText(version.getDescription()) //设置内容
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)) // 长文本, 富文本
                .setWhen(System.currentTimeMillis())  //设置时间
                .setSmallIcon(notificationSmallIcon)  //设置小图标  只能使用alpha图层的图片进行设置
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), notificationLargeIcon))   //设置大图标
                // 向通知添加声音、闪灯和振动效果
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        // 是否锁屏
//        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        Log.d(TAG, "屏幕是否锁屏KeyguardManager: " + keyguardManager.isKeyguardLocked());
//        if (keyguardManager.isKeyguardLocked()) {
            //获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            Log.d(TAG, "屏幕是否锁屏PowerManager: " + pm.isInteractive());
            if (!pm.isInteractive()) {
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
                wl.acquire();  //点亮屏幕
                wl.release();  //任务结束后释放
            }
            notificationManager.notify(notificationId, notification);
//        } else {
//            notificationManager.notify(notificationId, notification);
//        }
    }
}
