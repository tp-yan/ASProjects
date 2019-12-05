package com.example.nfcrwdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.aaa:
                startActivity(new Intent(this, NFCReadActivity.class));
                break;
            case R.id.abc:
                startActivity(new Intent(this, NFCWriteActivity.class));
                break;
        }
    }
}
