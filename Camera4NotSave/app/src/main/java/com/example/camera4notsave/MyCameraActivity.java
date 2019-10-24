package com.example.camera4notsave;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

public class MyCameraActivity extends AppCompatActivity {
    private static final String TAG = "MyCameraActivity";

    private final int REQUEST_SHOW_CODE = 11;   // 调用ShowMultiImageActivity请求码
    private int QUALITY = 60;

    /**
     * Camera类用于管理和操作camera资源，它提供了完整的相机底层接口，支持相机资源切换，
     * 可设置预览、拍摄尺寸，设定光圈、曝光、聚焦等相关参数，获取预览、拍摄帧数据等功能
     * 注：预览≠拍摄
     */
    private Camera camera; // 代表相机对象
    private Button takePhoto;

    private CameraPreview cameraPreview; // 相机预览组件

    private byte[] jpegData;  // 保存相机拍摄的照片数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera); // 自定义相机显示界面

        initViews();
    }

    private void initViews() {

        takePhoto = (Button) findViewById(R.id.take_photo);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.surface_view);
        cameraPreview = new CameraPreview(this);
        camera = cameraPreview.getCamera();
        frameLayout.addView(cameraPreview);

        // 设置自动对焦
        cameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(null);
            }
        });

        // 点击拍照
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera = cameraPreview.getCamera();
                //得到照相机的参数
                Camera.Parameters parameters = camera.getParameters();
                //图片的格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                //设置对焦模式，自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                //对焦成功后，自动拍照
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            // 这个是实现相机拍照的主要方法，包含了三个回调参数。
                            // shutter是快门按下时的回调，raw是获取拍照原始数据的回调(只有高级手机相机才支持获得原始格式的数据)
                            // 绝大多数手机是将raw格式数据转为jpeg格式输出保存到本地
                            camera.takePicture(null, raw, mPictureCallback);
//                            camera.stopPreview();
                        }
                    }
                });
            }
        });

    }

    // 获得最原始的 raw 格式数据，只有具有导出raw格式图像功能的手机才会被调用，一般raw格式图像至少几十M大小
    private Camera.PictureCallback raw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw：");
            if (data != null) {
                Log.d(TAG, "onPictureTaken: " + data.length / 1024 + "K");
            }
        }
    };

    // 创建jpeg图片回调数据对象，可在这里对图片数据byte[] data进行压缩等操作，以及在此决定是否将图片保存到本地
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "jpeg: " + data.length / 1024 + "K");
            int size = data.length / 1024; // xx K Bytes
            // 根据图片大小，选择压缩的质量因子
            if (size > 500) { // > 500 KB
                QUALITY = 20;
            } else if (size > 300) {
                QUALITY = 30;
            } else if (size > 200) {
                QUALITY = 40;
            } else if (size > 100) {
                QUALITY = 50;
            }

            // 对图片进行压缩后还是以byte[] 返回
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            // 不管是竖拍还是横拍， width > height
            Log.d(TAG, "bitmap width*height: " + b.getWidth() + "*" + b.getHeight()); // 1920*1080
            ByteArrayOutputStream bos = null;
            bos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos);  // 将图片压缩到流中
            jpegData = bos.toByteArray();

            // 预览拍照图像（压缩过后）
            showPreviewPic(jpegData);
            Log.d(TAG, "final jpeg: " + jpegData.length / 1024 + "K");  // 301k --> 67k
        }
    };

    // 注：intent传数据似乎不能超过 1M ，故若传输原始JPEG图像（现在一般几M）肯定不行，得先压缩后再传输
    private void showPreviewPic(byte[] data) {
        Intent intent = new Intent(MyCameraActivity.this, ShowMultiImageActivity.class);
        intent.putExtra("flag", 3);
        intent.putExtra("data", data);
        startActivityForResult(intent, REQUEST_SHOW_CODE);
    }

    // 预览后决定 重拍 还是 完成
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHOW_CODE) {
            if (resultCode == RESULT_OK) {
                int flag = data.getIntExtra("flag", -1);
                switch (flag) {
                    case 1: // 重拍
                        jpegData = null;
                        camera = cameraPreview.getCamera();
                        camera.startPreview();
                        break;
                    case 2: // 完成拍照：将压缩后的JPEG数据返回到 MainActivity显示
                        Intent intent = new Intent();
                        intent.putExtra("camera_data", jpegData);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                }
            }
        }
    }
}
