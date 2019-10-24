package com.example.camera4notsave.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;

import com.example.camera4notsave.MyUtils;
import com.example.camera4notsave.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


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
