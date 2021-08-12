package com.alin.android.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alin.android.app.R;
import com.alin.android.app.common.BaseAppActivity;
import com.github.gzuliyujiang.calendarpicker.CalendarPicker;
import com.github.gzuliyujiang.calendarpicker.OnSingleDatePickListener;
import com.github.gzuliyujiang.wheelpicker.DatePicker;
import com.github.gzuliyujiang.wheelpicker.DatimePicker;
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatimePickedListener;
import com.github.gzuliyujiang.wheelpicker.contract.OnTimePickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;
import com.github.gzuliyujiang.wheelpicker.widget.DatimeWheelLayout;

import java.util.Date;

/**
 * @Description 选择器
 * @Author zhangwl
 * @Date 2021/7/7 17:38
 */
public class PickerActivity extends BaseAppActivity implements OnDatePickedListener, OnTimePickedListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        Toast.makeText(this, year + "-" + month + "-" + day, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimePicked(int hour, int minute, int second) {
        Toast.makeText(this, hour + ":" + minute + ":" + second, Toast.LENGTH_SHORT).show();
    }

    public void onCalendarDateSingle(View view) {
        CalendarPicker picker = new CalendarPicker(this);
        picker.getOkView().setText("确定");
        picker.getCancelView().setText("取消");
        picker.setRangeDateOnFuture(3);
        picker.setOnSingleDatePickListener(new OnSingleDatePickListener() {
            @Override
            public void onSingleDatePicked(@NonNull Date date) {
                Toast.makeText(getApplicationContext(), date.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        picker.show();
    }

    public void onYearMonthDayTime(View view) {
        DatimePicker picker = new DatimePicker(this);
        picker.getOkView().setText("确定");
        picker.getCancelView().setText("取消");
        final DatimeWheelLayout wheelLayout = picker.getWheelLayout();
        picker.setOnDatimePickedListener(new OnDatimePickedListener() {
            @Override
            public void onDatimePicked(int year, int month, int day, int hour, int minute, int second) {
                String text = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
                text += wheelLayout.getTimeWheelLayout().isAnteMeridiem() ? " 上午" : " 下午";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
        wheelLayout.setTimeMode(TimeMode.HOUR_12_HAS_SECOND);
        wheelLayout.setRange(DatimeEntity.now(), DatimeEntity.yearOnFuture(10));
        wheelLayout.setDateLabel("年", "月", "日");
        wheelLayout.setTimeLabel("时", "分", "秒");
        picker.show();
    }

    public void onYearMonthDay(View view) {
        DatePicker picker = new DatePicker(this);
        picker.getOkView().setText("确定");
        picker.getCancelView().setText("取消");
        picker.setOnDatePickedListener(this);
        picker.setBodyWidth(240);
        DateWheelLayout wheelLayout = picker.getWheelLayout();
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
        wheelLayout.setDateLabel("年", "月", "日");
        wheelLayout.setRange(DateEntity.today(), DateEntity.yearOnFuture(30), DateEntity.yearOnFuture(10));
        wheelLayout.setCurtainEnabled(true);
        wheelLayout.setIndicatorEnabled(true);
        wheelLayout.setIndicatorSize(view.getResources().getDisplayMetrics().density * 2);
        picker.show();
    }
}
