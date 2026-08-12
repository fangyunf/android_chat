package com.zhihu.matisse.internal.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * http://stackoverflow.com/a/27271131/4739220
 *
 * Android 10+ / 华为等机型上 MediaStore._data 常为空，选图后需把 content Uri 拷到可读文件。
 */
public class PathUtils {
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Platform.hasKitKat() && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider

                final String id = DocumentsContract.getDocumentId(uri);
                try {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (isMediaDocument(uri)) { // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
            return uri.getPath();
        }

        return null;
    }

    /**
     * 返回可读本地文件路径：优先 MediaStore 路径；拿不到则把 Uri 拷到缓存（兼容华为/Android10+）。
     */
    public static String getReadablePath(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        String path = getPath(context, uri);
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            return path;
        }
        return copyUriToCacheFile(context, uri);
    }

    public static String copyUriToCacheFile(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        if (cacheDir == null) {
            return null;
        }
        String mime = context.getContentResolver().getType(uri);
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (TextUtils.isEmpty(ext)) {
            ext = "jpg";
        }
        boolean needJpeg =
                "heic".equalsIgnoreCase(ext)
                        || "heif".equalsIgnoreCase(ext)
                        || (mime != null && (mime.contains("heic") || mime.contains("heif")));
        File outFile =
                new File(
                        cacheDir,
                        "matisse_"
                                + System.currentTimeMillis()
                                + (needJpeg ? ".jpg" : ("." + ext)));
        try {
            if (needJpeg) {
                Bitmap bitmap;
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                    if (inputStream == null) {
                        return null;
                    }
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
                if (bitmap == null) {
                    return copyRaw(context, uri, outFile);
                }
                try (OutputStream outputStream = new FileOutputStream(outFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream);
                    outputStream.flush();
                } finally {
                    bitmap.recycle();
                }
                return outFile.getAbsolutePath();
            }
            return copyRaw(context, uri, outFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String copyRaw(Context context, Uri uri, File outFile) throws Exception {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(outFile)) {
            if (inputStream == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            return outFile.getAbsolutePath();
        }
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex(column);
                if (columnIndex < 0) {
                    return null;
                }
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
