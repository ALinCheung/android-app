package com.alin.android.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.alin.android.app.adapter.SearchActvAdapter;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.common.BaseAppObserver;
import com.alin.android.app.model.App;
import com.alin.android.app.service.app.AppService;
import com.alin.android.core.manager.RetrofitManager;
import com.alin.android.core.model.Result;
import com.alin.android.app.R;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-05 16:30
 **/
public class SearchBarActivity extends BaseAppActivity {

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
        retrofit.create(AppService.class).getAppList()
                .compose(RetrofitManager.<Result<List<App>>>ioMain())
                .subscribe(new BaseAppObserver<Result<List<App>>>(this) {
                    @Override
                    public void onAccept(Result<List<App>> o, String error) {
                        super.onAccept(o, error);
                        appList = o.getData();
                        actv.setAdapter(new SearchActvAdapter(appList, R.layout.support_simple_spinner_dropdown_item, context));
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
        if (targetApp != null && StringUtils.isNotBlank(targetApp.getClazz())){
            Class<?> clazz = null;
            try {
                clazz = getClassLoader().loadClass(targetApp.getClazz());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, clazz);
            startActivity(intent);
        } else {
            showErrorDialog("未找到应用");
        }
    }
}
