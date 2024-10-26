// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityChangeChatSkinBinding;
import com.turunsi.yaoxin.databinding.ActivityMineDownLoadBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;

public class ChangeSkinActivity extends BaseActivity implements View.OnClickListener {

  private ActivityChangeChatSkinBinding viewBinding;

  String bgIvName = "chat_view_bg_1";
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityChangeChatSkinBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {

    StatusBarUtils.setStatusBarLightMode(this, true, true);
    RelativeLayout.LayoutParams params =
            (RelativeLayout.LayoutParams) viewBinding.activityChangeChatSkinNav.getLayoutParams();
    params.height = params.height + BarUtils.getStatusBarHeight();
    viewBinding.activityChangeChatSkinNav.setLayoutParams(params);
    viewBinding.activityChangeChatSkinNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);

    viewBinding.activityChangeChatSkinNav.addCloseImageButton().setOnClickListener(this);
    viewBinding.activityChangeChatSkinConfirmTv.setOnClickListener(this);
    viewBinding.activityChangeChatSkinChangeTv.setOnClickListener(this);
    viewBinding.activityChangeChatSkinChooseTv.setOnClickListener(this);


  }

  @Override
  protected void _requestData() {

  }

  @Override
  public void onClick(View v) {
    if (v == viewBinding.activityChangeChatSkinNav.addCloseImageButton()) {
      finish();
    } else if (v == viewBinding.activityChangeChatSkinConfirmTv) {
      DataUtil.putSkinImageName(bgIvName);
      ToastUtils.toastMsg("修改成功");
      finish();
    } else if (v == viewBinding.activityChangeChatSkinChangeTv) {
      Random random = new Random();
      int randomNumber = random.nextInt(12) + 1; // 生成 1 到 12 的随机数

      String imageName = "chat_view_bg_" + randomNumber;
      bgIvName = imageName;
      Resources resources = getResources();
      int resId = resources.getIdentifier(imageName, "mipmap", getPackageName());
      // 如果找到了资源，则可以使用这个ID获取Drawable
      Drawable drawable = null;
      if (resId > 0) {
        drawable = ContextCompat.getDrawable(this, resId);
      }
      // 如果需要将drawable设置到ImageView中
      if (drawable != null) {
        viewBinding.activityChangeChatSkinBgIv.setImageDrawable(drawable);
      }

    } else if (v == viewBinding.activityChangeChatSkinChooseTv) {
      UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
      List<Uri> uris = Matisse.obtainResult(data);
      try {

        Uri selectedImageUri = Matisse.obtainResult(data).get(0);
        // 保存图片到本地
        saveImageToAppStorage(selectedImageUri);
      } catch (Exception e) {

      }
    }
  }

//  public Bitmap getSavedImageBitmap() {
//    File savedImageFile = getSavedImageFile();
//
//    if (savedImageFile != null) {
//      return BitmapFactory.decodeFile(savedImageFile.getAbsolutePath());
//    } else {
//      return null;
//    }
//  }
  public void saveImageToAppStorage(Uri imageUri) {
    ContentResolver resolver = getContentResolver();
    File appStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File destFile = new File(appStorageDir, "custom_chat_bg.png");

    try (InputStream in = resolver.openInputStream(imageUri);
         OutputStream out = new FileOutputStream(destFile)) {

      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }

      bgIvName = destFile.getAbsolutePath();
      viewBinding.activityChangeChatSkinBgIv.setImageURI(Uri.fromFile(destFile));
//      System.out.println("图片已保存到：" + destFile.getAbsolutePath());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
