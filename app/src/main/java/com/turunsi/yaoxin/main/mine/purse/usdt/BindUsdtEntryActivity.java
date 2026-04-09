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
 * {@code POST /bindCard/userZFB}，type=1。
 */
public class BindUsdtEntryActivity extends BaseActivity implements View.OnClickListener {

    private static final int API_TYPE_USDT = 1;

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

    /**
     * 与归档 SWWithdrawViewController {@code isUsdtBindReady} 一致：有效 USDT 需地址非空且 cardId 有效。
     */
    private static boolean isUsdtBindReady(UserBean model) {
        if (model == null) {
            return false;
        }
        String addr = model.usdt == null ? "" : model.usdt.trim();
        if (addr.isEmpty()) {
            return false;
        }
        String cid = model.cardId == null ? "" : model.cardId.trim();
        if (TextUtils.equals("zfb", cid)) {
            return false;
        }
        // 归档要求 cardId 有效；若接口只返回主键 id，用 id 兜底
        if (cid.isEmpty() && model.id <= 0) {
            return false;
        }
        return true;
    }

    private void refreshUi() {
        boolean bound = isUsdtBindReady(bindBean);
        binding.activityBindUsdtEntryInfoLl.setVisibility(bound ? View.VISIBLE : View.GONE);
        binding.activityBindUsdtEntryEmptyLl.setVisibility(bound ? View.GONE : View.VISIBLE);
        if (bound && bindBean != null) {
            binding.activityBindUsdtEntryAddressTv.setText(bindBean.usdt);
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
                if (bindBean.id > 0) {
                    map.put(BindUsdtActivity.EXTRA_BIND_ID, String.valueOf(bindBean.id));
                }
                if (!TextUtils.isEmpty(bindBean.usdt)) {
                    map.put(BindUsdtActivity.EXTRA_INITIAL_USDT, bindBean.usdt);
                }
            }
            BindUsdtActivity.start(BindUsdtActivity.class, this, map);
        }
    }
}
