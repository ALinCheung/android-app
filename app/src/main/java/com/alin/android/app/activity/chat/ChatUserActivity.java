package com.alin.android.app.activity.chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.activity.MainActivity;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

public class ChatUserActivity extends BaseAppActivity {

    private Context context;
    private Retrofit chatRetrofit;
    private ChatMessageReceiver chatMessageReceiver;
    private ChatUser user;
    private List<ChatUser> chatUsers;
    @BindView(R.id.chat_user_list)
    public ListView userListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_user_activity);
        ButterKnife.bind(this);

        context = ChatUserActivity.this;
        chatRetrofit = RetrofitManager.getInstance(context, getEnvString(Constant.KEY_CHAT_API_URL));

        // 是否登录
        user = ChatService.isLogin(context);
        if (user == null) {
            // 跳转至登录
            toLoginPage();
            return;
        }

        // 获取当前聊天用户列表
        initData();
        // 初始化界面
        initView();

        // 获取当前登录用户
        /*chatRetrofit.create(ChatApi.class).onlineUsers(user.getName())
                .compose(RetrofitManager.<Set<String>>ioMain())
                .subscribe(new BaseAppObserver<Set<String>>(this, true){
                    @Override
                    public void onAccept(Set<String> o, String error) {
                        super.onAccept(o, error);
                        if (StringUtils.isBlank(error)) {
                            for (String username : o) {

                            }
                        }
                    }
                });*/

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 聊天广播接收器
     */
    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取当前聊天用户列表
            initData();
            // 初始化界面
            initView();
        }
    }

    /**
     * 登出
     * @param v
     */
    public void onLogoutChat(View v) {
        ChatService.logout(context);
        // 重启聊天服务
        Intent chatServiceIntent = new Intent(context, ChatService.class);
        stopService(chatServiceIntent);
        startService(chatServiceIntent);
        // 跳转登录页
        toLoginPage();
    }

    /**
     * 跳转登录页
     */
    public void toLoginPage() {
        Intent intent = new Intent(this, ChatLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 初始化用户数据
     */
    private void initData() {
        chatUsers = ChatService.getChatUserList(user.getName(), context);
        if (chatUsers == null) {
            chatUsers = new ArrayList<>();
        }
        sortChatUsers();
    }

    /**
     * 用户列表排序
     */
    private void sortChatUsers() {
        chatUsers = chatUsers.stream().sorted(new Comparator<ChatUser>() {
            @Override
            public int compare(ChatUser o1, ChatUser o2) {
                // 按最新消息时间排序
                return Long.compare(o2.getLastChatTime().getTime(), o1.getLastChatTime().getTime());
            }
        }).collect(Collectors.toList());
    }

    /**
     * 初始化用户列表界面
     */
    private void initView() {
        Log.d(TAG, chatUsers.toString());
        userListView.setAdapter(new BaseCoreAdapter<ChatUser>(chatUsers, R.layout.item_chat_user, context) {
            @Override
            public void bindView(ViewHolder holder, ChatUser chatUser) {
                holder.setText(R.id.chat_user_name, chatUser.getName());
                holder.setText(R.id.chat_last_time, DateUtil.format(chatUser.getLastChatTime(), DateUtil.DATEFORMATSECOND));
                holder.setText(R.id.chat_last_message, chatUser.getLastChatMessage());
            }
        });
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.chat_user_name);
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra(Constant.KEY_CHAT_USER_FROM, user.getName());
                intent.putExtra(Constant.KEY_CHAT_USER_TO, textView.getText());
                startActivity(intent);
            }
        });
    }
}
