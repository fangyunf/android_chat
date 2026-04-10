package com.turunsi.yaoxin.main.mine.purse.tixian;

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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class PurseTiXianActivity extends BaseActivity implements View.OnClickListener {
    private static final double WITHDRAW_MIN_YUAN = 100;
    private static final double USDT_WITHDRAW_MIN_YUAN = 500;

    ActivityMinePurseTixianBinding binding;
    String accountMoeny;
    String payType = "alipay";

    UserBean aliPayBean;
    UserBean wxPayBean;
    UserBean yhkPayBean;
    private final Object lock = new Object();
    private int completedRequests = 0;
    private int resumeCount;

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount > 1) {
            reloadBindAccountsAndBalance();
        }
    }

    /**
     * 从绑定页返回后刷新可提现方式
     */
    private void reloadBindAccountsAndBalance() {
        synchronized (lock) {
            completedRequests = 0;
        }
        aliPayBean = null;
        wxPayBean = null;
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        accountMoeny = NumberUtil.formartMoney(bean.balance);
                        binding.activityMinePurseTixianAvailableBalanceTv.setText(
                                "可用余额 " + NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
        startAllRequests();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseTixianBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityMinePurseTixianNav);
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

        // 设置提现方式点击事件
        binding.activityMinePurseTixianfangshiLl.setOnClickListener(this);
        binding.activityMinePurseTixianfangshiEt.setFocusable(false);
        binding.activityMinePurseTixianfangshiEt.setFocusableInTouchMode(false);
        binding.activityMinePurseTixianfangshiEt.setClickable(true);
        binding.activityMinePurseTixianfangshiEt.setOnClickListener(this);
        binding.activityMinePurseTixianfangshiEt.setHint("请选择");
    }

    private void updateWithdrawNoteForPayType() {
        if ("usdt".equals(payType)) {
            binding.activityMinePurseTixianNoteTv.setText(R.string.usdt_withdraw_notes);
        } else {
            binding.activityMinePurseTixianNoteTv.setText(
                    "注:技术服务费率8%+服务费2元/笔\n提现时间（早8.00-晚20.00）\n发起提现后二小时内到账");
        }
    }

    void tiXianClick(String pwd) {
        if ("usdt".equals(payType)) {
            return;
        }
        String inputMoney = getTextStr(binding.activityMinePurseTixianMoneyEt);

        if (inputMoney.isEmpty()) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.payPassword = pwd;
        bean.amount = NumberUtil.formartUploadMoney(inputMoney);
        bean.type = 2;
        if (payType.equals("alipay")) {

            bean.zfbNo = aliPayBean.phone;
            bean.name = aliPayBean.name;
            bean.zfbUrl = aliPayBean.usdt;
            bean.userUsdtId = aliPayBean.id + "";
        } else if (payType.equals("wxpay")) {

            bean.zfbNo = wxPayBean.phone;
            bean.name = wxPayBean.name;
            bean.zfbUrl = wxPayBean.usdt;
            bean.userUsdtId = wxPayBean.id + "";
        } else if (payType.equals("yhkpay")) {

            bean.zfbNo = yhkPayBean.phone;
            bean.name = yhkPayBean.name;
            bean.zfbUrl = yhkPayBean.usdt;
            bean.userUsdtId = yhkPayBean.id + "";
        }
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
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        accountMoeny = NumberUtil.formartMoney(bean.balance);
                        binding.activityMinePurseTixianAvailableBalanceTv.setText("可用余额 " + NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

//        RegisterBean bean = new RegisterBean();
//        bean.type = 1;
//        HttpUtil.apiW().bindCard_userZFB(bean)
//                .enqueue(new CommonCallback<NetData>() {
//                    @Override
//                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                        Type type = new TypeToken<List<UserBean>>() {
//                        }.getType();
//                        List<UserBean> tempList = new Gson().fromJson(body.data.toString(), type);
//
//                        if (!tempList.isEmpty()) {
//                            aliPayBean = tempList.get(0);
//                            binding.activityMinePurseTixianAccoutTv.setText(aliPayBean.phone);
//                        } else {
//                            ToastUtils.toastMsg("请先绑定账号");
//                            finish();
////                            BindAlipayActivity.start(BindAlipayActivity.class,that,null);
//                        }
//                    }
//
//                    @Override
//                    public void Failure(Call<NetData> call, Throwable t) {
//
//                    }
//                });
        startAllRequests();
    }

    public void startAllRequests() {
        // 发起支付宝请求
        RegisterBean alipayBean = new RegisterBean();
        alipayBean.type = 2;
        makeRequest(alipayBean, "alipay");

        // 发起微信请求
        RegisterBean wechatBean = new RegisterBean();
        wechatBean.type = 1;
        makeRequest(wechatBean, "wechat");
//
//        // 发起银行卡请求
//        RegisterBean bankBean = new RegisterBean();
//        bankBean.type = 3;
//        makeRequest(bankBean, "bank");
    }

    private void makeRequest(RegisterBean bean, String requestType) {
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<UserBean>>() {
                        }.getType();
                        List<UserBean> tempList = new Gson().fromJson(body.data.toString(), type);
//                        if (tempList != null && !tempList.isEmpty()) {
//                            aliPayBean = tempList.get(0);
//                        }

//                        aliPayBean = new Gson().fromJson(body.data.toString(), UserBean.class);
//                        handleAllRequestsCompleted();
                        synchronized (lock) {
                            // 根据请求类型保存数据
                            switch (requestType) {
                                case "alipay":
                                    if (tempList != null && !tempList.isEmpty()) {

                                        aliPayBean = tempList.get(0);
                                    }
                                    break;
                                case "wechat":
                                    if (tempList != null && !tempList.isEmpty()) {
                                        wxPayBean = tempList.get(0);
                                    }
                                    break;
                                case "bank":
                                    if (tempList != null && !tempList.isEmpty()) {
                                        yhkPayBean = tempList.get(0);
                                    }
                                    break;
                            }

                            completedRequests++;

                            // 检查是否所有请求都完成了
                            if (completedRequests == 2) {
                                handleAllRequestsCompleted();
                            }
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        synchronized (lock) {
                            completedRequests++;

                            // 即使失败也要检查是否所有请求都完成了
                            if (completedRequests == 2) {
                                handleAllRequestsCompleted();
                            }
                        }
                    }
                });
    }

    private void handleAllRequestsCompleted() {
        if (!"usdt".equals(payType)) {
            // 所有请求完成后的处理逻辑（支付宝 > 微信 > 银行卡）
            if (aliPayBean != null) {
                payType = "alipay";
                binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.mipmap.recharge_index_zfb);
                binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                binding.activityMinePurseTixianfangshiEt.setText("支付宝:" + aliPayBean.phone);
            } else if (wxPayBean != null) {
                payType = "wxpay";
                binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.mipmap.recharge_index_wx);
                binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                binding.activityMinePurseTixianfangshiEt.setText("微信:" + wxPayBean.phone);
            } else if (yhkPayBean != null) {
                payType = "yhkpay";
                binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.mipmap.recharge_index_szrmb);
                binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                binding.activityMinePurseTixianfangshiEt.setText("银行卡:" + yhkPayBean.phone);
            }
        } else {
            binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.drawable.icon_usdt);
            binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
            binding.activityMinePurseTixianfangshiEt.setText("USDT-TRC20");
        }

        updateWithdrawNoteForPayType();

        if (aliPayBean == null && wxPayBean == null && yhkPayBean == null) {
            if (!"usdt".equals(payType)) {
                ToastUtils.toastMsg("请先绑定账号");
                finish();
            }
        }
    }

    private void showWithdrawPasswordPopup(String textStr) {
        PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                tiXianClick(password);
            }
        }, textStr);
        popEnterPassword.showAtLocation(binding.activityMinePurseTixianLl,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
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
            String textStr = getTextStr(binding.activityMinePurseTixianMoneyEt).trim();
            if (textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
            double amountYuan;
            try {
                amountYuan = Double.parseDouble(textStr);
            } catch (NumberFormatException e) {
                ToastUtils.toastMsg("请输入正确金额");
                return;
            }
            if ("usdt".equals(payType)) {
                Map<String, String> map = new HashMap<>();
                map.put(UsdtWithdrawActivity.EXTRA_INITIAL_CNY_YUAN, textStr);
                UsdtWithdrawActivity.start(UsdtWithdrawActivity.class, PurseTiXianActivity.this, map);
                return;
            }
            if (amountYuan < WITHDRAW_MIN_YUAN) {
                ToastUtils.toastMsg("提现金额100起");
                return;
            }
            showWithdrawPasswordPopup(textStr);
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
        } else if (v == binding.activityMinePurseTixianfangshiLl || v == binding.activityMinePurseTixianfangshiEt) {
            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(),
                    new String[]{"支付宝", "微信", "USDT"},
                    new DialogAlertUtil.DialogAlertUtilCallBack() {
                        @Override
                        public void clickType(int type) {
                            if (type == 0) {
                                return;
                            }
                            if (type == 3) {
                                payType = "usdt";
                                binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.drawable.icon_usdt);
                                binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                                binding.activityMinePurseTixianfangshiEt.setText("USDT");
                                updateWithdrawNoteForPayType();
                                UsdtWithdrawActivity.start(UsdtWithdrawActivity.class, PurseTiXianActivity.this, null);
                                return;
                            }
                            if (type == 1) {
                                if (aliPayBean != null) {
                                    binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.mipmap.recharge_index_zfb);
                                    binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                                    binding.activityMinePurseTixianfangshiEt.setText("支付宝:" + aliPayBean.phone);
                                } else {
                                    ToastUtils.toastMsg("请绑定支付宝账号");
                                    return;
                                }
                                payType = "alipay";
                            } else if (type == 2) {
                                if (wxPayBean != null) {
                                    binding.activityMinePurseTixianfangshiIconIv.setImageResource(R.mipmap.recharge_index_wx);
                                    binding.activityMinePurseTixianfangshiIconIv.setVisibility(View.VISIBLE);
                                    binding.activityMinePurseTixianfangshiEt.setText("微信:" + wxPayBean.phone);
                                } else {
                                    ToastUtils.toastMsg("请绑定微信账号");
                                    return;
                                }
                                payType = "wxpay";
                            }
                            updateWithdrawNoteForPayType();
                        }
                    });
        }
    }

}
