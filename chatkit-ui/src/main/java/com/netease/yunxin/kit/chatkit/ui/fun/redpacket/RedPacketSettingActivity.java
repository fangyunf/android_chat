package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.fun.auth.AuthHelper;
import com.netease.yunxin.kit.chatkit.ui.fun.auth.AuthStore;
import com.netease.yunxin.kit.chatkit.ui.fun.fan.FanUserListActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

public class RedPacketSettingActivity extends BaseActivity implements View.OnClickListener {

    private static final int COLOR_GREEN = Color.parseColor("#58BE6B");
    private static final int COLOR_TAB_UNSELECTED = Color.parseColor("#CCFFFFFF");

    private RedPacketAutoConfig config;
    private RedPacketAutoManager manager;
    private AuthStore authStore;

    private TextView tabBasic;
    private TextView tabSend;
    private TextView tabGrab;
    private TextView tabFan;
    private View tabBasicIndicator;
    private View tabSendIndicator;
    private View tabGrabIndicator;
    private View tabFanIndicator;
    private LinearLayout basicPanel;
    private LinearLayout sendPanel;
    private LinearLayout grabPanel;
    private LinearLayout fanPanel;

    private TextView authHintTv;
    private TextView authValueTv;
    private SwitchCompat masterSw;

    private EditText sendIntervalEt;
    private EditText amountsEt;
    private EditText countsEt;
    private EditText greetingsEt;
    private EditText payPasswordEt;
    private EditText grabDelayEt;
    private SwitchCompat grabExclusiveSw;
    private SwitchCompat grabLuckySw;
    private SwitchCompat voiceSw;
    private SwitchCompat fanPreciseSw;
    private EditText fanIntervalEt;
    private TextView fanDescTv;
    private TextView saveTv;

    private int currentTab = 0;

    public static void start(Context context) {
        Intent intent = new Intent(context, RedPacketSettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_red_packet_setting);
        config = RedPacketAutoManager.get().getConfig();
        manager = RedPacketAutoManager.get();
        authStore = AuthStore.get();

        View header = findViewById(R.id.red_packet_setting_header);
        header.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        getWindow().setStatusBarColor(COLOR_GREEN);
        StatusBarUtils.setStatusBarLightMode(this, false, false);
        findViewById(R.id.red_packet_setting_back).setOnClickListener(v -> finish());

        tabBasic = findViewById(R.id.red_packet_tab_basic);
        tabSend = findViewById(R.id.red_packet_tab_send);
        tabGrab = findViewById(R.id.red_packet_tab_grab);
        tabFan = findViewById(R.id.red_packet_tab_fan);
        tabBasicIndicator = findViewById(R.id.red_packet_tab_basic_indicator);
        tabSendIndicator = findViewById(R.id.red_packet_tab_send_indicator);
        tabGrabIndicator = findViewById(R.id.red_packet_tab_grab_indicator);
        tabFanIndicator = findViewById(R.id.red_packet_tab_fan_indicator);
        basicPanel = findViewById(R.id.red_packet_basic_panel);
        sendPanel = findViewById(R.id.red_packet_send_panel);
        grabPanel = findViewById(R.id.red_packet_grab_panel);
        fanPanel = findViewById(R.id.red_packet_fan_panel);

        authHintTv = findViewById(R.id.red_packet_auth_hint_tv);
        authValueTv = findViewById(R.id.red_packet_auth_value_tv);
        masterSw = findViewById(R.id.red_packet_master_sw);

        sendIntervalEt = findViewById(R.id.red_packet_send_interval_et);
        amountsEt = findViewById(R.id.red_packet_amounts_et);
        countsEt = findViewById(R.id.red_packet_counts_et);
        greetingsEt = findViewById(R.id.red_packet_greetings_et);
        payPasswordEt = findViewById(R.id.red_packet_pay_password_et);
        grabDelayEt = findViewById(R.id.red_packet_grab_delay_et);
        grabExclusiveSw = findViewById(R.id.red_packet_grab_exclusive_sw);
        grabLuckySw = findViewById(R.id.red_packet_grab_lucky_sw);
        voiceSw = findViewById(R.id.red_packet_voice_sw);
        fanPreciseSw = findViewById(R.id.red_packet_fan_precise_sw);
        fanIntervalEt = findViewById(R.id.red_packet_fan_interval_et);
        fanDescTv = findViewById(R.id.red_packet_fan_desc_tv);
        saveTv = findViewById(R.id.red_packet_setting_save_tv);

        findViewById(R.id.red_packet_tab_basic_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_tab_send_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_tab_grab_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_tab_fan_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_auth_row).setOnClickListener(this);
        findViewById(R.id.red_packet_fan_normal_row).setOnClickListener(this);
        findViewById(R.id.red_packet_fan_precise_row).setOnClickListener(this);
        findViewById(R.id.red_packet_fan_custom_row).setOnClickListener(this);
        saveTv.setOnClickListener(this);

        masterSw.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (!buttonView.isPressed()) {
                        return;
                    }
                    if (isChecked && !authStore.isAuthorized()) {
                        buttonView.setChecked(false);
                        AuthHelper.requireAuth(this);
                        return;
                    }
                    authStore.setMasterSwitch(isChecked);
                    if (!isChecked) {
                        manager.stopAll(null);
                    }
                });

        loadConfig();
        showTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAuthRow();
    }

    private void loadConfig() {
        masterSw.setChecked(authStore.isMasterSwitchOn());
        refreshAuthRow();
        sendIntervalEt.setText(String.valueOf(config.getSendInterval()));
        amountsEt.setText(config.getAmounts());
        countsEt.setText(config.getCounts());
        greetingsEt.setText(config.getGreetings());
        payPasswordEt.setText(config.getPayPassword());
        grabDelayEt.setText(String.valueOf(config.getGrabDelay()));
        grabExclusiveSw.setChecked(config.isGrabExclusiveEnabled());
        grabLuckySw.setChecked(config.isGrabLuckyEnabled());
        voiceSw.setChecked(config.isVoiceAnnounceEnabled());
        fanPreciseSw.setChecked(config.isAddPreciseCrowdEnabled());
        fanIntervalEt.setText(String.valueOf(config.getFanIntervalSec()));
        refreshFanDesc();
    }

    private void refreshAuthRow() {
        authHintTv.setText(authStore.authPlaceholder());
        authValueTv.setText(authStore.authDisplayValue());
        masterSw.setChecked(authStore.isMasterSwitchOn());
    }

    private void refreshFanDesc() {
        int interval = config.getFanIntervalSec();
        try {
            String raw = fanIntervalEt.getText().toString().trim();
            if (!TextUtils.isEmpty(raw)) {
                interval = Math.max(5, Integer.parseInt(raw));
            }
        } catch (Exception ignored) {
        }
        fanDescTv.setText(
                "1⃣️普通人群=点一下群里的「粉」保存当前群成员\n"
                        + "2⃣️精准人群=打开开关后自动监控账号所在全部群，保存所有群发包人（不必盯着某一个群）\n"
                        + "3⃣️如果需要大小号切换-请先用大号添加好数据后：- 直接切换账号到小号在爆粉区进行爆粉\n"
                        + "4⃣️平台限制，每"
                        + interval
                        + "秒添加1个好友");
    }

    private void showTab(int tab) {
        currentTab = tab;
        basicPanel.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        sendPanel.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        grabPanel.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);
        fanPanel.setVisibility(tab == 3 ? View.VISIBLE : View.GONE);
        // 基本页保存总开关即时生效，仍显示保存按钮用于其它页
        saveTv.setVisibility(tab == 0 ? View.GONE : View.VISIBLE);

        applyTabStyle(tabBasic, tabBasicIndicator, tab == 0);
        applyTabStyle(tabSend, tabSendIndicator, tab == 1);
        applyTabStyle(tabGrab, tabGrabIndicator, tab == 2);
        applyTabStyle(tabFan, tabFanIndicator, tab == 3);
        if (tab == 0) {
            refreshAuthRow();
        } else if (tab == 3) {
            refreshFanDesc();
        }
    }

    private void applyTabStyle(TextView title, View indicator, boolean selected) {
        title.setTextColor(selected ? Color.WHITE : COLOR_TAB_UNSELECTED);
        title.getPaint().setFakeBoldText(selected);
        indicator.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.red_packet_tab_basic_wrap) {
            showTab(0);
        } else if (id == R.id.red_packet_tab_send_wrap) {
            showTab(1);
        } else if (id == R.id.red_packet_tab_grab_wrap) {
            showTab(2);
        } else if (id == R.id.red_packet_tab_fan_wrap) {
            showTab(3);
        } else if (id == R.id.red_packet_auth_row) {
            if (authStore.isAuthorized()) {
                ToastUtils.toastMsg("已授权，到期：" + authStore.authDisplayValue());
                return;
            }
            AuthHelper.showActivate(this, this::refreshAuthRow);
        } else if (id == R.id.red_packet_fan_normal_row) {
            if (!AuthHelper.requireAuth(this)) {
                return;
            }
            FanUserListActivity.start(this, FanUserListActivity.MODE_NORMAL);
        } else if (id == R.id.red_packet_fan_precise_row) {
            if (!AuthHelper.requireAuth(this)) {
                return;
            }
            FanUserListActivity.start(this, FanUserListActivity.MODE_PRECISE);
        } else if (id == R.id.red_packet_fan_custom_row) {
            if (!AuthHelper.requireAuth(this)) {
                return;
            }
            FanUserListActivity.start(this, FanUserListActivity.MODE_CUSTOM);
        } else if (id == R.id.red_packet_setting_save_tv) {
            saveCurrentTab();
        }
    }

    private void saveCurrentTab() {
        if (currentTab == 1) {
            String intervalStr = sendIntervalEt.getText().toString().trim();
            double interval = 1;
            if (!TextUtils.isEmpty(intervalStr)) {
                try {
                    interval = Double.parseDouble(intervalStr);
                } catch (Exception ignored) {
                }
            }
            config.setSendInterval(interval);
            config.setAmounts(amountsEt.getText().toString().trim());
            config.setCounts(countsEt.getText().toString().trim());
            config.setGreetings(greetingsEt.getText().toString().trim());
            config.setPayPassword(payPasswordEt.getText().toString().trim());
            ToastUtils.toastMsg("发红包设置已保存");
        } else if (currentTab == 2) {
            String delayStr = grabDelayEt.getText().toString().trim();
            double delay = 0;
            if (!TextUtils.isEmpty(delayStr)) {
                try {
                    delay = Double.parseDouble(delayStr);
                } catch (Exception ignored) {
                }
            }
            config.setGrabDelay(delay);
            config.setGrabExclusiveEnabled(grabExclusiveSw.isChecked());
            config.setGrabLuckyEnabled(grabLuckySw.isChecked());
            config.setVoiceAnnounceEnabled(voiceSw.isChecked());
            manager.onGrabSettingsSaved();
            ToastUtils.toastMsg("抢红包设置已保存");
        } else if (currentTab == 3) {
            int fanInterval = 5;
            try {
                String raw = fanIntervalEt.getText().toString().trim();
                if (!TextUtils.isEmpty(raw)) {
                    fanInterval = Integer.parseInt(raw);
                }
            } catch (Exception ignored) {
            }
            config.setFanIntervalSec(fanInterval);
            config.setAddPreciseCrowdEnabled(fanPreciseSw.isChecked());
            refreshFanDesc();
            ToastUtils.toastMsg("爆粉设置已保存");
        }
    }
}
