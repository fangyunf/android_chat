package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendZhuanzhangPacketBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.GlideUtil;

import retrofit2.Call;
import retrofit2.Response;

public class FunSendZhuanZhangActivity extends BaseActivity implements View.OnClickListener {

    private static final int MAX_TRANSFER_AMOUNT = 2000 * 100;
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
            int amount = NumberUtil.formartUploadMoney(moneyStr);
            if (amount > MAX_TRANSFER_AMOUNT) {
                ToastUtils.toastMsg("单笔转账上限2000元");
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
        if (amount > MAX_TRANSFER_AMOUNT) {
            ToastUtils.toastMsg("单笔转账上限2000元");
            return;
        }

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
