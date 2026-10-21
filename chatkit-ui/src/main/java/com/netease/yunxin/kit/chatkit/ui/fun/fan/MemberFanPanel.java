package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

/**
 * 群成员详情右上浮动：「添加到自选爆粉区」（对齐 qiangbao MemberProfilePanel）。
 */
public final class MemberFanPanel {

    private static final String TAG = "yx_member_fan_panel";
    private static final int PANEL_BG = Color.argb(184, 26, 56, 61);

    private final Activity activity;
    private final LinearLayout root;
    private String userId;
    private String displayName;
    private String cloudId;
    private String groupId;
    private String avatar;

    private MemberFanPanel(Activity activity) {
        this.activity = activity;
        root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.END);
        root.setTag(TAG);

        TextView btn = new TextView(activity);
        btn.setText("添加到自选爆粉区");
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btn.setIncludeFontPadding(false);
        btn.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(4));
        bg.setColor(PANEL_BG);
        btn.setBackground(bg);
        btn.setOnClickListener(v -> addToCustomFan());
        root.addView(btn, new LinearLayout.LayoutParams(dp(148), dp(34)));
        enableDrag(root);
    }

    public void bind(
            String userId,
            String displayName,
            String cloudId,
            String groupId,
            String avatar,
            String ignoredMemberCode) {
        this.userId = userId == null ? "" : userId.trim();
        this.displayName = displayName == null ? "" : displayName.trim();
        this.cloudId = cloudId;
        this.groupId = groupId;
        this.avatar = avatar;
    }

    private void addToCustomFan() {
        if (!com.netease.yunxin.kit.chatkit.ui.fun.auth.AuthHelper.requireAuth(activity)) {
            return;
        }
        String myId = DataUtil.getUserid();
        String hostUid =
                !TextUtils.isEmpty(userId)
                        ? userId.trim()
                        : (!TextUtils.isEmpty(cloudId) ? cloudId.trim() : "");
        if (TextUtils.isEmpty(hostUid)) {
            ToastUtils.toastMsg("暂未解析到用户账号");
            return;
        }
        if (!TextUtils.isEmpty(myId) && myId.equals(hostUid)) {
            ToastUtils.toastMsg("请勿添加自己");
            return;
        }
        String name = TextUtils.isEmpty(displayName) ? hostUid : displayName;
        boolean added =
                FanStore.getInstance(activity)
                        .append(
                                FanMode.Custom,
                                new FanUser(hostUid, name, hostUid, groupId, avatar));
        int count = FanStore.getInstance(activity).list(FanMode.Custom).size();
        if (added) {
            ToastUtils.toastMsg("已加入自选（当前 " + count + " 人）");
        } else {
            ToastUtils.toastMsg("已在自选列表中（当前 " + count + " 人）");
        }
    }

    public static MemberFanPanel attach(Activity activity) {
        if (!com.netease.yunxin.kit.chatkit.ui.fun.redpacket.RedPacketFeature.ENABLE
                || activity == null) {
            return null;
        }
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return null;
        }
        View old = content.findViewWithTag(TAG);
        if (old != null && old.getParent() instanceof ViewGroup) {
            ((ViewGroup) old.getParent()).removeView(old);
        }
        MemberFanPanel panel = new MemberFanPanel(activity);
        int top = Math.max(statusBarHeight(activity) + panel.dp(8), panel.dp(56));
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.topMargin = top;
        lp.rightMargin = panel.dp(10);
        content.addView(panel.root, lp);
        return panel;
    }

    private void enableDrag(View v) {
        final float[] down = new float[2];
        final int[] start = new int[2];
        final boolean[] dragging = new boolean[1];
        v.setOnTouchListener(
                (view, e) -> {
                    ViewGroup.LayoutParams raw = view.getLayoutParams();
                    if (!(raw instanceof FrameLayout.LayoutParams)) {
                        return false;
                    }
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) raw;
                    switch (e.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            down[0] = e.getRawX();
                            down[1] = e.getRawY();
                            dragging[0] = false;
                            lp.gravity = Gravity.TOP | Gravity.START;
                            if (lp.leftMargin == 0 && lp.rightMargin > 0) {
                                View parent = (View) view.getParent();
                                if (parent != null) {
                                    lp.leftMargin =
                                            Math.max(
                                                    0,
                                                    parent.getWidth()
                                                            - view.getWidth()
                                                            - lp.rightMargin);
                                    lp.rightMargin = 0;
                                    view.setLayoutParams(lp);
                                }
                            }
                            start[0] = lp.leftMargin;
                            start[1] = lp.topMargin;
                            return false;
                        case MotionEvent.ACTION_MOVE:
                            float dx = e.getRawX() - down[0];
                            float dy = e.getRawY() - down[1];
                            if (!dragging[0]
                                    && (Math.abs(dx) > 8 || Math.abs(dy) > 8)) {
                                dragging[0] = true;
                                if (view.getParent() != null) {
                                    view.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                            }
                            if (!dragging[0]) {
                                return false;
                            }
                            lp.leftMargin = Math.max(0, (int) (start[0] + dx));
                            lp.topMargin = Math.max(0, (int) (start[1] + dy));
                            view.setLayoutParams(lp);
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            return dragging[0];
                        default:
                            return false;
                    }
                });
    }

    private static int statusBarHeight(Activity a) {
        int id = a.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return a.getResources().getDimensionPixelSize(id);
        }
        return (int) (28 * a.getResources().getDisplayMetrics().density);
    }

    private int dp(int v) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, v, activity.getResources().getDisplayMetrics());
    }
}
