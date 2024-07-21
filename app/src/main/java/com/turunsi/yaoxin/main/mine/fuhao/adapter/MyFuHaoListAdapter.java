package com.turunsi.yaoxin.main.mine.fuhao.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

public class MyFuHaoListAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean orderListBean) {
        TextView phoneTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_tv);
        phoneTv.setText(orderListBean.phoneFix + "后两位为00-19");
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_mine_fuhao_list_cell, viewGroup);
    }
}

