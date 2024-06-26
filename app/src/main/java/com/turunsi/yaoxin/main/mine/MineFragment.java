// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunChatSettingActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.AppSkinConfig;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.FragmentMineBinding;
import com.turunsi.yaoxin.main.mine.account.AccountAnQuanManagerActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.account.AccoutCodeDetailActivity;
import com.turunsi.yaoxin.main.mine.address.AddressListActivity;
import com.turunsi.yaoxin.main.mine.collection.CollectionListActivity;
import com.turunsi.yaoxin.main.mine.order.OrderListActivity;
import com.turunsi.yaoxin.main.mine.purse.PurseIndexActivity;
import com.turunsi.yaoxin.main.mine.setting.SettingActivity;
import com.turunsi.yaoxin.main.mine.setting.SettingNewActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.NumberUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MineFragment extends BaseFragment implements View.OnClickListener {
    private FragmentMineBinding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ALog.d(Constant.PROJECT_TAG, "MineFragment:onCreateView");
        binding = FragmentMineBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _initItems();
        binding.mineFragmentPacketMoneyCl.setOnClickListener(this);
        ALog.d(Constant.PROJECT_TAG, "MineFragment:onViewCreated");

        launcher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                refreshUserInfo(IMKitClient.account());
                            }
                        });

//    binding.aboutLl.setOnClickListener(
//        v -> {
//          Intent intent = new Intent(getContext(), AboutActivity.class);
//          startActivity(intent);
//        });

//    binding.userInfoClick.setOnClickListener(
//        v -> {
//          MineInfoActivity.launch(
//              getContext(),
//              new ActivityResultLauncher<Intent>() {
//                @Override
//                public void launch(Intent input, @Nullable ActivityOptionsCompat options) {
//                  startActivity(input);
//                }
//
//                @Override
//                public void unregister() {}
//
//                @NonNull
//                @Override
//                public ActivityResultContract<Intent, ?> getContract() {
//                  return null;
//                }
//              });
//        });
//    binding.collectLl.setOnClickListener(v -> ToastX.showShortToast(R.string.not_usable));
//
//    binding.settingLl.setOnClickListener(
//        v -> startActivity(new Intent(getContext(), SettingActivity.class)));
        binding.tvAccount.setText(DataUtil.getUserInfo().memberCode);
        _requestData();
    }


    void  _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        binding.mineFragmentPacketMoneyDetailTv.setText("￥ " + NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void _initItems() {
        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellCl.setOnClickListener(this);
        binding.fragmentMineQrcodeIv.setOnClickListener(this);

        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellTitle.setText("我的订单");
        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellTitle.setText("地址管理");
        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellTitle.setText("下载地址");
        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellTitle.setText("我的收藏");
        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellTitle.setText("设置");
        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellTitle.setText("账号与安全");
        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellTitle.setText("帮助中心");

        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_order);
        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_location);
        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_download);
        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_collection);
        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_setting);
        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_account);
        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_kefu);

        binding.cavIcon.setOnClickListener(this);
    }

    private void refreshUserInfo(String account) {
        int cornerRadius = SizeUtils.dp2px(30);
        binding.cavIcon.setCornerRadius(cornerRadius);
        List<String> userInfoList = new ArrayList<>();
        userInfoList.add(account);
        CommonRepo.getUserInfo(
                account,
                new FetchCallback<UserInfo>() {
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
        String name =
                TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName();
        binding.cavIcon.setData(
                userInfo.getAvatar(), name, AvatarColor.avatarColor(IMKitClient.account()));
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
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (v == binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellCl) {
//            startActivity(new Intent(getContext(), SettingActivity.class));
            SettingNewActivity.start(SettingNewActivity.class,getContext(),null);
        }
        if (v == binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellCl) {
//            startActivity(new Intent(getContext(), SettingActivity.class));
            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
                    .withParam(RouterConstant.CHAT_ID_KRY, DataUtil.getKeFuId())
                    .withContext(getContext())
                    .navigate();
        }
        if (v == binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellCl) {
//            OrderListActivity.start(OrderListActivity.class,context,null);
        }
        if (v == binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellCl) {
//            AddressListActivity.start(AddressListActivity.class,context,null);
        }
        if (v == binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellCl) {
            CollectionListActivity.start(CollectionListActivity.class,context,null);
//            XKitRouter.withKey(RouterConstant.PATH_FUN_COLLECTION_PAGE).withContext(this.requireContext()).navigate();
        }
        if (v == binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellCl) {
            AccountAnQuanManagerActivity.start(AccountAnQuanManagerActivity.class,context,null);
//            XKitRouter.withKey(RouterConstant.PATH_FUN_COLLECTION_PAGE).withContext(this.requireContext()).navigate();
        }
        if (v == binding.mineFragmentPacketMoneyCl) {
            PurseIndexActivity.start(PurseIndexActivity.class,context,null);
        }
        if (v == binding.cavIcon) {
            AccountDetailActivity.start(AccountDetailActivity.class,getContext(),null);
        }
        if (v ==  binding.fragmentMineQrcodeIv) {
            AccoutCodeDetailActivity.start(AccoutCodeDetailActivity.class,getContext(),null);
        }


    }
}
