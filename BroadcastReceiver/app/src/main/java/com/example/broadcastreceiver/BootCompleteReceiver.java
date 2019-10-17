package com.example.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// 静态注册的广播可以在app未启动的情况下接受广播，这里接受手机开机时的广播
// 由于是静态注册的，需要在 AndroidManifest中注册接收器<receiver>，同时为了接收到“开机广播”，需要在<receiver>中
// 增加 <intent-filter> <action android:name="android.intent.action.BOOT_COMPLETED"/> </intent-filter>
// 同时还需要注册监听系统开机广播的权限
public class BootCompleteReceiver extends BroadcastReceiver {
    // 广播接收器不允许开线程，若onReceive长时间没结束，则会报错
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Boot Complete", Toast.LENGTH_LONG).show();
        Log.d("BootCompleteReceiver", "onReceive Boot Complete");
    }
}
