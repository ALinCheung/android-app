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
import com.alin.android.app.api.chat.ChatApi;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.core.base.BaseFragment;
import com.alin.android.core.manager.RetrofitManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Retrofit;

/**
 * 聊天通讯录
 */
public class ChatBookFragment extends BaseFragment {

    @BindView(R.id.chat_header_title)
    public TextView chatHeaderTitleTv;
    @BindView(R.id.chat_book_list)
    public ListView charBookListView;

    private Context context;
    private Retrofit chatRetrofit;
    private Unbinder unbinder;
    private ChatBookFragment.ChatMessageReceiver chatMessageReceiver;
    private ChatUser user;
    private List<ChatUser> chatUsers;

    public ChatBookFragment(Context context, Retrofit chatRetrofit, ChatUser user) {
        this.context = context;
        this.chatRetrofit = chatRetrofit;
        this.user = user;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_book;
    }

    @Override
    protected void initContent() {
        // 注解绑定视图
        unbinder = ButterKnife.bind(this, view);
        // 初始化数据
        initDataAsync();
        // 动态注册广播接收器
        chatMessageReceiver = new ChatBookFragment.ChatMessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Action.CHAT_MESSAGE);
        filter.addAction(Action.CHAT_MESSAGE_RESTART);
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
    private void initData(Intent intent) {
        // 判断动作来源
        String action = intent.getAction() == null ? Action.CHAT_MESSAGE : intent.getAction();
        if (action.equals(Action.CHAT_MESSAGE_RESTART)) {
            initDataAsync();
        } else {
            initView();
        }
    }

    /**
     * 初始化数据(异步)
     */
    private void initDataAsync() {
        // 获取当前登录用户
        chatRetrofit.create(ChatApi.class).onlineUsers(user.getName())
                .compose(RetrofitManager.<Set<String>>ioMain())
                .subscribe(new BaseAppObserver<Set<String>>(){
                    @Override
                    public void onAccept(Set<String> o, String error) {
                        super.onAccept(o, error);
                        List<String> chatBookOnline = StringUtils.isBlank(error)?new ArrayList<>(o):new ArrayList<>();
                        // 保存数据
                        ChatService.saveChatBook(user.getName(), chatBookOnline, context);
                        // 初始化界面
                        initView();
                    }
                });
    }

    /**
     * 初始化用户列表界面
     */
    private void initView() {
        // 设置列表数据
        chatUsers = ChatService.getChatBook(user.getName(), context);
        sortChatBook();
        // 设置头标题
        chatHeaderTitleTv.setText(Constant.STRING_CHAT_BOOK);
        // 设置用户列表
        charBookListView.setAdapter(new BaseCoreAdapter<ChatUser>(chatUsers, R.layout.item_chat_user_book, context) {
            @Override
            public void bindView(ViewHolder holder, ChatUser chatUser) {
                holder.setText(R.id.chat_user_name, chatUser.getName());
            }
        });
        charBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
            // 初始化数据
            initData(intent);
        }
    }

    /**
     * 用户列表排序
     */
    private void sortChatBook() {
        chatUsers = chatUsers.stream().sorted(new Comparator<ChatUser>() {
            @Override
            public int compare(ChatUser o1, ChatUser o2) {
                // 按名称字符串排序
                return o1.getName().compareTo(o2.getName());
            }
        }).collect(Collectors.toList());
    }
}
