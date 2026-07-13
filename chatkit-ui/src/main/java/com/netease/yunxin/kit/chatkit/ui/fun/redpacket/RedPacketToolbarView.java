package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RedPacketToolbarView extends FrameLayout {

    private final RedPacketAutoManager manager = RedPacketAutoManager.get();
    private final String groupId;

    private LinearLayout expandedLayout;
    private LinearLayout collapsedLayout;
    private TextView btnExclusive;
    private TextView btnLucky;
    private TextView btnSend;

    private float lastX;
    private float lastY;
    private float downX;
    private float downY;
    private boolean dragging;
    private boolean expanded = true;

    public RedPacketToolbarView(Context context, String groupId) {
        super(context);
        this.groupId = groupId;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_red_packet_toolbar, this, true);
        expandedLayout = findViewById(R.id.red_packet_toolbar_expanded);
        collapsedLayout = findViewById(R.id.red_packet_toolbar_collapsed);
        btnExclusive = findViewById(R.id.red_packet_btn_exclusive);
        btnLucky = findViewById(R.id.red_packet_btn_lucky);
        btnSend = findViewById(R.id.red_packet_btn_send);
        TextView btnSetting = findViewById(R.id.red_packet_btn_setting);
        TextView btnClose = findViewById(R.id.red_packet_btn_close);

        btnExclusive.setOnClickListener(v -> onExclusiveClick());
        btnLucky.setOnClickListener(v -> onLuckyClick());
        btnSend.setOnClickListener(v -> onSendClick());
        btnSetting.setOnClickListener(v ->
                RedPacketSettingActivity.start(getContext()));
        btnClose.setOnClickListener(v -> setExpanded(false));
        collapsedLayout.setOnClickListener(v -> setExpanded(true));

        View.OnTouchListener dragListener = this::handleDragTouch;
        expandedLayout.setOnTouchListener(dragListener);
        collapsedLayout.setOnTouchListener(dragListener);

        refreshState();
    }

    private boolean handleDragTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                lastX = downX;
                lastY = downY;
                dragging = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;
                if (!dragging && (Math.abs(event.getRawX() - downX) > 12 || Math.abs(event.getRawY() - downY) > 12)) {
                    dragging = true;
                }
                if (dragging) {
                    moveBy(dx, dy);
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return false;
            default:
                return false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDetachedFromWindow();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateChanged(BaseEvent event) {
        if (RedPacketAutoManager.ACTION_STATE_CHANGED.equals(event.getTag())) {
            refreshState();
        }
    }

    private void onExclusiveClick() {
        RedPacketAutoConfig config = manager.getConfig();
        if (!config.isGrabExclusiveEnabled()) {
            ToastUtils.toastMsg("请先去设置抢专属红包");
            return;
        }
        manager.toggleGrabExclusive();
        refreshState();
    }

    private void onLuckyClick() {
        RedPacketAutoConfig config = manager.getConfig();
        if (!config.isGrabLuckyEnabled()) {
            ToastUtils.toastMsg("请先去设置抢拼手气红包");
            return;
        }
        manager.toggleGrabLucky(groupId);
        refreshState();
    }

    private void onSendClick() {
        if (!manager.getConfig().isSendConfigured()) {
            ToastUtils.toastMsg("请先去设置发红包");
            return;
        }
        manager.toggleAutoSend(groupId);
        refreshState();
    }

    private void refreshState() {
        setButtonActive(btnExclusive, manager.isGrabExclusiveActive());
        setButtonActive(btnLucky, manager.isGrabLuckyActive());
        if (manager.isAutoSendActive()) {
            btnSend.setText("停");
            setButtonActive(btnSend, true);
        } else {
            btnSend.setText("➤");
            setButtonActive(btnSend, false);
        }
    }

    private void setButtonActive(TextView view, boolean active) {
        if (active) {
            view.setBackgroundResource(R.drawable.bg_red_packet_toolbar_btn_active);
            view.setTextColor(Color.WHITE);
        } else {
            view.setBackgroundResource(R.drawable.bg_red_packet_toolbar_btn);
            int color = view.getId() == R.id.red_packet_btn_send ? Color.parseColor("#E54545") : Color.parseColor("#7B4DFF");
            if (view.getId() == R.id.red_packet_btn_setting || view.getId() == R.id.red_packet_btn_close) {
                color = Color.parseColor("#666666");
            }
            view.setTextColor(color);
        }
    }

    private void setExpanded(boolean value) {
        expanded = value;
        expandedLayout.setVisibility(value ? VISIBLE : GONE);
        collapsedLayout.setVisibility(value ? GONE : VISIBLE);
    }

    private void moveBy(float dx, float dy) {
        FrameLayout.LayoutParams lp = (LayoutParams) getLayoutParams();
        if (lp == null) {
            return;
        }
        lp.leftMargin += (int) dx;
        lp.topMargin += (int) dy;
        setLayoutParams(lp);
    }

    public static RedPacketToolbarView attach(FrameLayout parent, String groupId) {
        int size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 44, parent.getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12, parent.getResources().getDisplayMetrics());
        int top = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 180, parent.getResources().getDisplayMetrics());

        RedPacketToolbarView toolbar = new RedPacketToolbarView(parent.getContext(), groupId);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.TOP;
        lp.topMargin = top;
        lp.rightMargin = margin;
        parent.addView(toolbar, lp);
        return toolbar;
    }
}
