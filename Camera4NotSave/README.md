##### 先上效果图：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191024121707262.gif)

**github地址**：[https://github.com/tp-yan/ASProjects/tree/master/Camera4NotSave](https://github.com/tp-yan/ASProjects/tree/master/Camera4NotSave)


##### 1. 声明相机权限以及注册相关Activity
```xml
  <application
	  ...
	  >
        <activity
            android:name=".MyCameraActivity"
            android:theme="@style/AppTheme.NoActionBarFullScreen" />

        <activity
            android:name=".ShowMultiImageActivity"
            android:theme="@style/AppTheme.NoActionBarFullScreen" />

    </application>

    <uses-permission android:name="android.permission.CAMERA" />
```

##### 2. MainActivity
要处理的逻辑简单：
1. 动态申请权限
2. 打开相机
3. 将相机返回图像数据显示
```java
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
```
注：动态权限申请，使用了 [https://github.com/zincPower/JPermission](https://github.com/zincPower/JPermission) 提供的库，请自行参考使用细节


布局文件`activity_main.xml`，一个按钮+`ImageView`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:onClick="startMyCamera"
        android:text="启动自定义相机" />

    <ImageView
        android:id="@+id/image_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
</LinearLayout>
```
##### 3. MyCameraActivity：实现自定义相机
从`MainActivity`跳转到此，MyCameraActivity需要完成的功能：
1. 加载自定义相机预览组件`cameraPreview`，获得相机控制对象`Camera`
2. 提供相机拍照回调的接口` Camera.PictureCallback`，相机拍照时会回调此接口，将图像数据传过来，接下来我们就可以对拍摄的图像数据进行任意操作了（也是在此决定是否将数据保存到本地）
3. 将图像数据压缩后传到`ShowMultiImageActivity`完成预览功能，并根据`ShowMultiImageActivity`返回的结果决定是“重拍”还是“完成”拍摄

MyCameraActivity:
```java
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
```
需要注意2点：
1. 现在绝大多手机，拍照后会将`raw`格式的最原始的图像数据转为`jpeg`格式输出，只有很少的比较高端的手机会提供直接输出`raw`格式图像的功能。（关于raw格式和JPEG等格式，请自行Google数字图像的拍摄生成过程）。所以，我们需要在接口`mPictureCallback`中处理`jpeg`格式的图像数据`data`即可
2. Intent传不能输大数据，一般不能超过1M，故将图像数据传到`ShowMultiImageActivity`时，需要先压缩一下

其他细节看注释！
布局文件`activity_my_camera.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MyCameraActivity">

    <FrameLayout
        android:id="@+id/surface_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <FrameLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center">

        <Button
            android:id="@+id/take_photo"
            android:layout_width="wrap_content"
            android:padding="5dp"
            android:layout_height="wrap_content"
            android:text="@string/capture" />

    </FrameLayout>

</LinearLayout>
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019102411405780.png)

##### 4. CameraPreview
`CameraPreview`对象显示摄像头捕获的界面，在这里设置相机的参数等，具体细节已写在注释中。
CameraPreview：
```java
/**
 * surface是指向屏幕窗口原始图像缓冲区（raw buffer）的一个句柄，通过它可以获得这块屏幕上对应的canvas，
 * 进而完成在屏幕上绘制View的工作。
 * 此类能够显示相机的实时预览图像。
  */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    /**
     * SurfaceHolder是控制surface的一个抽象接口，它能够控制surface的尺寸和格式，修改surface的像素，监视surface的变化等等
     * 通过surfaceHolder可以将Camera和surface连接起来，当camera和surface连接后，camera获得的预览帧数据就可以通过surface显示在屏幕上了。
     */
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int picWidth = 1920;
    private int picHeight = 1080;

    public CameraPreview(Context context) {
        super(context);
        createCamera();
    }

    private void createCamera(){
        // 打开相机，获取camera实例
        mCamera =  Camera.open();;
        //得到SurfaceHolder对象
        mHolder = getHolder();
        //添加回调，得到Surface的三个声明周期方法
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public Camera getCamera(){
        if (mCamera == null)
            createCamera();
        return mCamera;
    }

    /**
     * 在surface创建后立即被调用。
     * 在开发自定义相机时，可以通过重载这个函数调用camera.open()、camera.setPreviewDisplay()，
     * 来实现获取相机资源、连接camera和surface等操作。
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (mCamera == null)
            createCamera();
        try {
            //设置预览方向
//            mCamera.setDisplayOrientation(90);
            //把这个预览效果展示在SurfaceView上面
            mCamera.setPreviewDisplay(mHolder);
            //开启预览效果
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    /**
     * 在surface发生format或size变化时调用。
     * 在开发自定义相机时，可以通过重载这个函数调用camera.startPreview来开启相机预览，
     * 使得camera预览帧数据可以传递给surface，从而实时显示相机预览图像。
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //	停止预览，关闭camra底层的帧数据传递以及surface上的绘制。
        mCamera.stopPreview();
        //重新设置预览效果
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCameraParams(mCamera,picWidth,picHeight);
        // 开始预览，将camera底层硬件传来的预览帧数据显示在绑定的surface上。
        mCamera.startPreview();
    }

    /**
     * 在surface销毁之前被调用。在开发自定义相机时，可以通过重载这个函数调用camera.stopPreview()，
     * camera.release()来实现停止相机预览及释放相机资源等操作。
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release(); // 释放Camera实例
            mCamera = null;
        }
    }

    // 设置相机参数：在surfaceChanged方法中执行mCamera.startPreview()前调用setCameraParams(mCamera, mScreenWidth, mScreenHeight); 就可以了
    private void setCameraParams(Camera mCamera, int width, int height) {
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();

        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        Collections.sort(pictureSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return o2.width - o1.width;
            }
        });
        for (Camera.Size size : pictureSizeList) {
            Log.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }

        /** 从列表中选取合适的分辨率 */
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        Log.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);

        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height);
        Log.d(TAG, "保存图片尺寸：width*height: "+ w + "*" + h);
        //this.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Collections.sort(previewSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return o2.width - o1.width;
            }
        });
        for (Camera.Size size : previewSizeList) {
            Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        Log.d(TAG, "预览图片尺寸：width*height: "+ preSize.width + "*" + preSize.height);

        parameters.setJpegQuality(100); // 设置照片质量
//        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); // 连续对焦模式
//        }

//        mCamera.cancelAutoFocus(); //自动对焦。
        // 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        // TODO 这里直接设置90°不严谨，
        //  具体见https://developer.android.com/reference/android/hardware/Camera.html#setPreviewDisplay%28android.view.SurfaceHolder%29
        mCamera.setDisplayOrientation(90); // 不设置的话，将导致相机画面很奇怪
        mCamera.setParameters(parameters);
    }


    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * tip：这里的w对应屏幕的height, h对应屏幕的width
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            if (size.width == 1920 && size.height == 1080){
                result = size;
                break;
            }

            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0  && size.width < 2500) {
                result = size;
                Log.d(TAG, "currentRatio - screenRatio == 0 : "+size.width);
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3 && size.width < 2500) {// 默认w:h = 4:3
                    result = size;
                    Log.d(TAG, "curRatio: "+curRatio);
                    break;
                }
            }
        }
        return result;
    }

}
```

##### 5. ShowMultiImageActivity
`ShowMultiImageActivity`就是显示拍摄的图片，提供预览功能，可以缩放拍摄的图片，同时需要在此决定是“重拍”还是“完成”拍摄，并将选择结果返回给`MyCameraActivity`：
```java
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
```
##### 6. RecycleScaleAdapter
给`RecyclerView`提供显示数据的`Adapter`，这个不多说，使用过`RecyclerView`的都不陌生：
```java
public class RecycleScaleAdapter extends RecyclerView.Adapter<RecycleScaleAdapter.MyViewHolder> {
    private static final String TAG = "RecycleScaleAdapter";

    private Context context;
    private List<byte[]> list;
    private View inflater;

    //构造方法，传入填充控件数据
    public RecycleScaleAdapter(Context context, List<byte[]> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.recyler_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // 将数据和控件绑定
        byte[] bytes = list.get(position);
        // 对图片进行旋转
        int orientation = MyUtils.getOrientation(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (orientation != -1) {
            bitmap = MyUtils.rotateBitmap(bitmap, orientation);
        }

        Log.d(TAG, "onBindViewHolder: " + "bitmap width*height: " + bitmap.getWidth() + "*" + bitmap.getHeight());

        holder.photoView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public MyViewHolder(View itemView) {
            super(itemView);
            photoView = (PhotoView) itemView.findViewById(R.id.photo_view);
        }
    }
}
```
需要注意一点：为了更好的显示图片，对图片数据进行了适当的方向旋转，该功能被封装到`MyUtils`中

其中图片全屏查看和缩放功能，使用了库：[https://github.com/chrisbanes/PhotoView](https://github.com/chrisbanes/PhotoView)

`recyler_item.xml`：
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

   <com.github.chrisbanes.photoview.PhotoView
       android:id="@+id/photo_view"
       android:layout_width="match_parent"
       android:layout_height="match_parent" />
</LinearLayout>
```
##### 7. MyUtils
封装一些共用的功能：
```java
public class MyUtils {
    private static final String TAG = "MyUtils";

    // 获取图像的拍照方向
    public static int getOrientation(byte[] data) {
        int orientation = -1;
        InputStream is = new ByteArrayInputStream(data);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                ExifInterface exifInterface = new ExifInterface(is);
                orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getOrientation: " + orientation);
        return orientation;
    }


    // 根据orientation旋转图片
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 0:
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}

```
