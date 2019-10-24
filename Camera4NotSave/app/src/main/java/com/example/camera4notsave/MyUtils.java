package com.example.camera4notsave;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
