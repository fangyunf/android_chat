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
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
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
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.pwdkeyboard.Keyboard;
import com.yaoxin.appbase.view.pwdkeyboard.PayEditText;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PurseTiXianActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseTixianBinding binding;
    UserBean accountBean;
    String  accountMoeny;
    int _tiXianType;
    List<BankCardListBean> bankCardListBeans;
    BankCardListBean selectBankBean;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseTixianBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgTv.setText("提现方式");
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);
        int gravity = Gravity.END | Gravity.CENTER_VERTICAL; // 组合重力

        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setText("支付宝");
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl.setOnClickListener(this);
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setFocusable(false);
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setFocusableInTouchMode(false);
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setClickable(true);
        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setOnClickListener(this);

        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgLl.setBackground(getResources().getDrawable(com.yaoxin.appbase.R.drawable.bg_f2f2f2_rounded_10));
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setBackground(getResources().getDrawable(R.color.transparent));
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgTv.setText("收款账号");
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgArrowIv.setVisibility(View.VISIBLE);

        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setGravity(gravity);
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText("支付宝");
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgLl.setOnClickListener(this);
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setFocusable(false);
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setFocusableInTouchMode(false);
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setClickable(true);
        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setOnClickListener(this);
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
        if (_tiXianType == 0) {
            bean.type = 2;
            bean.userUsdtId = accountBean.id + "";
        } else {
            bean.type = 3;
            bean.userUsdtId = selectBankBean.id + "";
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
                        UserBean bean = new Gson().fromJson(body.data.toString(),UserBean.class);
                        accountMoeny = NumberUtil.formartMoney(bean.balance);
//                        binding.activityMinePurseTixianAccoutTv.setText("¥"+ NumberUtil.formartMoney(bean.balance));
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
                        Type type = new TypeToken<List<UserBean>>() {}.getType();
                        List<UserBean> tempList = new Gson().fromJson(body.data.toString(), type);
                        if (tempList != null && !tempList.isEmpty()) {
                            accountBean = tempList.get(0);
                        }
                        _requestBankData();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    void _requestBankData() {
        RegisterBean bean = new RegisterBean();
        bean.type = 3;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<BankCardListBean>>() {}.getType();
                        bankCardListBeans = new Gson().fromJson(body.data.toString(), type);
                        if (!bankCardListBeans.isEmpty()) {
                            selectBankBean = bankCardListBeans.get(0);
                        }
                        if (accountBean == null && bankCardListBeans.isEmpty()) {
                            ToastUtils.toastMsg("请先绑定支付宝账号或者银行卡");
                            finish();
                            return;
                        }
                        if (accountBean != null) {
                            _tiXianType = 0;
                            binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setText("支付宝");
                            binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText(accountBean.phone);
                            return;
                        }
                        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setText("银行卡");
                        _tiXianType = 1;
                        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText(selectBankBean.getShowText());

//                        if (accountBean != null && bankCardListBeans.isEmpty()) {
//                            binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
//                            binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgTv.setText(accountBean.phone);
//                        }
//                        if (accountBean == null && !bankCardListBeans.isEmpty()) {
//                            selectBankBean = bankCardListBeans.get(0);
//                            binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
//                            binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgTv.setText(selectBankBean.certNo + selectBankBean.phone);
//                        }
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
        } else if (v == binding.activityMinePurseTixianType.viewTitleTfWithoutBgLl || v == binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt) {
            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), new String[]{"支付宝", "银行卡"}, new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        if (accountBean == null) {
                            ToastUtils.toastMsg("请绑定支付宝账号");
                            return;
                        }
                        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setText("支付宝");
                        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText(accountBean.phone);
                        _tiXianType = 0;
                    } else if (type == 2) {
                        if (bankCardListBeans == null || bankCardListBeans.isEmpty()) {
                            ToastUtils.toastMsg("请绑定银行卡");
                            return;
                        }
                        binding.activityMinePurseTixianType.viewTitleTfWithoutBgEt.setText("银行卡");
                        _tiXianType = 1;
                        selectBankBean = bankCardListBeans.get(0);
                        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText(selectBankBean.getShowText());


                    }
                }
            });
        } else if (v == binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgLl || v == binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt) {
            if (_tiXianType == 0) {
                return;
            }
            if (bankCardListBeans == null || bankCardListBeans.isEmpty()) {
                ToastUtils.toastMsg("请绑定银行卡");
                return;
            }
            ArrayList<String> arrayList = new ArrayList<>();
            for (BankCardListBean tempBean : bankCardListBeans) {
                arrayList.add(tempBean.getShowText());
            }
            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(),arrayList.toArray(new String[arrayList.size()]), new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type != 0) {
                        selectBankBean = bankCardListBeans.get(type - 1);
                        binding.activityMinePurseTixianDaozhang.viewTitleTfWithoutBgEt.setText(selectBankBean.getShowText());

                    }
                }
            });
        }
    }

}
