package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.content.Intent;
import android.net.Uri;
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
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindBankCardActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseRechargeBinding;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.PayParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PurseRechargeActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseRechargeBinding binding;
    String payType = "alipay";
    PayParamsBean _payParamsBean;
    PayParamsBean _selectCardBean;
    List<PayParamsBean> _bankCardList;
    int _type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseRechargeNav.addCloseImageButton().setOnClickListener(this);
        if (extras.get("type") != null) {
            _type = Integer.parseInt((String) extras.get("type"));
        }
        _initCell();
    }
    private void _initCell() {
//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        if (_type == 1) {
//            binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setVisibility(View.VISIBLE);
//        } else {
//            binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
//        }
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgTv.setText("充值方式");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);
        int gravity = Gravity.END | Gravity.CENTER_VERTICAL; // 组合重力
        int gravity1 = Gravity.START | Gravity.CENTER_VERTICAL; // 组合重力

        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("暂无银行卡");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setOnClickListener(this);


        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgTv.setVisibility(View.GONE);

        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgEt.setGravity(gravity1);
        binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgEt.setHint("请输入验证码");
        binding.activityMinePurseRechargeGetCode.btnCaptcha.setVisibility(View.VISIBLE);
        binding.activityMinePurseRechargeGetCode.btnCaptcha.setBackgroundColor(getResources().getColor(R.color.transparent));


        CountDownView mCountDownView = binding.activityMinePurseRechargeGetCode.btnCaptcha;
        mCountDownView.setCountDownTime(60);

        mCountDownView.needVerify = false;
        mCountDownView.setCaptchaListener(new LoginLoader.CaptchaListener() {
            @Override
            public void onPre() {
//                String phone = getTextStr(binding.fragmentOtherPlaceLoginPhoneEt);
//                CommonNetUtil.getPhoneCode(phone);
                String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
                if (inputMoney.isEmpty()) {
                    ToastUtils.toastMsg("请输入金额");
                    return;
                }

                getCode(inputMoney);
            }

            @Override
            public void onComplete(String phoneOrEmail) {
            }
        });




        binding.activityMinePurseRechargeEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityMinePurseRechargeEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

        binding.activityMinePurseRechargeEt.addTextChangedListener(new TextWatcher() {
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
                    binding.activityMinePurseRechargeEt.setText(cleanedInput.toString());
                    binding.activityMinePurseRechargeEt.setSelection(cleanedInput.length());
                }
            }
        });


//        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setEnabled(false);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setFocusable(false);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setFocusableInTouchMode(false);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setClickable(true);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setOnClickListener(this);



        binding.activityMinePurseRechargeRechargeRl.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney100.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney300.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney500.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney1000.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney3000.setOnClickListener(this);
        binding.activityMinePurseRechargeMoney5000.setOnClickListener(this);
        binding.activityMinePurseRechargeQqPayBtn.setOnClickListener(this);
        binding.activityMinePurseRechargeWxPayBtn.setOnClickListener(this);
        binding.activityMinePurseRechargeAliPayBtn.setOnClickListener(this);

        if (_type == 0) {
            binding.activityMinePurseRechargeQqPayBtn.setVisibility(View.GONE);
//            binding.activityMinePurseRechargeWxPayBtn.setVisibility(View.GONE);
        }
    }
    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
//                        binding.activityMinePurseRechargeAccountTv.setText("¥"+NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
        HttpUtil.apiW().bindCard_bindingCards()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
//                        binding.activityMinePurseRechargeAccountTv.setText("¥"+NumberUtil.formartMoney(bean.balance));

                        Type type = new TypeToken<List<PayParamsBean>>() {}.getType();
                        List<PayParamsBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        _bankCardList = tempList;
                        if (tempList != null && !tempList.isEmpty()) {
                            _selectCardBean = tempList.get(0);
                        }
                        if (_selectCardBean == null) {
                            CommonChoiceDialog dialog = new CommonChoiceDialog();
                            dialog
                                    .setTitleStr("提示")
                                    .setContentStr("暂无银行卡，是否前往绑定银行卡?")
                                    .setNegativeStr("取消")
                                    .setPositiveStr("确定")
                                    .setConfirmListener(
                                            new ChoiceListener() {
                                                @Override
                                                public void onPositive() {
                                                    BindBankCardActivity.start(BindBankCardActivity.class, PurseRechargeActivity.this, null);
                                                }

                                                @Override
                                                public void onNegative() {}
                                            })
                                    .show(getSupportFragmentManager());
                        } else {
                            binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText(_selectCardBean.bankName+ " （" + _selectCardBean.getBankCardNo() + "）");
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseRechargeNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseRechargeRechargeRl) {
            String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
            if (inputMoney.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            rechargeNew();
        } else if (v == binding.activityMinePurseRechargeMoney100) {
//            rechargeMoney("100");
            binding.activityMinePurseRechargeEt.setText("100");
        } else if (v == binding.activityMinePurseRechargeMoney300) {
//            rechargeMoney("300");
            binding.activityMinePurseRechargeEt.setText("300");
        } else if (v == binding.activityMinePurseRechargeMoney500) {
//            rechargeMoney("500");
            binding.activityMinePurseRechargeEt.setText("500");
        } else if (v == binding.activityMinePurseRechargeMoney1000) {
//            rechargeMoney("1000");
            binding.activityMinePurseRechargeEt.setText("1000");
        } else if (v == binding.activityMinePurseRechargeMoney3000) {
//            rechargeMoney("3000");
            binding.activityMinePurseRechargeEt.setText("3000");
        } else if (v == binding.activityMinePurseRechargeMoney5000) {
//            rechargeMoney("5000");
            binding.activityMinePurseRechargeEt.setText("5000");
        } else if (v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl || v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt) {
            if (_bankCardList != null && !_bankCardList.isEmpty()) {
                List<String> titles = new ArrayList<>();
                for (PayParamsBean payParamsBean : _bankCardList) {
                    titles.add(payParamsBean.bankName + " （" + payParamsBean.getBankCardNo() + "）");
                }
                String[] titlesArray = titles.toArray(new String[0]);
                DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), titlesArray, new DialogAlertUtil.DialogAlertUtilCallBack() {
                    @Override
                    public void clickType(int type) {
                        if (type > 0) {
                            _selectCardBean = _bankCardList.get(type - 1);
                            binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText(_selectCardBean.bankName+ " （" + _selectCardBean.getBankCardNo() + "）");
                        }
                    }
                });
            }
        } else if (v == binding.activityMinePurseRechargeQqPayBtn) {
            String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
            if (inputMoney.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            payType = "qqpay";
            rechargeMoney(inputMoney);
        } else if (v == binding.activityMinePurseRechargeWxPayBtn) {
            String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
            if (inputMoney.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            payType = "wxpay";
            rechargeMoney(inputMoney);
        } else if (v == binding.activityMinePurseRechargeAliPayBtn) {
            String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
            if (inputMoney.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            payType = "alipay";
            rechargeMoney(inputMoney);
        }

    }
    void rechargeMoney(String inputMoney) {
        RequestParamsBean registerBean = new RequestParamsBean();
        registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
        if (_type == 1) {
            registerBean.type = payType;
            HttpUtil.apiW().pay_sixL(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
//                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
                            startAlipayPayment(userBean.payUrl);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        } else {

            registerBean.name = "12";
            registerBean.configId = "2";
            registerBean.payWay = "sypay";
            registerBean.type = payType;
            registerBean.userId = DataUtil.getUserid();
            HttpUtil.apiW().pay_tyPay(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
//                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
                            startAlipayPayment(userBean.url);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }
    private void startAlipayPayment(String url) {
        if ( url != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // 设置URL，替换为你想打开的网页地址
                intent.setData(Uri.parse(url));

                // 启动Intent，跳转到浏览器
                startActivity(intent);
            } catch (Exception e) {
            }
        } else {
            ToastUtils.toastMsg("支付失败");
        }
        // 支付宝支付请求 URL
//        String alipayUrl = "alipayqr://platformapi/startapp?saId=10000007&qrcode="+url;
//
//        // 创建 Intent 打开支付宝
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipayUrl));
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        } else {
//            // 支付宝未安装处理
//            // 提示用户安装支付宝或者其他处理逻辑
//            ToastUtils.toastMsg("请安装支付宝");
//        }
    }



    public void getCode(String inputMoney) {
        int amount = NumberUtil.formartUploadMoney(inputMoney);


        if (amount <= 0) {
            ToastUtils.toastMsg("请选择金额");
            return;
        }
        if (_selectCardBean == null) {
            ToastUtils.toastMsg("请选择卡号");
            return;
        }

        ParamsBean registerBean = new ParamsBean();
        registerBean.amount = amount + "";
        registerBean.goodsTitle = "123";
        registerBean.goodsDesc = "1234";
        registerBean.description = "12345";
        registerBean.type = "fast_pay";
        registerBean.token_no = _selectCardBean.tokenNo;
        registerBean.configId = "3";
        registerBean.userId = DataUtil.getUserid();

        LoadingDialog.showDialog(getSupportFragmentManager(),"获取中..");
        HttpUtil.apiW().pay_adaPay(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        _payParamsBean = new Gson().fromJson(body.data.toString(), PayParamsBean.class);
                        if (_payParamsBean.error_msg != null && !_payParamsBean.error_msg.isEmpty()) {
                            ToastUtils.toastMsg(_payParamsBean.error_msg);
                        }  else {
                            ToastUtils.toastMsg(body.msg);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        LoadingDialog.dismissDialog();
                    }
                });
    }
    public void rechargeNew() {
        String code = getTextStr(binding.activityMinePurseRechargeGetCode.viewTitleTfWithoutBgEt);


        if (_payParamsBean == null || _payParamsBean.id.isEmpty()) {
            ToastUtils.toastMsg("请先获取验证码");
            return;
        }
        if (code.isEmpty()) {
            ToastUtils.toastMsg("请输入验证码");
            return;
        }

        RequestParamsBean registerBean = new RequestParamsBean();
        registerBean.payment_id = _payParamsBean.id;
        registerBean.order_no = _payParamsBean.order_no;
        registerBean.app_id = _payParamsBean.app_id;
        registerBean.configId = "3";
        registerBean.userId = DataUtil.getUserid();
        registerBean.sms_code = code;

        LoadingDialog.showDialog(getSupportFragmentManager(),"充值中..");
        HttpUtil.apiW().pay_adaPayConfirm(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        PayParamsBean payParamsBean = new Gson().fromJson(body.data.toString(), PayParamsBean.class);
                        if (payParamsBean.error_msg != null && !payParamsBean.error_msg.isEmpty()) {
                            ToastUtils.toastMsg(payParamsBean.error_msg);
                        }  else {
                            ToastUtils.toastMsg(body.msg);
                            finish();
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        LoadingDialog.dismissDialog();
                    }
                });
    }
}
