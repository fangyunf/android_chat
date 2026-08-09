package com.turunsi.yaoxin.register;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

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
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.forgetPwdActivityNav);
        binding.forgetPwdActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.forgetPwdActivityBtn.setOnClickListener(this);
        _initTfText();
    }

    private void styleInputField(View root, EditText et, String hint) {
        root.setBackgroundResource(R.drawable.bg_input_stroke_capsule);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
        if (lp != null) {
            lp.leftMargin = 0;
            lp.rightMargin = 0;
            root.setLayoutParams(lp);
        }
        root.setPadding(dp(16), 0, dp(12), 0);
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            if (vg.getChildCount() > 1) {
                vg.getChildAt(1).setVisibility(View.GONE);
            }
            View row = vg.getChildAt(0);
            if (row instanceof LinearLayout) {
                ((LinearLayout) row).setPadding(0, 0, 0, 0);
            }
        }
        ViewGroup.MarginLayoutParams etLp = (ViewGroup.MarginLayoutParams) et.getLayoutParams();
        if (etLp != null) {
            etLp.leftMargin = 0;
            et.setLayoutParams(etLp);
        }
        et.setHint(hint);
        et.setHintTextColor(Color.parseColor("#BBBBBB"));
        et.setTextSize(15);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    void _initTfText() {
        styleInputField(binding.forgetPwdActivityPhoneTf.getRoot(),
                binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt, "请输入账号");
        styleInputField(binding.forgetPwdActivityCodeTf.getRoot(),
                binding.forgetPwdActivityCodeTf.viewTitleTfCountEt, "请输入密保");
        styleInputField(binding.forgetPwdActivityPwdTf.getRoot(),
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt, "请输入新密码");

        binding.forgetPwdActivityPhoneTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountIv.setVisibility(View.GONE);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountIv.setVisibility(View.GONE);

        binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setInputType(InputType.TYPE_CLASS_TEXT);
        binding.forgetPwdActivityCodeTf.viewTitleTfCountEt.setTransformationMethod(null);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());

        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setVisibility(View.VISIBLE);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl.setOnClickListener(this);
        binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(true);

        binding.forgetPwdActivityCodeTf.viewTitleTfCountCaptcha.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.forgetPwdActivityNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeRl) {
            binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.setSelected(!binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected());
            if (binding.forgetPwdActivityPwdTf.viewTitleTfCountEyeIv.isSelected()) {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                binding.forgetPwdActivityPwdTf.viewTitleTfCountEt.setTransformationMethod(null);
            }
        } else if (v == binding.forgetPwdActivityBtn) {
            String phone = getTextStr(binding.forgetPwdActivityPhoneTf.viewTitleTfCountEt);
            if (phone.isEmpty()) {
                ToastUtils.toastMsg("请输入账号");
                return;
            }
            String ans = getTextStr(binding.forgetPwdActivityCodeTf.viewTitleTfCountEt);
            if (ans.isEmpty()) {
                ToastUtils.toastMsg("请输入密保");
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
            bean.ans = ans;

            HttpUtil.apiW().customer_updatePasswordZh(bean)
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
