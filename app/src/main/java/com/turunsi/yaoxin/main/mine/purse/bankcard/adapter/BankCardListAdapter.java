package com.turunsi.yaoxin.main.mine.purse.bankcard.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.turunsi.yaoxin.utils.Constant;
import com.yaoxin.appbase.model.UserBean;

import java.util.List;

public class BankCardListAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public interface OnUnbindClickListener {
        void onUnbindClick(@NonNull UserBean item, int position);
    }

    private OnUnbindClickListener onUnbindClickListener;

    public void setOnUnbindClickListener(OnUnbindClickListener listener) {
        this.onUnbindClickListener = listener;
    }

    @Override
    protected int getItemCount(@NonNull List<? extends UserBean> items) {
        return super.getItemCount(items) + 1;
    }

    @Override
    protected int getItemViewType(int position, @NonNull List<? extends UserBean> list) {
        if (position == list.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean orderListBean) {
        if (getItemViewType(i) == TYPE_FOOTER) {

        } else {
            quickViewHolder.setText(R.id.tvName, orderListBean.name);
            quickViewHolder.setText(R.id.tvBankNo, orderListBean.phone);

            // bind unbind button
            if (onUnbindClickListener != null) {
                quickViewHolder.getView(R.id.btnUnbind).setOnClickListener(v -> {
                    onUnbindClickListener.onUnbindClick(orderListBean, i);
                });
            }
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_FOOTER) {
            return new QuickViewHolder(R.layout.item_purse_bank_card_list_add_cell, viewGroup);
        }
        return new QuickViewHolder(R.layout.item_purse_bank_card_list_cell, viewGroup);
    }
}

