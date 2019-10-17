package com.example.broadcastbestpractice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    public static final String BROADCAST_FORCE_OFFLINE = "com.example.broadcastbestpractice.FORCE_OFFLINE";

    // 所有子类活动都会继承有成员变量 forceOfflineReceiver，无法直接访问，可通过父类的 protected和public访问
    private ForceOfflineReceiver forceOfflineReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    // 保证在栈顶的活动才会收到广播，故在 onResume()中注册广播接收器，在onPause()中取消注册
    // 应该保证只有一个活动接收到广播即可，故其他活动需取消注册
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_FORCE_OFFLINE);
        forceOfflineReceiver = new ForceOfflineReceiver();
        registerReceiver(forceOfflineReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (forceOfflineReceiver != null) {
            unregisterReceiver(forceOfflineReceiver);
            forceOfflineReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    // 只有动态注册的广播接收器才能创建显示UI组件，静态接收器不行
    class ForceOfflineReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("Warning")
                    .setMessage("强制下线，请重新登录!")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCollector.removeAll(); // 销毁所有活动！
                            // 重启登录活动
                            Intent loginIntent = new Intent(context, LoginActivity.class);
                            startActivity(loginIntent);
                        }
                    });
            builder.show();
        }
    }
}
