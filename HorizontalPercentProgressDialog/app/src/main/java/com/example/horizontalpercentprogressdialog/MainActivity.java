package com.example.horizontalpercentprogressdialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    ProgressDialog progressDialog;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (progressDialog != null) {
                progressDialog.setProgress(msg.what);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void horizontalProgressDialog(View view) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("水平进度条");
            progressDialog.setMessage("提示信息");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.incrementProgressBy(30); // 设置初始值
        }
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i <= 100) {
                    try {
                        Thread.sleep(100);
                        handler.sendEmptyMessage(i++);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
