package com.alin.android.app.activity.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.alin.android.app.R;
import com.alin.android.app.activity.MainActivity;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.fragment.chat.ChatBookFragment;
import com.alin.android.app.fragment.chat.ChatFragment;
import com.alin.android.app.fragment.chat.ChatUserFragment;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.manager.RetrofitManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;

/**
 * 聊天功能
 */
public class ChatUserActivity extends BaseAppActivity {

    private Context context;
    private ChatFragment chatFragment;
    private ChatBookFragment chatBookFragment;
    private ChatUserFragment chatUserFragment;
    private Retrofit chatRetrofit;
    private ChatUser user;

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

        // 初始化界面
        initChatFragment();
    }

    @OnClick({R.id.chat_menu_chat, R.id.chat_menu_book, R.id.chat_menu_user})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_menu_chat:
                initChatFragment();
                break;
            case R.id.chat_menu_book:
                initChatBookFragment();
                break;
            case R.id.chat_menu_user:
                initChatUserFragment();
                break;
            default:
                break;
        }
    }

    /**
     * 返回上层页
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 初始化聊天
     */
    private void initChatFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (chatFragment == null) {
            chatFragment = new ChatFragment(context, getIntent(), user);
            transaction.add(R.id.chat_fragment, chatFragment);
        }
        hideAllFragment(transaction);
        transaction.show(chatFragment);
        transaction.commit();
    }

    /**
     * 初始化通讯录
     */
    private void initChatBookFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (chatBookFragment == null) {
            chatBookFragment = new ChatBookFragment(context, chatRetrofit, user);
            transaction.add(R.id.chat_fragment, chatBookFragment);
        }
        hideAllFragment(transaction);
        transaction.show(chatBookFragment);
        transaction.commit();
    }

    /**
     * 初始化用户
     */
    private void initChatUserFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (chatUserFragment == null) {
            chatUserFragment = new ChatUserFragment(context);
            transaction.add(R.id.chat_fragment, chatUserFragment);
        }
        hideAllFragment(transaction);
        transaction.show(chatUserFragment);
        transaction.commit();
    }

    /**
     * 隐藏所有Fragment
     * @param transaction
     */
    private void hideAllFragment(FragmentTransaction transaction) {
        if (chatFragment != null) {
            transaction.hide(chatFragment);
        }
        if (chatBookFragment != null) {
            transaction.hide(chatBookFragment);
        }
        if (chatUserFragment != null) {
            transaction.hide(chatUserFragment);
        }
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
}
