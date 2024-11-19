package com.turunsi.yaoxin.main.mine.purse.bankcard.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.turunsi.yaoxin.utils.Constant;

import java.util.List;

public class BankCardListAdapter extends BaseQuickAdapter<BankCardListBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable BankCardListBean orderListBean) {
        ImageView iv = quickViewHolder.getView(R.id.item_purse_bank_card_list_cell_bank_bg_iv);
        if ("农业银行".equals(orderListBean.certNo)){
            iv.setImageResource(R.mipmap.bank_card_list_nyyh);
        } else if ("中国银行".equals(orderListBean.certNo)) {
            iv.setImageResource(R.mipmap.bank_card_list_zgyh);
        } else if ("建设银行".equals(orderListBean.certNo)) {
            iv.setImageResource(R.mipmap.bank_card_list_jsyh);
        } else if ("工商银行".equals(orderListBean.certNo)) {
            iv.setImageResource(R.mipmap.bank_card_list_gsyh);
        } else {
            iv.setImageResource(R.mipmap.bank_card_list_unknow);

        }
        quickViewHolder.setText(R.id.item_purse_bank_card_list_cell_bank_name,orderListBean.certNo)
                    .setText(R.id.item_purse_bank_card_list_cell_card_num,orderListBean.phone);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_purse_bank_card_list_cell, viewGroup);
    }
}

