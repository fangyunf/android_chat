// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.turunsi.yaoxin.databinding.ActivityMineAccountCodeBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;

import java.util.HashMap;
import java.util.Map;

public class AccoutCodeDetailActivity extends BaseActivity implements View.OnClickListener {

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
        Bitmap bitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (bitmap != null) {
            viewBinding.activityMineAccountCodeCodeIv.setImageBitmap(bitmap);
        }
        viewBinding.activityMineAccountCodeNameTv.setText(DataUtil.getUserInfo().username);
        viewBinding.activityMineAccountCode.setText("ID: " + DataUtil.getUserInfo().memberCode);
        GlideUtil.yh_loadImageRoundedCorner(
                this, viewBinding.activityMineAccountCodeHeadIv, DataUtil.getUserInfo().avatar, 2);
    }

    private Bitmap generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int width = 512;
            int height = 512;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 缩小白边，避免出现厚白色方框
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0x00000000);
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
            // 保存整张名片（图2效果），不含导航栏和底部按钮
            ImageUtil.saveViewToGallery(this, viewBinding.activityMineAccountCodeCard);
        }
    }
}
