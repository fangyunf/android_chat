package com.turunsi.yaoxin.main.mine.purse.usdt;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityBindUsdtBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 绑定 / 修改 USDT（TRC20）地址，对应归档 {@code SWUsdtBindViewController}：
 * {@code POST /bindCard/createUptadeZFB}，type=1，usdt 为地址。
 */
public class BindUsdtActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_BIND_ID = "bind_id";
    public static final String EXTRA_INITIAL_USDT = "initial_usdt";

    /** 与 iOS 一致的绑定类型 */
    private static final String BIND_TYPE_USDT = "1";

    private ActivityBindUsdtBinding binding;
    private long bindId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBindUsdtBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (extras != null) {
            String idStr = extras.getString(EXTRA_BIND_ID);
            if (idStr != null && !idStr.isEmpty()) {
                try {
                    bindId = Long.parseLong(idStr);
                } catch (NumberFormatException ignored) {
                    bindId = 0;
                }
            }
            String initial = extras.getString(EXTRA_INITIAL_USDT);
            if (initial != null && !initial.isEmpty()) {
                binding.activityBindUsdtAddress.viewTitleTfWithoutBgEt.setText(initial);
            }
        }

        boolean isEdit = bindId > 0;
        binding.activityBindUsdtNav.getTitleView().setText(isEdit ? "修改USDT绑定" : "绑定USDT");
        binding.activityBindUsdtAddress.viewTitleTfWithoutBgTv.setText("USDT地址");
        binding.activityBindUsdtAddress.viewTitleTfWithoutBgEt.setHint("请输入TRC20收款地址");

        binding.activityBindUsdtNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBindUsdtConfirmTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBindUsdtNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityBindUsdtConfirmTv) {
            submit();
        }
    }

    private void submit() {
        String addr = getTextStr(binding.activityBindUsdtAddress.viewTitleTfWithoutBgEt).trim();
        if (addr.isEmpty()) {
            ToastUtils.toastMsg("请输入USDT地址");
            return;
        }
        RequestParamsBean registerBean = new RequestParamsBean("", "", BIND_TYPE_USDT);
        registerBean.type = BIND_TYPE_USDT;
        registerBean.usdt = addr;
        if (bindId > 0) {
            registerBean.id = String.valueOf(bindId);
        }
        HttpUtil.apiW().bindCard_createUptadeZFB1(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }
}
