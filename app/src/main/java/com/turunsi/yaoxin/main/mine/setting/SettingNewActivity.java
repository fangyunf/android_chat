// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSettingBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.welcome.WelcomeActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SettingNewActivity extends BaseActivity implements View.OnClickListener {

  private ActivityMineSetNewBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityMineSetNewBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {

    viewBinding.activityMineSetNewNav.addCloseImageButton().setOnClickListener(this);
    viewBinding.activityMineSetNewPhone.viewTitleArrowLl.setOnClickListener(this);
      viewBinding.activityMineSetNewPhone.viewTitleArrowArrowIv.setVisibility(View.GONE);
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setText(DataUtil.getUserInfo().phoneNo);
    viewBinding.activityMineSetNewModifyPwd.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewFontChange.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewDeleteCache.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewAboutUs.viewTitleArrowLl.setOnClickListener(this);
    viewBinding.activityMineSetNewZhuxiaoAcountLl.setOnClickListener(this);
    viewBinding.activityMineSetNewTuichuAcountLl.setOnClickListener(this);
    viewBinding.activityMineSetNewExchangeAcountLl.setOnClickListener(this);


    viewBinding.activityMineSetNewPhone.viewTitleArrowTv.setText("当前手机号码");
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setText(DataUtil.getUserInfo().phoneNo);
    viewBinding.activityMineSetNewModifyPwd.viewTitleArrowTv.setText("修改登录密码");
    viewBinding.activityMineSetNewFontChange.viewTitleArrowTv.setText("字体调节");
      viewBinding.activityMineSetNewFontChange.viewTitleArrowLl.setVisibility(View.GONE);
    viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowTv.setText("通知声音");
    viewBinding.activityMineSetNewDeleteCache.viewTitleArrowTv.setText("清空缓存");
    viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowTv.setText("清空所有聊天记录");
    viewBinding.activityMineSetNewAboutUs.viewTitleArrowTv.setText("关于我们");
  }

  @Override
  public void onClick(View v) {
    if (v == viewBinding.activityMineSetNewPhone.viewTitleArrowLl) {

    } else if (v == viewBinding.activityMineSetNewModifyPwd.viewTitleArrowLl) {
      ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);

    } else if (v == viewBinding.activityMineSetNewFontChange.viewTitleArrowLl) {

    } else if (v == viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowLl) {
      startActivity(new Intent(SettingNewActivity.this, SettingNotifyActivity.class));
    } else if (v == viewBinding.activityMineSetNewDeleteCache.viewTitleArrowLl) {
      startActivity(new Intent(SettingNewActivity.this, ClearCacheActivity.class));

        DialogAlertUtil.showAlert("确定清空缓存吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
            @Override
            public void clickType(int type) {
                if (type == 1) {
                    MiscRepo.INSTANCE.clearCacheSize(
                            getSDKFileType(),
                            new FetchCallback<Void>() {
                                @Override
                                public void onSuccess(@Nullable Void param) {}

                                @Override
                                public void onFailed(int code) {}

                                @Override
                                public void onException(@Nullable Throwable exception) {}
                            });
                }
            }
        },getSupportFragmentManager());
    } else if (v == viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowLl) {
        DialogAlertUtil.showAlert("确定清空聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
            @Override
            public void clickType(int type) {
                if (type == 1) {
                    MiscRepo.INSTANCE.clearMessageCache();
                }
            }
        },getSupportFragmentManager());
    } else if (v == viewBinding.activityMineSetNewAboutUs.viewTitleArrowLl) {

        AboutUsNewActivity.start(AboutUsNewActivity.class,this,null);
    } else if (v == viewBinding.activityMineSetNewZhuxiaoAcountLl) {
      CommonChoiceDialog dialog = new CommonChoiceDialog();
      dialog
              .setTitleStr("温馨提示")
              .setContentStr("确定注销账号吗?")
              .setNegativeStr("取消")
              .setPositiveStr("确定")
              .setConfirmListener(
                      new ChoiceListener() {
                        @Override
                        public void onPositive() {

                          HttpUtil.apiW().home_logout()
                                  .enqueue(new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                      ToastUtils.toastMsg(body.msg);
                                      showLogin();
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {

                                    }
                                  });
                        }

                        @Override
                        public void onNegative() {}
                      })
              .show(getSupportFragmentManager());

    } else if (v == viewBinding.activityMineSetNewTuichuAcountLl) {
      showLogin();

    } else if (v == viewBinding.activityMineSetNewExchangeAcountLl) {

    } else if (v == viewBinding.activityMineSetNewNav.addCloseImageButton()) {
      finish();

    }
  }

  void showLogin() {
    IMKitClient.logoutIM(
            new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
              @Override
              public void onError(int errorCode, @NonNull String errorMsg) {
                Toast.makeText(
                                SettingNewActivity.this,
                                "error code is " + errorCode + ", message is " + errorMsg,
                                Toast.LENGTH_SHORT)
                        .show();
              }

              @Override
              public void onSuccess(@Nullable Void data) {
                if (getApplicationContext() instanceof IMApplication) {
                  ((IMApplication) getApplicationContext())
                          .clearActivity(SettingNewActivity.this);
                }
                DataUtil.deleteData();
                startActivity(new Intent(SettingNewActivity.this, LoginActivity.class));
                finish();
              }
            });
  }

    private List<DirCacheFileType> getSDKFileType() {
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);
        return types;
    }
}
