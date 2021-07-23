package com.alin.android.app.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.alin.android.app.adapter.MainGvAdapter;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.constant.AppType;
import com.alin.android.app.fragment.BannerFragment;
import com.alin.android.app.model.App;
import com.alin.android.app.model.Banner;
import com.alin.android.app.service.app.AppService;
import com.alin.android.core.constant.AppStatus;
import com.alin.android.core.manager.AppStatusManager;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.model.Result;
import com.alin.app.R;

import java.util.List;

public class MainActivity extends BaseAppActivity {

    private Context context;
    @BindView(R.id.main_scan)
    public TextView scan;
    @BindView(R.id.search_bar_text)
    public TextView searchBarText;
    @BindView(R.id.main_gl_view)
    public GridView mainGlView;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.context = MainActivity.this;
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

        AppService appService = retrofit.create(AppService.class);
        // 轮播图
        appService.getBannerList().compose(RetrofitManager.<Result<List<Banner>>>ioMain())
                .subscribe(new BaseAppObserver<Result<List<Banner>>>(this) {
                    @Override
                    public void onAccept(Result<List<Banner>> result, String error) {
                        super.onAccept(result, error);
                        List<Banner> bannerList = result.getData();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.main_banner_linear, new BannerFragment(context, bannerList));
                        fragmentTransaction.commit();
                    }
                });

        // 功能列表
        appService.getAppList().compose(RetrofitManager.<Result<List<App>>>ioMain())
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

    public void appVersionCheck() {
        AppStatusManager.getInstance().setAppStatus(AppStatus.STATUS_VERSION_CHECK);
        super.appVersionCheck(false);
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
