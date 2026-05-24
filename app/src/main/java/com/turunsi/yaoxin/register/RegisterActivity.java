package com.turunsi.yaoxin.register;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityRegisterBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    ActivityRegisterBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.layoutTitleBar);
        binding.layoutTitleBar.addCloseImageButton().setOnClickListener(v -> finish());
        binding.tvToAccount.setOnClickListener(v -> finish());
        binding.tvToLogin.setOnClickListener(v -> finish());
        binding.activityLoginIsCheckedTxt2.setOnClickListener(this);
        binding.activityLoginIsCheckedTxt4.setOnClickListener(this);
        binding.activityLoginIsAgreeLl.setOnClickListener(this);
        _initTfText();
    }

    void _initTfText() {
        binding.registerActivityPhoneTf.viewTitleTfTv.setText(R.string.label_account);
        binding.registerActivityCodeTf.viewTitleTfTv.setText(R.string.label_security_answer);
        binding.registerActivityPwdTf.viewTitleTfTv.setText("密码");
        binding.registerActivityPwd2Tf.viewTitleTfTv.setText("确认密码");
        binding.registerActivityPhoneTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.registerActivityCodeTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.registerActivityPhoneTf.viewTitleTfEt.setHint(R.string.hint_input_account);
        binding.registerActivityCodeTf.viewTitleTfEt.setHint(R.string.hint_input_security_answer);
        binding.registerActivityPwdTf.viewTitleTfEt.setHint("请输入密码");
        binding.registerActivityPwd2Tf.viewTitleTfEt.setHint("请输入密码");
        binding.registerActivityCodeTf.btnCaptcha.setVisibility(View.GONE);
        binding.activityRegisterBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityLoginIsAgreeLl) {
            binding.activityLoginIsCheckedIv.setSelected(!binding.activityLoginIsCheckedIv.isSelected());
        } else if (v == binding.activityLoginIsCheckedTxt2) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                    .withParam("type", "2")
                    .withParam("title", "服务协议")
                    .withContext(this)
                    .navigate();
        } else if (v == binding.activityLoginIsCheckedTxt4) {
            XKitRouter.withKey(Constant.BaseWebViewActivityKey)
                    .withParam("type", "1")
                    .withParam("title", "隐私政策")
                    .withContext(this)
                    .navigate();
        } else if (v == binding.activityRegisterBtn) {
            String account = getTextStr(binding.registerActivityPhoneTf.viewTitleTfEt);
            if (TextUtils.isEmpty(account)) {
                ToastUtils.toastMsg(getString(R.string.toast_account_empty));
                return;
            }
            String ans = getTextStr(binding.registerActivityCodeTf.viewTitleTfEt);
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
                return;
            }
            String pwd1 = getTextStr(binding.registerActivityPwdTf.viewTitleTfEt);
            String pwd2 = getTextStr(binding.registerActivityPwd2Tf.viewTitleTfEt);
            if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
                ToastUtils.toastMsg("两次密码不相同");
                return;
            }
            if (!binding.activityLoginIsCheckedIv.isSelected()) {
                ToastUtils.toastMsg("请同意协议");
                return;
            }
            RegisterBean registerBean = new RegisterBean();
            registerBean.phoneNo = account;
            registerBean.password = pwd1;
            registerBean.ans = ans;
            registerBean.deviceId = DeviceUtils.getDeviceId(this);
            registerBean.clientType = Constant.clientType;
            HttpUtil.apiW()
                    .customer_registerZh(registerBean)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call, Response<NetData> response, NetData body) {
                                    showShortMsg("注册成功");
                                    finish();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
    }
}
