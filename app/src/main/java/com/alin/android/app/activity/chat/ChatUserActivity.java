package com.alin.android.app.activity.chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.app.socket.ChatWebSocketClient;
import com.alin.android.core.utils.XmlUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatUserActivity extends BaseAppActivity {

    private Context context;
    private ChatMessageReceiver chatMessageReceiver;
    @BindView(R.id.chat_user_list)
    public ListView userListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_user_activity);
        ButterKnife.bind(this);

        this.context = ChatUserActivity.this;

        if (!ChatService.isLogin(context)) {
            Intent intent = new Intent(context, ChatLoginActivity.class);
            startActivity(intent);
        }

        // 动态注册广播接收器
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter(Action.CHAT_MESSAGE);
        registerReceiver(chatMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatMessageReceiver != null) {
            unregisterReceiver(chatMessageReceiver);
        }
    }

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

    /**
     * 登出
     * @param v
     */
    public void onLogoutChat(View v) {
        ChatService.logout(context);
        // 跳转登录页
        Intent intent = new Intent(this, ChatLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
