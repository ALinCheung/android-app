package com.alin.android.app.activity;

import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.alin.android.app.common.BaseActivity;
import com.alin.app.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-01 10:36
 **/
public class RotateActivity extends BaseActivity {

    private ImageView imageView;
    private RotateDrawable drawable;
    private Timer timer = new Timer();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123){
                drawable.setLevel(drawable.getLevel() + 400);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);

        imageView = (ImageView) findViewById(R.id.rotate_image);
        drawable = (RotateDrawable) imageView.getDrawable();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, 0, 100);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
