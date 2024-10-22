package com.yaoxin.appbase.utils;


import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.luban.Luban;
import com.yaoxin.appbase.utils.luban.OnCompressListener;
import com.zhihu.matisse.GifSizeFilter;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnSelectedListener;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

public class UploadUtil {
    public static void uploadImage(String imagePath, String descriptionText, CommonCallBack callBack) {
        compressImages(imagePath, new LubanCommonCallBack() {
            @Override
            public void finishCompress(String filePath) {
                // 获取文件路径
                File file = new File(filePath);

                // 创建 RequestBody 实例
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionText);

                HttpUtil.apiW().customer_upload(body,description)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                                callBack.onCallBackUserBean(bean);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        });

    }
    @AfterPermissionGranted(Constant.RC_PHOTO_PICKER_PERM)
    public static void openPhotoLibrary(Activity activity,int requestCode) {

        String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                    new String[] {
                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                    };
        }
        if (!EasyPermissions.hasPermissions(activity, permission)) {
            // 请求相机权限
            EasyPermissions.requestPermissions(activity, "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }


        String[] perms1 = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(activity, perms1)) {
            EasyPermissions.requestPermissions(activity, "需要访问相机权限", Constant.RC_PHOTO_CAMERA_PERM, perms1);
            return;
        }


        Matisse.from(activity)
                .choose(MimeType.ofAll())
                .countable(true)
                .maxSelectable(1)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, "com.turunsi.mengliao.IMKitFileProvider"))
                .imageEngine(new GlideEngine())
                .forResult(Constant.REQUEST_CODE_CHOOSE);

    }
    public static void compressImages(String imagePaths, LubanCommonCallBack callBack) {
        Luban.with(AppProxy.getInstance().getContext())
                .load(imagePaths)
                .ignoreBy(100) // 忽略小于 100KB 的图片
                .setTargetDir(AppProxy.getInstance().getContext().getExternalFilesDir(null).getAbsolutePath())
                .filter(path -> !(path == null || path.toLowerCase().endsWith(".gif")))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        // 压缩开始前的操作
                    }

                    @Override
                    public void onSuccess(File file) {
                        // 压缩成功的操作
                        String path = file.getPath();
                        callBack.finishCompress(path);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // 压缩失败的操作
                    }
                }).launch();
    }
    public interface LubanCommonCallBack {
        void finishCompress(String filePath);
    }

    @AfterPermissionGranted(Constant.RC_PHOTO_PICKER_PERM)
    public static void choosePhotoLibrary(Fragment fragment, int maxNum) {

        String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                    new String[] {
                            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                    };
        }
        if (!EasyPermissions.hasPermissions(fragment.getActivity(), permission)) {
            // 请求相机权限
            EasyPermissions.requestPermissions(fragment.getActivity(), "需要访问相册权限", Constant.RC_PHOTO_PICKER_PERM, permission);
            return;
        }


        String[] perms1 = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(fragment.getActivity(), perms1)) {
            EasyPermissions.requestPermissions(fragment.getActivity(), "需要访问相机权限", Constant.RC_PHOTO_CAMERA_PERM, perms1);
            return;
        }


        Matisse.from(fragment)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(maxNum)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, "com.turunsi.balishijia.IMKitFileProvider"))
                .imageEngine(new GlideEngine())
                .forResult(Constant.REQUEST_CODE_CHOOSE);

    }

}
