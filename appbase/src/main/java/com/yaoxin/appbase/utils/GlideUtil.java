package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yaoxin.appbase.R;


/**
 * @author yangzijian
 * @date 2018/12/3
 * @des
 * @modify
 **/
public class GlideUtil {

    private static boolean isSafe(Context context, View view) {
        if (context == null || view == null) {
            return false;
        }
        if (context instanceof Activity) {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !((Activity) context).isDestroyed();
        }
        return true;

    }

    public static void loadImage(Context context, ImageView imageView, String url, int placeRes, int errorRes) {
        if (!isSafe(context, imageView)) {
            return;
        }

        Glide.with(context).load(url)
                .apply(new RequestOptions().error(placeRes)
                        .placeholder(errorRes)
                )
                .into(imageView);
    }

    public static void yh_loadImage(Context context, ImageView imageView, String url) {
        if (!isSafe(context, imageView)) {
            return;
        }
        Glide.with(context).load(url)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.icon_defalut_photo)
                )
                .into(imageView);
    }

    public static void yh_loadImageRoundedCorner(Context context, ImageView imageView, String url, int cornerRadio) {
        if (!isSafe(context, imageView)) {
            return;
        }

        yh_loadImage(context, imageView, url);
//        Glide.with(context).load(url)
//                .apply(new RequestOptions()
//                        .placeholder(R.mipmap.yaoxin_default_avartor)
//                )
//                .into(imageView);
    }
}
