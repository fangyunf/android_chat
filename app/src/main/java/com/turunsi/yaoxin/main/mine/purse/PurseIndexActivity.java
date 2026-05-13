package com.turunsi.yaoxin.main.mine.purse;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketRecordListActivity;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.mine.purse.alipay.BindAlipayActivity;
import com.turunsi.yaoxin.main.mine.purse.bill.BillDetailListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianAddAccountActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseIndexBinding;
import com.turunsi.yaoxin.main.mine.purse.bankcard.BankCardListActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.PurseRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.recharge.UsdtRechargeActivity;
import com.turunsi.yaoxin.main.mine.purse.usdt.BindUsdtEntryActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.PurseTiXianActivity;
import com.turunsi.yaoxin.main.mine.purse.tixian.UsdtWithdrawActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.ICallBack;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class PurseIndexActivity extends BaseActivity implements View.OnClickListener {

    private static final int PAY_CHANNEL_NONE = 0;
    private static final int PAY_CHANNEL_WP = 1;
    private static final int PAY_CHANNEL_USDT = 2;

    /** 选中卡片不透明，未选中半透明 */
    private static final float PAY_CARD_ALPHA_SELECTED = 1f;
    private static final float PAY_CARD_ALPHA_UNSELECTED = 0.48f;

    ActivityMinePurseIndexBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseIndexNav.addCloseImageButton().setOnClickListener(this);

        StatusBarUtils.setStatusBarLightMode(this, true, true);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) binding.activityMinePurseIndexNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityMinePurseIndexNav.setLayoutParams(params);
        binding.activityMinePurseIndexNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        _initCell();
        binding.activityMinePurseIndexRechargeTv.setOnClickListener(this);
        binding.activityMinePurseIndexRechargeTv2.setOnClickListener(this);
        binding.activityMinePurseIndexTixianTv.setOnClickListener(this);
        Context that = this;
        binding.activityMinePurseIndexNav.setActionClickListener(new ICallBack() {
            @Override
            public void callBack() {
                BillDetailListActivity.start(BillDetailListActivity.class, that, null);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        _requestData();
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                        binding.activityMinePurseIndexBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void _initCell() {

        binding.activityMinePurseIndexHbjl.setOnClickListener(this);
        binding.activityMinePurseIndexLqmx.setOnClickListener(this);
        binding.activityMinePurseIndexSmrz.setOnClickListener(this);
        binding.activityMinePurseIndexWdkb.setOnClickListener(this);
        binding.activityMinePurseIndexBdwx.setOnClickListener(this);
        binding.activityMinePurseIndexBdzfb.setOnClickListener(this);
        binding.activityMinePurseIndexUstd.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseIndexNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseIndexRechargeTv) {
            showPayChannelDialog(true);
        } else if (v == binding.activityMinePurseIndexRechargeTv2) {
            Map map = new HashMap();
            map.put("type", "1");
            PurseRechargeActivity.start(PurseRechargeActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexTixianTv) {
            showPayChannelDialog(false);
        } else if (v == binding.activityMinePurseIndexHbjl) {
//            BillDetailListActivity.start(BillDetailListActivity.class,this,null);
            FunRedPacketRecordListActivity.start(FunRedPacketRecordListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexLqmx) {
            BillDetailListActivity.start(BillDetailListActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdzfb) {
            BindAlipayActivity.start(BindAlipayActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexUstd) {
            BindUsdtEntryActivity.start(BindUsdtEntryActivity.class, this, null);
        } else if (v == binding.activityMinePurseIndexBdwx) {
            Map map = new HashMap();
            map.put("type", "1");
            BindAlipayActivity.start(BindAlipayActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexWdkb) {

            Map map = new HashMap();
            map.put("type", "3");
            BindAlipayActivity.start(BindAlipayActivity.class, this, map);
        } else if (v == binding.activityMinePurseIndexSmrz) {
            ToastUtils.toastMsg("已完成实名");
//            if (!Constant.isRunningRealName) {
//                Constant.isRunningRealName = true;
//                XKitRouter.withKey(Constant.RealName_Router)
//                        .withContext(AppProxy.getInstance().getContext())
//                        .navigate();
//            }
        }
//        else if (v == binding.activityMinePurseIndexCell8.itemPurseIndexCellRl) {
//            HashMap map = new HashMap();
//            map.put("type","0");
//            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
//        } else if (v == binding.activityMinePurseIndexCell9.itemPurseIndexCellRl) {
//            HashMap map = new HashMap();
//            map.put("type","1");
//            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
//        }
    }

    /**
     * 底部选择支付方式：{@code ic_wp}（微信/支付宝）、{@code ic_usdt}（USDT）。
     *
     * @param isRecharge true 充值（去充值），false 提现（去提现）
     */
    private void showPayChannelDialog(boolean isRecharge) {
        final Dialog dialog = new Dialog(this, R.style.custom_dlg);
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_purse_pay_channel, null, false);
        dialog.setContentView(content);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout cardWp = content.findViewById(R.id.dialog_purse_pay_channel_wp);
        LinearLayout cardUsdt = content.findViewById(R.id.dialog_purse_pay_channel_usdt);
        ImageView ivWp = content.findViewById(R.id.dialog_purse_pay_channel_wp_iv);
        ImageView ivUsdt = content.findViewById(R.id.dialog_purse_pay_channel_usdt_iv);
        TextView confirm = content.findViewById(R.id.dialog_purse_pay_channel_confirm);
        confirm.setText(
                isRecharge ? R.string.purse_pay_channel_go_recharge : R.string.purse_pay_channel_go_withdraw);

        final int[] selected = {PAY_CHANNEL_WP};
        cardWp.setOnClickListener(v -> {
            selected[0] = PAY_CHANNEL_WP;
            cardWp.setSelected(true);
            cardUsdt.setSelected(false);
            applyPayChannelIconAlpha(ivWp, ivUsdt, selected[0]);
        });
        cardUsdt.setOnClickListener(v -> {
            selected[0] = PAY_CHANNEL_USDT;
            cardWp.setSelected(false);
            cardUsdt.setSelected(true);
            applyPayChannelIconAlpha(ivWp, ivUsdt, selected[0]);
        });
        cardWp.setSelected(true);
        cardUsdt.setSelected(false);
        applyPayChannelIconAlpha(ivWp, ivUsdt, PAY_CHANNEL_WP);

        confirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (isRecharge) {
                if (selected[0] == PAY_CHANNEL_WP) {
                    PurseRechargeActivity.start(PurseRechargeActivity.class, PurseIndexActivity.this, null);
                } else {
                    UsdtRechargeActivity.start(UsdtRechargeActivity.class, PurseIndexActivity.this, null);
                }
            } else {
                if (selected[0] == PAY_CHANNEL_WP) {
                    PurseTiXianActivity.start(PurseTiXianActivity.class, PurseIndexActivity.this, null);
                } else {
                    UsdtWithdrawActivity.start(UsdtWithdrawActivity.class, PurseIndexActivity.this, null);
                }
            }
        });

        dialog.show();
    }

    /** 仅通过 {@link ImageView#setImageAlpha(int)}：选中 255，未选中半透明；{@link #PAY_CHANNEL_NONE} 时两图均为 255。 */
    private static void applyPayChannelIconAlpha(ImageView ivWp, ImageView ivUsdt, int selectedChannel) {
        final int opaque = 255;
        final int dimmed = 110;
        if (selectedChannel == PAY_CHANNEL_NONE) {
            ivWp.setImageAlpha(opaque);
            ivUsdt.setImageAlpha(opaque);
        } else if (selectedChannel == PAY_CHANNEL_WP) {
            ivWp.setImageAlpha(opaque);
            ivUsdt.setImageAlpha(dimmed);
        } else {
            ivWp.setImageAlpha(dimmed);
            ivUsdt.setImageAlpha(opaque);
        }
    }

}
