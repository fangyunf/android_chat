package com.yaoxin.appbase.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

public class UploadUtil {

    private static WeakReference<Activity> pendingPhotoActivity;
    private static int pendingPhotoRequestCode = Constant.REQUEST_CODE_CHOOSE;

    public static void uploadImage(String imagePath, String descriptionText, CommonCallBack callBack) {
        if (TextUtils.isEmpty(imagePath)) {
            ToastUtils.toastMsg("图片无效，请重新选择");
            return;
        }
        compressImages(imagePath, new LubanCommonCallBack() {
            @Override
            public void finishCompress(String filePath) {
                File file = new File(filePath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionText);

                HttpUtil.apiW().customer_upload(body, description)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                                callBack.onCallBackUserBean(bean);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                ToastUtils.toastMsg("上传失败，请重试");
                            }
                        });
            }
        });
    }

    /** 权限授予后由 BaseActivity 回调，继续打开相册 */
    public static void resumePendingPhotoLibrary() {
        Activity activity = pendingPhotoActivity != null ? pendingPhotoActivity.get() : null;
        if (activity == null || activity.isFinishing()) {
            return;
        }
        openPhotoLibrary(activity, pendingPhotoRequestCode);
    }

    public static void openPhotoLibrary(Activity activity, int requestCode) {
        if (activity == null) {
            return;
        }
        pendingPhotoActivity = new WeakReference<>(activity);
        pendingPhotoRequestCode = requestCode;

        String[] permission = getAlbumPermissions();
        if (!EasyPermissions.hasPermissions(activity, permission)) {
            EasyPermissions.requestPermissions(
                    activity, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }

        // 选相册不强制相机；有相机权限才开拍照，避免拒相机后点上传无反应
        boolean canCapture = EasyPermissions.hasPermissions(activity, Manifest.permission.CAMERA);
        String authority = activity.getPackageName() + ".IMKitFileProvider";

        Matisse.from(activity)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(1)
                .capture(canCapture)
                .captureStrategy(new CaptureStrategy(false, authority))
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    public static void choosePhotoLibrary(Fragment fragment, int maxNum) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        Activity activity = fragment.getActivity();
        pendingPhotoActivity = new WeakReference<>(activity);
        pendingPhotoRequestCode = Constant.REQUEST_CODE_CHOOSE;

        String[] permission = getAlbumPermissions();
        if (!EasyPermissions.hasPermissions(activity, permission)) {
            EasyPermissions.requestPermissions(
                    activity, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }

        boolean canCapture = EasyPermissions.hasPermissions(activity, Manifest.permission.CAMERA);
        String authority = activity.getPackageName() + ".IMKitFileProvider";

        Matisse.from(fragment)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(maxNum)
                .capture(canCapture)
                .captureStrategy(new CaptureStrategy(false, authority))
                .imageEngine(new GlideEngine())
                .forResult(Constant.REQUEST_CODE_CHOOSE);
    }

    private static String[] getAlbumPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
            };
        }
        return new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    /**
     * 兼容 Android 10+：Matisse path 可能为空，优先有效文件路径，否则把 Uri 拷到缓存再上传。
     */
    public static String resolveSelectedImagePath(Context context, Intent data) {
        if (context == null || data == null) {
            return null;
        }
        List<String> paths = Matisse.obtainPathResult(data);
        if (paths != null && !paths.isEmpty()) {
            String path = paths.get(0);
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                return path;
            }
        }
        List<Uri> uris = Matisse.obtainResult(data);
        if (uris != null && !uris.isEmpty() && uris.get(0) != null) {
            return copyUriToCacheFile(context, uris.get(0));
        }
        return null;
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
        File outFile = new File(cacheDir, "upload_" + System.currentTimeMillis() + ".jpg");
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void compressImages(String imagePaths, LubanCommonCallBack callBack) {
        if (TextUtils.isEmpty(imagePaths)) {
            ToastUtils.toastMsg("图片无效，请重新选择");
            return;
        }
        File targetDir = AppProxy.getInstance().getContext().getExternalFilesDir(null);
        if (targetDir == null) {
            targetDir = AppProxy.getInstance().getContext().getFilesDir();
        }
        Luban.with(AppProxy.getInstance().getContext())
                .load(imagePaths)
                .ignoreBy(100)
                .setTargetDir(targetDir.getAbsolutePath())
                .filter(path -> !(path == null || path.toLowerCase().endsWith(".gif")))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {}

                    @Override
                    public void onSuccess(File file) {
                        callBack.finishCompress(file.getPath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.toastMsg("图片处理失败，请重试");
                    }
                })
                .launch();
    }

    public interface LubanCommonCallBack {
        void finishCompress(String filePath);
    }
}
