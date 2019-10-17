package com.example.broadcastreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String NETWORK_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String MY_BROADCAST_ACTION = "com.example.broadcastreceiver.MY_BROADCAST";
    public static final String LOCAL_BROADCAST_ACTION = "com.example.broadcastreceiver.LOCAL_BROADCAST";
    private static final String TAG = "MainActivity";

    private IntentFilter intentFilter;  // 意图过滤器
    private NetworkChangeReceiver receiver;

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "MainActivity BroadcastReceiver myBroadcastReceiver", Toast.LENGTH_SHORT).show();
        }
    };

    private LocalReceiver localReceiver;
    // 本地广播需要借助LocalBroadcastManager
    private LocalBroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter = new IntentFilter();
        // 添加过滤器，只接受 Action 为 NETWORK_CHANGE_ACTION 的 广播
        intentFilter.addAction(NETWORK_CHANGE_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver,intentFilter); // 动态注册广播
//        registerMyBroadcastReceiver();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        registerLocalReceiver();
    }

    private void registerMyBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_BROADCAST_ACTION);
        registerReceiver(myBroadcastReceiver,intentFilter);
    }

    // 发送自定义标准广播
    public void sendMyBroadcast(View view) {
        Intent intent = new Intent(MY_BROADCAST_ACTION);
        // 添加此条 Flag 使其他 app 在未启动的情况下能够收到广播，否则其他app静态注册的广播接收器也无法收到广播
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        // 适配 在Android 8.0以上静态注册的广播接收器，但是此方法只能让自己的静态注册的广播接收器收到，其他app无法接收
        // 参数1指的是你的app的包名，参数2指的是你的自定义广播所在的路径
//        ComponentName componentName = new ComponentName(this,"com.example.broadcastreceiver.MyBroadcastReceiver");
//        intent.setComponent(componentName);
        sendBroadcast(intent);
//        sendOrderedBroadcast(intent,null); // 发送有序广播
        Log.d(TAG, "sendMyBroadcast sendBroadcast");
    }

    // 接受系统网络状态变化的广播，显示网络是否可用
    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取专用于管理网络连接的系统服务类：ConnectivityManager
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                Toast.makeText(context, "network is available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "network is unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 发送本地广播：广播只在App内传播
    public void sendLocalBroadcast(View view) {
        Intent intent = new Intent(LOCAL_BROADCAST_ACTION);
        broadcastManager.sendBroadcast(intent);
    }

    // 注册本地广播接收器
    private void registerLocalReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LOCAL_BROADCAST_ACTION);
        localReceiver = new LocalReceiver();
        broadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "接收到本地广播", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver); // 动态注册广播,必须手动注销！
//        unregisterReceiver(myBroadcastReceiver);
        broadcastManager.unregisterReceiver(localReceiver);
    }
}
