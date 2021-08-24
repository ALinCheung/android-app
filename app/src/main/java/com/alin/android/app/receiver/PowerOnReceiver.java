package com.alin.android.app.receiver;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 开机广播接收器
 */
public class PowerOnReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "开机广播");
    }
}
