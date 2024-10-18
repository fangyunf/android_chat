package com.turunsi.yaoxin.main.mine.purse.pwdmanager;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerSetBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class PursePwdManagerSetActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePursePwdManagerSetBinding binding;
    //0:设置支付密码  1：修改支付密码  2：忘记支付密码
    private int type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePursePwdManagerSetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePursePwdManagerSetNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePursePwdManagerSetForgetPwdTv.setOnClickListener(this);
        binding.activityMineAddressAddSaveRl.setOnClickListener(this);

        if (extras != null) {
            String typeString = (String)extras.get("type");
            type = Integer.parseInt(typeString);
        }

        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgTv.setText("验证码");

        binding.activityMinePursePwdManagerGetCode.btnCaptcha.setVisibility(View.VISIBLE);

        CountDownView mCountDownView = binding.activityMinePursePwdManagerGetCode.btnCaptcha;
        mCountDownView.setUserEdit(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
                CommonNetUtil.getPhoneCode(phone);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });


//        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        if (type == 0) {
            _initSetCell();
            binding.activityMinePursePwdManagerSetNav.setTitle("设置支付密码");
        } else if (type == 1) {
            binding.activityMinePursePwdManagerSetNav.setTitle("修改支付密码");
            _initModifyCell();
            binding.activityMinePursePwdManagerSetForgetPwdTv.setVisibility(View.VISIBLE);
        } else if (type == 2) {
            binding.activityMinePursePwdManagerSetNav.setTitle("忘记支付密码");

            _initForgetCell();
        }
    }
    private void _initSetCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");


        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);

//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
    }
    private void _initModifyCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");

//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
    }

    private void _initForgetCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("验证码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("新密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入手机号");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入验证码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");

//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
//
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePursePwdManagerSetNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePursePwdManagerSetForgetPwdTv) {
            HashMap map = new HashMap();
            map.put("type","2");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
        } else if (v == binding.activityMineAddressAddSaveRl) {
            String phone = getTextStr(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt);
            String pwd2 = getTextStr(binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.mobile = phone;
            registerBean.payPassword = pwd1;
            registerBean.smsCode = code;

            Activity that = this;
            HttpUtil.api8447().home_updateFullPassword(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg(body.msg);
                            finish();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
            
        }
    }

}
