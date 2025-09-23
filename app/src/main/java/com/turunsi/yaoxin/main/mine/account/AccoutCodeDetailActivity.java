// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.turunsi.yaoxin.databinding.ActivityMineAccountCodeBinding;
import com.turunsi.yaoxin.databinding.ActivityMineAccountDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.zhihu.matisse.GifSizeFilter;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class AccoutCodeDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;
    private ActivityMineAccountCodeBinding viewBinding;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
        Bitmap bitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (bitmap != null) {
            viewBinding.activityMineAccountCodeCodeIv.setImageBitmap(bitmap);
        }
        viewBinding.activityMineAccountCodeNameTv.setText(DataUtil.getUserInfo().username);
        viewBinding.activityMineAccountCode.setText("ID:" + DataUtil.getUserInfo().memberCode);
        GlideUtil.yh_loadImageRoundedCorner(this, viewBinding.activityMineAccountCodeHeadIv, DataUtil.getUserInfo().avatar, 2);
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

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineAccountCodeNav.addCloseImageButton()) {
            finish();
        } else if (v == viewBinding.activityMineAccountCodeSavePhoto) {
            ImageUtil.saveImageViewToGallery(this, viewBinding.activityMineAccountCodeCodeIv);
        }
    }

}
