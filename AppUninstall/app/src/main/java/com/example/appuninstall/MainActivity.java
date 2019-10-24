package com.example.appuninstall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void callUninstallBySelf(View view) {
        MyUtils.uninstallApp(this);
    }

    // 调用Android系统自带卸载功能，会弹出询问对话框
    // 在Android 9.0上注意添加权限：REQUEST_DELETE_PACKAGES
    public void callSystemUninstall(View view) {
        String uri = "package:" + MainActivity.class.getPackage().getName();
        Log.d(TAG, "package: "+uri);
        Uri packageURI = Uri.parse(uri);
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(packageURI);
        startActivity(intent);
    }
}
