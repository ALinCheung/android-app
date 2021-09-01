package com.alin.android.app.fragment.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alin.android.app.R;
import com.alin.android.app.activity.chat.ChatDetailActivity;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.core.base.BaseFragment;
import com.alin.android.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 聊天首页
 */
public class ChatFragment extends BaseFragment {

    @BindView(R.id.chat_header_title)
    public TextView chatHeaderTitleTv;
    @BindView(R.id.chat_user_list)
    public ListView userListView;

    private Context context;
    private Intent intent;
    private Unbinder unbinder;
    private ChatFragment.ChatMessageReceiver chatMessageReceiver;
    private ChatUser user;
    private List<ChatUser> chatUsers;

    public ChatFragment(Context context, Intent intent, ChatUser user) {
        this.context = context;
        this.intent = intent;
        this.user = user;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initContent() {
        // 注解绑定视图
        unbinder = ButterKnife.bind(this, view);
        // 获取当前聊天用户列表
        initData();
        // 初始化界面
        initView(intent);
        // 动态注册广播接收器
        chatMessageReceiver = new ChatFragment.ChatMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Action.CHAT_MESSAGE);
        filter.addAction(Action.CHAT_MESSAGE_LOADING);
        filter.addAction(Action.CHAT_MESSAGE_ERROR);
        context.registerReceiver(chatMessageReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (chatMessageReceiver != null) {
            context.unregisterReceiver(chatMessageReceiver);
        }
        super.onDestroyView();
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
        if (chatUsers != null && !chatUsers.isEmpty()) {
            chatUsers = chatUsers.stream().sorted(new Comparator<ChatUser>() {
                @Override
                public int compare(ChatUser o1, ChatUser o2) {
                    // 按最新消息时间排序
                    return Long.compare(o2.getLastChatTime().getTime(), o1.getLastChatTime().getTime());
                }
            }).collect(Collectors.toList());
        }
    }

    /**
     * 初始化用户列表界面
     */
    private void initView(Intent intent) {
        // 设置页面标题
        String action = intent.getAction() == null ? Action.CHAT_MESSAGE : intent.getAction();
        switch (action) {
            case Action.CHAT_MESSAGE_LOADING:
                chatHeaderTitleTv.setText(Constant.STRING_CHAT_LOADING);
                break;
            case Action.CHAT_MESSAGE_ERROR:
                chatHeaderTitleTv.setText(Constant.STRING_CHAT_ERROR);
                break;
            default:
                chatHeaderTitleTv.setText(Constant.STRING_CHAT);
                break;
        }
        // 设置用户列表
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

    /**
     * 聊天广播接收器
     */
    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取当前聊天用户列表
            initData();
            // 初始化界面
            initView(intent);
        }
    }
}
