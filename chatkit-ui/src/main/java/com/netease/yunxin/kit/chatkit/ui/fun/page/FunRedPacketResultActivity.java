package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunRedPacketResultDetailBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendRedPacketBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.adapter.RedPacketResultDetailAdapter;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunOpenRedPacketFragment;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.actionsheet.ActionSheet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunRedPacketResultActivity extends BaseActivity implements View.OnClickListener {
    ActivityFunRedPacketResultDetailBinding binding;
    String redpacketId = "";
    RedPacketResultDetailAdapter adapter = new RedPacketResultDetailAdapter();

    CustomMsgBean redBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (extras != null && extras.get("redpacketId") != null) {
            redpacketId = (String) extras.get("redpacketId");
        }
        _requestData1();
        binding = ActivityFunRedPacketResultDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();

        StatusBarUtils.setStatusBarLightMode(this, true, true);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) binding.activityFunRedPacketResultDetailNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityFunRedPacketResultDetailNav.setLayoutParams(params);
        binding.activityFunRedPacketResultDetailNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        binding.activityFunRedPacketResultDetailNav.setActionText("红包记录");

        binding.activityFunRedPacketResultDetailNav.setActionClickListener(() -> {
            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class, this, null);
        });
    }

    protected void _requestData1() {
        if (redpacketId == null || redpacketId.isEmpty()) {
            ToastUtils.toastMsg("网络错误");
            finish();
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.redpacketId = redpacketId;
        HttpUtil.apiW().red_redpacketDetail(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        redBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                        List<CustomMsgBean> claimRecords = buildClaimRecords(redBean);
                        if (!claimRecords.isEmpty() && claimRecords.size() == parseTotalNum(redBean.totalNum)) {
                            double maxMoeny = 0;
                            int index = 0;
                            int bestIndex = 0;
                            for (CustomMsgBean tempBean : claimRecords) {
                                if (Double.parseDouble(tempBean.amount) > maxMoeny) {
                                    maxMoeny = Double.parseDouble(tempBean.amount);
                                    bestIndex = index;
                                }
                                index++;
                            }
                            claimRecords.get(bestIndex).isBest = true;
                        }

                        adapter.setItems(claimRecords);
                        adapter.notifyDataSetChanged();
                        _updateUI();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });

    }

    void _updateUI() {
        GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunRedPacketResultDetailSenderHeadIv, redBean.sendAvatar, 17);
        binding.activityFunRedPacketResultDetailSenderTv.setText(redBean.sendName);
        binding.activityFunRedPacketResultDetailGreetingTv.setText(redBean.title);
        if (redBean.redpacketType == 21) {
            binding.activityFunRedPacketResultDetailSenderTv.setText(redBean.sendName + "发出的专属红包");

        } else if (redBean.redpacketType == 22) {
            binding.activityFunRedPacketResultDetailSenderTv.setText(redBean.sendName + "发出的个人红包");
        } else if (redBean.redpacketType == 23) {
            binding.activityFunRedPacketResultDetailSenderTv.setText(redBean.sendName + "发出的拼手气红包");

        }
        List<CustomMsgBean> claimRecords = buildClaimRecords(redBean);
        if (redBean.type == 5 && redBean.redpacketType != 22) {
            binding.activityFunRedPacketResultDetailMoneyTv.setText(NumberUtil.formartMoney(redBean.sendAmount));
            binding.activityFunRedPacketResultDetailBottomLl.setVisibility(View.GONE);
        } else {
            binding.activityFunRedPacketResultDetailBottomLl.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(redBean.lootAll)) {
                binding.activityFunRedPacketResultDetailRvDetailTv.setText("已领取" + claimRecords.size() + "/" + parseTotalNum(redBean.totalNum) + "个  共" + NumberUtil.formartMoney(redBean.sendAmount) + "元");
            } else {
                binding.activityFunRedPacketResultDetailRvDetailTv.setText(redBean.totalNum + "个红包共" + NumberUtil.formartMoney(redBean.sendAmount) + "元，" + redBean.lootAll + "被抢光");
            }
            boolean hasme = false;
            for (CustomMsgBean tempBean : claimRecords) {
                if (DataUtil.getUserid().equals(tempBean.userId)) {
                    hasme = true;
                    binding.activityFunRedPacketResultDetailMoneyTv.setVisibility(View.VISIBLE);
                    binding.activityFunRedPacketResultDetailMoneyTv.setText(NumberUtil.formartMoney(tempBean.amount));
                    break;
                }
            }
            if (!hasme) {
                if (redBean.redpacketType == 22 && !claimRecords.isEmpty()) {
                    binding.activityFunRedPacketResultDetailMoneyTv.setVisibility(View.GONE);
                } else {
                    binding.activityFunRedPacketResultDetailMoneyTv.setVisibility(View.GONE);
                    binding.activityFunRedPacketResultDetailMoneyTv.setText("手慢了，已抢完");
                }
            }
        }
    }

    private List<CustomMsgBean> buildClaimRecords(CustomMsgBean redBean) {
        if (redBean == null) {
            return new ArrayList<>();
        }
        if (redBean.vos != null && !redBean.vos.isEmpty()) {
            return redBean.vos;
        }
        if (redBean.redpacketType == 22) {
            CustomMsgBean claimRecord = new CustomMsgBean();
            if (!TextUtils.isEmpty(redBean.userId)) {
                claimRecord.userId = redBean.userId;
                claimRecord.name = !TextUtils.isEmpty(redBean.name) ? redBean.name : redBean.receiveUserName;
            } else if (!TextUtils.isEmpty(redBean.receiveUserId)) {
                claimRecord.userId = redBean.receiveUserId;
                claimRecord.name = redBean.receiveUserName;
            } else {
                return new ArrayList<>();
            }
            claimRecord.avatar = redBean.avatar;
            claimRecord.amount = !TextUtils.isEmpty(redBean.reciveAmount) ? redBean.reciveAmount : redBean.amount;
            claimRecord.reciveTime = redBean.reciveTime;
            List<CustomMsgBean> claimRecords = new ArrayList<>();
            claimRecords.add(claimRecord);
            return claimRecords;
        }
        return new ArrayList<>();
    }

    private int parseTotalNum(String totalNum) {
        if (TextUtils.isEmpty(totalNum)) {
            return 1;
        }
        try {
            return Integer.parseInt(totalNum);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    protected void _initView() {

        binding.activityFunRedPacketResultDetailNav.addCloseImageButton().setOnClickListener(this);
//        binding.activityFunRedPacketResultDetailRedPacketRecordTv.setOnClickListener(this);
//        binding.activityFunRedPacketResultDetailNav.setActionText("红包记录");
        binding.activityFunRedPacketResultDetailRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityFunRedPacketResultDetailRv.setAdapter(adapter);


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
