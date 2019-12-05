package com.example.nfcrwdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * NFC基类
 */
public class BaseNFCActivity extends AppCompatActivity {
    private NfcAdapter adapter;
    private PendingIntent intent;

    @Override
    protected void onStart() {
        super.onStart();
        adapter = NfcAdapter.getDefaultAdapter(this);
        intent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //判断设备是否支持NFC
        if (adapter == null) {
            Toast.makeText(this, "设备不支持NFC功能", Toast.LENGTH_SHORT).show();
            return;
        }

        //判断设备NFC功能是否打开
        if (!adapter.isEnabled()) {
            Toast.makeText(this, "请到系统设置中打开NFC功能!", Toast.LENGTH_SHORT);
            return;
        }

        //设置处理 优于其他所有NFC的处理
        if (adapter != null) {
            adapter.enableForegroundDispatch(this, intent, null, null);
            // 打开前台发布系统，使页面优于其它nfc处理.当检测到一个Tag标签就会执行mPendingItent
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_nfc);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //恢复默认状态
        if (adapter != null) {
            //页面失去焦点时关闭前台发布系统
            adapter.disableForegroundDispatch(this);
        }
    }
}
