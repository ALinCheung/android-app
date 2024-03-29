package com.alin.android.app.activity;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.alin.android.app.constant.Constant.KEY_APP_VERSION_CHECK;
import static com.alin.android.app.constant.NotificationId.APP_VERSION_CHECK;
import static com.mylhyl.circledialog.res.values.CircleColor.FOOTER_BUTTON_TEXT_NEGATIVE;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.alin.android.app.R;
import com.alin.android.app.adapter.MainGvAdapter;
import com.alin.android.app.api.app.AppApi;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.constant.AppType;
import com.alin.android.app.fragment.BannerFragment;
import com.alin.android.app.model.App;
import com.alin.android.app.model.AppVersion;
import com.alin.android.app.model.Banner;
import com.alin.android.app.service.ChatService;
import com.alin.android.core.constant.AppStatus;
import com.alin.android.core.constant.ReturnCode;
import com.alin.android.core.manager.AppStatusManager;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.model.Result;
import com.alin.android.core.utils.OsUtil;
import com.mylhyl.circledialog.CircleDialog;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseAppActivity {

    private Context context;
    private NotificationManager notificationManager;
    @BindView(R.id.main_scan)
    public TextView scan;
    @BindView(R.id.search_bar_text)
    public TextView searchBarText;
    @BindView(R.id.main_gl_view)
    public GridView mainGlView;

    private String channelId = "VersionCheck";
    private String channelName = "版本更新";
    private int notificationId = APP_VERSION_CHECK;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.context = MainActivity.this;
        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        ButterKnife.bind(this);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // 扫一扫
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toWeChatScan();
            }
        });

        AppApi appApi = retrofit.create(AppApi.class);
        // 轮播图
        appApi.getBannerList().compose(RetrofitManager.<Result<List<Banner>>>ioMain())
                .subscribe(new BaseAppObserver<Result<List<Banner>>>(this) {
                    @Override
                    public void onAccept(Result<List<Banner>> result, String error) {
                        super.onAccept(result, error);
                        List<Banner> bannerList = result.getData();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.add(R.id.main_banner_linear, new BannerFragment(bannerList));
                        fragmentTransaction.commit();
                    }
                });

        // 功能列表
        appApi.getAppList().compose(RetrofitManager.<Result<List<App>>>ioMain())
                .subscribe(new BaseAppObserver<Result<List<App>>>(this, true) {
                    @Override
                    public void onAccept(Result<List<App>> result, String error) {
                        super.onAccept(result, error);
                        final List<App> appList = result.getData();
                        mainGlView.setAdapter(new MainGvAdapter(appList, R.layout.main_gridview_item));
                        mainGlView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                TextView tv = (TextView) view.findViewById(R.id.main_gl_item_tv);
                                appDistribution(appList.get(i));
                            }
                        });
                    }
                });

        // 右下角图标
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        // 获取跳转参数
        Intent intent = getIntent();
        boolean appVersionCheck = intent.getBooleanExtra(KEY_APP_VERSION_CHECK, false);
        // 跳转检测应用版本
        if (appVersionCheck) {
            this.appVersionCheck();
            // 关闭通知
            notificationManager.cancel(APP_VERSION_CHECK);
        }

        // 开启聊天室服务
        if (!OsUtil.isServiceRunning(ChatService.class.getName(), context)) {
            startService(new Intent(context, ChatService.class));
        }
    }

    /**
     * 按应用类型分发应用功能
     * @param app
     */
    private void appDistribution(App app) {
        try {
            switch (app.getType()) {
                case AppType.ACTIVITY:
                    Class<?> clz = getClassLoader().loadClass(app.getClazz());
                    Intent intent = new Intent(context, clz);
                    startActivity(intent);
                    break;
                case AppType.BUTTON:
                    this.getClass().getDeclaredMethod(app.getMethod()).invoke(this);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 应用更新方式弹窗
     */
    public void appVersionCheckDialog() {
        // 对话框
        new CircleDialog.Builder()
                .setWidth(0.7f)
                .setCanceledOnTouchOutside(true)
                .setCancelable(false)
                .setTitle("版本升级")
                .setText("请选择版本更新通知方式")
                .configText(params -> {
                    params.gravity = Gravity.LEFT | Gravity.TOP;
                })
                .configPositive(params -> {
                    params.textColor = FOOTER_BUTTON_TEXT_NEGATIVE;
                })
                .setNegative("弹窗", v -> {
                    Log.i(TAG, "应用内弹窗通知版本更新");
                    appVersionCheck();
                    return true;
                })
                .setPositive("通知", v -> {
                    Log.i(TAG, "通知栏通知版本更新");
                    appVersionCheckNotification();
                    return true;
                })
                .show(getSupportFragmentManager());
    }

    /**
     * 检测应用版本
     */
    public void appVersionCheck() {
        AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_VERSION_CHECK);
        super.appVersionCheck(false);
    }

    /**
     * 检测应用版本通知
     */
    public void appVersionCheckNotification() {
        // 判断通知栏权限
        if (!isNotificationEnabled(this)) {
            new CircleDialog.Builder()
                    .setWidth(0.7f)
                    .setCanceledOnTouchOutside(false)
                    .setCancelable(false)
                    .setTitle("通知权限")
                    .setText("尚未开启通知权限，点击去开启")
                    .configText(params -> {
                        params.gravity = Gravity.CENTER | Gravity.TOP;
                    })
                    .setNegative("取消", v -> true)
                    .setPositive("确定", v -> {
                        Log.i(TAG, "跳转应用通知权限设置页");
                        redirectNotificationSetting(this);
                        return true;
                    })
                    .show(getSupportFragmentManager());
        } else {
            // 设置通知跳转
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(KEY_APP_VERSION_CHECK, true);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);
            // 创建消息渠道
            initNotification(this, channelId, channelName);
            try {
                // 当前app版本
                final PackageInfo pkInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                retrofit.create(AppApi.class).getAppVersion()
                        .compose(RetrofitManager.<Result<AppVersion>>ioMain())
                        .subscribe(new BaseAppObserver<Result<AppVersion>>(this, false) {
                            @Override
                            public void onAccept(Result<AppVersion> o, String error) {
                                if (StringUtils.isBlank(error) && ReturnCode.SUCCESS == o.getCode()) {
                                    AppVersion version = o.getData();
                                    // 判断新版本
                                    if (!StringUtils.equalsIgnoreCase(pkInfo.versionName, version.getVersion()) && ".apk".equals(version.getApk_url().replaceAll(".*?(\\.apk)$", "$1"))) {
                                        // 创建消息
                                        sendNotification(context, pi, channelId, channelName, version.getDescription(), notificationId);
                                    } else {
                                        showInfoDialog("已是最新版本");
                                    }
                                } else {
                                    super.onAccept(o, error);
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toWeChatScan() {
        try {
            //利用Intent打开微信
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            startActivity(intent);
        } catch (Exception e) {
            //若无法正常跳转，在此进行错误处理
            Toast.makeText(context, "无法跳转到微信，请检查是否安装了微信", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSearchBar(View v){
        switch (v.getId()){
            case R.id.search_bar:
                Toast.makeText(context, "搜索条被点击", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, SearchBarActivity.class);
                intent.putExtra("search_text", (String) searchBarText.getText());
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
