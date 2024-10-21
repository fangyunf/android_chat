package com.turunsi.yaoxin.main.mine.purse.tixian;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindAlipayActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseTixianBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.pwdkeyboard.Keyboard;
import com.yaoxin.appbase.view.pwdkeyboard.PayEditText;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PurseTiXianActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseTixianBinding binding;
    UserBean accountBean;
    String  accountMoeny;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseTixianBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this,binding.activityMinePurseTixianNav);
        binding.activityMinePurseTixianNav.addCloseImageButton().setOnClickListener(this);
//        binding.

        binding.activityMinePurseTixianMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityMinePurseTixianMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

        binding.activityMinePurseTixianMoneyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String input = s.toString();
                int dotCount = input.length() - input.replace(".", "").length();
                if (dotCount > 1) {
                    // 找到第一个小数点的位置
                    int firstDotIndex = input.indexOf(".");
                    // 移除第一个小数点之后的所有小数点
                    StringBuilder cleanedInput = new StringBuilder(input.substring(0, firstDotIndex + 1));
                    for (int i = firstDotIndex + 1; i < input.length(); i++) {
                        if (input.charAt(i) != '.') {
                            cleanedInput.append(input.charAt(i));
                        }
                    }
                    // 设置过滤后的文本
                    binding.activityMinePurseTixianMoneyEt.setText(cleanedInput.toString());
                    binding.activityMinePurseTixianMoneyEt.setSelection(cleanedInput.length());
                }
            }
        });

//        binding.activityMinePurseTixianTixianTypeLl.setOnClickListener(this);
        binding.activityMinePurseTixianAllTixianTv.setOnClickListener(this);
        binding.activityMinePurseTixianTixianBtn.setOnClickListener(this);
        binding.activityMinePurseTixianAccoutTv.setEnabled(false);

        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgTv.setText("我的零钱");
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgArrowIv.setVisibility(View.GONE);
        int gravity = Gravity.END | Gravity.CENTER_VERTICAL; // 组合重力

        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setGravity(gravity);
//        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setText("支付宝");
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgLl.setOnClickListener(this);
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setFocusable(false);
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setFocusableInTouchMode(false);
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setClickable(true);
        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setOnClickListener(this);
    }
    void tiXianClick(String pwd) {
        String inputMoney = getTextStr(binding.activityMinePurseTixianMoneyEt);

        if (inputMoney.isEmpty()) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.payPassword = pwd;
        bean.amount = NumberUtil.formartUploadMoney(inputMoney);
        bean.type = 2;
        bean.zfbNo = accountBean.phone;
        bean.name = accountBean.name;
        HttpUtil.apiW().withdraw_withdrawDeposit(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg("提现成功");
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
//        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.api8447().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(new Gson().toJson(body.data),UserBean.class);
                        accountMoeny = NumberUtil.formartMoney(bean.balance);
                        binding.activityMinePurseMyLingqian.viewTitleTfWithoutBgEt.setText("¥"+ NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

        RegisterBean bean = new RegisterBean();
        bean.type = 2;
        Context that = this;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        accountBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        if (accountBean != null && !accountBean.phone.isEmpty()) {
                            binding.activityMinePurseTixianAccoutTv.setText(accountBean.phone);
                        } else {
                            ToastUtils.toastMsg("请先绑定支付宝账号");
                            finish();
                            BindAlipayActivity.start(BindAlipayActivity.class,that,null);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseTixianNav.addCloseImageButton()) {
            finish();
        }
//        else if (v == binding.activityMinePurseTixianTixianTypeLl) {
//            String[] strings = {"支付宝", "银行卡"};
//            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), new String[]{"支付宝", "银行卡"}, new DialogAlertUtil.DialogAlertUtilCallBack() {
//                @Override
//                public void clickType(int type) {
//
//                    if (type == 1) {
//                        binding.activityMinePurseTixianTixianTypeTv.setText(strings[type - 1]);
//                        tiXianType = type;
//                        binding.activityMinePurseTixianToAccoutTv.setText("支付宝账号");
//                        binding.activityMinePurseTixianToAccoutEt.setHint("请输入支付宝号");
//                    } else if (type == 2) {
//                        binding.activityMinePurseTixianTixianTypeTv.setText(strings[type - 1]);
//                        tiXianType = type;
//                        binding.activityMinePurseTixianToAccoutTv.setText("银行卡账号");
//                        binding.activityMinePurseTixianToAccoutEt.setHint("请输入银行卡号");
//                    }
//                }
//            });
//        }
        else if (v == binding.activityMinePurseTixianTixianBtn) {
            String textStr = getTextStr(binding.activityMinePurseTixianMoneyEt);
            if (textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            if (textStr.isEmpty() && Integer.parseInt(textStr) < 100) {
                ToastUtils.toastMsg("金额必须大于100");
                return;
            }
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
//                        sendRedWithPwd(password);
                    tiXianClick(password);

                }

            },textStr);
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityMinePurseTixianLl,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
//            if (accountBean == null || accountBean.name.isEmpty() || accountBean.phone.isEmpty() || accountBean.zfb.isEmpty()) {
//                String inputMoney = getTextStr(binding.activityMinePurseTixianMoneyEt);
//
//
//                HashMap map = new HashMap<>();
//                map.put("inputMoney",inputMoney);
//                PurseTiXianAddAccountActivity.start(PurseTiXianAddAccountActivity.class,this,map);
//            } else {
////                binding.activityFunSendRedPacketKeybordRl.setVisibility(View.VISIBLE);
//
//
//
//            }
        } else if (v == binding.activityMinePurseTixianAllTixianTv) {
            binding.activityMinePurseTixianMoneyEt.setText(accountMoeny);
        }
    }

}
