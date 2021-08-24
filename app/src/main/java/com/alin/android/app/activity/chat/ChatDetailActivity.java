package com.alin.android.app.activity.chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.service.ChatService;
import com.alin.android.app.socket.ChatWebSocketClient;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatDetailActivity extends BaseAppActivity {

    private Context context;
    private ChatWebSocketClient client;
    private ChatService.ChatWebSocketClientBinder binder;
    private ChatService chatService;
    private ChatMessageReceiver chatMessageReceiver;
    @BindView(R.id.chatmsg_listView)
    public ListView userListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_detail_activity);
        ButterKnife.bind(this);

        this.context = ChatDetailActivity.this;

        // 绑定聊天室服务
        Intent bindIntent = new Intent(context, ChatService.class);
        bindIntent.putExtra("username", "TOM");
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        // 动态注册广播接收器
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter(Action.CHAT_MESSAGE);
        registerReceiver(chatMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        if (chatMessageReceiver != null) {
            unregisterReceiver(chatMessageReceiver);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e(TAG, "服务与活动成功绑定");
            binder = (ChatService.ChatWebSocketClientBinder) iBinder;
            ChatService service = binder.getService();
            client = service.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //服务与活动断开
            Log.e(TAG, "服务与活动成功断开");
        }
    };

    /**
     * 聊天广播接收器
     */
    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.e(TAG, "广播接收:" + message);
        }
    }
}
