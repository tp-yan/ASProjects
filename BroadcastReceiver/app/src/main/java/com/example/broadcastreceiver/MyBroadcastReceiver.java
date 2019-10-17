package com.example.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// 接收自己发出的广播
public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "MyBroadcastReceiver onReceive: ");
        Toast.makeText(context, "MyBroadcastReceiver onReceive", Toast.LENGTH_LONG).show();
//        abortBroadcast();  // 截断有序广播
    }
}
