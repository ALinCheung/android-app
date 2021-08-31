package com.alin.android.app.fragment.chat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.alin.android.app.R;
import com.alin.android.app.activity.chat.ChatLoginActivity;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 聊天用户
 */
public class ChatUserFragment extends BaseFragment {

    @BindView(R.id.chat_header_title)
    public TextView chatHeaderTitleTv;

    private Context context;
    private Unbinder unbinder;

    public ChatUserFragment(Context context) {
        this.context = context;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_user;
    }

    @Override
    protected void initContent() {
        // 注解绑定视图
        unbinder = ButterKnife.bind(this, view);
        // 初始化数据
        initData();
        // 初始化界面
        initView();
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }

    @OnClick({R.id.logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logout:
                ChatService.logout(context);
                // 重启聊天服务
                Intent chatServiceIntent = new Intent(context, ChatService.class);
                context.stopService(chatServiceIntent);
                context.startService(chatServiceIntent);
                // 跳转登录页
                toLoginPage();
                break;
            default:
                break;
        }
    }

    /**
     * 跳转登录页
     */
    public void toLoginPage() {
        Intent intent = new Intent(context, ChatLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * 初始化用户数据
     */
    private void initData() {

    }

    /**
     * 初始化用户列表界面
     */
    private void initView() {
        // 设置头标题
        chatHeaderTitleTv.setText(Constant.STRING_CHAT_USER);
    }
}
