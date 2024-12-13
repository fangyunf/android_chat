package com.turunsi.yaoxin.main.mine.purse.alipay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.databinding.ActivityMineBankCardListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineBindAlipayBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.PurseBankListAddActivity;
import com.turunsi.yaoxin.main.mine.purse.bankcard.adapter.BankCardListAdapter;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.zhihu.matisse.Matisse;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BindAlipayActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineBindAlipayBinding binding;
    String qrcodeImgUrl;
    UserBean bindBean;
    int _type = 0;

    int _bindType = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineBindAlipayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineBindAlipayNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineBindAlipayGotoBindTv.setOnClickListener(this);
        binding.activityMineBindAlipayBindTv.setOnClickListener(this);
        binding.activityMineBindAlipayRebindTv.setOnClickListener(this);
        binding.activityMineBindAlipayBindSuccessTv.setOnClickListener(this);
        binding.activityMineBindAlipayUploadLl.setOnClickListener(this);

        if (extras != null && extras.get("type") != null) {
            String tempType = (String) extras.get("type");
            _bindType = Integer.parseInt(tempType);
        }

        binding.activityMineBindKaihuName.viewTitleTfWithoutBgLl.setVisibility(View.GONE);
        switch (_bindType) {
            case 1:
                binding.activityMineBindAlipayNav.getTitleView().setText("微信");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgTv.setText("微信账号");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgTv.setText("真实姓名");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt.setHint("请输入微信账号");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt.setHint("请输入您的真实姓名");
                break;
            case 2:

                binding.activityMineBindAlipayNav.getTitleView().setText("支付宝");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgTv.setText("支付宝账号");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgTv.setText("真实姓名");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt.setHint("请输入支付宝账号");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt.setHint("请输入您的真实姓名");
                break;
            case 3:
                binding.activityMineBindAlipayNav.getTitleView().setText("银行卡");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgTv.setText("银行卡");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgTv.setText("真实姓名");
                binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt.setHint("请输入银行卡号");
                binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt.setHint("请输入您的真实姓名");
                binding.activityMineBindKaihuName.viewTitleTfWithoutBgTv.setText("开户行");
                binding.activityMineBindKaihuName.viewTitleTfWithoutBgEt.setHint("请输入开户行");
                binding.activityMineBindKaihuName.viewTitleTfWithoutBgLl.setVisibility(View.VISIBLE);
                binding.activityMineBindAlipayUploadLl.setVisibility(View.GONE);
                break;
        }
        _requestData1();
    }

    protected void _requestData1() {
        RegisterBean bean = new RegisterBean();
        bean.type = _bindType;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<UserBean>>() {
                        }.getType();
                        List<UserBean> tempList = new Gson().fromJson(body.data.toString(), type);
//                        bindBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        if (tempList.isEmpty()) {
                            _type = 0;
                        } else {
                            _type = 1;
                            bindBean = tempList.get(0);
                        }
                        updateUI();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    public void updateUI() {

        binding.activityMineBindAlipayGotoBindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayRebindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayBindLl.setVisibility(View.GONE);
        binding.activityMineBindAlipayBindSuccessLl.setVisibility(View.GONE);
        if (_type == 0) {
            binding.activityMineBindAlipayGotoBindLl.setVisibility(View.VISIBLE);
        } else if (_type == 1) {
            if (!bindBean.phone.isEmpty()) {
                binding.activityMineBindAlipayRebindContent.viewTitleDetailTemplateLeftTv.setText("已绑定");
                binding.activityMineBindAlipayRebindContent.viewTitleDetailTemplateRightTv.setText(bindBean.phone);
            }
            binding.activityMineBindAlipayRebindLl.setVisibility(View.VISIBLE);

        } else if (_type == 2) {

            binding.activityMineBindAlipayBindLl.setVisibility(View.VISIBLE);
        } else if (_type == 3) {

            binding.activityMineBindAlipayBindSuccessLl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineBindAlipayNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineBindAlipayGotoBindTv) {
            _type = 2;
            updateUI();
        } else if (v == binding.activityMineBindAlipayRebindTv) {
            _type = 2;
            updateUI();
        } else if (v == binding.activityMineBindAlipayBindTv) {
            String phone = getTextStr(binding.activityMineBindAlipayAccount.viewTitleTfWithoutBgEt);
            String name = getTextStr(binding.activityMineBindAlipayName.viewTitleTfWithoutBgEt);
            String account = getTextStr(binding.activityMineBindKaihuName.viewTitleTfWithoutBgEt);
            if (phone.isEmpty()) {
                ToastUtils.toastMsg("请输入手机号");
                return;
            }
            if (name.isEmpty()) {
                ToastUtils.toastMsg("请输入姓名");
                return;
            }

            if (_bindType == 3) {
                if (account.isEmpty()) {
                    ToastUtils.toastMsg("请输入开户行");
                    return;
                }
            } else {

                if (qrcodeImgUrl == null) {
                    ToastUtils.toastMsg("请上传收款码");
                    return;
                }
            }
            RequestParamsBean registerBean = new RequestParamsBean(phone, name, "2");
            registerBean.type = _bindType + "";
            if (_bindType == 2 || _bindType == 1) {
                registerBean.usdt = qrcodeImgUrl;
            } else {
                registerBean.usdt = account;
            }
            if (bindBean != null && bindBean.id > 0) {
                registerBean.id = bindBean.id + "";
            }
            HttpUtil.apiW().bindCard_createUptadeZFB1(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg(body.msg);
                            _type = 3;
                            updateUI();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        } else if (v == binding.activityMineBindAlipayBindSuccessTv) {
            finish();
        } else if (v == binding.activityMineBindAlipayUploadLl) {
            UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> strings = Matisse.obtainPathResult(data);
            if (!strings.isEmpty()) {
                UploadUtil.uploadImage(strings.get(0), "", new CommonCallBack() {
                    @Override
                    public void onCallBackUserBean(UserBean userBean) {
                        ToastUtils.toastMsg("上传成功");
                        qrcodeImgUrl = userBean.url;
                    }
                });
            }
        }
    }
}
