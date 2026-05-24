package com.turunsi.yaoxin.register;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityForgetPwdBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

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
        StatusBarUtils.transtStatusBar(this, binding.forgetPwdActivityNav);
        _initTfText();
    }

    void _initTfText() {
        binding.forgetPwdActivityPhoneTf.viewTitleTfTv.setText(R.string.label_account);
        binding.forgetPwdActivityCodeTf.viewTitleTfTv.setText(R.string.label_security_answer);
        binding.forgetPwdActivityPwdTf.viewTitleTfTv.setText("密码");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfTv.setText("确认密码");

        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setInputType(InputType.TYPE_CLASS_TEXT);

        binding.forgetPwdActivityPhoneTf.viewTitleTfEt.setHint(R.string.hint_input_account);
        binding.forgetPwdActivityCodeTf.viewTitleTfEt.setHint(R.string.hint_input_security_answer);
        binding.forgetPwdActivityPwdTf.viewTitleTfEt.setHint("请输入密码");
        binding.forgetPwdActivityPwd2Tf.viewTitleTfEt.setHint("请输入密码");

        binding.forgetPwdActivityCodeTf.btnCaptcha.setVisibility(View.GONE);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.forgetPwdActivityNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.forgetPwdActivityBtn) {
            String account = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfEt);
            if (TextUtils.isEmpty(account)) {
                ToastUtils.toastMsg(getString(R.string.toast_account_empty));
                return;
            }
            String ans = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfEt);
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
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
            bean.phoneNo = account;
            bean.ans = ans;
            HttpUtil.apiW()
                    .customer_updatePasswordZh(bean)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call, Response<NetData> response, NetData body) {
                                    ToastUtils.toastMsg("修改成功");
                                    finish();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
        }
    }
}
