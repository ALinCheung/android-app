package com.alin.customapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.alin.customapp.R;
import com.alin.customapp.common.BaseActivity;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-09 16:39
 **/
public class BrowserActivity extends BaseActivity {

    private WebView webView;
    private long exitTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.browser_activity);

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().getPath());
                return true;
            }
        });
        //设置WebView属性,运行执行js脚本
        webView.getSettings().setJavaScriptEnabled(true);
        //调用loadView方法为WebView加入链接
        webView.loadUrl("https://www.baidu.com/");
        webView.getSettings().setSavePassword(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(true);
        // 扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        // 自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        //调用Activity提供的setContentView将webView显示出来
        setContentView(webView);
    }

    //我们需要重写回退按钮的时间,当用户点击回退按钮：
    //1.webView.canGoBack()判断网页是否能后退,可以则goback()
    //2.如果不可以连续点击两次退出App,否则弹出提示Toast
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
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
}
