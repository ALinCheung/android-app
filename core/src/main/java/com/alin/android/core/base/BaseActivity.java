package com.alin.android.core.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alin.android.core.R;
import com.alin.android.core.constant.AppStatus;
import com.alin.android.core.manager.AppStatusManager;
import com.jaeger.library.StatusBarUtil;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.view.listener.OnButtonClickListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * @description: 基础活动
 * @author: Create By ZhangWenLin
 * @create: 2021年7月9日12:52:03
 **/
public abstract class BaseActivity extends AppCompatActivity implements BaseNotification, BGASwipeBackHelper.Delegate, View.OnClickListener {
    protected BGASwipeBackHelper mSwipeBackHelper;
    protected PromptDialog promptDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(this.getClass().getName(), "开启页面");
        // 「必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回」
        // 在 super.onCreate(savedInstanceState) 之前调用该方法
        initSwipeBackFinish();
        super.onCreate(savedInstanceState);
        // 绑定注入view, 如无使用注解进行绑定则会报错
        //ButterKnife.bind(this);
        // 设置屏幕旋转
        setScreenRoate(true);
        // 设置状态栏
        //setStatusBar();
        // 设置启动页或者闪屏页
        //setStartPage();
    }

    /**
     * 初始化滑动返回。在 super.onCreate(savedInstanceState) 之前调用该方法
     */
    private void initSwipeBackFinish() {
        mSwipeBackHelper = new BGASwipeBackHelper(this, this);

        // 「必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回」
        // 下面几项可以不配置，这里只是为了讲述接口用法。

        // 设置滑动返回是否可用。默认值为 true
        mSwipeBackHelper.setSwipeBackEnable(true);
        // 设置是否仅仅跟踪左侧边缘的滑动返回。默认值为 true
        mSwipeBackHelper.setIsOnlyTrackingLeftEdge(true);
        // 设置是否是微信滑动返回样式。默认值为 true
        mSwipeBackHelper.setIsWeChatStyle(true);
        // 设置阴影资源 id。默认值为 R.drawable.bga_sbl_shadow
        mSwipeBackHelper.setShadowResId(R.drawable.bga_sbl_shadow);
        // 设置是否显示滑动返回的阴影效果。默认值为 true
        mSwipeBackHelper.setIsNeedShowShadow(true);
        // 设置阴影区域的透明度是否根据滑动的距离渐变。默认值为 true
        mSwipeBackHelper.setIsShadowAlphaGradient(true);
        // 设置触发释放后自动滑动返回的阈值，默认值为 0.3f
        mSwipeBackHelper.setSwipeBackThreshold(0.3f);
        // 设置底部导航条是否悬浮在内容上，默认值为 false
        mSwipeBackHelper.setIsNavigationBarOverlap(false);
    }

    /**
     * 是否支持滑动返回。这里在父类中默认返回 true 来支持滑动返回，如果某个界面不想支持滑动返回则重写该方法返回 false 即可
     *
     * @return
     */
    @Override
    public boolean isSupportSwipeBack() {
        return true;
    }

    /**
     * 正在滑动返回
     *
     * @param slideOffset 从 0 到 1
     */
    @Override
    public void onSwipeBackLayoutSlide(float slideOffset) {

    }

    /**
     * 没达到滑动返回的阈值，取消滑动返回动作，回到默认状态
     */
    @Override
    public void onSwipeBackLayoutCancel() {

    }

    /**
     * 滑动返回执行完毕，销毁当前 Activity
     */
    @Override
    public void onSwipeBackLayoutExecuted() {
        mSwipeBackHelper.swipeBackward();
    }

    @Override
    public void onBackPressed() {
        // 正在滑动返回的时候取消返回按钮事件
        if (mSwipeBackHelper.isSliding()) {
            return;
        }
        mSwipeBackHelper.backward();
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * 设置屏幕横竖屏切换
     * @param screenRoate true  竖屏     false  横屏
     */
    private void setScreenRoate(Boolean screenRoate) {
        if (screenRoate) {
            //设置竖屏模式
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 沉浸式实现
     */
    protected void setStatusBar() {
        StatusBarUtil.setColorForSwipeBack(this, 0, 0);
        // StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
    }

    /**
     * 设置启动页, 如果启动页为main则设置为null
     */
    protected Class getStartActivity() {
        return null;
    }

    protected void setStartPage() {
        if (AppStatusManager.getInstance().appStatus == AppStatus.STATUS_FORCE_KILLED) {
            startApp(getStartActivity());
        }
    }

    private void startApp(Class clz) {
        if (clz != null) {
            Intent intent = new Intent(this, clz);
            // 清空页面栈并启动
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 获取assets文件字符串内容
     * @param fileName 文件地址
     * @return
     */
    protected String getAssetsString(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                getAssets().open(fileName), "UTF-8") )) {
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 获取文件字符串内容
     * @param fileName 文件地址
     * @return
     */
    protected String getFileString(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8") )) {
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 信息提示框
     */
    public void showInfoDialog(String msg) {
        new CircleDialog.Builder()
                .setTitle("信息")
                .configTitle(params -> params.isShowBottomDivider = true)
                .setWidth(0.7f)
                .setText(msg)
                .setPositive("确定", null)
                .show(getSupportFragmentManager());
    }

    /**
     * 信息提示框
     */
    public void showInfoDialog(String msg, OnButtonClickListener positiveListener, OnButtonClickListener negativeListener) {
        new CircleDialog.Builder()
                .setTitle("信息")
                .configTitle(params -> params.isShowBottomDivider = true)
                .setWidth(0.7f)
                .setText(msg)
                .setPositive("确定", positiveListener)
                .setNegative("取消", negativeListener)
                .show(getSupportFragmentManager());
    }

    /**
     * 错误提示框
     */
    public void showErrorDialog(String msg) {
        if (promptDialog == null) {
            promptDialog = new PromptDialog(this);
        }
        promptDialog.showError(msg);
    }

    /**
     * 成功提示框
     */
    public void showSuccessDialog(String msg) {
        if (promptDialog == null) {
            promptDialog = new PromptDialog(this);
        }
        promptDialog.showSuccess(msg);
    }

    /**
     * 加载框
     */
    public void showLoadingDialog(String msg) {
        if (promptDialog == null) {
            promptDialog = new PromptDialog(this);
        }
        promptDialog.showLoading(msg);
    }

    /**
     * 关闭提示框
     */
    public void dismissDialog() {
        if (promptDialog == null) {
            promptDialog = new PromptDialog(this);
        }
        promptDialog.dismiss();
    }
}
