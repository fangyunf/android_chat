package com.turunsi.yaoxin.main.mine.setting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

public class ExchangeAccountAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean orderListBean) {
        quickViewHolder.setText(R.id.item_set_exchange_account_list_cell_name_tv,orderListBean.username)
                .setText(R.id.item_set_exchange_account_list_cell_id_tv, "ID：" + orderListBean.memberCode);
        RoundedImageView iv = quickViewHolder.getView(R.id.item_set_exchange_account_list_cell_head_iv);
        TextView dangqianTv = quickViewHolder.getView(R.id.item_set_exchange_account_list_cell_current_tv);
        GlideUtil.yh_loadImage(iv.getContext(),iv,orderListBean.avatar);
        if (orderListBean.userId.equals(DataUtil.getUserid())) {
            dangqianTv.setVisibility(View.VISIBLE);
        } else {
            dangqianTv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_set_exchange_account_list_cell, viewGroup);
    }
}

