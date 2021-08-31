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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.adapter.ChatDetailAdapter;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatMessage;
import com.alin.android.app.service.ChatService;
import com.alin.android.app.socket.ChatWebSocketClient;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatDetailActivity extends BaseAppActivity {

    private Context context;
    private ChatWebSocketClient client;
    private ChatService.ChatWebSocketClientBinder binder;
    private String currentUsername;
    private String chatUsername;
    private List<ChatMessage> chatMessages;
    private ChatService chatService;
    private ChatMessageReceiver chatMessageReceiver;
    @BindView(R.id.chat_header_return)
    public ImageButton chatHeaderReturnBtn;
    @BindView(R.id.chat_header_title)
    public TextView chatHeaderTitleTv;
    @BindView(R.id.chat_message_list)
    public ListView chatMessageListView;
    @BindView(R.id.chat_content_et)
    public EditText chatContentEt;
    @BindView(R.id.chat_send_btn)
    public Button chatSendBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_detail_activity);
        ButterKnife.bind(this);

        context = ChatDetailActivity.this;


        // 初始化数据
        initData();
        // 初始化视图
        initView();

        // 绑定聊天室服务
        Intent bindIntent = new Intent(context, ChatService.class);
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

    /**
     * 服务绑定连接
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务与活动成功绑定
            Log.e(TAG, "服务与活动成功绑定");
            binder = (ChatService.ChatWebSocketClientBinder) iBinder;
            chatService = binder.getService();
            client = chatService.client;
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
            // 初始化数据
            initData();
            // 初始化视图
            initView();
        }
    }

    /**
     * 返回上层页
     * @param v
     */
    @OnClick(R.id.chat_header_return)
    public void onBackPage(View v) {
        Intent intent = new Intent(context, ChatUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void initData() {
        currentUsername = getIntent().getStringExtra(Constant.KEY_CHAT_USER_FROM);
        chatUsername = getIntent().getStringExtra(Constant.KEY_CHAT_USER_TO);
        chatMessages = ChatService.getChatMessage(currentUsername, chatUsername, context);
    }

    public void initView() {
        chatHeaderReturnBtn.setVisibility(View.VISIBLE);
        chatHeaderTitleTv.setText(chatUsername);
        // 初始化对话
        chatMessageListView.setAdapter(new ChatDetailAdapter(currentUsername, chatMessages));
        chatMessageListView.setSelection(chatMessages.size());
        // 监听输入框的变化
        chatContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (chatContentEt.getText().toString().length() > 0) {
                    chatSendBtn.setVisibility(View.VISIBLE);
                } else {
                    chatSendBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.chat_detail_layout)
    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.chat_content_et) {
            chatContentEt.clearFocus();
        }
    }

    /**
     * 发送信息
     * @param v
     */
    public void onSendMessage(View v) {
        String content = chatContentEt.getText().toString();
        if (content.length() <= 0) {
            showInfoDialog("消息不能为空哟");
            return;
        }

        if (client != null && client.isOpen()) {
            //暂时将发送的消息加入消息列表，实际以发送成功为准（也就是服务器返回你发的消息时）
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setFrom(currentUsername);
            chatMessage.setTo(chatUsername);
            chatMessage.setText(content);
            chatMessage.setDate(new Date());
            chatMessages.add(chatMessage);
            chatService.sendMessage(chatMessage);
            initData();
            initView();
            chatContentEt.setText("");
        } else {
            showInfoDialog("连接已断开，请稍等或重启App哟");
        }
    }
}
