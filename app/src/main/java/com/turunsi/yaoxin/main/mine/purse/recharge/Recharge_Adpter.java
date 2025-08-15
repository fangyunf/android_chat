package com.turunsi.yaoxin.main.mine.purse.recharge;


import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;

/***
 * Created by wangyong951 on 2019/6/27.
 */
public class Recharge_Adpter extends
        BaseQuickAdapter<String, QuickViewHolder> {
    public String selectStr = "100";

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable String orderListBean) {
        TextView tv = quickViewHolder.findView(R.id.activity_mine_purse_recharge_item_tv);
        tv.setText(orderListBean);
        tv.setSelected(selectStr.equals(orderListBean));

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.activity_mine_purse_recharge_item, viewGroup);
    }
}
