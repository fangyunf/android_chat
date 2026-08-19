package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.forgetPwdActivityNav);
        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        _initTfText();
    }

    void _initTfText() {
        // 设置图标和提示文本
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_phone);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_code);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountIv.setImageResource(R.mipmap.login_icon_password);

        // 设置输入类型和提示文本
        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setHint("输入11位中国大陆手机*");
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setHint("输入验证码*");
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setHint("输入您的密码*");

        // 显示密码可见性切换按钮
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);

        // 显示验证码按钮
        binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.VISIBLE);
        CountDownView mCountDownView = binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha;
        mCountDownView.setUserEdit(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
        mCountDownView.setCountDownTime(60);
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
                String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
                CommonNetUtil.getPhoneCode(ForgetPwdActivity.this, phone, mCountDownView);
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
        } else if (v == binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl) {
            // 切换密码可见性
            binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(!binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected());
            if (binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected()) {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.forgetPwdActivityBtn) {
            String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
            if (phone.length() != 11) {
                ToastUtils.toastMsg("手机格式错误");
                return;
            }
            String code = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfCountEt);
            if (code.length() > 6 || code.isEmpty()) {
                ToastUtils.toastMsg("验证码错误");
                return;
            }
            String pwd1 = getTextStr(binding.forgetPwdActivityPwdTf.viewTitleTfCountEt);
            if (pwd1.isEmpty()) {
                ToastUtils.toastMsg("请输入密码");
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
