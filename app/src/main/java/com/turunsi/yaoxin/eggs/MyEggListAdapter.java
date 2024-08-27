package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;

public class MyEggListAdapter extends BaseQuickAdapter<CustomMsgBean, QuickViewHolder> {

    boolean canSend = false;
    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CustomMsgBean bean) {

        //[{"id":1,"img":"111111","price":18800,"amount":17800,"num":2}]
        TextView titleTv = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_my_egg_list_cell_title_tv);
        if (canSend) {
            titleTv.setText(bean.num + "个" + NumberUtil.formartMoney_zhengshu(bean.amount)+"彩蛋");
        } else {
            titleTv.setText("1个" + NumberUtil.formartMoney_zhengshu(bean.amount)+"彩蛋");
        }
        TextView detailTv = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_my_egg_list_cell_detail_tv);
        detailTv.setText(canSend ? "彩蛋还未发放，可发放至指定群聊": "彩蛋已发放至 "+ bean.groupName+" 群聊");
        TextView optTv = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_my_egg_list_cell_opt_tv);
        optTv.setText(canSend ? "放彩蛋":"已发放");
        optTv.setSelected(canSend);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(com.yaoxin.appbase.R.layout.item_my_egg_list_cell,viewGroup);
    }
}

