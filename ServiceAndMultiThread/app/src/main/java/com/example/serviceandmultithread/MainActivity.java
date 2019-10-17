package com.example.serviceandmultithread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String BROADCAST_ACTION_UPLOAD_RESULT = "com.example.serviceandmultithread.UPLOAD_RESULT";

    public static final int UPDATE_TEXT = 1; // 代表更新UI 的操作

    TextView textView;
    LinearLayout linearLayout;

    private LocalBroadcastManager localBroadcastManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    textView.setText("message from child thread");
                    break;
            }
        }
    };

    // 对MyService中的mBinder对象的引用，通过它的public方法去控制和监督Service的行为
    private MyService.DownloadBinder downloadBinder;
    // Activity绑定Service必须实现的接口，相当于Activity与Service之间的桥梁
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection onServiceConnected: ");
            // iBinder:Service onBind()返回的对象
            downloadBinder = (MyService.DownloadBinder) iBinder; // Binder实现了IBinder接口
            downloadBinder.startDownload();
            downloadBinder.getProgress();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "ServiceConnection onServiceDisconnected: ");
        }
    };

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "com.example.serviceandmultithread onReceive", Toast.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_ACTION_UPLOAD_RESULT.equals(intent.getAction())) {
                String imgPath = intent.getStringExtra(MyIntentService.EXTRA_IMG_PATH);
                handleResult(imgPath);
            }
        }
    };

    // 处理从MyIntentService返回的结果
    private void handleResult(String imgPath) {
        // 更新原来控件的内容
        TextView tv = linearLayout.findViewWithTag(imgPath);
        tv.setText(imgPath + " upload success !");
    }

    private void registerMyBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcastreceiver.MY_BROADCAST");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private void registerResultReceiver() {
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION_UPLOAD_RESULT);
        localBroadcastManager.registerReceiver(resultReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 线程的基本使用
        startThread();
        // 获取控件并绑定监听器
        initViews();

//        registerMyBroadcastReceiver(); // 动态注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        registerResultReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(myBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(resultReceiver);
    }

    private void initViews() {
        textView = findViewById(R.id.textview);
        linearLayout = findViewById(R.id.txt_container);
        final Button startService = findViewById(R.id.start_service);
        final Button stopService = findViewById(R.id.stop_service);
        final Button bindService = findViewById(R.id.bind_service);
        final Button unbindService = findViewById(R.id.unbind_service);
        final Button updateUI = findViewById(R.id.update_UI);
        final Button startIntentService = findViewById(R.id.start_intent_service);

        updateUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = UPDATE_TEXT;
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 启动服务与启动Activity类似，都是通过Intent 传达意图
                // 启动Service的意图:指定要启动的Service
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                startService(startIntent); // context类的方法，启动服务，回调服务的 onStartCommand()
                // 每调一次 startService 就回调一次 onStartCommand
            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 停止服务的意图:指定需要停止的Service
                Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                stopService(stopIntent); // context类的方法
                // 除了在 Activity中停止Service外，在Service的任何地方调用 stopSelf() 可让服务自我停止
            }
        });

        bindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bindIntent = new Intent(MainActivity.this, MyService.class);
                // bindService：获取服务的持久连接，会回调Service的onBind()，若服务还没创建，则在onBind()前会执行onCreate
                // BIND_AUTO_CREATE:绑定时就创建Service，Service的onCreate会执行，但 onStartCommand 不会执行
                // 只要调用方和服务之间的连接没断，服务就一直保持运行状态
                // 绑定操作只能一次有效
                bindService(bindIntent, connection, BIND_AUTO_CREATE);
            }
        });
        unbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService(connection); // 解绑后，Service执行onDestroy
            }
        });
        // 注：若对一个服务调用了 startService 和 bindService，则必须stopService和unbindService都调用了服务才会停止
        // 一个服务只要被启动或者绑定后，就一直处于运行状态
        // 任何一个服务应用程序范围内都是通用的，一个Service可以和多个Activity绑定，绑定后获得的是同一个DownloadBinder实例

        // 启动 IntentService
        startIntentService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Thread id is " + Thread.currentThread().getId());
                Intent intentService = new Intent(MainActivity.this, MyIntentService.class);
                startService(intentService);
            }
        });
    }

    // 点击一次向 IntentService 添加一个任务，相当于调用了一次 onStartCommand
    // 通过startService(Intent)提交请求。IntentService 只会开启一个工作线程，依次处理请求的任务
    int counter = 0;
    public void addTask(View view) {
        // 模型路径
        String imgPath = "/sdcard/imgs/" + (++counter) + ".png";
        MyIntentService.startUploadImg(this,imgPath); // 发送上传请求

        TextView tv = new TextView(this);
        linearLayout.addView(tv);
        tv.setText(imgPath+" is uploading...");
        tv.setTag(imgPath); // 给控件打Tag，以便后续获取控件
    }






    /*============================================ 多线程 =============================================*/
    // Android多线程基本使用方式与Java一样，有如下2种基本使用方式
    // 1. 继承 Thread 类，重写 run()
    class MyThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "MyThread  extends Thread  ");
        }
    }

    // 2. 实现 Runnable 接口，实现 run()。比 1 更实用
    class MyThread2 implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "MyThread2 implements Runnable ");
        }
    }

    // 3. 启动线程：调用Thread类的 start()
    private void startThread() {
        MyThread thread1 = new MyThread();
        thread1.start();

        MyThread2 thread2 = new MyThread2();
        new Thread(thread2).start();    // 实现 Runnable 接口的对象，需要作为 Thread构造函数的参数
        // 更常见：更简洁的写法，使用匿名类
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "匿名类：new Thread(new Runnable(){...})");
            }
        }).start();

        // 启动异步任务
        new MyDownloadTask().execute();
    }
    /*============================================ 多线程 =============================================*/


    // ======================  AsyncTask  ==========================
    private ProgressDialog progressDialog;

    class MyDownloadTask extends AsyncTask<Void, Integer, Boolean> {
        // 任务开始前调用，一般完成界面初始化操作，比如创建对话框
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "MyDownloadTask onPreExecute: ");
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("任务提示");
            }
            progressDialog.show();
        }

        // 只有 doInBackground 方法是在 子线程中执行，其他方法都是在主线程执行，故它们可以进行UI更新
        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(TAG, "MyDownloadTask doInBackground: ");
            while (true) {
                int downloadPercent = doDownload(false);
                publishProgress(downloadPercent);
                if (downloadPercent >= 100) {
                    break;
                }
                try {
                    // 模拟下载耗时
                    Thread.sleep(5); // 让当前线程（即子线程）停顿一下
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        // 任务进行中，在 doInBackground 中调用 publishProgress() 时 此方法被触发，用于更新 进度条对话框中进度值
        @Override
        protected void onProgressUpdate(Integer... values) { // values:即publishProgress的参数
            Log.d(TAG, "MyDownloadTask onProgressUpdate: ");
            progressDialog.setMessage("Download " + values[0] + "%");
        }

        // 在任务执行完后被调用，doInBackground 的返回值作为传入参数
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d(TAG, "MyDownloadTask onPostExecute: ");
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (aBoolean) {
                Toast.makeText(MainActivity.this, "Download Succeeded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 模拟下载任务，返回下载进度百分比0~100
    private int percent = 0;

    private int doDownload(boolean init) {
        if (init)
            percent = 0;
        return percent++;
    }
    // ======================  AsyncTask  ==========================
}
