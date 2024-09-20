package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.UserBean;

public class MyHuiYuanListAdapter extends BaseQuickAdapter<HuiYuanBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable HuiYuanBean orderListBean) {
//        TextView phoneTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_tv);
//        phoneTv.setText(orderListBean.phoneFix + "后两位为00-19");
        String resourceName = "mine_huiyuan_top_bg_" + (i + 1); // 假设图片名为 image_1, image_2, etc.

        // 动态获取资源 ID
        int resId = getContext(). getResources().getIdentifier(resourceName, "mipmap", getContext(). getPackageName());

        if (resId != 0) {
            // 使用资源 ID 加载图片（例如将其设置为 ImageView 的内容）
            LinearLayout linearLayout = quickViewHolder.getView(R.id.item_huiyuan_card_cell_bg_ll);
            linearLayout.setBackground(getContext().getResources().getDrawable(resId));
        } else {
            System.out.println("资源 " + resourceName + " 未找到！");
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_huiyuan_card_cell, viewGroup);
    }
}

