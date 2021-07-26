package com.alin.android.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.alin.android.app.R;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.constant.Constant;
import com.just.agentweb.AgentWeb;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-09 16:39
 **/
public class BrowserActivity extends BaseAppActivity {

    private AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private long exitTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_activity);

        // 获取跳转地址
        Intent intent = getIntent();
        String url = Constant.BROWSER_DEFAULT_URL;
        String intentUrl = intent.getStringExtra(Constant.KEY_BROWSER_URL);
        if (StringUtils.isNotBlank(intentUrl)) {
            url = intentUrl;
        }

        mLinearLayout = (LinearLayout) findViewById(R.id.container);
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent((LinearLayout) mLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(url);
    }

    @Override
    public void onBackPressed() {
        if (mAgentWeb.back()) {
            super.onBackPressed();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出浏览器",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }
}
