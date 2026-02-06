package com.turunsi.yaoxin.main.mine.fuhao.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.NumberUtil;

public class GroupBuyListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean bean) {
        // 价格 - 直接显示，不除以100（因为价格已经是元为单位）
        quickViewHolder.setText(R.id.cell_buy_group_feature_money_tv, "¥ " + bean.price);
        
        // 描述
        if (bean.groupMemberNum == -1) {
            quickViewHolder.setText(R.id.cell_buy_group_feature_detail_tv, "购买即升级当前群组为1000以上人群");
        } else {
            quickViewHolder.setText(R.id.cell_buy_group_feature_detail_tv, "购买即升级当前群组为" + bean.groupMemberNum + "人群");
        }
        
        // 隐藏选择图标（不再需要）
        ImageView selectIv = quickViewHolder.getView(R.id.cell_buy_group_feature_select_iv);
        if (selectIv != null) {
            selectIv.setVisibility(android.view.View.GONE);
        }
    }


    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_buy_group_feature, viewGroup);
    }
}

