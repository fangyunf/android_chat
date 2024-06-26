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
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class PurseRechargeActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseRechargeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseRechargeNav.addCloseImageButton().setOnClickListener(this);

        _initCell();
    }
    private void _initCell() {
        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setHint("请输入充值金额");
        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgTv.setText("充值金额");

        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgTv.setText("充值选择");
        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);

        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgTv.setText("充值方式");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);
        int gravity = Gravity.END | Gravity.CENTER_VERTICAL; // 组合重力

        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("支付宝");
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgArrowIv.setVisibility(View.GONE);

        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.addTextChangedListener(new TextWatcher() {
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
                    binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setText(cleanedInput.toString());
                    binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setSelection(cleanedInput.length());
                }
            }
        });


        binding.activityMinePurseRechargeRechargeChoose.viewTitleTfWithoutBgEt.setEnabled(false);
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setEnabled(false);

        binding.activityMinePurseRechargeRechargeRl.setOnClickListener(this);

    }
    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        binding.activityMinePurseRechargeAccountTv.setText("¥"+NumberUtil.formartMoney(bean.balance));
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
            String inputMoney = getTextStr(binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt);
            if (inputMoney.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }

            RegisterBean registerBean = new RegisterBean();
            registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
            HttpUtil.apiW().pay_jhzs(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
                            startAlipayPayment(userBean.qrCode);
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }

    }
    private void startAlipayPayment(String url) {
        // 支付宝支付请求 URL
        String alipayUrl = "alipayqr://platformapi/startapp?saId=10000007&qrcode="+url;

        // 创建 Intent 打开支付宝
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alipayUrl));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // 支付宝未安装处理
            // 提示用户安装支付宝或者其他处理逻辑
            ToastUtils.toastMsg("请安装支付宝");
        }
    }

}
