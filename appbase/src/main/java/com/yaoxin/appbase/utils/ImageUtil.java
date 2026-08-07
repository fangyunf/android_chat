package com.yaoxin.appbase.utils;

import static com.netease.yunxin.kit.common.utils.ThreadUtils.runOnUiThread;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.yaoxin.appbase.net.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class ImageUtil {
    public static void saveImageViewToGallery(Context context, ImageView imageView) {
        if (!ensureWriteGalleryPermission(context)) {
            return;
        }
        imageView.setDrawingCacheEnabled(true);
        Bitmap cache = imageView.getDrawingCache();
        if (cache == null) {
            imageView.setDrawingCacheEnabled(false);
            ToastUtils.toastMsg("保存失败");
            return;
        }
        Bitmap bitmap = Bitmap.createBitmap(cache);
        imageView.setDrawingCacheEnabled(false);
        saveBitmapToGallery(context, bitmap);
    }

    /**
     * 将整块 View 绘制成图片并保存到相册（Android 10+ 走 MediaStore，无需存储权限）
     */
    public static void saveViewToGallery(Context context, android.view.View view) {
        if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) {
            ToastUtils.toastMsg("保存失败");
            return;
        }
        if (!ensureWriteGalleryPermission(context)) {
            return;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        view.draw(canvas);
        saveBitmapToGallery(context, bitmap);
    }

    private static boolean ensureWriteGalleryPermission(Context context) {
        // Android 10+ 使用 MediaStore 写入公开相册，无需申请存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        String[] permission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (!EasyPermissions.hasPermissions(context, permission)) {
            EasyPermissions.requestPermissions(
                    (Activity) context,
                    "需要访问相册权限",
                    Constant.RC_PHOTO_PICKER_PERM,
                    permission);
            return false;
        }
        return true;
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

    public static void downloadImage(String imageUrl, File outputFile) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, byteCount);
                }

                fileOutputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadImageSync(final String imageUrl, final File outputFile) {
        // 创建一个新线程执行下载操作
        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 在新线程中执行下载操作
                    ImageUtil.downloadImage(imageUrl, outputFile);
                    // 下载完成后可以做进一步操作
                    // 例如：更新UI（记得要在主线程中更新UI）
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 启动线程
        downloadThread.start();

        // 等待线程完成后再执行其他操作
        try {
            downloadThread.join();  // 等待线程执行完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 继续执行下载完成后的后续操作
        // 注意：下载任务完成后在此位置可以进行后续工作
        Log.d("Download", "Image download completed and thread has finished.");
    }
}
