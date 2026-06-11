package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatZhuanZhangHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunRedPacketResultDetailBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

public class FunZhuanZhangResultActivity extends BaseActivity implements View.OnClickListener {
  ActivityFunRedPacketResultDetailBinding binding;
  CustomMsgBean zhuanZhangBean;
  private View closeButton;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (extras != null && extras.get("bean") != null) {
      String jsonString = (String) extras.get("bean");
      zhuanZhangBean = new Gson().fromJson(jsonString, CustomMsgBean.class);
      ChatZhuanZhangHelper.normalizeDetail(zhuanZhangBean);
    }
    binding = ActivityFunRedPacketResultDetailBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    if (zhuanZhangBean == null) {
      Toast.makeText(this, R.string.msg_multi_forward_download_error_tips, Toast.LENGTH_SHORT)
          .show();
      finish();
      return;
    }
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
    if (zhuanZhangBean == null) {
      return;
    }
    if (!TextUtils.isEmpty(zhuanZhangBean.sendAvatar)) {
      GlideUtil.yh_loadImageRoundedCorner(
          this,
          binding.activityFunRedPacketResultDetailSenderHeadIv,
          zhuanZhangBean.sendAvatar,
          17);
    }
    boolean receiver = ChatZhuanZhangHelper.isReceiver(zhuanZhangBean);
    String subtitle = ChatZhuanZhangHelper.getSubtitleText(this, zhuanZhangBean, receiver);
    if (!TextUtils.isEmpty(subtitle)) {
      binding.activityFunRedPacketResultDetailSenderTv.setText(subtitle);
    } else if (!TextUtils.isEmpty(zhuanZhangBean.sendName)) {
      binding.activityFunRedPacketResultDetailSenderTv.setText(zhuanZhangBean.sendName);
    }
    binding.activityFunRedPacketResultDetailGreetingTv.setText(
        ChatZhuanZhangHelper.getTitleText(this, receiver));
    binding.activityFunRedPacketResultDetailMoneyTv.setText(
        "￥" + ChatZhuanZhangHelper.formatAmount(zhuanZhangBean.amount));
  }

  @Override
  protected void _initView() {
    binding.activityFunRedPacketResultDetailNav.getTitleView().setText("");
    closeButton = binding.activityFunRedPacketResultDetailNav.addCloseImageButton();
    closeButton.setOnClickListener(this);
    binding.activityFunRedPacketResultDetailBottomLl.setVisibility(View.GONE);
  }

  @Override
  public void onClick(View v) {
    if (v == closeButton) {
      finish();
    }
  }
}
