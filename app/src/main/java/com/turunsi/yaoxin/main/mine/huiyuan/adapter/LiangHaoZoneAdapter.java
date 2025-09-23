package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;

public class LiangHaoZoneAdapter extends BaseQuickAdapter<String, QuickViewHolder> {
    public static final int TYPE_V1 = 1;
    public static final int TYPE_V2 = 2;
    public static final int TYPE_V3 = 3;
    private int type;

    public LiangHaoZoneAdapter(int type) {
        this.type = type;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable String item) {
        TextView tv = holder.getView(R.id.item_lianghao_number_tv);
        tv.setText(item);
        int color = 0xFFFFA726; // 默认橙色
        if (type == TYPE_V2) color = 0xFF42A5F5; // 蓝色
        if (type == TYPE_V3) color = 0xFF7E57C2; // 紫色
        GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(2, color);
        drawable.setCornerRadius(12);
        drawable.setColor(0xFFFFFFFF);
        tv.setBackground(drawable);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_lianghao_number, parent);
    }
} 