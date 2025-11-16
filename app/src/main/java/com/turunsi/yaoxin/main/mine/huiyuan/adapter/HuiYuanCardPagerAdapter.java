package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.utils.NumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager2 适配器，用于显示会员卡片
 */
public class HuiYuanCardPagerAdapter extends RecyclerView.Adapter<HuiYuanCardPagerAdapter.CardViewHolder> {

    private List<HuiYuanBean> dataList = new ArrayList<>();

    public void setDataList(List<HuiYuanBean> list) {
        this.dataList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_huiyuan_card_cell, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (position < 0 || position >= dataList.size()) {
            return;
        }
        HuiYuanBean bean = dataList.get(position);
        
        // 设置背景图片
        String resourceName = "mine_huiyuan_top_vip_" + (position + 1);
        int resId = holder.itemView.getContext().getResources()
                .getIdentifier(resourceName, "mipmap", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.cardLayout.setBackground(holder.itemView.getContext().getResources().getDrawable(resId));
        }
        
        // 设置文本
        holder.tvProductName.setText(bean.memberConfig.productName);
        holder.tvPrice.setText("￥" + NumberUtil.formartMoney(bean.memberConfig.price));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardLayout;
        TextView tvProductName;
        TextView tvPrice;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLayout = itemView.findViewById(R.id.item_huiyuan_card_cell_bg_ll);
            tvProductName = itemView.findViewById(R.id.item_huiyuan_card_cell_tv1);
            tvPrice = itemView.findViewById(R.id.item_huiyuan_card_cell_tv2);
        }
    }
}

