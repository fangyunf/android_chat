// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityAccountAnquanManagerBinding;
import com.turunsi.yaoxin.databinding.ActivityEggListIndexBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class EggListIndexActivity extends BaseActivity implements View.OnClickListener {

    ActivityEggListIndexBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityEggListIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        StatusBarUtils.openImmersiveStatusBar(this);


    }

    @Override
    protected void _initView() {
//        binding.activityEggListIndexNav.addCloseImageButton().setOnClickListener(this);

        binding.activityEggListIndex188.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_188_big);
        binding.activityEggListIndex288.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_288_big);
        binding.activityEggListIndex388.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_388_big);
        binding.activityEggListIndex588.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_588_big);
        binding.activityEggListIndex666.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_666_big);
        binding.activityEggListIndex888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_888_big);
        binding.activityEggListIndex1888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_1888_big);
        binding.activityEggListIndex2888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_2888_big);
        binding.activityEggListIndex3888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_3888_big);

        binding.activityEggListIndex188.eggListIndexItemViewEggTv.setText("￥188");
        binding.activityEggListIndex288.eggListIndexItemViewEggTv.setText("￥288");
        binding.activityEggListIndex388.eggListIndexItemViewEggTv.setText("￥388");
        binding.activityEggListIndex588.eggListIndexItemViewEggTv.setText("￥588");
        binding.activityEggListIndex666.eggListIndexItemViewEggTv.setText("￥666");
        binding.activityEggListIndex888.eggListIndexItemViewEggTv.setText("￥888");
        binding.activityEggListIndex1888.eggListIndexItemViewEggTv.setText("￥1888");
        binding.activityEggListIndex2888.eggListIndexItemViewEggTv.setText("￥2888");
        binding.activityEggListIndex3888.eggListIndexItemViewEggTv.setText("￥3888");

        binding.activityEggListIndex188.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex288.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex388.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex588.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex666.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex888.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex1888.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex2888.eggListIndexItemViewEggRl.setOnClickListener(this);
        binding.activityEggListIndex3888.eggListIndexItemViewEggRl.setOnClickListener(this);


    }

    @Override
    protected void _requestData() {

    }



    @Override
    public void onClick(View view) {
        if (view == binding.activityEggListIndex188.eggListIndexItemViewEggRl) {
            payWithMoney("188");
        } else if (view == binding.activityEggListIndex288.eggListIndexItemViewEggRl) {
            payWithMoney("288");
        } else if (view == binding.activityEggListIndex388.eggListIndexItemViewEggRl) {

            payWithMoney("388");
        } else if (view == binding.activityEggListIndex588.eggListIndexItemViewEggRl) {
            payWithMoney("588");

        } else if (view == binding.activityEggListIndex666.eggListIndexItemViewEggRl) {
            payWithMoney("666");

        } else if (view == binding.activityEggListIndex888.eggListIndexItemViewEggRl) {
            payWithMoney("888");

        } else if (view == binding.activityEggListIndex1888.eggListIndexItemViewEggRl) {
            payWithMoney("1888");

        } else if (view == binding.activityEggListIndex2888.eggListIndexItemViewEggRl) {
            payWithMoney("2888");

        } else if (view == binding.activityEggListIndex3888.eggListIndexItemViewEggRl) {
            payWithMoney("3888");

        }
    }


    void payWithMoney(String money) {
        Context that = this;
        PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                EggSuccessDialogFragment.showV(getSupportFragmentManager(), new EggSuccessDialogFragment.EggSuccessDialogFragmentBlock() {
                    @Override
                    public void upGrade() {

                        GroupListActivity.start(GroupListActivity.class,that,null );
                    }
                });
//                RegisterBean registerBean = new RegisterBean();
//                registerBean.phone = "1"+ phone+"000";
//                registerBean.password = password;
//                LoadingDialog.showDialog(getSupportFragmentManager(),"购买中..");
//                HttpUtil.apiW().home_gmfh(registerBean)
//                        .enqueue(new CommonCallback<NetData>() {
//                            @Override
//                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                                ToastUtils.toastMsg("购买成功");
//                                EventBus.getDefault().post(new BaseEvent("reload_fuhao"));
//                                finish();
//                            }
//
//                            @Override
//                            public void Failure(Call<NetData> call, Throwable t) {
//
//                            }
//
//                            @Override
//                            public void end() {
//                                super.end();
//                                LoadingDialog.dismissDialog();
//                            }
//                        });
            }
        },money);

        // 显示窗口
        popEnterPassword.showAtLocation(binding.activityEggListIndexRootRl,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

    }


}
