// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;

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
import com.turunsi.yaoxin.databinding.FragmentMineBinding;
import com.turunsi.yaoxin.eggs.EggListIndexActivity;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.WelcomeLoginActivity;
import com.turunsi.yaoxin.main.mine.account.AccountAnQuanManagerActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.account.AccoutCodeDetailActivity;
import com.turunsi.yaoxin.main.mine.fuhao.BuyFeatureActivity;
import com.turunsi.yaoxin.main.mine.fuhao.MyFuHaoListActivity;
import com.turunsi.yaoxin.main.mine.huiyuan.LiangHaoZoneActivity;
import com.turunsi.yaoxin.main.mine.huiyuan.MyHuiYuanListActivity;
import com.turunsi.yaoxin.main.mine.purse.PurseIndexActivity;
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
import java.util.HashMap;
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

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.layoutContent.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);

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


        binding.tvAccount.setText("ID: " + DataUtil.getUserInfo().memberCode);
        _requestData();
    }


    void _requestData() {
        HttpUtil.apiW().home_balance().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
//                        binding.mineFragmentPacketMoneyDetailTv.setText("￥ " + NumberUtil.formartMoney(bean.balance));
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
                        binding.fragmentMineGradeIv.setImageDrawable(ResourceHelper.getGradeDrawable(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.tvName.setTextColor(ResourceHelper.getGradeColor(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.tvGradeBg.setImageDrawable(ResourceHelper.getGradeBackground(requireActivity(), DataUtil.getUserInfo().grade));
                        binding.fragmentMineGradeIv.setVisibility(View.VISIBLE);
                        binding.tvGradeBg.setVisibility(View.VISIBLE);
//                        binding.fragmentMineGradeIv.setVisibility(View.VISIBLE);
//                        String imageName = "mine_grade_level_" + DataUtil.getUserInfo().grade;
//                        Resources resources = getResources();
//                        int resId = resources.getIdentifier(imageName, "mipmap", getContext().getPackageName());
//                        // 如果找到了资源，则可以使用这个ID获取Drawable
//                        Drawable drawable = null;
//                        if (resId > 0) {
//                            drawable = ContextCompat.getDrawable(getContext(), resId);
//                        }
//                        // 如果需要将drawable设置到ImageView中
//                        if (drawable != null) {
//                            binding.fragmentMineGradeIv.setImageDrawable(drawable);
//                        }
                    } else {
                        binding.fragmentMineGradeIv.setVisibility(View.GONE);
                        binding.tvGradeBg.setVisibility(View.GONE);
                    }
                    //updateUIGrade();
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    private void _initItems() {
        binding.fragmentMineRenzhengView.setOnClickListener(this);
        binding.fragmentMineIndexCollec.setOnClickListener(this);
        binding.fragmentMineLtszView.setOnClickListener(this);
        binding.fragmentMineYsglView.setOnClickListener(this);
        binding.fragmentMineZhglView.setOnClickListener(this);
        binding.fragmentMineHmdView.setOnClickListener(this);
        binding.fragmentMineErweimaIv.setOnClickListener(this);
        binding.fragmentMineYlyxView.setOnClickListener(this);
//        binding.fragmentMineCaidanView.setOnClickListener(this);
//        binding.fragmentMineQrcodeIv.setOnClickListener(this);
        binding.layoutEditUserInfo.setOnClickListener(this);
        binding.fragmentMineEditIv.setOnClickListener(this);
        //binding.fragmentMineHyzxView.setOnClickListener(this);
        binding.fragmentMineMmszView.setOnClickListener(this);
        //binding.fragmentMineFxyyView.setOnClickListener(this);
        binding.fragmentMineYysjView.setOnClickListener(this);
        binding.fragmentMineXtszView.setOnClickListener(this);
        binding.fragmentMineCopyIv.setOnClickListener(this);
        binding.fragmentMineGotoUpgradeTv.setOnClickListener(this);
        binding.fragmentMineIndexCdscLl.setOnClickListener(this);
        binding.fragmentMineIndexWdqbLl.setOnClickListener(this);
        binding.fragmentMineWdfhView.setOnClickListener(this);

        binding.tvInfo.setOnClickListener(this);
        binding.fragmentMineLogoutView.setOnClickListener(this);

//        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellCl.setOnClickListener(this);
//        binding.fragmentMineQrcodeIv.setOnClickListener(this);
//
//        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellTitle.setText("我的订单");
//        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellTitle.setText("地址管理");
//        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellTitle.setText("下载地址");
//        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellTitle.setText("我的收藏");
//        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellTitle.setText("设置");
//        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellTitle.setText("账号与安全");
//        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellTitle.setText("帮助中心");
//
//        binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_order);
//        binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_location);
//        binding.mineFragmentMyManagerItem3.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_download);
//        binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_collection);
//        binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_setting);
//        binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_account);
//        binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellIcon.setImageResource(R.mipmap.mine_fragment_index_cell_icon_kefu);

        binding.cavIcon.setOnClickListener(this);

        binding.fragmentMineIndexChoujiang.setOnClickListener(this);
    }

    private void refreshUserInfo(String account) {
        int cornerRadius = SizeUtils.dp2px(50);
        binding.cavIcon.setCornerRadius(cornerRadius);
        List<String> userInfoList = new ArrayList<>();
        userInfoList.add(account);
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
//        binding.cavIcon.setData(
//                userInfo.getAvatar(), name, AvatarColor.avatarColor(IMKitClient.account()));
        GlideUtil.yh_loadImageRoundedCorner(getContext(), binding.cavIcon, DataUtil.getUserInfo().avatar, 2);
        binding.tvName.setText(name);
    }

    private void updateUIGrade() {
        if (DataUtil.getUserInfo().grade > 0) {
            binding.fragmentMineGradeRl.setVisibility(View.VISIBLE);
            binding.fragmentMineGradeTv.setVisibility(View.VISIBLE);
            binding.fragmentMineGradeIv.setVisibility(View.VISIBLE);
            binding.fragmentMineGradeTv.setText(DataUtil.getUserInfo().grade + "级靓号用户");
            String imageName = "mine_grade_level_" + DataUtil.getUserInfo().grade;
            Resources resources = getResources();
            int resId = resources.getIdentifier(imageName, "mipmap", getContext().getPackageName());
            // 如果找到了资源，则可以使用这个ID获取Drawable
            Drawable drawable = null;
            if (resId > 0) {
                drawable = ContextCompat.getDrawable(getContext(), resId);
            }
            // 如果需要将drawable设置到ImageView中
            if (drawable != null) {
                binding.fragmentMineGradeIv.setImageDrawable(drawable);
            }
        } else {
            binding.fragmentMineGradeRl.setVisibility(View.GONE);
            binding.fragmentMineGradeTv.setVisibility(View.GONE);
            binding.fragmentMineGradeIv.setVisibility(View.GONE);
        }
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
        if (v == binding.fragmentMineIndexChoujiang) {
            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey).withParam("type", "4").withParam("title", "").withContext(requireActivity()).navigate();
        } else if (v == binding.fragmentMineRenzhengView) {
            ToastUtils.toastMsg("已完成实名");
        } else if (v == binding.fragmentMineIndexCollec) {
//            MyHuiYuanListActivity.start(MyHuiYuanListActivity.class, context, null);
            LiangHaoZoneActivity.start(LiangHaoZoneActivity.class, context, null);
            // CollectionListActivity.start(CollectionListActivity.class, context, null);
        } else if (v == binding.fragmentMineErweimaIv) {
//            if (getActivity() != null) {
            // AccountCodeDialogFragment.showV(getActivity().getSupportFragmentManager());
//            }
            AccoutCodeDetailActivity.start(AccoutCodeDetailActivity.class, context, null);
        } else if (v == binding.fragmentMineYsglView) {
            AccountAnQuanManagerActivity.start(AccountAnQuanManagerActivity.class, context, null);
        } else if (v == binding.fragmentMineYlyxView) {
            ToastUtils.toastMsg("敬请期待,等待开放");
        } else if (v == binding.fragmentMineZhglView) {
            // AccountAnQuanActivity.start(AccountAnQuanActivity.class, context, null);
            ExchangeAccountActivity.start(ExchangeAccountActivity.class, context, null);
        } else if (v == binding.fragmentMineLtszView) {
//            startActivity(new Intent(getContext(), SettingNotifyActivity.class));
//            startActivity(new Intent(getContext(), SettingNotifyNewActivity.class));
            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.CollectionListActivityKey).withParam("type", "5")
//                  .withParam("groupId",groupId)
                    .withContext(requireContext()).navigate();
        } else if (v == binding.fragmentMineHmdView) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE).withContext(requireContext()).navigate();
        } else if (v == binding.fragmentMineMmszView) {
            // Mine_Pwd_Set_ManagerActivity.start(Mine_Pwd_Set_ManagerActivity.class, getContext(), null);
            SettingNewActivity.start(SettingNewActivity.class, getContext(), null);
        } else if (v == binding.fragmentMineXtszView) {
//            SettingNewActivity.start(SettingNewActivity.class,getContext(),null);
        } else if (v == binding.fragmentMineHyzxView || v == binding.fragmentMineGotoUpgradeTv) {
            MyHuiYuanListActivity.start(MyHuiYuanListActivity.class, context, null);
//            Activity that = getActivity();
//
//            if ("1".equals(DataUtil.getUserInfo().hy)) {
//                ToastUtils.toastMsg("已经是会员");
//            } else {
//
//                DialogAlertUtil.showAlert("确定购买会员\n成为会员后砸蛋中奖几率翻倍，更有机会获得彩蛋", new DialogAlertUtil.DialogAlertUtilCallBack() {
//                    @Override
//                    public void clickType(int type) {
//                        if (type == 1) {
//                            HttpUtil.apiW().caidan_huiYuanJia(new RegisterBean())
//                                    .enqueue(new CommonCallback<NetData>() {
//                                        @Override
//                                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                                            CustomMsgBean msgBean = new Gson().fromJson(body.data.toString(),CustomMsgBean.class);
//                                            String moneyStr = NumberUtil.formartMoney_zhengshu(msgBean.price);
//
//                                            PopEnterPassword popEnterPassword = new PopEnterPassword(that, new OnPasswordInputFinish() {
//                                                @Override
//                                                public void inputFinish(String password) {
//
//                                                    HttpUtil.apiW().caidan_gmHuiYuan(new RegisterBean())
//                                                            .enqueue(new CommonCallback<NetData>() {
//                                                                @Override
//                                                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                                                                    ToastUtils.toastMsg("购买成功");
//                                                                    UserBean userInfo = DataUtil.getUserInfo();
//                                                                    userInfo.hy = "1";
//                                                                    DataUtil.putUserInfo(userInfo);
//
//                                                                }
//
//                                                                @Override
//                                                                public void Failure(Call<NetData> call, Throwable t) {
//
//                                                                }
//                                                            });
//
//                                                }
//                                            },moneyStr);
//
//                                            // 显示窗口
//                                            popEnterPassword.showAtLocation(binding.fragmentMineRootCl,
//                                                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
//
//
//                                        }
//
//                                        @Override
//                                        public void Failure(Call<NetData> call, Throwable t) {
//
//                                        }
//                                    });
//
//                        }
//                    }
//                },getActivity().getSupportFragmentManager());
//
//            }

//            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey)
//                    .withParam("type","3")
//                    .withParam("title","靓号")
//                    .withParam("url",com.yaoxin.appbase.net.Constant.BASE_URL_H5 + ":8087/app/account")
//                    .withContext(getContext())
//                    .navigate();
        }
//        else if (v == binding.fragmentMineChoujiang) {
//            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey)
//                    .withParam("type","3")
//                    .withParam("title","抽奖")
//                    .withParam("url",com.yaoxin.appbase.net.Constant.BASE_URL_H5 + ":8087/activity/draw")
//                    .withContext(getContext())
//                    .navigate();
//        }
        else if (v == binding.fragmentMineWdfhView) {
            // MyFuHaoListActivity.start(MyFuHaoListActivity.class, getActivity(), null);
//            HashMap map = new HashMap();
//            map.put("type", "1");
//            BuyFeatureActivity.start(BuyFeatureActivity.class, getActivity(), map);
            MyFuHaoListActivity.start(MyFuHaoListActivity.class, getActivity(), null);
        }
//        if (v == binding.mineFragmentMyManagerItem5.viewMineFragmentItemCellCl) {
////            startActivity(new Intent(getContext(), SettingActivity.class));
//            SettingNewActivity.start(SettingNewActivity.class,getContext(),null);
//        }
//        if (v == binding.mineFragmentMyManagerItem7.viewMineFragmentItemCellCl) {
////            startActivity(new Intent(getContext(), SettingActivity.class));
//            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
//                    .withParam(RouterConstant.CHAT_ID_KRY, DataUtil.getKeFuId())
//                    .withContext(getContext())
//                    .navigate();
//        }
//        if (v == binding.mineFragmentMyManagerItem1.viewMineFragmentItemCellCl) {
////            OrderListActivity.start(OrderListActivity.class,context,null);
//        }
//        if (v == binding.mineFragmentMyManagerItem2.viewMineFragmentItemCellCl) {
////            AddressListActivity.start(AddressListActivity.class,context,null);
//        }
//        if (v == binding.mineFragmentMyManagerItem4.viewMineFragmentItemCellCl) {
//            CollectionListActivity.start(CollectionListActivity.class,context,null);
////            XKitRouter.withKey(RouterConstant.PATH_FUN_COLLECTION_PAGE).withContext(this.requireContext()).navigate();
//        }
//        if (v == binding.mineFragmentMyManagerItem6.viewMineFragmentItemCellCl) {

////            XKitRouter.withKey(RouterConstant.PATH_FUN_COLLECTION_PAGE).withContext(this.requireContext()).navigate();
//        }
        if (v == binding.cavIcon || v == binding.fragmentMineEditIv || v == binding.layoutEditUserInfo || v == binding.tvInfo) {
            AccountDetailActivity.start(AccountDetailActivity.class, getContext(), null);
        }
//        if (v == binding.fragmentMineCaidanView) {
//            EggListIndexActivity.start(EggListIndexActivity.class,getContext(),null);
////            ToastUtils.toastMsg("敬请期待,等待开放");
//        }
        if (v == binding.fragmentMineCopyIv) {
            // 获取剪切板管理器
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            // 创建一个ClipData对象，包含要复制的文本
            ClipData clip = ClipData.newPlainText("label", binding.tvAccount.getText().toString());

            // 将ClipData对象放入剪切板
            clipboard.setPrimaryClip(clip);
            ToastUtils.toastMsg("复制成功");
        }
        if (v == binding.fragmentMineFxyyView) {
            DownLoadActivity.start(DownLoadActivity.class, getContext(), null);
        }
        if (v == binding.fragmentMineYysjView) {
            // XKitRouter.withKey(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey).withParam("url", com.yaoxin.appbase.net.Constant.XIAZAIZHONG_H5).withParam("type", "5").withParam("title", "下载更新").withContext(getActivity()).navigate();
            // AppUpdateActivity.start(AppUpdateActivity.class, getContext(), null);
            DownLoadActivity.start(DownLoadActivity.class, getContext(), null);
        }
        if (v == binding.fragmentMineIndexCdscLl) {
            EggListIndexActivity.start(EggListIndexActivity.class, getContext(), null);
        } else if (v == binding.fragmentMineIndexWdqbLl) {
            PurseIndexActivity.start(PurseIndexActivity.class, context, null);
        }
        if (v == binding.fragmentMineLogoutView) {
            showLogin();
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        getActivity(),
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {

                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(getActivity(), WelcomeLoginActivity.class));
                        getActivity().finish();
                    }
                });
    }
}
