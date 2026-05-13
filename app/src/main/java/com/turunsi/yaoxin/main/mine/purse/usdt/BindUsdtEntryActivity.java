package com.turunsi.yaoxin.main.mine.purse.usdt;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.databinding.ActivityBindUsdtEntryBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * USDT 绑定入口，对应归档 {@code SWUsdtBindEntryViewController}：
 * {@code POST /bindCard/userZFB}，查询 USDT 绑定时 type=5（以后台为准）。
 */
public class BindUsdtEntryActivity extends BaseActivity implements View.OnClickListener {

    private static final int API_TYPE_USDT = 5;

    private ActivityBindUsdtEntryBinding binding;
    private UserBean bindBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBindUsdtEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityBindUsdtEntryNav);
        binding.activityBindUsdtEntryNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBindUsdtEntryRebindTv.setOnClickListener(this);
        binding.activityBindUsdtEntryGotoBindTv.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBindInfo();
    }
    
    private void loadBindInfo() {
        RegisterBean bean = new RegisterBean();
        bean.type = API_TYPE_USDT;
        HttpUtil.apiW().bindCard_userZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        if (body == null || body.data == null) {
                            bindBean = null;
                            refreshUi();
                            return;
                        }
                        Type type = new TypeToken<List<UserBean>>() {
                        }.getType();
                        List<UserBean> list = new Gson().fromJson(body.data.toString(), type);
                        bindBean = pickBindRecord(list);
                        refreshUi();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        bindBean = null;
                        refreshUi();
                    }
                });
    }

    private static UserBean pickBindRecord(List<UserBean> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    private void refreshUi() {
        boolean bound = bindBean != null;
        binding.activityBindUsdtEntryInfoLl.setVisibility(bound ? View.VISIBLE : View.GONE);
        binding.activityBindUsdtEntryEmptyLl.setVisibility(bound ? View.GONE : View.VISIBLE);
        if (bound) {
            String addr = bindBean.usdt != null ? bindBean.usdt.trim() : "";
            binding.activityBindUsdtEntryAddressTv.setText(addr.isEmpty() ? "--" : addr);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBindUsdtEntryNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityBindUsdtEntryGotoBindTv) {
            BindUsdtActivity.start(BindUsdtActivity.class, this, null);
        } else if (v == binding.activityBindUsdtEntryRebindTv) {
            Map<String, String> map = new HashMap<>();
            if (bindBean != null) {
                if (!TextUtils.isEmpty(bindBean.cardId)) {
                    map.put(BindUsdtActivity.EXTRA_BIND_ID, bindBean.cardId);
                }
                if (!TextUtils.isEmpty(bindBean.usdt)) {
                    map.put(BindUsdtActivity.EXTRA_INITIAL_USDT, bindBean.usdt);
                }
            }
            BindUsdtActivity.start(BindUsdtActivity.class, this, map);
        }
    }
}
