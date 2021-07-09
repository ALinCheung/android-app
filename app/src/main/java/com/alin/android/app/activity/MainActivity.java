package com.alin.android.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.alibaba.fastjson.JSONObject;
import com.alin.android.app.fragment.BannerFragment;
import com.alin.app.R;
import com.alin.android.app.adapter.MainGvAdapter;
import com.alin.android.app.common.BaseActivity;
import com.alin.android.app.model.App;
import com.alin.android.app.model.Banner;

import java.util.List;

public class MainActivity extends BaseActivity{

    private Context context;
    private List<Banner> bannerList;
    private List<App> appList;
    private TextView scan;
    private TextView searchBarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.context = MainActivity.this;

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        scan = (TextView) findViewById(R.id.main_scan);
        searchBarText = (TextView) findViewById(R.id.search_bar_text);

        // 扫一扫
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toWeChatScan();
            }
        });

        // 轮播图
        String bannerListString = getAssetsString("json/banner_list.json");
        this.bannerList = JSONObject.parseArray(bannerListString, Banner.class);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_banner_linear, new BannerFragment(context, bannerList));
        fragmentTransaction.commit();

        // 功能列表
        String appListString = getAssetsString("json/app_list.json");
        this.appList = JSONObject.parseArray(appListString, App.class);
        GridView mainGlView = (GridView) findViewById(R.id.main_gl_view);
        mainGlView.setAdapter(new MainGvAdapter(MainActivity.this, appList));
        mainGlView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view.findViewById(R.id.main_gl_item_tv);
                Toast.makeText(context, tv.getText(), Toast.LENGTH_LONG).show();
                Class<?> clz = null;
                try {
                    clz = getClassLoader().loadClass(appList.get(i).getClazz());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, clz);
                startActivity(intent);
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
