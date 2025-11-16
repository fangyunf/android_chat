package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.UserBean;

public class MyLiangHaoListAdapter extends BaseQuickAdapter<Integer, QuickViewHolder> {

    public Integer selectNumber = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable Integer orderListBean) {
        TextView phoneTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_tv);
        phoneTv.setTextSize(18);
        phoneTv.setTextColor(getContext().getResources().getColor(R.color.color_white));
        phoneTv.setText(orderListBean + "");

        phoneTv.setTextColor(getContext().getResources().getColor(selectNumber != orderListBean ? R.color.color_white : R.color.color_FF8A0B));
        phoneTv.setBackground(getContext().getResources().getDrawable(R.drawable.trans_bg_rounded_stroke));
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_mine_fuhao_list_cell, viewGroup);
    }
}

