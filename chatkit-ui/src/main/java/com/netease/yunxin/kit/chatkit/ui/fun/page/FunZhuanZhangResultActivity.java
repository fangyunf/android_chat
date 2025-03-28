package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunRedPacketResultDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

public class FunZhuanZhangResultActivity extends BaseActivity implements View.OnClickListener {
    ActivityFunRedPacketResultDetailBinding binding;
    CustomMsgBean zhuanZhangBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (extras != null && extras.get("bean") != null) {
            String jsonString = (String) extras.get("bean");
            zhuanZhangBean = new Gson().fromJson(jsonString, CustomMsgBean.class);
        }
        binding = ActivityFunRedPacketResultDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        _updateUI();

        StatusBarUtils.setStatusBarLightMode(this, true, true);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) binding.activityFunRedPacketResultDetailNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityFunRedPacketResultDetailNav.setLayoutParams(params);
        binding.activityFunRedPacketResultDetailNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
    }


    void _updateUI() {
        GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunRedPacketResultDetailSenderHeadIv, zhuanZhangBean.sendAvatar, 17);
        binding.activityFunRedPacketResultDetailSenderTv.setText(zhuanZhangBean.sendName);
        if (zhuanZhangBean.toUserId.equals(DataUtil.getUserid())) {
            binding.activityFunRedPacketResultDetailGreetingTv.setText("你收到一笔转账");
        } else {
            binding.activityFunRedPacketResultDetailGreetingTv.setText("你发起一笔转账");
        }

        binding.activityFunRedPacketResultDetailMoneyTv.setText("￥" + NumberUtil.formartMoney(zhuanZhangBean.amount));
    }

    @Override
    protected void _initView() {

        binding.activityFunRedPacketResultDetailNav.getTitleView().setText("");
        binding.activityFunRedPacketResultDetailNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunRedPacketResultDetailBottomLl.setVisibility(View.GONE);
//        binding.activityFunRedPacketResultDetailRedPacketRecordTv.setOnClickListener(this);
//        binding.activityFunRedPacketResultDetailNav.setActionText("红包记录");
//        binding.activityFunRedPacketResultDetailRv.setLayoutManager(new LinearLayoutManager(this));
//        binding.activityFunRedPacketResultDetailRv.setAdapter(adapter);


    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunRedPacketResultDetailNav.addCloseImageButton()) {
            finish();
        }
//        else if (v == binding.activityFunRedPacketResultDetailRedPacketRecordTv) {
//            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class,this,null);
//        }
//        else if (v == binding.activityFunSendRedPacketPinChangeTypeLl) {
//        }
    }

}
