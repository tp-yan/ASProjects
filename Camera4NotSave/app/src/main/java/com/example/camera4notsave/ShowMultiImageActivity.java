package com.example.camera4notsave;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.camera4notsave.adapter.RecycleScaleAdapter;

import java.util.ArrayList;

public class ShowMultiImageActivity extends Activity {
    private static final String TAG = "ShowMultiImageActivity";

    RecyclerView recyclerView;
    ArrayList<byte[]> byteList;
    RecycleScaleAdapter adapter;

    private Button takeAgain, finish;
    private LinearLayout linearLayout;  // takeAgain,finish的父控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_multi_image);

        takeAgain = (Button) findViewById(R.id.take_photo_again);
        finish = (Button) findViewById(R.id.finish);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);

        initByteList();

        adapter = new RecycleScaleAdapter(this, byteList);
        recyclerView.setAdapter(adapter);
    }

    // 获取传过来的图像数据，对byteList初始化
    private void initByteList() {
        Intent intent = getIntent();
        int flag = intent.getIntExtra("flag", -1);
        if (flag == -1) {
            Toast.makeText(this, "获取数据失败！", Toast.LENGTH_SHORT).show();
            finish();
        } else if (flag == 3) {// 预览照片
            byteList = new ArrayList<>();
            byte[] data = intent.getByteArrayExtra("data");
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "bitmap width*height: " + b.getWidth() + "*" + b.getHeight()); // 1920*1080
            byteList.add(data);
            doExtraThing();
        }
    }

    private void doExtraThing() {
        linearLayout.setVisibility(View.VISIBLE);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("flag", 2);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        takeAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureAgain();
            }
        });
    }

    private void takePictureAgain() {
        Intent intent = new Intent();
        intent.putExtra("flag", 1);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (linearLayout != null) {
            if (linearLayout.getVisibility() == View.VISIBLE) {
                takePictureAgain();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}
