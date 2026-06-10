// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import static com.yzq.zxinglibrary.common.Constant.CODED_CONTENT;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.turunsi.yaoxin.databinding.ActivityMineAccountCodeBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yzq.zxinglibrary.android.CaptureActivity;

import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

public class AccoutCodeDetailActivity extends BaseActivity
        implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_SCAN = 0x01;

    private ActivityMineAccountCodeBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMineAccountCodeBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        transtStatusBar(viewBinding.activityMineAccountCodeNav);
        initView();
    }

    private void initView() {
        viewBinding.activityMineAccountCodeNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineAccountCodeSavePhoto.setOnClickListener(this);
        viewBinding.activityMineAccountCodeScanLl.setOnClickListener(this);

        Bitmap bitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (bitmap != null) {
            viewBinding.activityMineAccountCodeCodeIv.setImageBitmap(bitmap);
        }
        viewBinding.activityMineAccountCodeNameTv.setText(DataUtil.getUserInfo().username);
        viewBinding.activityMineAccountCode.setText(DataUtil.getUserInfo().memberCode);
        GlideUtil.yh_loadImageRoundedCorner(
                this, viewBinding.activityMineAccountCodeHeadIv, DataUtil.getUserInfo().avatar, 36);
    }

    private Bitmap generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int width = 512;
            int height = 512;
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCardImage() {
        Runnable saveTask =
                () -> {
                    Bitmap bitmap = captureCardBitmap();
                    if (bitmap == null) {
                        ToastUtils.toastMsg("保存失败");
                        return;
                    }
                    ImageUtil.saveImageToGallery(this, bitmap);
                };
        if (viewBinding.activityMineAccountCodeCardLl.getWidth() == 0) {
            viewBinding.activityMineAccountCodeCardLl.post(saveTask);
        } else {
            saveTask.run();
        }
    }

    @Nullable
    private Bitmap captureCardBitmap() {
        View card = viewBinding.activityMineAccountCodeCardLl;
        if (card.getWidth() <= 0 || card.getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap =
                Bitmap.createBitmap(card.getWidth(), card.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        card.draw(canvas);
        return bitmap;
    }

    private void checkAndRequestScanPermissions() {
        String[] storagePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermission =
                    new String[] {
                        Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                    };
        } else {
            storagePermission =
                    new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
        }

        if (!EasyPermissions.hasPermissions(this, storagePermission)) {
            EasyPermissions.requestPermissions(
                    this,
                    "需要访问相册权限才能使用扫码功能",
                    Constant.RC_PHOTO_PICKER_PERM,
                    storagePermission);
            return;
        }

        String[] cameraPermission = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(this, cameraPermission)) {
            EasyPermissions.requestPermissions(
                    this,
                    "需要访问相机权限才能使用扫码功能",
                    Constant.RC_PHOTO_CAMERA_PERM,
                    cameraPermission);
            return;
        }

        openScanActivity();
    }

    private void openScanActivity() {
        startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN);
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineAccountCodeNav.addCloseImageButton()) {
            finish();
        } else if (v == viewBinding.activityMineAccountCodeSavePhoto) {
            saveCardImage();
        } else if (v == viewBinding.activityMineAccountCodeScanLl) {
            checkAndRequestScanPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || requestCode != REQUEST_CODE_SCAN) {
            return;
        }
        String content = data.getStringExtra(CODED_CONTENT);
        if (content == null) {
            return;
        }
        String result = content;
        String[] split = content.split("\\.");
        if (split.length == 3) {
            result = split[1];
        }
        RegisterBean bean = new RegisterBean();
        bean.phoneAndCode = result;
        bean.type = 0;
        String finalResult = result;
        HttpUtil.apiW()
                .friends_search(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                UserBean userInfo =
                                        new Gson().fromJson(body.data.toString(), UserBean.class);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("user", new Gson().toJson(userInfo));
                                FunAddFriendVerifyActivity.start(
                                        FunAddFriendVerifyActivity.class,
                                        AccoutCodeDetailActivity.this,
                                        map);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                ToastUtils.toastMsg(finalResult);
                            }
                        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.RC_SAVE_IMAGE_GALLERY) {
            ImageUtil.onSaveImageGalleryPermissionResult(
                    this, requestCode, permissions, grantResults);
        } else {
            EasyPermissions.onRequestPermissionsResult(
                    requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        checkAndRequestScanPermissions();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        ToastUtils.toastMsg("需要相关权限才能使用扫码功能");
    }
}
