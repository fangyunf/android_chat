// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.turunsi.yaoxin.databinding.ActivityMineAccountCodeBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;

public class AccoutCodeDetailActivity extends BaseActivity implements View.OnClickListener {

    /** 与页面背景接近的深蓝，二维码模块色 */
    private static final int QR_MODULE_COLOR = 0xFF1A335B;

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

        UserBean user = DataUtil.getUserInfo();
        if (user != null) {
            viewBinding.activityMineAccountCodeNameTv.setText(
                    TextUtils.isEmpty(user.username) ? "" : user.username);
            viewBinding.activityMineAccountCodeIdTv.setText(
                    "ID " + (TextUtils.isEmpty(user.memberCode) ? "" : user.memberCode));
            GlideUtil.yh_loadImage(
                    this, viewBinding.activityMineAccountCodeHeadIv, user.avatar);
            Bitmap bitmap = generateQRCode(user.memberCode);
            if (bitmap != null) {
                viewBinding.activityMineAccountCodeCodeIv.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap generateQRCode(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int width = 512;
            int height = 512;
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? QR_MODULE_COLOR : 0xFFFFFFFF);
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
            // 保存整张名片卡片（头像/昵称/ID/二维码），不含导航栏和按钮
            ImageUtil.saveViewToGallery(this, viewBinding.activityMineAccountCodeCard);
        }
    }
}
