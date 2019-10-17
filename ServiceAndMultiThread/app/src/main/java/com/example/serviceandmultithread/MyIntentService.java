package com.example.serviceandmultithread;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyIntentService extends IntentService {
    private static final String TAG = "MyIntentService";
    private static final String SERVICE_ACTION_UPLOAD_IMG ="com.example.serviceandmultithread.UPLOAD_IMG";
    public static final String EXTRA_IMG_PATH = "com.example.serviceandmultithread.IMG_PATH";

    private LocalBroadcastManager localBroadcastManager;
    /**
     * 封装一个方法以便多次调用，添加多个任务。
     * 向 MyIntentService 发送上传图片请求
     * @param context
     * @param imgPath 图片路径
     */
    public static void startUploadImg(Context context, String imgPath) {
        Intent intent = new Intent(context,MyIntentService.class);
        intent.setAction(SERVICE_ACTION_UPLOAD_IMG);
        intent.putExtra(EXTRA_IMG_PATH,imgPath);
        context.startService(intent); // 发送请求
    }

    // 1. 必须实现一个无参构造函数，并调用父类的带参构造函数
    public MyIntentService() {
        super("MyIntentService");
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    // 2. 重写抽象方法 onHandleIntent，在这个方法中实现逻辑处理，此方法在子线程中执行
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) { // startService(intent) 传过来的 intent
            if (SERVICE_ACTION_UPLOAD_IMG.equals(intent.getAction())) { // 保证请求服务来自此类
                String imgPath = intent.getStringExtra(EXTRA_IMG_PATH);
                // 逻辑处理
                handleUploadImg(imgPath);
            }
        }
        Log.d(TAG, "Thread id is " + Thread.currentThread().getId());
    }

    private void handleUploadImg(String imgPath) {
        try {
            // 模型耗时任务
            Thread.sleep(3000);
            // 将处理结果通过广播（可以是全局或者本地广播）传给Activity
            Intent resultIntent = new Intent(MainActivity.BROADCAST_ACTION_UPLOAD_RESULT);
            resultIntent.putExtra(EXTRA_IMG_PATH,imgPath); // 代表 imgPath 图片上传成功
            localBroadcastManager.sendBroadcast(resultIntent);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 3. IntentService特性：服务运行结束后自动停止
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return super.onBind(intent);
    }
}
