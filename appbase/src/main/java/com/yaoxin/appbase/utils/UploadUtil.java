package com.yaoxin.appbase.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.luban.Luban;
import com.yaoxin.appbase.utils.luban.OnCompressListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

public class UploadUtil {
    public static void uploadImage(String imagePath, String descriptionText, CommonCallBack callBack) {
        if (TextUtils.isEmpty(imagePath) || callBack == null) {
            return;
        }
        compressImages(imagePath, new LubanCommonCallBack() {
            @Override
            public void finishCompress(String filePath) {
                if (TextUtils.isEmpty(filePath)) {
                    return;
                }
                // 获取文件路径
                File file = new File(filePath);
                if (!file.exists()) {
                    return;
                }

                // 创建 RequestBody 实例
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"),
                        descriptionText == null ? "" : descriptionText);

                HttpUtil.apiW().customer_upload(body, description)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                try {
                                    if (body == null || body.data == null) {
                                        return;
                                    }
                                    UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                                    if (bean != null) {
                                        callBack.onCallBackUserBean(bean);
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        });

    }

    @AfterPermissionGranted(Constant.RC_PHOTO_PICKER_PERM)
    public static void openPhotoLibrary(Activity activity, int requestCode) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES
                    };
        }
        if (!EasyPermissions.hasPermissions(activity, permission)) {
            // 请求相册权限
            EasyPermissions.requestPermissions(activity, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }


        String[] perms1 = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(activity, perms1)) {
            EasyPermissions.requestPermissions(activity, "需要访问相机权限", Constant.RC_PHOTO_CAMERA_PERM, perms1);
            return;
        }

        // 必须与 Manifest 中 ${applicationId}.IMKitFileProvider 一致，否则部分机型拍照会直接闪退
        String authority = activity.getPackageName() + ".IMKitFileProvider";
        Matisse.from(activity)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, authority))
                .imageEngine(new GlideEngine())
                .showSingleMediaType(true)
                .forResult(requestCode);

    }

    public static void compressImages(String imagePaths, LubanCommonCallBack callBack) {
        if (TextUtils.isEmpty(imagePaths) || callBack == null) {
            return;
        }
        Context context = AppProxy.getInstance().getContext();
        if (context == null) {
            return;
        }
        File targetDir = context.getExternalFilesDir(null);
        if (targetDir == null) {
            targetDir = context.getFilesDir();
        }
        if (targetDir == null) {
            return;
        }
        Luban.with(context)
                .load(imagePaths)
                .ignoreBy(100) // 忽略小于 100KB 的图片
                .setTargetDir(targetDir.getAbsolutePath())
                .filter(path -> !(path == null || path.toLowerCase().endsWith(".gif")))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        // 压缩开始前的操作
                    }

                    @Override
                    public void onSuccess(File file) {
                        // 压缩成功的操作
                        if (file != null) {
                            callBack.finishCompress(file.getPath());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 压缩失败时回退原图路径，避免直接中断
                        callBack.finishCompress(imagePaths);
                    }
                }).launch();
    }

    /**
     * 优先使用 Matisse path；部分 Android 10+ 机型 path 为空，回退把 content Uri 拷到缓存。
     */
    public static String resolveImagePath(Context context, IntentData intentData) {
        if (intentData == null) {
            return "";
        }
        if (intentData.paths != null) {
            for (String path : intentData.paths) {
                if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                    return path;
                }
            }
        }
        if (intentData.uris != null) {
            for (Uri uri : intentData.uris) {
                String copied = copyUriToCache(context, uri);
                if (!TextUtils.isEmpty(copied)) {
                    return copied;
                }
            }
        }
        return "";
    }

    public static IntentData parseMatisseResult(android.content.Intent data) {
        IntentData result = new IntentData();
        if (data == null) {
            return result;
        }
        result.uris = Matisse.obtainResult(data);
        result.paths = Matisse.obtainPathResult(data);
        return result;
    }

    public static String copyUriToCache(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "";
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return "";
            }
            File dir = context.getExternalCacheDir();
            if (dir == null) {
                dir = context.getCacheDir();
            }
            if (dir == null) {
                return "";
            }
            if (!dir.exists() && !dir.mkdirs()) {
                return "";
            }
            File outFile = new File(dir, "upload_" + System.currentTimeMillis() + ".jpg");
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public interface LubanCommonCallBack {
        void finishCompress(String filePath);
    }

    public static class IntentData {
        public List<Uri> uris;
        public List<String> paths;
    }

    @AfterPermissionGranted(Constant.RC_PHOTO_PICKER_PERM)
    public static void choosePhotoLibrary(Fragment fragment, int maxNum) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        Activity activity = fragment.getActivity();

        String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES
                    };
        }
        if (!EasyPermissions.hasPermissions(activity, permission)) {
            // 请求相册权限
            EasyPermissions.requestPermissions(activity, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }


        String[] perms1 = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(activity, perms1)) {
            EasyPermissions.requestPermissions(activity, "需要访问相机权限", Constant.RC_PHOTO_CAMERA_PERM, perms1);
            return;
        }

        String authority = activity.getPackageName() + ".IMKitFileProvider";
        Matisse.from(fragment)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(Math.max(1, maxNum))
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, authority))
                .imageEngine(new GlideEngine())
                .showSingleMediaType(true)
                .forResult(Constant.REQUEST_CODE_CHOOSE);

    }

}
