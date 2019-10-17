package com.example.broadcastbestpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // 发送强制下线的广播
    public void sendForceOffline(View view) {
        Intent intent = new Intent(BaseActivity.BROADCAST_FORCE_OFFLINE);
        sendBroadcast(intent);
    }
}
