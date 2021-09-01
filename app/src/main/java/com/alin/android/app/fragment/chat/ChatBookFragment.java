package com.alin.android.app.fragment.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alin.android.app.R;
import com.alin.android.app.activity.chat.ChatDetailActivity;
import com.alin.android.app.api.chat.ChatApi;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.constant.Action;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatMessage;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.base.BaseCoreAdapter;
import com.alin.android.core.base.BaseFragment;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.utils.CharUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.BGARefreshViewHolder;
import retrofit2.Retrofit;

/**
 * 聊天通讯录
 */
public class ChatBookFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    @BindView(R.id.fragment_chat_book)
    public BGARefreshLayout mRefreshLayout;
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
        // 初始化下拉刷新
        initRefreshLayout();
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
     * 初始化下拉刷新
     */
    private void initRefreshLayout() {
        // 为BGARefreshLayout 设置代理
        mRefreshLayout.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
//        BGARefreshViewHolder refreshViewHolder = new BGARefreshViewHolder(context, true) {
//            @Override
//            public View getRefreshHeaderView() {
//                return null;
//            }
//
//            @Override
//            public void handleScale(float scale, int moveYDistance) {
//
//            }
//
//            @Override
//            public void changeToIdle() {
//
//            }
//
//            @Override
//            public void changeToPullDown() {
//
//            }
//
//            @Override
//            public void changeToReleaseRefresh() {
//
//            }
//
//            @Override
//            public void changeToRefreshing() {
//
//            }
//
//            @Override
//            public void onEndRefreshing() {
//
//            }
//        };
        // 设置下拉刷新和上拉加载更多的风格
        // mRefreshLayout.setRefreshViewHolder(refreshViewHolder);

        // 为了增加下拉刷新头部和加载更多的通用性，提供了以下可选配置选项  -------------START
        // 设置正在加载更多时不显示加载更多控件
        // mRefreshLayout.setIsShowLoadingMoreView(false);
        // 设置正在加载更多时的文本
        //refreshViewHolder.setLoadingMoreText(loadingMoreText);
        // 设置整个加载更多控件的背景颜色资源 id
        //refreshViewHolder.setLoadMoreBackgroundColorRes(loadMoreBackgroundColorRes);
        // 设置整个加载更多控件的背景 drawable 资源 id
        //refreshViewHolder.setLoadMoreBackgroundDrawableRes(loadMoreBackgroundDrawableRes);
        // 设置下拉刷新控件的背景颜色资源 id
        //refreshViewHolder.setRefreshViewBackgroundColorRes(refreshViewBackgroundColorRes);
        // 设置下拉刷新控件的背景 drawable 资源 id
        //refreshViewHolder.setRefreshViewBackgroundDrawableRes(refreshViewBackgroundDrawableRes);
        // 设置自定义头部视图（也可以不用设置）     参数1：自定义头部视图（例如广告位）， 参数2：上拉加载更多是否可用
        //mRefreshLayout.setCustomHeaderView(mBanner, false);
        // 可选配置  -------------END
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
        initDataAsync(null);
    }

    private void initDataAsync(BGARefreshLayout refreshLayout) {
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
                        // 下拉刷新
                        if (refreshLayout != null) {
                            refreshLayout.endRefreshing();
                        }
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
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getIsGroupKey()) {
                    return 0;// 返回的数据位角标
                } else {
                    return 1;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ChatUser chatUser = getItem(position);
                ViewHolder holder;
                if (chatUser.getIsGroupKey()) {
                    holder = ViewHolder.bind(mContext != null?mContext:parent.getContext(), convertView, parent, R.layout.item_chat_user_book_key, position);
                } else {
                    holder = ViewHolder.bind(mContext != null?mContext:parent.getContext(), convertView, parent, R.layout.item_chat_user_book, position);
                }
                bindView(holder, chatUser);
                return holder.getItemView();
            }

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

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        // 初始化数据
        initDataAsync(mRefreshLayout);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
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
        if (chatUsers != null && !chatUsers.isEmpty()) {
            List<ChatUser> sortChatUsers = new ArrayList<>();
            chatUsers = chatUsers.stream().sorted(new Comparator<ChatUser>() {
                @Override
                public int compare(ChatUser o1, ChatUser o2) {
                    // 按名称字符串排序
                    return CharUtil.getFullSpell(o1.getName()).compareTo(CharUtil.getFullSpell(o2.getName()));
                }
            }).collect(Collectors.toList());
            // 根据首字母分组
            for (int i = 1; i <= 26; i++) {
                String word = String.valueOf((char) (96 + i)).toUpperCase(Locale.ROOT);
                List<ChatUser> wordChatUsers = new ArrayList<>();
                for (ChatUser chatUser : chatUsers) {
                    if (word.equals(CharUtil.getFullSpell(chatUser.getName()).substring(0, 1).toUpperCase(Locale.ROOT))) {
                        wordChatUsers.add(chatUser);
                    }
                }
                if (!wordChatUsers.isEmpty()) {
                    sortChatUsers.add(new ChatUser(word, true));
                    sortChatUsers.addAll(wordChatUsers);
                }
            }
            chatUsers = sortChatUsers;
        }
    }
}
