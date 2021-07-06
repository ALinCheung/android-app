package com.alin.customapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.Nullable;
import com.alibaba.fastjson.JSONObject;
import com.alin.customapp.R;
import com.alin.customapp.adapter.SearchActvAdapter;
import com.alin.customapp.common.BaseActivity;
import com.alin.customapp.model.App;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-05 16:30
 **/
public class SearchBarActivity extends BaseActivity{

    private Context context;
    private List<App> appList;
    private AutoCompleteTextView actv;
    private ImageView deleteIv;
    private TextView cancelTv;

    private String[] placeList = new String[]{"Bei Jing", "Shang Hai", "Guang Zhou", "Shen Zhen"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bar);
        context = SearchBarActivity.this;
        initBind();
    }

    private void initBind(){
        actv = (AutoCompleteTextView) findViewById(R.id.search_bar_text);
        deleteIv = (ImageView) findViewById(R.id.search_bar_delete);
        cancelTv = (TextView) findViewById(R.id.search_bar_cancel);

        // 输入文本框
        String assetsString = getAssetsString("json/app_list.json");
        appList = JSONObject.parseArray(assetsString, App.class);
        actv.setAdapter(new SearchActvAdapter(context, appList));
        actv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH){
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    search();
                }
                return false;
            }
        });
        // 删除按钮
        deleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actv.setText("");
            }
        });
        // 取消按钮
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void search(){
        String searchText = actv.getText().toString();
        Toast.makeText(context, searchText, Toast.LENGTH_SHORT).show();
        App targetApp = null;
        for (App app : appList) {
            if (app.getName().equals(searchText)){
                targetApp = app;
            }
        }
        if (targetApp != null && targetApp.getClazz() != null && !"".equals(targetApp.getClazz())){
            Class<?> clazz = null;
            try {
                clazz = getClassLoader().loadClass(targetApp.getClazz());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, clazz);
            startActivity(intent);
        }
    }
}
