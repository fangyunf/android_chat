package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendZhuanzhangPacketBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class FunSendZhuanZhangActivity extends BaseActivity implements View.OnClickListener {

    ActivityFunSendZhuanzhangPacketBinding binding;
    protected ActivityResultLauncher<Intent> forwardTeamLauncher;
    private String sessionId = "";
    private int sessionType = 0;
    private String selectToUserId = "";
    private UserBean targetUserBean;
    private GroupInfoBean groupInfoBean;
    private GroupInfoBean exclusiveTargetUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunSendZhuanzhangPacketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityFunSendRedPacketNav);
        if (extras != null && extras.get("sessionId") != null) {
            sessionId = (String) extras.get("sessionId");
        }
        if (extras != null && extras.get("sessionType") != null) {
            String tempSessionType = (String) extras.get("sessionType");
            sessionType = Integer.parseInt(tempSessionType);
        }
        if (extras != null && extras.get("userInfo") != null) {
            try {
                String userInfoJson = (String) extras.get("userInfo");
                if (userInfoJson != null && !userInfoJson.isEmpty()) {
                    exclusiveTargetUser = new Gson().fromJson(userInfoJson, GroupInfoBean.class);
                }
            } catch (Exception ignored) {
            }
        }
        forwardTeamLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
                return;
            }
            Intent data = result.getData();
            if (data == null) {
                return;
            }
            String userInfoJson = data.getStringExtra("userInfo");
            if (userInfoJson == null || userInfoJson.isEmpty()) {
                return;
            }
            GroupInfoBean userInfo = new Gson().fromJson(userInfoJson, GroupInfoBean.class);
            updateSelectedGroupMember(userInfo);
        });
        _initView();
        _requestUserInfo();
        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (body != null && body.data != null) {
                    UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                    if (bean != null) {
                        binding.activityFunSendRedPacketBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
            }
        });
    }

    private void _requestUserInfo() {
        if (sessionType == 1) {
            binding.activityFunSendRedPacketNav.getTitleView().setText("群内转账");
            binding.activityFunSendRedPacketToPeopleNameTv.setText("请选择收款人");
            binding.activityFunSendZhuanzhangLiushuihaoTv.setVisibility(View.VISIBLE);
            binding.activityFunSendZhuanzhangLiushuihaoTv.setText("点击选择收款人");
            HttpUtil.apiW().group_groupHomeInfo(sessionId).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    if (body != null && body.data != null) {
                        groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                    }
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {
                }
            });
            return;
        }
        if (sessionType == 2) {
            binding.activityFunSendRedPacketNav.getTitleView().setText("专属转账");
            if (exclusiveTargetUser != null) {
                updateSelectedGroupMember(exclusiveTargetUser);
                // 专属转账不允许在页面内切换收款人
                binding.activityFunSendRedPacketToPeopleLl.setEnabled(false);
            } else {
                binding.activityFunSendRedPacketToPeopleNameTv.setText("请选择收款人");
            }
            return;
        }
        if (sessionId == null || sessionId.isEmpty()) return;
        RegisterBean bean = new RegisterBean();
        bean.userId = sessionId;
        HttpUtil.apiW().friends_searchByUserIdF(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (body != null && body.data != null) {
                    targetUserBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                    if (targetUserBean != null) {
                        binding.activityFunSendRedPacketToPeopleNameTv.setText(targetUserBean.name);
                        if (targetUserBean.avatar != null) {
                            GlideUtil.yh_loadImage(FunSendZhuanZhangActivity.this, binding.activityFunSendRedPacketToPeopleHeadIv, targetUserBean.avatar);
                        }
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                binding.activityFunSendRedPacketToPeopleNameTv.setText(sessionId);
            }
        });
    }

    @Override
    protected void _initView() {
        binding.activityFunSendRedPacketNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunSendRedPacketSendTv.setOnClickListener(this);
        binding.activityFunSendRedPacketToPeopleLl.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityFunSendRedPacketMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        binding.activityFunSendRedPacketGreetingEt.setHint("添加转账说明");
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunSendRedPacketNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityFunSendRedPacketToPeopleLl) {
            if (sessionType != 1) {
                return;
            }
            if (groupInfoBean == null) {
                ToastUtils.toastMsg("群成员加载中");
                return;
            }
            DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
            XKitRouter.withKey(Constant.FunSelected_User_ActivityKey)
                    .withParam("type", "4")
                    .withParam("groupId", sessionId)
                    .withContext(this)
                    .navigate(forwardTeamLauncher);
        } else if (v == binding.activityFunSendRedPacketSendTv) {
            if (sessionType == 1 && (selectToUserId == null || selectToUserId.isEmpty())) {
                ToastUtils.toastMsg("请选择收款人");
                return;
            }
            String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
            if (moneyStr == null || moneyStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    sendZhuanZhangWithPwd(password);
                }
            }, moneyStr);
            popEnterPassword.showAtLocation(binding.activityFunSendRedPacketLl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }
    }

    void sendZhuanZhangWithPwd(String pwd) {
        String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
        String greeting = getTextStr(binding.activityFunSendRedPacketGreetingEt);

        int amount = 0;
        if (moneyStr != null && !moneyStr.isEmpty()) {
            amount = NumberUtil.formartUploadMoney(moneyStr);
        }

        if (amount <= 0) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.amount = amount;
        bean.title = (greeting == null || greeting.isEmpty()) ? "你发起了一笔转账" : greeting;
        bean.password = pwd;
        if (sessionType == 1 || sessionType == 2) {
            if (selectToUserId == null || selectToUserId.isEmpty()) {
                ToastUtils.toastMsg("请选择收款人");
                return;
            }
            bean.toUserId = selectToUserId;
            bean.groupId = sessionId;
            HttpUtil.apiW().red_groupZZ(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("发送成功");
                    finish();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {
                    if (handlePayPasswordNotSet(t)) {
                        return;
                    }
                    ToastUtils.toastMsg(t.getMessage());
                }
            });
            return;
        }
        bean.toUserId = sessionId;
        HttpUtil.apiW().red_zz(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg("发送成功");
                finish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                if (handlePayPasswordNotSet(t)) {
                    return;
                }
                ToastUtils.toastMsg(t.getMessage());
            }
        });
    }

    private void updateSelectedGroupMember(GroupInfoBean userInfo) {
        if (userInfo == null || userInfo.userId == null || userInfo.userId.isEmpty()) {
            return;
        }
        if (userInfo.userId.equals(DataUtil.getUserid())) {
            ToastUtils.toastMsg("不能给自己转账");
            return;
        }
        selectToUserId = userInfo.userId;
        binding.activityFunSendRedPacketToPeopleNameTv.setText(userInfo.name);
        binding.activityFunSendZhuanzhangLiushuihaoTv.setVisibility(View.VISIBLE);
        binding.activityFunSendZhuanzhangLiushuihaoTv.setText("点击更换收款人");
        GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunSendRedPacketToPeopleHeadIv, userInfo.avatar, 6);
    }

    private boolean handlePayPasswordNotSet(Throwable t) {
        if (!(t instanceof NetServerException) || ((NetServerException) t).getErrCode() != 8008) {
            return false;
        }
        try {
            Intent intent = new Intent();
            intent.putExtra("type", "0");
            intent.setClassName(getPackageName(), "com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity");
            startActivity(intent);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
