package com.example.camera4notsave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zinc.libpermission.annotation.Permission;
import com.zinc.libpermission.annotation.PermissionCanceled;
import com.zinc.libpermission.annotation.PermissionDenied;
import com.zinc.libpermission.bean.CancelInfo;
import com.zinc.libpermission.bean.DenyInfo;

public class MainActivity extends AppCompatActivity {
    public final int CAMERA_PERMISSION_REQUEST = 100; // 申请相机请求码
    public static final int INTENT_MY_CAMERA = 209;   // 启动MyCameraActivity请求码
    private static final String TAG = "MainActivity";

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
    }

    // 启动自定义相机
    @Permission(value = {Manifest.permission.CAMERA}, requestCode = CAMERA_PERMISSION_REQUEST)
    public void startMyCamera(View view) {
        startActivityForResult(new Intent(MainActivity.this,
                MyCameraActivity.class), INTENT_MY_CAMERA);
        Log.d(TAG, "startMyCamera success ");
    }

    // 取消权限申请
    @PermissionCanceled()
    private void permissionCancel(CancelInfo cancelInfo) {
        Toast.makeText(this, "取消权限申请", Toast.LENGTH_SHORT).show();
    }

    // 拒绝权限申请
    @PermissionDenied()
    private void deny(DenyInfo denyInfo) {
        Toast.makeText(this, "拒绝权限申请", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_MY_CAMERA:
                if (resultCode == RESULT_OK) { // 将 相机拍摄传回的byte[] jpegData转为Bitmap再显示
                    byte[] jpegData = data.getByteArrayExtra("camera_data");
                    int orientation = MyUtils.getOrientation(jpegData);
                    Bitmap b = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
                    if (orientation != -1) {
                        b = MyUtils.rotateBitmap(b, orientation);
                    }
                    imageView.setImageBitmap(b);
                }
                break;
        }
    }
}
