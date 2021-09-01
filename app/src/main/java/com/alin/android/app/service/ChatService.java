package com.alin.android.app.service;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.alin.android.app.constant.NotificationId.CHAT_MESSAGE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alin.android.app.activity.chat.ChatUserActivity;
import com.alin.android.app.common.BaseAppService;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatMessage;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.socket.ChatWebSocketClient;
import com.alin.android.core.base.BaseNotification;
import com.alin.android.core.utils.FileUtil;
import com.alin.android.core.utils.XmlUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天室服务
 */
public class ChatService extends BaseAppService implements BaseNotification {

    private ChatUser user;
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
        mHandler.removeCallbacks(heartBeatRunnable);
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
        Log.d(TAG, "初始化WebSocket客户端");
        user = isLogin(this);
        String username = user==null?"Guest":user.getName();
        URI uri = URI.create(getEnvString(Constant.KEY_CHAT_WS_URL)+username);
        client = new ChatWebSocketClient(uri) {
            @Override
            public void onMessage(String messageString) {
                // message就是接收到的消息
                Log.d(TAG, messageString);
                ChatMessage chatMessage = JSON.parseObject(messageString, ChatMessage.class);
                if (StringUtils.isNotBlank(chatMessage.getFrom())) {
                    // 存储用户
                    saveChatUser(username, chatMessage.getFrom(), chatMessage);
                    // 存储消息
                    saveChatMessage(username, chatMessage.getFrom(), chatMessage);
                    // 发送通知
                    sendNotification(chatMessage);
                    // 发送广播
                    Intent broadcastIntent = new Intent(Action.CHAT_MESSAGE);
                    sendBroadcast(broadcastIntent);
                } else if (StringUtils.isNotBlank(chatMessage.getTo())) {
                    // 保存通讯录
                    saveChatBook(username, chatMessage.getText(), chatMessage.getDate(), "0".equals(chatMessage.getTo()));
                    // 发送广播
                    Intent broadcastIntent = new Intent(Action.CHAT_MESSAGE);
                    sendBroadcast(broadcastIntent);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                reconnectWs();
            }
        };
        connect();
    }

    /**
     * 发送通知
     * @param chatMessage
     */
    private void sendNotification(ChatMessage chatMessage) {
        // 设置通知跳转
        Intent intent = new Intent(context, ChatUserActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);
        // 创建消息渠道
        String channelId = "chat_message";
        String channelName = "聊天消息";
        initNotification(this, channelId, channelName);
        // 创建消息
        int notificationId = CHAT_MESSAGE + chatMessage.getFrom().getBytes(StandardCharsets.UTF_8).hashCode();
        sendNotification(this, pi, channelId, chatMessage.getFrom(), chatMessage.getText(), notificationId);
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
     * @param chatMessage
     */
    public void sendMessage(ChatMessage chatMessage) {
        if (null != client) {
            String message = JSONObject.toJSONString(chatMessage);
            Log.d(TAG, "发送的消息：" + message);
            // 存储用户
            saveChatUser(chatMessage.getFrom(), chatMessage.getTo(), chatMessage);
            // 储存消息
            saveChatMessage(chatMessage.getFrom(), chatMessage.getTo(), chatMessage);
            // 发送消息
            client.send(message);
        }
    }

    //    -------------------------------------websocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000;//每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "心跳包检测websocket连接状态");
            if (client != null) {
                if (!client.isOpen() || client.isClosed()) {
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
                // 发送重连广播
                Intent broadcastIntent = new Intent(Action.CHAT_MESSAGE_LOADING);
                sendBroadcast(broadcastIntent);
                boolean reconnectState;
                try {
                    Log.e(TAG, "开启重连");
                    reconnectState = client.reconnectBlocking();
                } catch (Exception e) {
                    e.printStackTrace();
                    reconnectState = false;
                }
                Log.d(TAG, "重连状态为:"+reconnectState);
                if (reconnectState) {
                    Log.d(TAG, "重连成功");
                    // 发送重连成功广播
                    broadcastIntent = new Intent(Action.CHAT_MESSAGE_RESTART);
                    sendBroadcast(broadcastIntent);
                } else {
                    Log.d(TAG, "重连失败");
                    // 发送重连失败广播
                    broadcastIntent = new Intent(Action.CHAT_MESSAGE_ERROR);
                    sendBroadcast(broadcastIntent);
                }
            }
        }.start();
    }

    /**
     * 是否登录
     * @return
     */
    public static ChatUser isLogin(Context context) {
        return XmlUtil.pullXmlSingle(ChatUser.class, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_LOGIN_USER);
    }

    /**
     * 登录
     * @param chatUser
     * @param context
     * @return
     */
    public static boolean login(ChatUser chatUser, Context context) {
        return XmlUtil.parseSingle(chatUser, context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_LOGIN_USER);
    }

    /**
     * 登出
     * @param context
     * @return
     */
    public static boolean logout(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), Constant.XML_CHAT_LOGIN_USER);
        return file.delete();
    }

    /**
     * 保存聊天用户
     * @param username
     * @param chatUsername
     * @param chatMessage
     */
    private void saveChatUser(String username, String chatUsername, ChatMessage chatMessage) {
        ChatUser chatUser = new ChatUser();
        chatUser.setName(chatUsername);
        chatUser.setLastChatMessage(chatMessage.getText());
        chatUser.setLastChatTime(chatMessage.getDate());
        List<ChatUser> chatUserList = getChatUserList(username, context);
        if (chatUserList != null && !chatUserList.isEmpty()) {
            List<String> chatUsernames = chatUserList.stream().map(ChatUser::getName).collect(Collectors.toList());
            if (chatUsernames.contains(chatUsername)) {
                chatUserList.removeIf(user -> chatUsername.equals(user.getName()));
            }
        } else {
            chatUserList = new ArrayList<>();
        }
        chatUserList.add(chatUser);
        setChatUserList(username, chatUserList, context);
    }

    /**
     * 保存通讯录
     * @param username
     * @param chatUsername
     * @param onlineTime
     * @param online
     */
    private void saveChatBook(String username, String chatUsername, Date onlineTime, boolean online) {
        List<ChatUser> chatUserList = getChatBook(username, context);
        if (chatUserList != null && !chatUserList.isEmpty()) {
            List<String> chatUsernames = chatUserList.stream().map(ChatUser::getName).collect(Collectors.toList());
            if (chatUsernames.contains(chatUsername)) {
                chatUserList.removeIf(user -> chatUsername.equals(user.getName()));
            }
        } else {
            chatUserList = new ArrayList<>();
        }
        if (online) {
            ChatUser chatUser = new ChatUser();
            chatUser.setName(chatUsername);
            chatUser.setLastChatTime(onlineTime);
            chatUserList.add(chatUser);
        }
        setChatBook(username, chatUserList, context);
    }

    /**
     * 保存通讯录
     * @param username
     * @param chatUsername
     */
    public static void saveChatBook(String username, List<String> chatUsername, Context context) {
        List<ChatUser> chatUserList = new ArrayList<>();
        if (chatUsername != null && !chatUsername.isEmpty()) {
            for (String cun : chatUsername) {
                ChatUser chatUser = new ChatUser();
                chatUser.setName(cun);
                chatUser.setLastChatTime(new Date());
                chatUserList.add(chatUser);
            }
        }
        setChatBook(username, chatUserList, context);
    }

    /**
     * 保存聊天信息
     * @param username
     * @param chatUsername
     * @param chatMessage
     */
    private void saveChatMessage(String username, String chatUsername, ChatMessage chatMessage) {
        List<ChatMessage> chatMessages = getChatMessage(username, chatUsername, context);
        if (chatMessages == null || chatMessages.isEmpty()) {
            chatMessages = new ArrayList<>();
        }
        chatMessages.add(chatMessage);
        setChatMessage(username, chatUsername, chatMessages, context);
    }

    /**
     * 设置聊天用户列表
     * @param username
     * @param chatUsers
     * @param context
     * @return
     */
    public static boolean setChatUserList(String username, List<ChatUser> chatUsers, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username;
        FileUtil.mkdirs(filePath);
        return XmlUtil.parse(chatUsers, filePath, Constant.XML_CHAT_USER);
    }

    /**
     * 获取聊天用户列表
     * @param username
     * @param context
     * @return
     */
    public static List<ChatUser> getChatUserList(String username, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username;
        return XmlUtil.pullXml(ChatUser.class, filePath, Constant.XML_CHAT_USER);
    }

    /**
     * 设置通讯录
     * @param username
     * @param chatUsers
     * @param context
     * @return
     */
    public static boolean setChatBook(String username, List<ChatUser> chatUsers, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username;
        FileUtil.mkdirs(filePath);
        return XmlUtil.parse(chatUsers, filePath, Constant.XML_CHAT_BOOK);
    }

    /**
     * 获取通讯录
     * @param username
     * @param context
     * @return
     */
    public static List<ChatUser> getChatBook(String username, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username;
        return XmlUtil.pullXml(ChatUser.class, filePath, Constant.XML_CHAT_BOOK);
    }

    /**
     * 设置聊天信息
     * @param username
     * @param chatUsername
     * @param message
     * @param context
     * @return
     */
    public static boolean setChatMessage(String username, String chatUsername, List<ChatMessage> message, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username + "/" + chatUsername;
        FileUtil.mkdirs(filePath);
        String fileName = String.format(Constant.XML_CHAT_MESSAGE_PREFIX, "", "") + Constant.XML_SUFFIX;
        return XmlUtil.parse(message, filePath, fileName);
    }

    /**
     * 获取聊天信息
     * @param username
     * @param chatUsername
     * @param context
     * @return
     */
    public static List<ChatMessage> getChatMessage(String username, String chatUsername, Context context) {
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + username + "/" + chatUsername;
        FileUtil.mkdirs(filePath);
        String fileName = String.format(Constant.XML_CHAT_MESSAGE_PREFIX, "", "") + Constant.XML_SUFFIX;
        return XmlUtil.pullXml(ChatMessage.class, filePath, fileName);
    }
}
