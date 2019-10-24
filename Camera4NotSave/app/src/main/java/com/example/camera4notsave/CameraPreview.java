package com.example.camera4notsave;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
