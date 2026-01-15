// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.setting.ExchangeAccountActivity;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
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
        viewBinding = ActivityMineAccountDetailBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtils.transtStatusBar(this, viewBinding.activityMineAccountDetailNav);

        viewBinding.activityMineAccountDetailNav.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        viewBinding.activityMineAccountDetailNav.setActionText("完成");
        viewBinding.activityMineAccountDetailNav.setActionClickListener(new ICallBack() {
            @Override
            public void callBack() {
                String name = viewBinding.activityMineAccountDetailNameTv.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    ToastUtils.toastLongMsg("请输入用户名称");
                    return;
                }
                updatePersonInfo("", name);
            }
        });

        initView();
        EventBus.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event.getTag().equals("refreshUserInfo")) {
            viewBinding.activityMineAccountDetailNameTv.setText(event.getText());
            if (viewBinding.tvNickname != null) {
                viewBinding.tvNickname.setText(event.getText());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {


        // 头像点击
        viewBinding.activityMineAccountDetailHeadIv.setOnClickListener(this);
        // 设置新照片文字点击
        viewBinding.tvSetPhoto.setOnClickListener(this);

        // 昵称点击
        viewBinding.flNickname.setOnClickListener(this);

        // 账号复制
        viewBinding.ivAccountCopy.setOnClickListener(this);
        viewBinding.activityMineAccountDetailDetailIdTv.setOnClickListener(this);

        // 切换其他账号
        viewBinding.tvSwitchAccount.setOnClickListener(this);

        // 退出当前账号
        viewBinding.tvLogout.setOnClickListener(this);

        // 加载用户数据
        loadUserData();
    }

    private void loadUserData() {
        UserBean userInfo = DataUtil.getUserInfo();
        // 设置头像
        GlideUtil.yh_loadImageRoundedCorner(this, viewBinding.activityMineAccountDetailHeadIv, userInfo.avatar, 40);
        // 设置昵称（如果有name字段，否则使用username）
        String nickname = TextUtils.isEmpty(userInfo.name) ? userInfo.username : userInfo.name;
        viewBinding.tvNickname.setText(nickname);
        // 设置账号
        viewBinding.activityMineAccountDetailDetailIdTv.setText(TextUtils.isEmpty(userInfo.memberCode) ? "" : userInfo.memberCode);
    }

    private void copyAccount() {
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String memberCode = DataUtil.getUserInfo() != null ? DataUtil.getUserInfo().memberCode : "";
        if (!TextUtils.isEmpty(memberCode)) {
            ClipData clipData = ClipData.newPlainText(null, memberCode);
            cmb.setPrimaryClip(clipData);
            ToastUtils.toastMsg("复制成功");
        }
    }

    private void logout() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        AccountDetailActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(AccountDetailActivity.this);
                        }
                        DataUtil.deleteData();
                        startActivity(new Intent(AccountDetailActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineAccountDetailHeadIv || v == viewBinding.tvSetPhoto) {
            // 上传头像
            UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
        } else if (v == viewBinding.flName || v == viewBinding.flNickname) {
            // 修改用户名/昵称
            ModifyTextActivity.start(ModifyTextActivity.class, this, null);
        } else if (v == viewBinding.ivAccountCopy || v == viewBinding.activityMineAccountDetailDetailIdTv) {
            // 复制账号
            copyAccount();
        } else if (v == viewBinding.tvSwitchAccount) {
            // 切换其他账号
            Intent intent = new Intent(AccountDetailActivity.this, ExchangeAccountActivity.class);
            startActivity(intent);
        } else if (v == viewBinding.tvLogout) {
            // 退出当前账号
            logout();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uris = Matisse.obtainResult(data);
            List<String> strings = Matisse.obtainPathResult(data);

            if (!strings.isEmpty()) {
                uploadImage(strings.get(0), "");
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
                GlideUtil.yh_loadImageRoundedCorner(viewBinding.activityMineAccountDetailHeadIv.getContext(), viewBinding.activityMineAccountDetailHeadIv, userBean.url, 40);
                updatePersonInfo(userBean.url, "");
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

    void updatePersonInfo(String headUrl, String name) {
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
                        if (!TextUtils.isEmpty(name)) {
                            finish();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

}
