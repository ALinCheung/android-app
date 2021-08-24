package com.alin.android.app.service;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.constraintlayout.widget.Constraints.TAG;

import static com.alin.android.app.constant.Constant.KEY_APP_VERSION_CHECK;
import static com.alin.android.app.constant.NotificationId.APP_VERSION_CHECK;
import static com.alin.android.app.constant.NotificationId.CHAT_MESSAGE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.alin.android.app.R;
import com.alin.android.app.activity.MainActivity;
import com.alin.android.app.activity.chat.ChatUserActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.model.Message;
import com.alin.android.app.socket.ChatWebSocketClient;
import com.alin.android.core.base.BaseNotification;
import com.alin.android.core.utils.XmlUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ChatService extends Service implements BaseNotification {

    private URI uri;
    private Context context;
    public ChatWebSocketClient client;
    private ChatWebSocketClientBinder mBinder = new ChatWebSocketClientBinder();

    //用于Activity和service通讯
    public class ChatWebSocketClientBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    PowerManager.WakeLock wakeLock;//锁屏唤醒
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "聊天服务启动");
        super.onCreate();
        context = mBinder.getService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "聊天服务执行");
        // 初始化websocket
        this.initChatWebSocket();
        // 开启心跳检测
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        // 保持服务
        acquireWakeLock();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "聊天服务销毁");
        closeConnect();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "聊天服务低内存");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "聊天服务解绑");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "聊天服务重新绑定");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "聊天服务绑定");
        return mBinder;
    }

    /**
     * 初始化WS
     */
    private void initChatWebSocket() {
        Log.d("ChatService", "初始化WebSocket客户端");
        URI uri = URI.create("ws://192.168.97.96:8080/webSocket/TOM");
        client = new ChatWebSocketClient(uri) {
            @Override
            public void onMessage(String messageString) {
                // message就是接收到的消息
                Log.d("ChatService", messageString);
                Message message = JSON.parseObject(messageString, Message.class);
                if (StringUtils.isNotBlank(message.getFrom())) {
                    // 发送通知
                    sendNotification(message);
                    // 发送广播
                    Intent broadcastIntent = new Intent(Action.CHAT_MESSAGE);
                    broadcastIntent.putExtra("message", messageString);
                    sendBroadcast(broadcastIntent);
                }
            }
        };
        connect();
    }

    /**
     * 发送通知
     * @param message
     */
    private void sendNotification(Message message) {
        // 设置通知跳转
        Intent intent = new Intent(context, ChatUserActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);
        // 创建消息渠道
        String channelId = "chat_message";
        String channelName = "聊天消息";
        initNotification(this, channelId, channelName);
        // 创建消息
        int notificationId = CHAT_MESSAGE + message.getFrom().getBytes(StandardCharsets.UTF_8).hashCode();
        sendNotification(this, pi, channelId, message.getFrom(), message.getText(), notificationId);
    }

    /**
     * 连接websocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 断开连接
     */
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }

    /**
     * 发送消息
     * @param msg
     */
    public void sendMsg(String msg) {
        if (null != client) {
            Log.e(TAG, "发送的消息：" + msg);
            client.send(msg);
        }
    }

    //    -------------------------------------websocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }
            } else {
                //如果client已为空，重新初始化连接
                client = null;
                initChatWebSocket();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    /**
     * 开启重连
     */
    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 是否登录
     * @return
     */
    public static boolean isLogin(Context context) {
        ChatUser chatUser = XmlUtil.pullXmlSingle(ChatUser.class, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_USER);
        return chatUser != null;
    }

    /**
     * 登录
     * @param chatUser
     * @param context
     * @return
     */
    public static boolean login(ChatUser chatUser, Context context) {
        return XmlUtil.parseSingle(chatUser, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_USER);
    }

    /**
     * 登出
     * @param context
     * @return
     */
    public static boolean logout(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_USER);
        return file.delete();
    }
}
