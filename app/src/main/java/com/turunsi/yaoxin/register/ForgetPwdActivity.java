package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityForgetPwdBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class ForgetPwdActivity extends BaseActivity implements View.OnClickListener {
    ActivityForgetPwdBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPwdBinding.inflate(getLayoutInflater());
        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        setContentView(binding.getRoot());
        _initTfText();
    }

    void _initTfText() {
        binding.forgetPwdActivityPhoneTf.viewTitleTfTv.setText("手机号");
        binding.forgetPwdActivityCodeTf.viewTitleTfTv.setText("验证码");
        binding.forgetPwdActivityPwdTf.viewTitleTfTv.setText("密码");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfTv.setText("确认密码");

        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setHint("请输入手机号");
        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setHint("请输入验证码");
        binding.forgetPwdActivityPwdTf.viewTitleTfEt.setHint("请输入密码");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfEt.setHint("请输入密码");

        binding.forgetPwdActivityCodeTf.btnCaptcha.setVisibility(View.VISIBLE);
        CountDownView mCountDownView = binding.forgetPwdActivityCodeTf.btnCaptcha;
        mCountDownView.setUserEdit(binding.forgetPwdActivityPhoneTf.viewTitleTfEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfEt);
                CommonNetUtil.getPhoneCode(phone);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.forgetPwdActivityNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.forgetPwdActivityBtn) {
            String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.forgetPwdActivityPwdTf.viewTitleTfEt);
            String pwd2 = getTextStr(binding.forgetPwdActivityPwd2Tf.viewTitleTfEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            RegisterBean bean = new RegisterBean();
            bean.password = pwd1;
            bean.phoneNo = phone;
            bean.captcha = code;

            Activity that = this;
            HttpUtil.apiW().customer_updatePassword(bean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg("修改成功");
                            finish();

                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
}
