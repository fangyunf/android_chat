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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityExchangeAccountBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.WelcomeLoginActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.turunsi.yaoxin.main.mine.setting.adapter.ExchangeAccountAdapter;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.utils.DataUtils;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ExchangeAccountActivity extends BaseActivity implements View.OnClickListener {

    private ActivityExchangeAccountBinding viewBinding;
    ExchangeAccountAdapter adapter = new ExchangeAccountAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityExchangeAccountBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        viewBinding.activityExchangeAccountNav.addCloseImageButton().setOnClickListener(this);

        viewBinding.activityExchangeAccountRv.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.activityExchangeAccountRv.setAdapter(adapter);

        viewBinding.activityExchangeAccountTuichuTv.setOnClickListener(this);

        _requastData();
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<UserBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (baseQuickAdapter.getItemViewType(i) == ExchangeAccountAdapter.TYPE_ITEM) {
                    UserBean item = baseQuickAdapter.getItem(i);
                    if (!item.userId.equals(DataUtil.getUserid())) {
                        DialogAlertUtil.showAlert("确认切换账号吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                            @Override
                            public void clickType(int type) {
                                if (type == 1) {
                                    exchangeLogin(item);
                                }
                            }
                        }, getSupportFragmentManager());

                    }

                } else {
                    showLogin();
                }
            }
        });
//        adapter.addOnItemChildClickListener(R.id.item_set_exchange_account_list_cell_delete_tv, new BaseQuickAdapter.OnItemChildClickListener<UserBean>() {
//            @Override
//            public void onItemClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
//                DataUtil.deleteLoginUserInfoList(baseQuickAdapter.getItem(i));
//                _requastData();
//            }
//        });


    }

    void _requastData() {

        List<UserBean> loginUserInfoList = DataUtil.getLoginUserInfoList();
        adapter.setItems(loginUserInfoList);
        adapter.notifyDataSetChanged();
    }

    private void initView() {

        viewBinding.activityExchangeAccountNav.addCloseImageButton().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityExchangeAccountNav.addCloseImageButton()) {
            finish();
        } else if (viewBinding.activityExchangeAccountTuichuTv == v) {
            // showLogin();
            ZhuXiaoConfrimActivity.start(ZhuXiaoConfrimActivity.class, this, null);
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        ExchangeAccountActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(ExchangeAccountActivity.this);
                        }
                        DataUtil.deleteData();
                        startActivity(new Intent(ExchangeAccountActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

    void exchangeLogin(UserBean userBean) {
        Activity that = this;
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        ExchangeAccountActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(ExchangeAccountActivity.this);
                        }
                        DataUtil.deleteData();
//                        startActivity(new Intent(ExchangeAccountActivity.this, LoginActivity.class));
//                        finish();

                        DataUtil.putUserInfo(userBean);
                        DataUtil.putToken(userBean.token);
//                        DataUtil.addLoginUserInfoList(userBean);
                        IMUtil.loginIM(that, userBean.userId, userBean.imToken);
                    }
                });
    }

}
