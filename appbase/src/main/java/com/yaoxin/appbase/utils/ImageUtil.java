package com.yaoxin.appbase.utils;

import static com.netease.yunxin.kit.common.utils.ThreadUtils.runOnUiThread;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

    /** 保存图片到相册所需权限（与项目其它相册逻辑一致） */
    public static String[] getImageSavePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {Manifest.permission.READ_MEDIA_IMAGES};
        }
        return new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    public static boolean hasImageSavePermission(Context context) {
        return EasyPermissions.hasPermissions(context, getImageSavePermissions());
    }

    /** 保存视频到相册所需权限 */
    public static String[] getVideoSavePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {Manifest.permission.READ_MEDIA_VIDEO};
        }
        return new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    /** 从 ImageView 取出 Bitmap（优先 drawable，避免 drawingCache 为空） */
    public static Bitmap getBitmapFromImageView(ImageView imageView) {
        if (imageView == null) {
            return null;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                return bitmap;
            }
        }
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap cache = imageView.getDrawingCache();
        if (cache != null) {
            Bitmap copy = Bitmap.createBitmap(cache);
            imageView.setDrawingCacheEnabled(false);
            return copy;
        }
        imageView.setDrawingCacheEnabled(false);
        return null;
    }

    /**
     * 保存 Bitmap 到相册（先申请权限，授权后自动继续保存）。
     * 调用方 Activity/Fragment 需在 onRequestPermissionsResult 中调用
     * {@link #onSaveImageGalleryPermissionResult(Activity, int, String[], int[])}。
     */
    public static void saveImageToGallery(Activity activity, Bitmap bitmap) {
        if (activity == null || bitmap == null || bitmap.isRecycled()) {
            ToastUtils.toastMsg("保存失败");
            return;
        }
        pendingGalleryBitmap = bitmap;
        pendingGalleryActivity = activity;
        String[] permissions = getImageSavePermissions();
        if (EasyPermissions.hasPermissions(activity, permissions)) {
            saveBitmapToGallery(activity, bitmap);
            clearPendingGallerySave();
            return;
        }
        EasyPermissions.requestPermissions(
                activity, "保存图片需要访问相册权限", Constant.RC_SAVE_IMAGE_GALLERY, permissions);
    }

    public static void saveImageViewToGallery(Activity activity, ImageView imageView) {
        if (activity == null || imageView == null) {
            return;
        }
        Bitmap bitmap = getBitmapFromImageView(imageView);
        if (bitmap == null) {
            ToastUtils.toastMsg("保存失败");
            return;
        }
        saveImageToGallery(activity, bitmap);
    }

    private static Bitmap pendingGalleryBitmap;
    private static Activity pendingGalleryActivity;

    private static void clearPendingGallerySave() {
        pendingGalleryBitmap = null;
        pendingGalleryActivity = null;
    }

    public static void onSaveImageGalleryPermissionResult(
            Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, activity);
        if (requestCode != Constant.RC_SAVE_IMAGE_GALLERY) {
            return;
        }
        Activity pendingActivity = pendingGalleryActivity;
        Bitmap pendingBitmap = pendingGalleryBitmap;
        clearPendingGallerySave();
        if (pendingActivity == null
                || pendingBitmap == null
                || activity != pendingActivity
                || !EasyPermissions.hasPermissions(activity, getImageSavePermissions())) {
            return;
        }
        saveBitmapToGallery(activity, pendingBitmap);
    }

    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap) {
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

        if (savedImageURL != null) {
            ToastUtils.toastMsg("保存成功");
            return true;
        }
        ToastUtils.toastMsg("保存失败");
        return false;
    }

    /** 将本地图片文件保存到系统相册，兼容 Android 10+ */
    public static boolean saveImageFileToGallery(Context context, String sourcePath) {
        if (context == null || sourcePath == null || sourcePath.isEmpty()) {
            return false;
        }
        File source = new File(sourcePath);
        if (!source.exists() || !source.isFile()) {
            return false;
        }
        String fileName = source.getName();
        if (!fileName.contains(".")) {
            fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        }
        String mimeType = "image/jpeg";
        if (fileName.toLowerCase().endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.toLowerCase().endsWith(".webp")) {
            mimeType = "image/webp";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            mimeType = "image/gif";
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri item = context.getContentResolver().insert(collection, values);
                if (item == null) {
                    return false;
                }
                try (InputStream in = new java.io.FileInputStream(source);
                        OutputStream out = context.getContentResolver().openOutputStream(item)) {
                    if (out == null) {
                        return false;
                    }
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                context.getContentResolver().update(item, values, null, null);
                return true;
            } else {
                File picturesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!picturesDir.exists() && !picturesDir.mkdirs()) {
                    return false;
                }
                File dest = new File(picturesDir, fileName);
                try (InputStream in = new java.io.FileInputStream(source);
                        OutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
                MediaStore.Images.Media.insertImage(
                        context.getContentResolver(), dest.getAbsolutePath(), fileName, fileName);
                context.sendBroadcast(
                        new android.content.Intent(
                                android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(dest)));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
