// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityMineSetAnquanSetBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.DownLoadActivity;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AnQuanSetNewActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineSetAnquanSetBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
        viewBinding = ActivityMineSetAnquanSetBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        _requestData();
    }

    private void initView() {
        viewBinding.activityMineSetAnquanSetNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineSetAnquanSetRefreshTv.setOnClickListener(this);
        viewBinding.activityMineSetAnquanSetSwitchIv.setOnClickListener(this);
    }

    public void _requestData() {

        HttpUtil.apiW().home_sdState(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        viewBinding.activityMineSetAnquanSetSwitchIv.setSelected("1".equals(bean.state));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }


    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineSetAnquanSetNav.addCloseImageButton()) {
            finish();
        } else if (v == viewBinding.activityMineSetAnquanSetRefreshTv) {

            Activity that = this;
            HttpUtil.apiW().home_flushToken(new RegisterBean())
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                            DataUtil.putToken(bean.token);
                            IMUtil.loginIM(that, DataUtil.getUserid(), bean.imToken);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        } else if (v == viewBinding.activityMineSetAnquanSetSwitchIv) {

            HttpUtil.apiW().home_updateSd(new RegisterBean())
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                            ToastUtils.toastMsg("设置成功");
                            viewBinding.activityMineSetAnquanSetSwitchIv.setSelected(!viewBinding.activityMineSetAnquanSetSwitchIv.isSelected());
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {
                            ToastUtils.toastMsg("设置失败");
                        }
                    });
        }
    }
}
