package com.turunsi.yaoxin.main.mine.purse.bankcard;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseBankListAddBinding;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

public class PurseBankListAddActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseBankListAddBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseBankListAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseBankCardListAddNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePurseBankCardListAddNextRl.setOnClickListener(this);

        _initSetCell();
    }
    private void _initSetCell() {
        binding.activityMinePurseBankListAddKaihuming.viewTitleTfWithoutBgTv.setText("持卡人");
        binding.activityMinePurseBankListAddKahao.viewTitleTfWithoutBgTv.setText("银行卡号");
        binding.activityMinePurseBankListAddKaihuhang.viewTitleTfWithoutBgTv.setText("开户银行");
        binding.activityMinePurseBankListAddUsdt.viewTitleTfWithoutBgTv.setText("预留手机号");

        binding.activityMinePurseBankListAddKaihuming.viewTitleTfWithoutBgEt.setHint("请输入持卡人");
        binding.activityMinePurseBankListAddKahao.viewTitleTfWithoutBgEt.setHint("请输入银行卡号");
        binding.activityMinePurseBankListAddKaihuhang.viewTitleTfWithoutBgEt.setHint("请输入开户银行");
        binding.activityMinePurseBankListAddUsdt.viewTitleTfWithoutBgEt.setHint("请输入预留手机号");

        binding.activityMinePurseBankListAddKahao.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityMinePurseBankListAddUsdt.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);

    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseBankCardListAddNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseBankCardListAddNextRl) {
            String kaihuming = getTextStr(binding.activityMinePurseBankListAddKaihuming.viewTitleTfWithoutBgEt);
            String kahao = getTextStr(binding.activityMinePurseBankListAddKahao.viewTitleTfWithoutBgEt);
            String kaihuhang = getTextStr(binding.activityMinePurseBankListAddKaihuhang.viewTitleTfWithoutBgEt);
            String usdt = getTextStr(binding.activityMinePurseBankListAddUsdt.viewTitleTfWithoutBgEt);
            if (kaihuming.isEmpty()) {
                ToastUtils.toastMsg("请输入开户名");
                return;
            }
            if (kahao.isEmpty()) {
                ToastUtils.toastMsg("请输入卡号");
                return;
            }
            if (kaihuhang.isEmpty()) {
                ToastUtils.toastMsg("请输入开户行");
                return;
            }
            if (usdt.isEmpty()) {
                ToastUtils.toastMsg("请输入预留电话");
                return;
            }
            RequestParamsBean registerBean = new RequestParamsBean(kahao,kaihuming,"3");
            registerBean.certNo = kaihuhang;
            registerBean.usdt = usdt;

            HttpUtil.apiW().bindCard_createUptadeZFB1(registerBean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg(body.msg);
                            EventBus.getDefault().post(new BaseEvent("refresh_bank_list"));
                            finish();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        }
    }

}
