package com.alin.android.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.core.constant.AppStatusConstant;
import com.alin.android.core.manager.AppStatusManager;
import com.alin.app.R;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * @Description 闪屏页
 * @Author zhangwl
 * @Date 2021/7/6 9:37
 */
public class SplashActivity extends BaseAppActivity {

    public final static int SPLASH_PROCESS = 1;
    public final static int SPLASH_END = 2;
    private final static int PROCESS_SECOND = 3;
    private Context context;
    private Handler mHandle;

    @BindView(R.id.splash_second)
    public TextView mSplashSecondTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        this.context = SplashActivity.this;
        this.mHandle = new SplashHandle(this);
        // 定时关闭
        SplashActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int second = PROCESS_SECOND; second >= 0; second--) {
                    mHandle.sendMessageDelayed(Message.obtain(mHandle, SPLASH_PROCESS, second, 0, mSplashSecondTv), (PROCESS_SECOND - second) * 1000);
                }
                mHandle.sendEmptyMessageDelayed(SPLASH_END, (PROCESS_SECOND + 1) * 1000);
                // 版本检测状态
                AppStatusManager.getInstance().setAppStatus(AppStatusConstant.STATUS_VERSION_CHECK);
            }
        });
    }

    public static class SplashHandle extends Handler {

        private WeakReference<SplashActivity> mWeakReference;

        SplashHandle(SplashActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashActivity splashActivity = mWeakReference.get();
            switch (msg.what) {
                case SPLASH_PROCESS:
                    int second = msg.arg1;
                    TextView mSplashSecondTv = (TextView) msg.obj;
                    Log.i(this.getClass().getName(), "闪屏页秒数" + String.valueOf(second) + ", 当前时间" + new Date().toString());
                    mSplashSecondTv.setText(String.valueOf(second));
                    break;
                case SPLASH_END:
                    splashActivity.startActivity(new Intent(splashActivity, MainActivity.class));
                    splashActivity.finish();
                    Log.i(this.getClass().getName(), "闪屏页结束, 当前时间" + new Date().toString());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 屏蔽返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}
