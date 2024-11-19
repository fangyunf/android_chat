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
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseRechargeBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import retrofit2.Call;
import retrofit2.Response;

public class PurseRechargeActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseRechargeBinding binding;

    int _rechargeType = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseRechargeNav.addCloseImageButton().setOnClickListener(this);

        _initCell();
    }
    private void _initCell() {
//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));

//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setHint("请输入充值金额");
//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgTv.setText("充值金额");


        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgTv.setText("充值方式");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);
        int gravity = Gravity.END | Gravity.CENTER_VERTICAL; // 组合重力

//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("支付宝");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setOnClickListener(this);

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
            rechargeMoney(inputMoney);
        } else if (v == binding.activityMinePurseRechargeMoney100) {
            rechargeMoney("100");
        } else if (v == binding.activityMinePurseRechargeMoney300) {
            rechargeMoney("300");
        } else if (v == binding.activityMinePurseRechargeMoney500) {
            rechargeMoney("500");
        } else if (v == binding.activityMinePurseRechargeMoney1000) {
            rechargeMoney("1000");
        } else if (v == binding.activityMinePurseRechargeMoney3000) {
            rechargeMoney("3000");
        } else if (v == binding.activityMinePurseRechargeMoney5000) {
            rechargeMoney("5000");
        } else if (v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl || v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt) {
            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), new String[]{"支付宝", "银行卡"}, new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("支付宝");
                        _rechargeType = 0;
                    } else if (type == 2) {
                        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("银行卡");
                        _rechargeType = 1;


                    }
                }
            });
        }

    }
    void rechargeMoney(String inputMoney) {

        RequestParamsBean registerBean = new RequestParamsBean();
        registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
        if (_rechargeType == 1) {
            LoadingDialog.showDialog(getSupportFragmentManager(),"请求中");
            HttpUtil.apiW().pay_sandPay(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
                            RechargeScanFragment.showV(getSupportFragmentManager(),"请使用云闪付/银行App扫码",userBean.qrCode);

//                            startAlipayPayment(userBean.qrCode);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }

                        @Override
                        public void end() {
                            super.end();
                            LoadingDialog.dismissDialog();
                        }
                    });
            return;
        }
        LoadingDialog.showDialog(getSupportFragmentManager(),"请求中");

        HttpUtil.apiW().pay_six(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        startAlipayPayment(userBean.payUrl);
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                        LoadingDialog.dismissDialog();

                    }
                });
    }
    private void startAlipayPayment(String url) {
        if ( url != null && url.startsWith("https")) {
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

}
