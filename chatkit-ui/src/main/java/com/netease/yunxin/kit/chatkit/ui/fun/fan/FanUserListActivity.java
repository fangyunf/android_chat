package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.chatkit.ui.fun.redpacket.RedPacketAutoConfig;
import com.netease.yunxin.kit.chatkit.ui.fun.redpacket.RedPacketAutoManager;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 爆粉列表页（对齐 qiangbao FanUserListActivity）：选人 → 间隔加人。
 */
public class FanUserListActivity extends BaseActivity implements FanAddQueue.Listener {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_PRECISE = 1;
    public static final int MODE_CUSTOM = 2;

    private static final String EXTRA_MODE = "mode";
    private static final int TITLE_GREEN = Color.parseColor("#58BE6B");
    private static final int PAGE_BG = Color.WHITE;
    private static final int DARK_CARD = Color.WHITE;
    private static final int ACTION_GREEN = Color.parseColor("#58BE6B");
    private static final int THEME_GOLD = Color.parseColor("#58BE6B");
    private static final int DESC_TEXT = Color.parseColor("#999999");
    private static final int ROW_TITLE = Color.parseColor("#333333");
    private static final int STOP_RED = Color.parseColor("#E54545");
    private static final int INPUT_BORDER = Color.parseColor("#DDDDDD");
    private static final int INPUT_BG = Color.parseColor("#FFFFFF");

    private FanMode fanMode;
    private FanStore store;
    private RecyclerView recyclerView;
    private FanListAdapter fanAdapter;
    private TextView emptyLabel;
    private EditText intervalField;
    private EditText greetingField;
    private TextView selectAllBtn;
    private TextView submitBtn;
    private TextView totalCountLabel;
    private FrameLayout progressMask;
    private TextView progressStatus;
    private TextView progressCountdown;

    private final List<FanUser> users = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    private final Map<String, Runnable> rowPainters = new HashMap<>();
    private FanAddQueue addQueue;
    private boolean queueRunning;

    public static void start(Context ctx, int mode) {
        Intent intent = new Intent(ctx, FanUserListActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        if (!(ctx instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 入口处已 requireAuth 弹窗；此处仅兜底，避免 finish 冲掉对话框
        if (!com.netease.yunxin.kit.chatkit.ui.fun.auth.AuthHelper.canUse()) {
            finish();
            return;
        }
        fanMode = modeFromIntent(getIntent().getIntExtra(EXTRA_MODE, MODE_NORMAL));
        store = FanStore.getInstance(this);

        getWindow().setStatusBarColor(TITLE_GREEN);
        StatusBarUtils.setStatusBarLightMode(this, false, false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAGE_BG);
        root.addView(buildTopBar());

        FrameLayout listHost = new FrameLayout(this);
        listHost.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        fanAdapter = new FanListAdapter();
        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fanAdapter);
        recyclerView.setPadding(dp(8), dp(4), dp(8), dp(8));
        recyclerView.setClipToPadding(false);
        recyclerView.setBackgroundColor(PAGE_BG);
        emptyLabel = new TextView(this);
        emptyLabel.setGravity(Gravity.CENTER);
        emptyLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        emptyLabel.setTextColor(DESC_TEXT);
        emptyLabel.setPadding(dp(24), dp(60), dp(24), dp(60));
        listHost.addView(
                recyclerView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listHost.addView(
                emptyLabel,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(listHost);
        root.addView(buildBottomPanel());
        setContentView(root);

        progressMask = buildProgressMask();
        ((ViewGroup) getWindow().getDecorView()).addView(progressMask);
        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!queueRunning) {
            loadUsers();
        }
    }

    private FrameLayout buildTopBar() {
        FrameLayout bar = new FrameLayout(this);
        bar.setMinimumHeight(dp(44) + BarUtils.getStatusBarHeight());
        bar.setBackgroundColor(TITLE_GREEN);
        bar.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);

        TextView title = new TextView(this);
        title.setText(titleForMode(fanMode));
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams titleLp =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        titleLp.gravity = Gravity.CENTER;
        titleLp.leftMargin = dp(72);
        titleLp.rightMargin = dp(72);
        bar.addView(title, titleLp);

        TextView cancel = new TextView(this);
        cancel.setText("取消");
        cancel.setTextColor(Color.WHITE);
        cancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cancel.setGravity(Gravity.CENTER);
        cancel.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams cancelLp = new FrameLayout.LayoutParams(dp(60), dp(44));
        cancelLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        cancelLp.leftMargin = dp(12);
        bar.addView(cancel, cancelLp);

        totalCountLabel = new TextView(this);
        totalCountLabel.setTextColor(Color.WHITE);
        totalCountLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        totalCountLabel.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams countLp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        countLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        countLp.rightMargin = dp(12);
        bar.addView(totalCountLabel, countLp);
        return bar;
    }

    private View buildBottomPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(10), dp(12), dp(16));
        panel.setBackgroundColor(PAGE_BG);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(DARK_CARD);
        cardBg.setCornerRadius(dp(12));
        cardBg.setStroke(dp(1), Color.parseColor("#F0F0F0"));
        card.setBackground(cardBg);

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        selectAllBtn = greenBtn("全选", this::toggleSelectAll);
        LinearLayout.LayoutParams half =
                new LinearLayout.LayoutParams(0, dp(34), 1f);
        half.rightMargin = dp(6);
        row1.addView(selectAllBtn, half);
        row1.addView(
                greenBtn("清空", this::confirmClear),
                new LinearLayout.LayoutParams(0, dp(34), 1f));
        card.addView(row1);

        LinearLayout intervalRow = new LinearLayout(this);
        intervalRow.setOrientation(LinearLayout.HORIZONTAL);
        intervalRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView intervalLabel = new TextView(this);
        intervalLabel.setText("爆粉间隔");
        intervalLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        intervalLabel.setTextColor(DESC_TEXT);
        intervalLabel.setGravity(Gravity.CENTER_VERTICAL);
        intervalRow.addView(intervalLabel, new LinearLayout.LayoutParams(dp(70), dp(36)));
        intervalField = new EditText(this);
        intervalField.setHint("默认加人间隔5秒");
        intervalField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        intervalField.setTextColor(ROW_TITLE);
        intervalField.setHintTextColor(DESC_TEXT);
        intervalField.setInputType(InputType.TYPE_CLASS_NUMBER);
        intervalField.setPadding(dp(10), dp(8), dp(10), dp(8));
        intervalField.setBackground(borderedInputBg());
        intervalField.setText(
                String.valueOf(RedPacketAutoManager.get().getConfig().getFanIntervalSec()));
        LinearLayout.LayoutParams fieldLp = new LinearLayout.LayoutParams(0, dp(36), 1f);
        fieldLp.leftMargin = dp(4);
        intervalRow.addView(intervalField, fieldLp);
        LinearLayout.LayoutParams intervalLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        intervalLp.topMargin = dp(10);
        card.addView(intervalRow, intervalLp);

        greetingField = new EditText(this);
        greetingField.setHint("打招呼内容");
        greetingField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        greetingField.setTextColor(ROW_TITLE);
        greetingField.setHintTextColor(DESC_TEXT);
        greetingField.setPadding(dp(10), dp(8), dp(10), dp(8));
        greetingField.setBackground(borderedInputBg());
        greetingField.setText("加我通过下");
        LinearLayout.LayoutParams greetLp =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        greetLp.topMargin = dp(8);
        card.addView(greetingField, greetLp);

        submitBtn = greenBtn("打招呼添加", this::startAdd);
        LinearLayout.LayoutParams submitLp =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        submitLp.topMargin = dp(10);
        card.addView(submitBtn, submitLp);
        panel.addView(card);
        return panel;
    }

    private FrameLayout buildProgressMask() {
        FrameLayout mask = new FrameLayout(this);
        mask.setBackgroundColor(Color.argb(115, 0, 0, 0));
        mask.setVisibility(View.GONE);
        mask.setClickable(true);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));
        card.setBackground(roundedBg(Color.WHITE, dp(10)));
        progressStatus = new TextView(this);
        progressStatus.setTextSize(16);
        progressStatus.setTextColor(Color.BLACK);
        progressStatus.setGravity(Gravity.CENTER);
        progressCountdown = new TextView(this);
        progressCountdown.setTextSize(14);
        progressCountdown.setTextColor(Color.GRAY);
        progressCountdown.setGravity(Gravity.CENTER);
        card.addView(progressStatus);
        LinearLayout.LayoutParams cdLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cdLp.topMargin = dp(8);
        card.addView(progressCountdown, cdLp);
        TextView stop = greenBtn("停止添加", this::stopAdd);
        stop.setBackgroundColor(STOP_RED);
        LinearLayout.LayoutParams stopLp = new LinearLayout.LayoutParams(dp(200), dp(40));
        stopLp.topMargin = dp(16);
        card.addView(stop, stopLp);
        FrameLayout.LayoutParams cardLp =
                new FrameLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.gravity = Gravity.CENTER;
        mask.addView(card, cardLp);
        return mask;
    }

    private void loadUsers() {
        users.clear();
        users.addAll(store.list(fanMode));
        selected.retainAll(userIdSet());
        renderList();
    }

    private Set<String> userIdSet() {
        Set<String> ids = new LinkedHashSet<>();
        for (FanUser u : users) {
            ids.add(u.userId);
        }
        return ids;
    }

    private void renderList() {
        rowPainters.clear();
        totalCountLabel.setText("共 " + users.size() + " 人");
        if (users.isEmpty()) {
            emptyLabel.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyLabel.setText(emptyTextForMode(fanMode));
        } else {
            emptyLabel.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            fanAdapter.notifyDataSetChanged();
        }
        updateSelectAllLabel();
    }

    private class FanListAdapter extends RecyclerView.Adapter<FanListAdapter.Holder> {
        class Holder extends RecyclerView.ViewHolder {
            Holder(View v) {
                super(v);
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout frame = new FrameLayout(parent.getContext());
            frame.setLayoutParams(
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            return new Holder(frame);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            FrameLayout frame = (FrameLayout) holder.itemView;
            frame.removeAllViews();
            frame.addView(buildCell(position + 1, users.get(position)));
        }
    }

    private View buildCell(int index, FanUser user) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(64));
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(DARK_CARD);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), Color.parseColor("#F0F0F0"));
        row.setBackground(bg);
        LinearLayout.LayoutParams rowLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(6);
        row.setLayoutParams(rowLp);
        row.setEnabled(!queueRunning);
        row.setAlpha(queueRunning ? 0.55f : 1f);

        String showName = TextUtils.isEmpty(user.name) ? user.userId : user.name;
        String letter =
                showName.trim().isEmpty()
                        ? "?"
                        : String.valueOf(showName.trim().charAt(0));

        FrameLayout avatarBox = new FrameLayout(this);
        GradientDrawable oval = new GradientDrawable();
        oval.setColor(THEME_GOLD);
        oval.setShape(GradientDrawable.OVAL);
        avatarBox.setBackground(oval);
        int size = dp(42);
        TextView letterTv = new TextView(this);
        letterTv.setText(letter);
        letterTv.setGravity(Gravity.CENTER);
        letterTv.setTextColor(Color.WHITE);
        letterTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        letterTv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        ImageView avatarIv = new ImageView(this);
        avatarIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarIv.setVisibility(View.GONE);
        avatarBox.addView(letterTv, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        avatarBox.addView(
                avatarIv,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        row.addView(avatarBox, new LinearLayout.LayoutParams(size, size));
        if (!TextUtils.isEmpty(user.avatar)) {
            GlideUtil.yh_loadImage(this, avatarIv, user.avatar);
            avatarIv.setVisibility(View.VISIBLE);
        }

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_VERTICAL);
        TextView nameTv = new TextView(this);
        nameTv.setText(index + ". " + showName);
        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        nameTv.setTextColor(ROW_TITLE);
        nameTv.setMaxLines(1);
        nameTv.setEllipsize(TextUtils.TruncateAt.END);
        col.addView(nameTv);
        TextView idTv = new TextView(this);
        idTv.setText("ID：" + user.userId);
        idTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        idTv.setTextColor(DESC_TEXT);
        idTv.setMaxLines(1);
        idTv.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams idLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        idLp.topMargin = dp(4);
        col.addView(idTv, idLp);
        LinearLayout.LayoutParams colLp =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        colLp.leftMargin = dp(10);
        colLp.rightMargin = dp(10);
        row.addView(col, colLp);

        TextView check = new TextView(this);
        check.setGravity(Gravity.CENTER);
        check.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        Runnable paint =
                () -> {
                    boolean chosen = selected.contains(user.userId);
                    check.setText(chosen ? "已选" : "选择");
                    check.setTextColor(chosen ? Color.WHITE : ROW_TITLE);
                    GradientDrawable chip = new GradientDrawable();
                    chip.setCornerRadius(dp(14));
                    if (chosen) {
                        chip.setColor(THEME_GOLD);
                    } else {
                        chip.setColor(Color.TRANSPARENT);
                        chip.setStroke(dp(1), THEME_GOLD);
                    }
                    check.setBackground(chip);
                };
        paint.run();
        rowPainters.put(user.userId, paint);
        row.addView(check, new LinearLayout.LayoutParams(dp(54), dp(28)));
        row.setOnClickListener(
                v -> {
                    if (queueRunning) {
                        return;
                    }
                    if (selected.contains(user.userId)) {
                        selected.remove(user.userId);
                    } else {
                        selected.add(user.userId);
                    }
                    paint.run();
                    updateSelectAllLabel();
                });
        return row;
    }

    private void toggleSelectAll() {
        if (queueRunning) {
            return;
        }
        boolean allSelected = selected.size() == users.size() && !users.isEmpty();
        selected.clear();
        if (!allSelected) {
            for (FanUser u : users) {
                selected.add(u.userId);
            }
        }
        for (Runnable r : rowPainters.values()) {
            r.run();
        }
        fanAdapter.notifyDataSetChanged();
        updateSelectAllLabel();
    }

    private void updateSelectAllLabel() {
        selectAllBtn.setText(
                !users.isEmpty() && selected.size() == users.size() ? "取消全选" : "全选");
    }

    private void confirmClear() {
        if (queueRunning || users.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("清空名单")
                .setMessage("将清空本页全部 " + users.size() + " 人本地名单，不可恢复。是否继续？")
                .setNegativeButton("取消", null)
                .setPositiveButton(
                        "确定",
                        (d, w) -> {
                            store.clear(fanMode);
                            selected.clear();
                            loadUsers();
                        })
                .show();
    }

    private void startAdd() {
        if (queueRunning) {
            return;
        }
        String greet = greetingField.getText().toString().trim();
        if (TextUtils.isEmpty(greet)) {
            ToastUtils.toastMsg("请输入打招呼内容");
            return;
        }
        List<FanUser> picked = new ArrayList<>();
        for (FanUser u : users) {
            if (selected.contains(u.userId)) {
                picked.add(u);
            }
        }
        if (picked.isEmpty()) {
            ToastUtils.toastMsg("请选择");
            return;
        }
        int interval = 5;
        try {
            interval = Integer.parseInt(intervalField.getText().toString().trim());
        } catch (Exception ignored) {
        }
        interval = Math.max(5, interval);
        RedPacketAutoConfig cfg = RedPacketAutoManager.get().getConfig();
        cfg.setFanIntervalSec(interval);
        queueRunning = true;
        submitBtn.setText("停止添加");
        submitBtn.setBackgroundColor(STOP_RED);
        submitBtn.setOnClickListener(v -> stopAdd());
        progressMask.setVisibility(View.VISIBLE);
        addQueue = new FanAddQueue();
        addQueue.start(picked, greet, interval, this);
    }

    private void stopAdd() {
        if (addQueue != null) {
            addQueue.stop();
        }
        queueRunning = false;
        progressMask.setVisibility(View.GONE);
        submitBtn.setText("打招呼添加");
        submitBtn.setBackground(roundedBg(ACTION_GREEN, dp(4)));
        submitBtn.setOnClickListener(v -> startAdd());
        renderList();
    }

    @Override
    public void onProgress(int current, int total, int countdownSec, String status) {
        progressStatus.setText(status);
        progressCountdown.setText(
                countdownSec > 0 ? ("下次添加 " + countdownSec + "s（平台限速）") : "");
    }

    @Override
    public void onFinished() {
        stopAdd();
    }

    private TextView greenBtn(String text, Runnable click) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(roundedBg(ACTION_GREEN, dp(4)));
        tv.setOnClickListener(v -> click.run());
        return tv;
    }

    private GradientDrawable roundedBg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    private GradientDrawable borderedInputBg() {
        GradientDrawable g = new GradientDrawable();
        g.setColor(INPUT_BG);
        g.setCornerRadius(dp(6));
        g.setStroke(dp(1), INPUT_BORDER);
        return g;
    }

    private int dp(int v) {
        return (int)
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private FanMode modeFromIntent(int mode) {
        if (mode == MODE_PRECISE) {
            return FanMode.Precise;
        }
        if (mode == MODE_CUSTOM) {
            return FanMode.Custom;
        }
        return FanMode.Normal;
    }

    private String titleForMode(FanMode mode) {
        switch (mode) {
            case Precise:
                return "查看红包用户";
            case Custom:
                return "自选人群爆粉列表";
            case Normal:
            default:
                return "开始爆粉";
        }
    }

    private String emptyTextForMode(FanMode mode) {
        switch (mode) {
            case Precise:
                return "暂无红包用户";
            case Custom:
                return "请先「添加到自选爆粉区」";
            case Normal:
            default:
                return "暂无用户数据\n请先在群里保存爆粉人群";
        }
    }
}
