package com.turunsi.yaoxin.main.mine.purse.alipay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.databinding.ActivityMineBindAlipayBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBindBankcardBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.PayParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.yaoxin.appbase.view.LoadingDialog;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;
import com.zhihu.matisse.Matisse;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BindBankCardActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBindBankcardBinding binding;
    PayParamsBean payParamsBean;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBindBankcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineBindBankcardNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineBindAlipayBindTv.setOnClickListener(this);

        binding.activityMineBindBankcardName.viewTitleTfWithoutBgTv.setText("真实姓名");
        binding.activityMineBindBankcardIdCard.viewTitleTfWithoutBgTv.setText("身份证号");
        binding.activityMineBindBankcardCardNum.viewTitleTfWithoutBgTv.setText("银行卡号");
        binding.activityMineBindBankcardBankName.viewTitleTfWithoutBgTv.setText("银行名称");
        binding.activityMineBindBankcardYuliuPhone.viewTitleTfWithoutBgTv.setText("预留手机号");
        binding.activityMineBindBankcardVerifyCode.viewTitleTfWithoutBgTv.setText("输入验证码");
        binding.activityMineBindBankcardVerifyCode.btnCaptcha.setVisibility(View.VISIBLE);

        CountDownView mCountDownView = binding.activityMineBindBankcardVerifyCode.btnCaptcha;
        mCountDownView.setUserEdit(binding.activityMineBindBankcardYuliuPhone.viewTitleTfWithoutBgEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                getCode();
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });

        binding.activityMineBindBankcardName.viewTitleTfWithoutBgEt.setHint("请输入真实姓名");
        binding.activityMineBindBankcardIdCard.viewTitleTfWithoutBgEt.setHint("请输入身份证号");
        binding.activityMineBindBankcardCardNum.viewTitleTfWithoutBgEt.setHint("请输入银行卡号");
        binding.activityMineBindBankcardBankName.viewTitleTfWithoutBgEt.setHint("请输入银行名称");
        binding.activityMineBindBankcardYuliuPhone.viewTitleTfWithoutBgEt.setHint("请输入预留手机号");
        binding.activityMineBindBankcardVerifyCode.viewTitleTfWithoutBgEt.setHint("请输入验证码");

        if (BuildConfig.DEBUG) {
            binding.activityMineBindBankcardName.viewTitleTfWithoutBgEt.setText("万运浩");
            binding.activityMineBindBankcardIdCard.viewTitleTfWithoutBgEt.setText("320911199304010018");
            binding.activityMineBindBankcardCardNum.viewTitleTfWithoutBgEt.setText("6230580000095169152");
            binding.activityMineBindBankcardBankName.viewTitleTfWithoutBgEt.setText("平安银行");
            binding.activityMineBindBankcardYuliuPhone.viewTitleTfWithoutBgEt.setText("17721111165");
            binding.activityMineBindBankcardVerifyCode.viewTitleTfWithoutBgEt.setText("123456");
        }
    }



    @Override
    public void onClick(View v) {
        if (v == binding.activityMineBindBankcardNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBindAlipayBindTv) {
            confrimBind();
        }
    }

    public void getCode() {
        String name = getTextStr(binding.activityMineBindBankcardName.viewTitleTfWithoutBgEt);
        String idcard = getTextStr(binding.activityMineBindBankcardIdCard.viewTitleTfWithoutBgEt);
        String cardNum = getTextStr(binding.activityMineBindBankcardCardNum.viewTitleTfWithoutBgEt);
        String bankName = getTextStr(binding.activityMineBindBankcardBankName.viewTitleTfWithoutBgEt);
        String phone = getTextStr(binding.activityMineBindBankcardYuliuPhone.viewTitleTfWithoutBgEt);
        String code = getTextStr(binding.activityMineBindBankcardVerifyCode.viewTitleTfWithoutBgEt);


        if (name.isEmpty()) {
            ToastUtils.toastMsg("请输入姓名");
            return;
        }
        if (idcard.isEmpty()) {
            ToastUtils.toastMsg("请输入身份证号");
            return;
        }
        if (cardNum.isEmpty()) {
            ToastUtils.toastMsg("请输入银行卡号");
            return;
        }
        if (bankName.isEmpty()) {
            ToastUtils.toastMsg("请输入银行名称");
            return;
        }
        if (phone.isEmpty()) {
            ToastUtils.toastMsg("请输入手机号");
            return;
        }

        RequestParamsBean registerBean = new RequestParamsBean(phone, name, "2");
        registerBean.bankUserName = name;
        registerBean.bankCardNo = cardNum;
        registerBean.bankName = bankName;
        registerBean.bankPhone = phone;
        registerBean.idNumber = idcard;
        registerBean.configId = "3";

        LoadingDialog.showDialog(getSupportFragmentManager(),"获取中..");
        HttpUtil.apiW().pay_createCardApply(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        payParamsBean = new Gson().fromJson(body.data.toString(), PayParamsBean.class);
                        if (payParamsBean.error_msg != null && !payParamsBean.error_msg.isEmpty()) {
                            ToastUtils.toastMsg(payParamsBean.error_msg);
                        }  else {
                            ToastUtils.toastMsg(body.msg);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        LoadingDialog.dismissDialog();
                    }
                });
    }
    public void confrimBind() {
        String code = getTextStr(binding.activityMineBindBankcardVerifyCode.viewTitleTfWithoutBgEt);


        if (payParamsBean == null || payParamsBean.id.isEmpty()) {
            ToastUtils.toastMsg("请先获取验证码");
            return;
        }
        if (code.isEmpty()) {
            ToastUtils.toastMsg("请输入验证码");
            return;
        }

        RequestParamsBean registerBean = new RequestParamsBean();
        registerBean.apply_id = payParamsBean.id;
        registerBean.memberId = payParamsBean.member_id;
        registerBean.smsCode = code;
        registerBean.configId = "3";
        registerBean.userId = DataUtil.getUserid();

        LoadingDialog.showDialog(getSupportFragmentManager(),"绑卡中..");
        HttpUtil.apiW().pay_createCardconfirm(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                         payParamsBean = new Gson().fromJson(body.data.toString(), PayParamsBean.class);
                         if (payParamsBean.error_msg != null && !payParamsBean.error_msg.isEmpty()) {
                           ToastUtils.toastMsg(payParamsBean.error_msg);
                         }  else {
                             ToastUtils.toastMsg(body.msg);
                             finish();
                         }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        LoadingDialog.dismissDialog();
                    }
                });
    }

}
