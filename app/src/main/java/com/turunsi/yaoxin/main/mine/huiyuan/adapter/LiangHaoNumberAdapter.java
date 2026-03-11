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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ResourceHelper;

public class LiangHaoNumberAdapter extends BaseQuickAdapter<Integer, QuickViewHolder> {
    private int type; // 1:V1, 2:V2, 3:V3

    public LiangHaoNumberAdapter(int type) {
        this.type = type;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable Integer item) {
        TextView tv = holder.getView(R.id.item_lianghao_number_tv);
        tv.setText(item + "");
//        int color = 0xFFFFA726; // V1橙色
//        if (type == 2) color = 0xFF42A5F5; // V2蓝色
//        if (type == 3) color = 0xFF7E57C2; // V3紫色
        int color = ResourceHelper.getGradeColor(tv.getContext(), this.type);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(2, color);
        drawable.setCornerRadius(12);
        drawable.setColor(0xFFFFFFFF);
        tv.setBackground(drawable);
        tv.setTextColor(color);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_lianghao_number, parent);
    }
} 