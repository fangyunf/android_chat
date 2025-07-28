package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alipay.sdk.app.AlipayApi;
import com.alipay.sdk.app.PayTask;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseRechargeBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class PurseRechargeActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseRechargeBinding binding;
    String payType = "alipay";
    int _type = 0;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView1;
    Recharge_Adpter adpter;
    Recharge_PayType_Adpter adpter1;
    private static final int SDK_PAY_FLAG = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SDK_PAY_FLAG) {
                @SuppressWarnings("unchecked") Map<String, String> result = (Map<String, String>) msg.obj;
                Log.d("Alipay", "Result === " + result.toString());

                // 支付结果处理逻辑
                String resultStatus = result.get("resultStatus");

                switch (resultStatus) {
                    case "9000":
                        Toast.makeText(PurseRechargeActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                        break;
                    case "8000":
                        Toast.makeText(PurseRechargeActivity.this, "支付结果正在确认中", Toast.LENGTH_SHORT).show();
                        break;
                    case "4000":
                        Toast.makeText(PurseRechargeActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        break;
                    case "5000":
                        Toast.makeText(PurseRechargeActivity.this, "重复请求", Toast.LENGTH_SHORT).show();
                        break;
                    case "6001":
                        Toast.makeText(PurseRechargeActivity.this, "用户取消支付", Toast.LENGTH_SHORT).show();
                        break;
                    case "6002":
                        Toast.makeText(PurseRechargeActivity.this, "网络连接出错", Toast.LENGTH_SHORT).show();
                        break;
                    case "6004":
                        Toast.makeText(PurseRechargeActivity.this, "支付结果未知，请稍后查询", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(PurseRechargeActivity.this, "其他支付状态：" + resultStatus, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseRechargeNav.addCloseImageButton().setOnClickListener(this);
        recyclerView = binding.activityMinePurseRechargeRv;
        recyclerView1 = binding.activityMinePurseRechargeRv1;
        _initRecycleView();
        if (extras.get("type") != null) {
            _type = Integer.parseInt((String) extras.get("type"));
        }
        _initCell();
        StatusBarUtils.transtStatusBar(this, binding.activityMinePurseRechargeNav);
    }

    void _initRecycleView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        Recharge_GridSpacingItemDecoration gridSpacingItemDecoration = new Recharge_GridSpacingItemDecoration(4, SizeUtils.dp2px(10f), false);
        gridSpacingItemDecoration.leftSpace = SizeUtils.dp2px(10f);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setLayoutManager(gridLayoutManager);
        adpter = new Recharge_Adpter();
        List<String> list = new ArrayList<>();
        list.add("100");
        list.add("300");
        list.add("400");
        list.add("500");
        list.add("800");
        list.add("1000");
        list.add("2000");
        list.add("3000");
        adpter.setItems(list);
        adpter.selectStr = "100";
//        binding.activityMinePurseRechargeDetailTv.setText("≈" + adpter.selectStr+"CNY");
        recyclerView.setAdapter(adpter);
        adpter.notifyDataSetChanged();
        adpter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<String>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<String, ?> baseQuickAdapter, @NonNull View view, int i) {
                adpter.selectStr = adpter.getItem(i);
//                binding.activityMinePurseRechargeDetailTv.setText("≈" + adpter.selectStr+"CNY");
                adpter.notifyDataSetChanged();
            }
        });


        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        adpter1 = new Recharge_PayType_Adpter();
        List<String> list1 = new ArrayList<>();
        list1.add("支付宝");
        list1.add("微信");
//        list1.add("微信充值一");
//        list1.add("支付宝充值二");
//        list1.add("微信充值二");
        adpter1.setItems(list1);
        adpter1.payType = "支付宝";
        recyclerView1.setAdapter(adpter1);
        adpter1.notifyDataSetChanged();
        adpter1.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<String>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<String, ?> baseQuickAdapter, @NonNull View view, int i) {
                adpter1.payType = adpter1.getItem(i);
                switch (adpter1.payType) {
                    case "支付宝":
                        payType = "alipay";
                        break;
                    case "微信":
                        payType = "wxpay";
                        break;
                }
                adpter1.notifyDataSetChanged();
            }
        });
//        adpter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
//            @Override
//            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
//                SheYang_OnePicSearchResultBean item = (SheYang_OnePicSearchResultBean) adapter.getItem(position);
//                if (item != null && item.url != null) {
//                    ServiceProtocol.instance().startService(getActivity(), item.url);
//                }
//            }
//        });


    }

    private void _initCell() {
//        binding.activityMinePurseRechargeRechargeMoney.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
//        if (_type == 1) {
//            binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setVisibility(View.VISIBLE);
//        } else {
        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
//        }
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
//        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setOnClickListener(this);

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
//        binding.activityMinePurseRechargeMoney100.setOnClickListener(this);
//        binding.activityMinePurseRechargeMoney300.setOnClickListener(this);
//        binding.activityMinePurseRechargeMoney500.setOnClickListener(this);
//        binding.activityMinePurseRechargeMoney1000.setOnClickListener(this);
//        binding.activityMinePurseRechargeMoney3000.setOnClickListener(this);
//        binding.activityMinePurseRechargeMoney5000.setOnClickListener(this);

    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
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
//            String inputMoney = getTextStr(binding.activityMinePurseRechargeEt);
//            if (inputMoney.isEmpty()) {
//                ToastUtils.toastMsg("请输入金额");
//                return;
//            }
            rechargeMoney(adpter.selectStr);
        }
//        else if (v == binding.activityMinePurseRechargeMoney100) {
//            rechargeMoney("1000");
//        } else if (v == binding.activityMinePurseRechargeMoney300) {
//            rechargeMoney("1500");
//        } else if (v == binding.activityMinePurseRechargeMoney500) {
//            rechargeMoney("2000");
//        } else if (v == binding.activityMinePurseRechargeMoney1000) {
//            rechargeMoney("2500");
//        } else if (v == binding.activityMinePurseRechargeMoney3000) {
//            rechargeMoney("3000");
//        } else if (v == binding.activityMinePurseRechargeMoney5000) {
//            rechargeMoney("5000");
//        }
        else if (v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgLl || v == binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt) {
            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), new String[]{"支付宝", "微信", "银行卡"}, new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("支付宝");
                        payType = "alipay";
                    } else if (type == 2) {
                        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("微信");
                        payType = "wxpay";
                    } else if (type == 3) {
                        binding.activityMinePurseRechargeRechargeType.viewTitleTfWithoutBgEt.setText("微信");
                        payType = "bank";
                    }
                }
            });
        }

    }

    /**
     * 启动支付宝支付
     *
     * @param orderInfo 后台返回的 orderInfo（即 iOS 中 response[@"data"][@"url"]）
     */
    private void startAlipay(String orderInfo) {
        Runnable payRunnable = () -> {
            PayTask alipay = new PayTask(PurseRechargeActivity.this);
            Map<String, String> result = alipay.payV2(orderInfo, true);
            Message msg = new Message();
            msg.what = SDK_PAY_FLAG;
            msg.obj = result;
            mHandler.sendMessage(msg);
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    void rechargeMoney(String inputMoney) {
        String inputMoney1 = getTextStr(binding.activityMinePurseRechargeEt1);
        if (!inputMoney1.isEmpty()) {
            inputMoney = inputMoney1;
        }
        if (payType.equals("")) {
            return;
        }

        RequestParamsBean registerBean = new RequestParamsBean();
        registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
        registerBean.type = payType;
        HttpUtil.apiW().pay_six(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                if (!TextUtils.isEmpty(userBean.payurl)) {
                    startAlipayPayment1(userBean.payurl);
                } else if (!TextUtils.isEmpty(userBean.qrcode)) {
                    RechargeScanFragment.showV(getSupportFragmentManager(), payType.equals("wxpay") ? "请使用微信扫码" : "请使用支付宝扫码", userBean.qrcode);
                } else if (!TextUtils.isEmpty(userBean.urlscheme)) {
                    startAlipayPayment1(userBean.urlscheme);
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
        return;


//        if ("微信，支付宝充值".equals(adpter1.payType)) {
//            RequestParamsBean registerBean = new RequestParamsBean();
//            registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
//            registerBean.type = payType;
//            HttpUtil.apiW().pay_six(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
//                            startAlipayPayment1(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        } else if ("微信充值一".equals(adpter1.payType)) {
//
//            RequestParamsBean registerBean = new RequestParamsBean();
//            registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
//            registerBean.type = payType;
//
//            HttpUtil.apiW().pay_sixwx(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
////                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
//                            startAlipayPayment(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        } else if ("支付宝充值二".equals(adpter1.payType)) {
//
//            RequestParamsBean registerBean = new RequestParamsBean();
//            registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
//            registerBean.type = payType;
//
//            HttpUtil.apiW().pay_sixwx(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
////                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
//                            startAlipayPayment1(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        } else if ("微信充值二".equals(adpter1.payType)) {
//
//            RequestParamsBean registerBean = new RequestParamsBean();
//            registerBean.amount = NumberUtil.formartUploadMoney(inputMoney);
//            registerBean.type = payType;
//
//            HttpUtil.apiW().pay_sixL(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
////                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
//                            startAlipayPayment(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        }
//        registerBean.payChannel = payType;
//        if (_type == 1) {
//            registerBean.type = payType;
//            HttpUtil.apiW().pay_sixL(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
////                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
//                            startAlipayPayment(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        } else {

//            HttpUtil.apiW().pay_tyPay(registerBean)
//                    .enqueue(new CommonCallback<NetData>() {
//                        @Override
//                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                            UserBean userBean = new Gson().fromJson(body.data.toString(),UserBean.class);
////                        RechargeScanFragment.showV(getSupportFragmentManager(),payType.equals("wxpay")?"请使用微信扫码":"请使用支付宝扫码",userBean.payUrl);
//                            startAlipayPayment(userBean.url);
//                        }
//
//                        @Override
//                        public void Failure(Call<NetData> call, Throwable t) {
//
//                        }
//                    });
//        }
    }

    private void startAlipayPayment1(String url) {
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("alipay://")) {
            startAlipayPayment(url);
        } else {
            startAlipay(url);
        }

    }

    private void startAlipayPayment(String url) {
        if (url != null) {
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
