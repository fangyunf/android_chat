package com.turunsi.yaoxin.main.mine.huiyuan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.databinding.ActivityLianghaoZoneBinding;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.LiangHaoGroupAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

public class LiangHaoZoneActivity extends BaseActivity {
    ActivityLianghaoZoneBinding binding;
    private HuiYuanBean dataBean;
    private LiangHaoGroupAdapter groupAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLianghaoZoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventBus.getDefault().register(this);
        binding.activityMineMyHuiyuanListNav.addCloseImageButton().setOnClickListener(view -> finish());
        initView();
        loadData();
    }

    private void initView() {
        binding.rvGroup.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new LiangHaoGroupAdapter();
        binding.rvGroup.setAdapter(groupAdapter);
        binding.layoutLiangHaoLast.setOnClickListener(view -> {
            ExchangeLiangHaoDialogFragment dialog = new ExchangeLiangHaoDialogFragment()
                    .setOnExchangeListener((code, level) -> {
                        if (dataBean == null || dataBean.list == null || dataBean.list.isEmpty()) {
                            ToastUtils.toastMsg("数据加载中，请稍后重试");
                            return;
                        }
                        HuiYuanBean target = null;
                        for (HuiYuanBean item : dataBean.list) {
                            if (item != null && item.memberConfig != null && level.equals(item.memberConfig.memberLevel)) {
                                target = item;
                                break;
                            }
                        }
                        if (target == null) {
                            ToastUtils.toastMsg("请输入正确的靓号等级");
                            return;
                        }
                        Intent intent = new Intent(LiangHaoZoneActivity.this, LiangHaoZoneMoreActivity.class);
                        intent.putExtra("code", code);
                        intent.putExtra("huiyuan", target);
                        startActivity(intent);
                    });
            dialog.showNow(getSupportFragmentManager(), "ExchangeLiangHaoDialog");
        });
        binding.layoutLiangHaoCj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XKitRouter.withKey(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey).withParam("type", "4").withParam("title", "抽奖").withContext(getBaseContext()).navigate();
            }
        });
    }

    private void loadData() {
        HttpUtil.apiW().meteor_list().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                dataBean = new Gson().fromJson(body.data.toString(), HuiYuanBean.class);
                groupAdapter.setItems(dataBean.list);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                // 可加错误提示
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
        if ("reload_fuhao".equals(event.getTag())) {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}