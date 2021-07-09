package com.alin.android.app.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.app.R;
import top.leefeng.datepicker.DatePickerView;

/**
 * @Description 选择器
 * @Author zhangwl
 * @Date 2021/7/7 17:38
 */
public class PickerActivity extends BaseAppActivity {

    @BindView(R.id.date_picker_view)
    public DatePickerView datePickerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        ButterKnife.bind(this);
    }
}
