package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendRedPacketBinding;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.pwdkeyboard.Keyboard;
import com.yaoxin.appbase.view.pwdkeyboard.PayEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class FunSendZhuanZhangActivity extends BaseActivity implements View.OnClickListener {
    ActivityFunSendRedPacketBinding binding;
    //0： 个人 1：拼手气  2：专属
    private int type = 0;
    private int sessionType = 0;
    private String sessionId = "";
    private String toUserId = "";
    private String selectToUserId = "";
    private UserInfo targetUserInfo;

    private PayEditText payEditText;
    private Keyboard keyboard;

    private GroupInfoBean groupInfoBean;

    ArrayList<GroupInfoBean> userList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunSendRedPacketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityFunSendRedPacketNav);
        payEditText = binding.PayEditTextPay;
        keyboard = binding.KeyboardViewPay;
        if (extras.get("sessionId") != null) {
            sessionId = (String) extras.get("sessionId");
        }
        _initView();
        _updateUI();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        binding.activityFunSendRedPacketBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

    }

    @Override
    protected void _initView() {
        binding.activityFunSendRedPacketNav.addCloseImageButton().setOnClickListener(this);
//        binding.activityFunSendRedPacketPinChangeTypeLl.setOnClickListener(this);
        binding.activityFunSendRedPacketSendTv.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityFunSendRedPacketMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));


        binding.activityFunSendRedPacketCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityFunSendRedPacketToPeopleLl.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String input = s.toString();
                String formattedValue;
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
                    binding.activityFunSendRedPacketMoneyEt.setText(cleanedInput.toString());
                    binding.activityFunSendRedPacketMoneyEt.setSelection(cleanedInput.length());
                    formattedValue = String.format("%.2f", Double.parseDouble(cleanedInput.toString()));
                } else {
                    if (!input.isEmpty()) {
                        formattedValue = String.format("%.2f", Double.parseDouble(input));
                    } else {
                        formattedValue = "0.00";
                    }
                }

                binding.activityFunSendRedPacketTotalTv.setText(formattedValue);
            }
        });
    }

    private void _updateUI() {
        //0： 个人 1：拼手气  2：专属
        binding.layoutFilter.setVisibility(View.GONE);
        binding.activityFunSendRedPacketNav.getTitleView().setText("转账");
        binding.activityFunSendRedPacketPinLl.setVisibility(View.GONE);
        binding.activityFunSendRedPacketMoneyTv.setText("金额");
        binding.activityFunSendRedPacketGreetingLl.setVisibility(View.VISIBLE);
        binding.activityFunSendRedPacketGreetingEt.setHint("转账说明");
        binding.activityFunSendRedPacketSendTv.setText("转账");
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunSendRedPacketNav.addCloseImageButton()) {
            finish();
        }
//        else if (v == binding.activityFunSendRedPacketPinChangeTypeLl) {
//            ActionSheet.createBuilder(this, getSupportFragmentManager())
//                    .setCancelButtonTitle("取消")
//                    .setOtherButtonTitles("拼手气红包", "专属红包")
//                    .setCancelableOnTouchOutside(true)
//                    .setListener(new ActionSheet.ActionSheetListener() {
//                        @Override
//                        public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
//
//                        }
//
//                        @Override
//                        public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//                            if (index == 0) {
//                                type = 1;
//                            } else if (index == 1) {
//                                type = 2;
//                            }
//                            _updateUI();
//
//                        }
//                    }).show();
//        }
        else if (v == binding.activityFunSendRedPacketSendTv) {
            String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);

            if (moneyStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
//            binding.activityFunSendRedPacketKeybordRl.setVisibility(View.VISIBLE);
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    sendRedWithPwd(password);
                }
            }, moneyStr);
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityFunSendRedPacketLl,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置


        }
    }

    void sendRedWithPwd(String pwd) {
        String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
        String greeting = getTextStr(binding.activityFunSendRedPacketGreetingEt);

        int amout = 0;
        if (!moneyStr.isEmpty()) {
            amout = NumberUtil.formartUploadMoney(moneyStr);
        }

        if (amout <= 0) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.amount = amout;
        bean.title = greeting.isEmpty() ? "你发起了一笔转账" : greeting;
        bean.password = pwd;
        bean.toUserId = sessionId;
        HttpUtil.apiW().red_zz(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        ToastUtils.toastMsg("发送成功");
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
