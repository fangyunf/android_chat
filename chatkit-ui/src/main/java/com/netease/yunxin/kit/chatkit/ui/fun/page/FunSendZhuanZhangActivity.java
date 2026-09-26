package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendZhuanzhangPacketBinding;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunSendZhuanZhangActivity extends BaseActivity implements View.OnClickListener {

    //private static final int MAX_TRANSFER_AMOUNT = 2000 * 100;
    ActivityFunSendZhuanzhangPacketBinding binding;
    private String sessionId = "";
    private UserBean targetUserBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunSendZhuanzhangPacketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityFunSendRedPacketNav);
        if (extras != null && extras.get("sessionId") != null) {
            sessionId = (String) extras.get("sessionId");
        }
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
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        // 不走 friends/searchByUserId：用好友缓存 / 云信资料即可展示
        List<GroupInfoBean> friends = DataUtil.getFriendInfoList();
        if (friends != null) {
            for (GroupInfoBean friend : friends) {
                if (friend != null
                        && !TextUtils.isEmpty(friend.userId)
                        && friend.userId.equals(sessionId)) {
                    targetUserBean = new UserBean();
                    targetUserBean.userId = friend.userId;
                    targetUserBean.name =
                            !TextUtils.isEmpty(friend.remark) ? friend.remark : friend.name;
                    targetUserBean.avatar = friend.avatar;
                    targetUserBean.memberCode = friend.memberCode;
                    bindTargetUserUi();
                    return;
                }
            }
        }
        UserInfo nimUser = MessageHelper.getChatMessageUserInfo(sessionId);
        if (nimUser != null) {
            targetUserBean = new UserBean();
            targetUserBean.userId = sessionId;
            targetUserBean.name =
                    !TextUtils.isEmpty(nimUser.getName()) ? nimUser.getName() : sessionId;
            targetUserBean.avatar = nimUser.getAvatar();
            bindTargetUserUi();
            return;
        }
        binding.activityFunSendRedPacketToPeopleNameTv.setText(sessionId);
    }

    private void bindTargetUserUi() {
        if (targetUserBean == null) {
            return;
        }
        if (!TextUtils.isEmpty(targetUserBean.name)) {
            binding.activityFunSendRedPacketToPeopleNameTv.setText(targetUserBean.name);
        }
        if (!TextUtils.isEmpty(targetUserBean.avatar)) {
            GlideUtil.yh_loadImage(
                    this, binding.activityFunSendRedPacketToPeopleHeadIv, targetUserBean.avatar);
        }
    }

    @Override
    protected void _initView() {
        binding.activityFunSendRedPacketNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunSendRedPacketSendTv.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityFunSendRedPacketMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        binding.activityFunSendRedPacketGreetingEt.setHint("添加转账说明");
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunSendRedPacketNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityFunSendRedPacketSendTv) {
            String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
            if (moneyStr == null || moneyStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
//            int amount = NumberUtil.formartUploadMoney(moneyStr);
//            if (amount > MAX_TRANSFER_AMOUNT) {
//                ToastUtils.toastMsg("单笔转账上限2000元");
//                return;
//            }
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
//        if (amount > MAX_TRANSFER_AMOUNT) {
//            ToastUtils.toastMsg("单笔转账上限2000元");
//            return;
//        }

        RegisterBean bean = new RegisterBean();
        bean.amount = amount;
        bean.title = (greeting == null || greeting.isEmpty()) ? "你发起了一笔转账" : greeting;
        bean.password = pwd;
        bean.toUserId = sessionId;
        HttpUtil.apiW().red_zz(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg("发送成功");
                finish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                ToastUtils.toastMsg("转账失败");
            }
        });
    }
}
