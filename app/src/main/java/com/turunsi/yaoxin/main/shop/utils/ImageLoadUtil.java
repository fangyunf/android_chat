package com.turunsi.yaoxin.main.shop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.yaoxin.appbase.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片加载工具类（支持从assets加载）
 */
public class ImageLoadUtil {
    
    /**
     * 从assets加载图片到ImageView
     * @param context 上下文
     * @param imageView 目标ImageView
     * @param imagePath assets中的图片路径，例如 "images/carme_main_1_1.jpg"
     */
    public static void loadFromAssets(Context context, ImageView imageView, String imagePath) {
        if (context == null || imageView == null || imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            // 方法1：尝试使用Glide加载（Glide可能不支持file:///android_asset/格式）
            // 如果失败，使用AssetManager直接加载
            InputStream is = context.getAssets().open(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            // 加载失败，设置默认图片
            imageView.setImageResource(R.mipmap.app_default_base_icon_geren);
        }
    }
    
    /**
     * 从assets加载图片（带圆角）
     */
    public static void loadFromAssetsRounded(Context context, ImageView imageView, String imagePath, int cornerRadius) {
        if (context == null || imageView == null || imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            InputStream is = context.getAssets().open(imagePath);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                // 使用Glide应用圆角
                Glide.with(context)
                        .load(bitmap)
                        .apply(new RequestOptions()
                                .placeholder(R.mipmap.app_default_base_icon_geren)
                                .error(R.mipmap.app_default_base_icon_geren)
                                .transform(new RoundedCorners(cornerRadius)))
                        .into(imageView);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            imageView.setImageResource(R.mipmap.app_default_base_icon_geren);
        }
    }
}

