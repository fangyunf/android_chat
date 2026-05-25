package com.turunsi.yaoxin.main.mine.purse.pwdmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.LoginActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerSetBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class PursePwdManagerSetActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePursePwdManagerSetBinding binding;
    /** 0:设置支付密码 1：修改支付密码 2：忘记支付密码 100：确认注销 */
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
            String typeString = (String) extras.get("type");
            type = Integer.parseInt(typeString);
        }

        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgTv.setText(
                R.string.label_security_answer);
        binding.activityMinePursePwdManagerGetCode.btnCaptcha.setVisibility(View.GONE);

        if (type == 0) {
            _initSetCell();
            binding.activityMinePursePwdManagerSetNav.setTitle("设置支付密码");
            binding.activityMinePursePwdManagerSetPhone.getRoot().setVisibility(View.GONE);
            binding.activityMinePursePwdManagerGetCode.getRoot().setVisibility(View.GONE);
        } else if (type == 1) {
            binding.activityMinePursePwdManagerSetNav.setTitle("修改支付密码");
            _initModifyCell();
            binding.activityMinePursePwdManagerSetForgetPwdTv.setVisibility(View.VISIBLE);
            binding.activityMinePursePwdManagerGetCode.getRoot().setVisibility(View.GONE);
        } else if (type == 2) {
            binding.activityMinePursePwdManagerSetNav.setTitle("忘记支付密码");
            _initForgetCell();
        } else if (type == 100) {
            binding.activityMinePursePwdManagerSetNav.setTitle("注销账号");
            binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
            binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgLl.setVisibility(
                    View.GONE);
            binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText(
                    R.string.label_account);
        }
        updateSecurityRowVisibility();
    }

    private void updateSecurityRowVisibility() {
        binding.activityMinePursePwdManagerGetCode
                .getRoot()
                .setVisibility(type == 0 || type == 100 ? View.VISIBLE : View.GONE);
    }

    private void _initSetCell() {
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgTv.setText(
                R.string.label_security_answer);
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt.setHint(
                R.string.hint_input_security_answer);
        binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_TEXT);
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入密码");
    }

    private void _initModifyCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText("原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText("新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("确认密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint("请输入原密码");
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
    }

    private void _initForgetCell() {
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgTv.setText(R.string.label_account);
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgTv.setText(
                R.string.label_security_answer);
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgTv.setText("新密码");

        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setHint(
                R.string.hint_input_account);
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setHint(
                R.string.hint_input_security_answer);
        binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt.setHint("请输入新密码");
        binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_TEXT);
        binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt.setInputType(
                InputType.TYPE_CLASS_TEXT);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePursePwdManagerSetNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePursePwdManagerSetForgetPwdTv) {
            HashMap<String, String> map = new HashMap<>();
            map.put("type", "2");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class, this, map);
        } else if (v == binding.activityMineAddressAddSaveRl) {
            submit();
        }
    }

    private void submit() {
        if (type == 100) {
            String account = getTextStr(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
            if (TextUtils.isEmpty(account)) {
                ToastUtils.toastMsg(getString(R.string.toast_account_empty));
                return;
            }
            String ans = getTextStr(binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt);
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
                return;
            }
            RegisterBean bean = new RegisterBean();
            bean.ans = ans;
            HttpUtil.apiW()
                    .home_logout1(bean)
                    .enqueue(
                            new CommonCallback<NetData>() {
                                @Override
                                public void Successful(
                                        Call<NetData> call, Response<NetData> response, NetData body) {
                                    ToastUtils.toastMsg("注销成功");
                                    showLogin();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {}
                            });
            return;
        }

        String pwd1 = getTextStr(binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt);
        String pwd2 = getTextStr(binding.activityMinePursePwdManagerSetConfirmPwd.viewTitleTfWithoutBgEt);
        if (type != 2 && TextUtils.isEmpty(pwd1)) {
            ToastUtils.toastMsg("请输入密码");
            return;
        }
        if (!pwd1.isEmpty() && !pwd2.isEmpty() && !pwd1.equals(pwd2)) {
            ToastUtils.toastMsg("两次密码不相同");
            return;
        }

        RegisterBean registerBean = new RegisterBean();
        if (type == 0) {
            String ans = getTextStr(binding.activityMinePursePwdManagerGetCode.viewTitleTfWithoutBgEt);
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
                return;
            }
            registerBean.ans = ans;
            registerBean.password = pwd1;
        } else if (type == 1) {
            registerBean.password = pwd1;
        } else if (type == 2) {
            String account = getTextStr(binding.activityMinePursePwdManagerSetPhone.viewTitleTfWithoutBgEt);
            if (TextUtils.isEmpty(account)) {
                ToastUtils.toastMsg(getString(R.string.toast_account_empty));
                return;
            }
            String ans = getTextStr(binding.activityMinePursePwdManagerSetSetPwd.viewTitleTfWithoutBgEt);
            if (TextUtils.isEmpty(ans)) {
                ToastUtils.toastMsg(getString(R.string.toast_security_answer_empty));
                return;
            }
            registerBean.phoneNo = account;
            registerBean.ans = ans;
            registerBean.password = pwd2;
        }

        HttpUtil.apiW()
                .home_updateFullPasswordZh(registerBean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg(body.msg);
                                finish();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {}
                        });
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        PursePwdManagerSetActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(PursePwdManagerSetActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(PursePwdManagerSetActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}
