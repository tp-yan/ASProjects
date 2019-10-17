package com.example.serviceandmultithread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// 接收另一个 我的App 发送的广播
public class MyAnotherBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "MyAnotherBroadcastReceiver onReceive", Toast.LENGTH_SHORT).show();
    }
}
