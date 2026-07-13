package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

public class RedPacketSettingActivity extends BaseActivity implements View.OnClickListener {

    private static final int COLOR_GREEN = Color.parseColor("#58BE6B");
    private static final int COLOR_TAB_UNSELECTED = Color.parseColor("#CCFFFFFF");

    private RedPacketAutoConfig config;
    private RedPacketAutoManager manager;

    private TextView tabSend;
    private TextView tabGrab;
    private View tabSendIndicator;
    private View tabGrabIndicator;
    private LinearLayout sendPanel;
    private LinearLayout grabPanel;

    private EditText sendIntervalEt;
    private EditText amountsEt;
    private EditText countsEt;
    private EditText greetingsEt;
    private EditText payPasswordEt;
    private EditText grabDelayEt;
    private SwitchCompat grabExclusiveSw;
    private SwitchCompat grabLuckySw;
    private SwitchCompat voiceSw;

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

        View header = findViewById(R.id.red_packet_setting_header);
        header.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        getWindow().setStatusBarColor(COLOR_GREEN);
        StatusBarUtils.setStatusBarLightMode(this, false, false);
        findViewById(R.id.red_packet_setting_back).setOnClickListener(v -> finish());

        tabSend = findViewById(R.id.red_packet_tab_send);
        tabGrab = findViewById(R.id.red_packet_tab_grab);
        tabSendIndicator = findViewById(R.id.red_packet_tab_send_indicator);
        tabGrabIndicator = findViewById(R.id.red_packet_tab_grab_indicator);
        sendPanel = findViewById(R.id.red_packet_send_panel);
        grabPanel = findViewById(R.id.red_packet_grab_panel);

        sendIntervalEt = findViewById(R.id.red_packet_send_interval_et);
        amountsEt = findViewById(R.id.red_packet_amounts_et);
        countsEt = findViewById(R.id.red_packet_counts_et);
        greetingsEt = findViewById(R.id.red_packet_greetings_et);
        payPasswordEt = findViewById(R.id.red_packet_pay_password_et);
        grabDelayEt = findViewById(R.id.red_packet_grab_delay_et);
        grabExclusiveSw = findViewById(R.id.red_packet_grab_exclusive_sw);
        grabLuckySw = findViewById(R.id.red_packet_grab_lucky_sw);
        voiceSw = findViewById(R.id.red_packet_voice_sw);

        findViewById(R.id.red_packet_tab_send_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_tab_grab_wrap).setOnClickListener(this);
        findViewById(R.id.red_packet_setting_save_tv).setOnClickListener(this);

        loadConfig();
        showTab(0);
    }

    private void loadConfig() {
        sendIntervalEt.setText(String.valueOf(config.getSendInterval()));
        amountsEt.setText(config.getAmounts());
        countsEt.setText(config.getCounts());
        greetingsEt.setText(config.getGreetings());
        payPasswordEt.setText(config.getPayPassword());
        grabDelayEt.setText(String.valueOf(config.getGrabDelay()));
        grabExclusiveSw.setChecked(config.isGrabExclusiveEnabled());
        grabLuckySw.setChecked(config.isGrabLuckyEnabled());
        voiceSw.setChecked(config.isVoiceAnnounceEnabled());
    }

    private void showTab(int tab) {
        currentTab = tab;
        boolean send = tab == 0;
        sendPanel.setVisibility(send ? View.VISIBLE : View.GONE);
        grabPanel.setVisibility(send ? View.GONE : View.VISIBLE);

        tabSend.setTextColor(send ? Color.WHITE : COLOR_TAB_UNSELECTED);
        tabGrab.setTextColor(send ? COLOR_TAB_UNSELECTED : Color.WHITE);
        tabSend.getPaint().setFakeBoldText(send);
        tabGrab.getPaint().setFakeBoldText(!send);
        tabSendIndicator.setVisibility(send ? View.VISIBLE : View.INVISIBLE);
        tabGrabIndicator.setVisibility(send ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.red_packet_tab_send_wrap) {
            showTab(0);
        } else if (id == R.id.red_packet_tab_grab_wrap) {
            showTab(1);
        } else if (id == R.id.red_packet_setting_save_tv) {
            saveCurrentTab();
        }
    }

    private void saveCurrentTab() {
        if (currentTab == 0) {
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
        } else {
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
        }
    }
}
