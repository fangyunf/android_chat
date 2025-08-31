package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
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
import com.yaoxin.appbase.utils.StatusBarUtils;
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
        StatusBarUtils.transtStatusBar(this, binding.forgetPwdActivityNav);
        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        setContentView(binding.getRoot());
        _initTfText();
    }

    void _initTfText() {


        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setHint("请输入手机号");
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setHint("请输入验证码");
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setHint("请输入密码");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt.setHint("请输入密码");
        
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_phone);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_code);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);
        binding.forgetPwdActivityPwd2Tf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);

        binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        CountDownView mCountDownView = binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
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
            String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfCountEt);
            if (code.length() > 6) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.forgetPwdActivityPwdTf.viewTitleTfCountEt);
            String pwd2 = getTextStr(binding.forgetPwdActivityPwd2Tf.viewTitleTfCountEt);
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
