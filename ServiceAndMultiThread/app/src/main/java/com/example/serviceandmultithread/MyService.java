package com.example.serviceandmultithread;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

// Android的四大组件都必须在清单文件中进行注册，所以Service也需在 manifest 中注册！！
public class MyService extends Service { // Service 是抽象类
    private static final String TAG = "MyService";

    private DownloadBinder mBinder = new DownloadBinder();

    // DownloadBinder作为MyService的内部类，可以访问MyService的任何成员和方法
    // DownloadBinder 的public方法，将是Activity可以访问的接口
    class DownloadBinder extends Binder {
        public void startDownload() {
            Log.d(TAG, "DownloadBinder startDownload: ");
        }

        public int getProgress() {
            Log.d(TAG, "DownloadBinder getProgress: ");
            return 0;
        }
    }

    public MyService() {
        Log.d(TAG, "构造方法 MyService: ");
    }

    // Service创建时被调用，只调用一次
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service创建 onCreate: ");

        Intent intent = new Intent(this, MainActivity.class); // 指定点击状态栏时跳转的Activity
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("This is content title")
                .setContentText("text text text text")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();

        // 以上内容跟创建通知的方法一样，只是创建的Notification对象没有使用NotificationManager来显示通知，而是调用了 startForeground
        startForeground(1, notification); // 转为前台服务； 第一个参数是通知 id
    }

    // 每次启动Service时被调用，一个Service只会有一个实例，onCreate只会调用一次，而 onStartCommand可被多次调用
    // onStartCommand 只有在 startService方式启动Service下才会被调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        // 标准的服务形式
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 处理逻辑的代码
                Log.d(TAG, "耗时任务...");
                stopSelf(); // 任务执行完服务停止
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    // Service 中唯一抽象的方法
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind: ");
        return mBinder; // 当Activity与Service绑定时，返回的对象
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
