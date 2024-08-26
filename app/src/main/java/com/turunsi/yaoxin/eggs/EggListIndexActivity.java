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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityAccountAnquanManagerBinding;
import com.turunsi.yaoxin.databinding.ActivityEggListIndexBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
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
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class EggListIndexActivity extends BaseActivity implements View.OnClickListener {

    ActivityEggListIndexBinding binding;

    List<CustomMsgBean> eggList = new ArrayList<>();
    EggIndexListAdapter adapter = new EggIndexListAdapter();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityEggListIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        StatusBarUtils.openImmersiveStatusBar(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.activityEggListIndexRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(2, SizeUtils.dp2px(10), false);
        binding.activityEggListIndexRv.addItemDecoration(gridSpacingItemDecoration);
        binding.activityEggListIndexRv.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<CustomMsgBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<CustomMsgBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                payWithMoney(baseQuickAdapter.getItem(i).amount,baseQuickAdapter.getItem(i).id);
            }
        });

        binding.activityEggListIndexMyIv.setOnClickListener(this);
    }

    @Override
    protected void _initView() {


//        binding.activityEggListIndexNav.addCloseImageButton().setOnClickListener(this);

//        binding.activityEggListIndex188.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_188_big);
//        binding.activityEggListIndex288.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_288_big);
//        binding.activityEggListIndex388.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_388_big);
//        binding.activityEggListIndex588.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_588_big);
//        binding.activityEggListIndex666.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_666_big);
//        binding.activityEggListIndex888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_888_big);
//        binding.activityEggListIndex1888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_1888_big);
//        binding.activityEggListIndex2888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_2888_big);
//        binding.activityEggListIndex3888.eggListIndexItemViewEggIv.setImageResource(com.yaoxin.appbase.R.mipmap.egg_3888_big);
//
//        binding.activityEggListIndex188.eggListIndexItemViewEggTv.setText("￥188");
//        binding.activityEggListIndex288.eggListIndexItemViewEggTv.setText("￥288");
//        binding.activityEggListIndex388.eggListIndexItemViewEggTv.setText("￥388");
//        binding.activityEggListIndex588.eggListIndexItemViewEggTv.setText("￥588");
//        binding.activityEggListIndex666.eggListIndexItemViewEggTv.setText("￥666");
//        binding.activityEggListIndex888.eggListIndexItemViewEggTv.setText("￥888");
//        binding.activityEggListIndex1888.eggListIndexItemViewEggTv.setText("￥1888");
//        binding.activityEggListIndex2888.eggListIndexItemViewEggTv.setText("￥2888");
//        binding.activityEggListIndex3888.eggListIndexItemViewEggTv.setText("￥3888");
//
//        binding.activityEggListIndex188.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex288.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex388.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex588.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex666.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex888.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex1888.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex2888.eggListIndexItemViewEggRl.setOnClickListener(this);
//        binding.activityEggListIndex3888.eggListIndexItemViewEggRl.setOnClickListener(this);


    }

    @Override
    protected void _requestData() {

        HttpUtil.apiW().caidan_caidanList(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<CustomMsgBean>>() {}.getType();
                        List<CustomMsgBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        eggList.addAll(tempList);
                        eggList.addAll(tempList);

                        adapter.setItems(eggList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }



    @Override
    public void onClick(View view) {
        if (binding.activityEggListIndexMyIv == view) {
            MyEggListActivity.start(MyEggListActivity.class,this,null);
        }
    }


    void payWithMoney(String money, String id) {
        if (money.length() > 2) {
            int tempM = Integer.parseInt(money) / 100;
            money = tempM + "";
        }
        Context that = this;
        PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                RegisterBean registerBean = new RegisterBean();
                registerBean.caiDanId = id;
                HttpUtil.apiW().caidan_gmCaidan(registerBean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                EggSuccessDialogFragment.showV(getSupportFragmentManager(), new EggSuccessDialogFragment.EggSuccessDialogFragmentBlock() {
                                    @Override
                                    public void upGrade() {
                                        GroupListActivity.start(GroupListActivity.class,that,null );

                                    }
                                });
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });

            }
        },money);

        // 显示窗口
        popEnterPassword.showAtLocation(binding.activityEggListIndexRootRl,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

    }


}
