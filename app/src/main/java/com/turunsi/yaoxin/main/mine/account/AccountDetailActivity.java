// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineAccountDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.zhihu.matisse.GifSizeFilter;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AccountDetailActivity extends BaseActivity implements View.OnClickListener {

  private static final int REQUEST_CODE_CHOOSE = 23;
  private ActivityMineAccountDetailBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityMineAccountDetailBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
      EventBus.getDefault().register(this);
  }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("refreshUserInfo")) {
//            viewBinding.activityMineAccountDetailUsername.viewTitleArrowRightTv.setText(event.getText());
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        transtStatusBar(viewBinding.activityMineAccountDetailNav);
    viewBinding.activityMineAccountDetailNav.addCloseImageButton().setOnClickListener(this);
    viewBinding.activityMineAccountDetailModifyHeadTv.setOnClickListener(this);
    viewBinding.activityMineAccountDetailModifyNameTv.setOnClickListener(this);
    viewBinding.activityMineAccountDetailNav.getTitleView().setVisibility(View.GONE);
//    viewBinding.activityMineAccountDetailUsername.viewTitleArrowLl.setOnClickListener(this);
//    viewBinding.activityMineAccountDetailModifyHeadIvRl.setOnClickListener(this);
//
//
//    viewBinding.activityMineAccountDetailAccountId.viewTitleArrowTv.setText("账号ID");
//    viewBinding.activityMineAccountDetailUsername.viewTitleArrowTv.setText("用户名");
//    viewBinding.activityMineAccountDetailPhoneNum.viewTitleArrowTv.setText("手机号");
//
//    viewBinding.activityMineAccountDetailAccountId.viewTitleArrowArrowIv.setVisibility(View.GONE);
//    viewBinding.activityMineAccountDetailPhoneNum.viewTitleArrowArrowIv.setVisibility(View.GONE);
//
//    viewBinding.activityMineAccountDetailAccountId.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
//    viewBinding.activityMineAccountDetailPhoneNum.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
//
//    viewBinding.activityMineAccountDetailPhoneNum.viewTitleArrowRightTv.setText("+86 "+DataUtil.getUserInfo().phoneNo);
//    viewBinding.activityMineAccountDetailAccountId.viewTitleArrowRightTv.setText("ID：" + DataUtil.getUserInfo().memberCode);
//    GlideUtil.yh_loadImageRoundedCorner(this,viewBinding.activityMineAccountDetailHeadIv,DataUtil.getUserInfo().avatar,25);
//      viewBinding.activityMineAccountDetailUsername.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
//    viewBinding.activityMineAccountDetailUsername.viewTitleArrowRightTv.setText(DataUtil.getUserInfo().username);
  }

  @Override
  public void onClick(View v) {
    if (v == viewBinding.activityMineAccountDetailModifyNameTv) {

        ModifyTextActivity.start(ModifyTextActivity.class,this,null);
    } else if (v == viewBinding.activityMineAccountDetailModifyHeadTv) {
        UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
    } else if (v == viewBinding.activityMineAccountDetailNav.addCloseImageButton()) {
      finish();

    }
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
        List<Uri> uris = Matisse.obtainResult(data);
        List<String> strings = Matisse.obtainPathResult(data);

        if (!strings.isEmpty()) {
            uploadImage(strings.get(0),"");
        }
    }
  }

  public void uploadImage(String imagePath, String descriptionText) {
    // 获取文件路径
      UploadUtil.uploadImage(imagePath, "", new CommonCallBack() {
          @Override
          public void onCallBackUserBean(UserBean userBean) {
              UserBean userInfo = DataUtil.getUserInfo();
              userInfo.avatar = userBean.url;
              DataUtil.putUserInfo(userInfo);
              DataUtil.updateLoginUserInfoList(userInfo);
              GlideUtil.yh_loadImage(viewBinding.activityMineAccountDetailHeadIv.getContext(),viewBinding.activityMineAccountDetailHeadIv, userBean.url);
              updatePersonInfo(userBean.url,"");
          }
      });
//    File file = new File(imagePath);
//
//    // 创建 RequestBody 实例
//    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
//    RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionText);
//
//    HttpUtil.apiW().customer_upload(body,description)
//            .enqueue(new CommonCallback<NetData>() {
//              @Override
//              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
//              UserBean userInfo = DataUtil.getUserInfo();
//              userInfo.avatar = bean.url;
//              DataUtil.putUserInfo(userInfo);
//                updatePersonInfo(bean.url,"");
//              }
//
//              @Override
//              public void Failure(Call<NetData> call, Throwable t) {
//
//              }
//            });
  }

  void updatePersonInfo(String headUrl,String name) {
    RegisterBean bean = new RegisterBean();
    if (!headUrl.isEmpty()) {
      bean.avatar = headUrl;
    }
    if (!name.isEmpty()) {
      bean.name = name;
    }
    HttpUtil.apiW().home_changeInfo(bean)
            .enqueue(new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                ToastUtils.toastMsg(body.msg);
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {

              }
            });
  }

}
