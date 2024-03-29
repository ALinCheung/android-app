package com.alin.android.app.activity.chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.activity.MainActivity;
import com.alin.android.app.api.chat.ChatApi;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.constant.Constant;
import com.alin.android.app.model.ChatUser;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.manager.RetrofitManager;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;

public class ChatLoginActivity extends BaseAppActivity {

    private Context context;
    private Retrofit chatRetrofit;
    @BindView(R.id.chat_header_return)
    public ImageButton chatHeaderReturnBtn;
    @BindView(R.id.chat_header_title)
    public TextView chatHeaderTitleTv;
    @BindView(R.id.chat_username)
    public EditText usernameEditText;
    @BindView(R.id.chat_password)
    public EditText passwordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_login_activity);
        ButterKnife.bind(this);

        context = ChatLoginActivity.this;
        chatRetrofit = RetrofitManager.getInstance(context, getEnvString(Constant.KEY_CHAT_API_URL));

        chatHeaderReturnBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 返回上层页
     * @param v
     */
    @OnClick(R.id.chat_header_return)
    public void onBackPage(View v) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 登录聊天
     * @param v
     */
    public void onLoginChat(View v) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        Log.e(TAG, "username: " + username + ", password: " + password);
        // 用户登录
        chatRetrofit.create(ChatApi.class).loginValidate(username, password)
                .compose(RetrofitManager.<String>ioMain())
                .subscribe(new BaseAppObserver<String>(this, true){
                    @Override
                    public void onAccept(String o, String error) {
                        super.onAccept(o, error);
                        if (StringUtils.isBlank(error)) {
                            if (!o.contains("密码错误")) {
                                // 保存用户信息
                                ChatService.login(new ChatUser(3L, username), context);
                                // 重启聊天服务
                                Intent chatServiceIntent = new Intent(context, ChatService.class);
                                stopService(chatServiceIntent);
                                startService(chatServiceIntent);
                                // 跳转聊天用户列表
                                Intent intent = new Intent(context, ChatUserActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                showInfoDialog("登录失败");
                            }
                        }
                    }
                });
    }
}
