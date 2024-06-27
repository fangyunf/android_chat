package com.yaoxin.appbase.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.yaoxin.appbase.net.Constant;

import java.io.OutputStream;

import pub.devrel.easypermissions.EasyPermissions;

public class ImageUtil {
    public static void saveImageViewToGallery(Context context, ImageView imageView) {
        String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                    new String[] {
                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                    };
        }
        if (!EasyPermissions.hasPermissions(context, permission)) {
            // 请求相机权限
            EasyPermissions.requestPermissions((Activity) context, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }

        // 获取 ImageView 中的 Bitmap
        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
        imageView.setDrawingCacheEnabled(false);

        // 保存 Bitmap 到相册
        saveBitmapToGallery(context, bitmap);
    }

    private static void saveBitmapToGallery(Context context, Bitmap bitmap) {
        // 获取外部存储路径
        String savedImageURL = null;
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        String imageDescription = "ImageView to Gallery";

        // Android 10 及以上使用 MediaStore 保存图片
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.Images.Media.DESCRIPTION, imageDescription);
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = context.getContentResolver().insert(collection, values);

            try (OutputStream outstream = context.getContentResolver().openOutputStream(item)) {
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream)) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    context.getContentResolver().update(item, values, null, null);
                    savedImageURL = item.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Android 10 以下版本使用传统方式保存图片
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            java.io.File imageFile = new java.io.File(imagesDir, imageFileName);

            try (java.io.OutputStream out = new java.io.FileOutputStream(imageFile)) {
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    savedImageURL = imageFile.getAbsolutePath();
                    // 通知图库更新
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), imageFile.getAbsolutePath(), imageFileName, imageDescription);
                    context.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 打印保存的图片路径
        if (savedImageURL != null) {
            ToastUtils.toastMsg("保存成功");
        } else {
            ToastUtils.toastMsg("保存失败");
        }
    }
}
