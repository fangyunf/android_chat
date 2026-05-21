// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.corekit.im.login.LoginCallback;

import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.FragmentMine1Binding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.account.AccountAnQuanManagerActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.account.AccoutCodeDetailActivity;
import com.turunsi.yaoxin.main.mine.huiyuan.LiangHaoZoneActivity;
import com.turunsi.yaoxin.main.mine.purse.PurseIndexActivity;
import com.turunsi.yaoxin.main.mine.DownLoadActivity;
import com.turunsi.yaoxin.main.mine.setting.ExchangeAccountActivity;
import com.turunsi.yaoxin.main.mine.setting.SettingNewActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ResourceHelper;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MineFragment1 extends BaseFragment implements View.OnClickListener {
    private FragmentMine1Binding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ALog.d(Constant.PROJECT_TAG, "MineFragment:onCreateView");
        binding = FragmentMine1Binding.inflate(inflater);
        ViewGroup.LayoutParams spacerParams = binding.mineStatusSpacer.getLayoutParams();
        spacerParams.height = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(12);
        binding.mineStatusSpacer.setLayoutParams(spacerParams);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _initItems();
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                refreshUserInfo(IMKitClient.account());
            }
        });
        binding.tvAccount.setText("ID: " + DataUtil.getUserInfo().memberCode);
        _requestData();
    }

    void _requestData() {
        HttpUtil.apiW().home_balance().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
        HttpUtil.apiW().home_getUserByToken(new RegisterBean()).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                if (userBean != null) {
                    DataUtil.putUserInfo(userBean);
                    DataUtil.putToken(userBean.token);
                    if (DataUtil.getUserInfo().grade > 0) {
                        binding.fragmentMineGradeIv.setImageDrawable(
                                ResourceHelper.getGradeDrawable(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.tvName.setTextColor(
                                ResourceHelper.getGradeColor(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.tvGradeBg.setImageDrawable(
                                ResourceHelper.getGradeBackground(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.fragmentMineGradeIv.setVisibility(View.VISIBLE);
                        binding.tvGradeBg.setVisibility(View.VISIBLE);
                    } else {
                        binding.fragmentMineGradeIv.setVisibility(View.GONE);
                        binding.tvGradeBg.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    private void _initItems() {
        setupSettingCard(binding.fragmentMineRenzhengView.getRoot(), R.mipmap.ic_menu1);
        setupSettingCard(binding.fragmentMineIndexWdqbLl.getRoot(), R.mipmap.ic_menu4);
        setupSettingCard(binding.fragmentMineYsglView.getRoot(), R.mipmap.ic_menu2);
        setupSettingCard(binding.fragmentMineZhglView.getRoot(), R.mipmap.ic_menu3);

        setupOtherFuncItem(binding.fragmentMineProfileView.getRoot(), "我的资料");
        setupOtherFuncItem(binding.fragmentMineYysjView.getRoot(), "下载链接");
        setupOtherFuncItem(binding.fragmentMineWdfhView.getRoot(), "购买生成号");
        setupOtherFuncItem(binding.fragmentMineLtszView.getRoot(), "我的收藏");
        setupOtherFuncItem(binding.fragmentMineMmszView.getRoot(), "我的设置");
        setupOtherFuncItem(binding.fragmentMineAccountManageView.getRoot(), "账号管理");
        setupOtherFuncItem(binding.fragmentMineLogoutView.getRoot(), "退出登录");

        binding.fragmentMineRenzhengView.getRoot().setOnClickListener(this);
        binding.fragmentMineIndexWdqbLl.getRoot().setOnClickListener(this);
        binding.fragmentMineYsglView.getRoot().setOnClickListener(this);
        binding.fragmentMineZhglView.getRoot().setOnClickListener(this);
        binding.fragmentMineProfileView.getRoot().setOnClickListener(this);
        binding.fragmentMineYysjView.getRoot().setOnClickListener(this);
        binding.fragmentMineWdfhView.getRoot().setOnClickListener(this);
        binding.fragmentMineLtszView.getRoot().setOnClickListener(this);
        binding.fragmentMineMmszView.getRoot().setOnClickListener(this);
        binding.fragmentMineAccountManageView.getRoot().setOnClickListener(this);
        binding.fragmentMineLogoutView.getRoot().setOnClickListener(this);

        binding.fragmentMineErweimaIv.setOnClickListener(this);
        binding.layoutEditUserInfo.setOnClickListener(this);
        binding.cavIcon.setOnClickListener(this);
    }

    private void setupSettingCard(View root, int imageRes) {
        View iconView = root.findViewById(R.id.view_mine_setting_card_icon);
        if (iconView instanceof android.widget.ImageView) {
            ((android.widget.ImageView) iconView).setImageResource(imageRes);
        }
    }

    private void setupOtherFuncItem(View root, String title) {
        View titleView = root.findViewById(R.id.view_mine_other_func_title);
        if (titleView instanceof android.widget.TextView) {
            ((android.widget.TextView) titleView).setText(title);
        }
    }

    private void refreshUserInfo(String account) {
        int cornerRadius = SizeUtils.dp2px(50);
        binding.cavIcon.setCornerRadius(cornerRadius);
        CommonRepo.getUserInfo(account, new FetchCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo param) {
                if (param != null) {
                    updateUI(param);
                }
            }

            @Override
            public void onFailed(int code) {
                ToastX.showShortToast(R.string.user_fail);
                updateUI(new UserInfo(account, account, ""));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                ToastX.showShortToast(R.string.user_fail);
                updateUI(new UserInfo(account, account, ""));
            }
        });
    }

    private void updateUI(UserInfo userInfo) {
        String name = TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName();
        GlideUtil.yh_loadImageRoundedCorner(getContext(), binding.cavIcon, DataUtil.getUserInfo().avatar, 2);
        binding.tvName.setText(name);
    }

    @Override
    public void onResume() {
        super.onResume();
        ALog.d(Constant.PROJECT_TAG, "MineFragment:onResume");
        String account = IMKitClient.account();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        _requestData();
        refreshUserInfo(account);
        binding.tvAccount.setText("ID: " + DataUtil.getUserInfo().memberCode);
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        View renzhengRoot = binding.fragmentMineRenzhengView.getRoot();
        View walletRoot = binding.fragmentMineIndexWdqbLl.getRoot();
        View ysglRoot = binding.fragmentMineYsglView.getRoot();
        View zhglRoot = binding.fragmentMineZhglView.getRoot();
        View profileRoot = binding.fragmentMineProfileView.getRoot();
        View downloadRoot = binding.fragmentMineYysjView.getRoot();
        View buyRoot = binding.fragmentMineWdfhView.getRoot();
        View collectRoot = binding.fragmentMineLtszView.getRoot();
        View settingRoot = binding.fragmentMineMmszView.getRoot();
        View accountManageRoot = binding.fragmentMineAccountManageView.getRoot();
        View logoutRoot = binding.fragmentMineLogoutView.getRoot();

        if (v == binding.fragmentMineRenzhengView.getRoot()) {
            ToastUtils.toastMsg("已完成实名");
        } else if (v == walletRoot) {
            PurseIndexActivity.start(PurseIndexActivity.class, context, null);
        } else if (v == ysglRoot) {
            AccountAnQuanManagerActivity.start(AccountAnQuanManagerActivity.class, context, null);
        } else if (v == zhglRoot) {
            ExchangeAccountActivity.start(ExchangeAccountActivity.class, context, null);
        } else if (v == profileRoot || v == binding.cavIcon || v == binding.layoutEditUserInfo) {
            AccountDetailActivity.start(AccountDetailActivity.class, context, null);
        } else if (v == downloadRoot) {
            DownLoadActivity.start(DownLoadActivity.class, context, null);
        } else if (v == buyRoot) {
            LiangHaoZoneActivity.start(LiangHaoZoneActivity.class, context, null);
        } else if (v == collectRoot) {
            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.CollectionListActivityKey)
                    .withParam("type", "5")
                    .withContext(requireContext())
                    .navigate();
        } else if (v == settingRoot) {
            SettingNewActivity.start(SettingNewActivity.class, context, null);
        } else if (v == accountManageRoot) {
            ExchangeAccountActivity.start(ExchangeAccountActivity.class, context, null);
        } else if (v == logoutRoot) {
            showLogin();
        } else if (v == binding.fragmentMineErweimaIv) {
            AccoutCodeDetailActivity.start(AccoutCodeDetailActivity.class, context, null);
        }
    }

    private void showLogin() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        IMKitClient.logoutIM(
                new LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        activity,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    }
                });
    }
}
