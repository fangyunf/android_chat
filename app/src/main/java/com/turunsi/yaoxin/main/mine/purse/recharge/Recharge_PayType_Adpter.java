package com.turunsi.yaoxin.main.mine.purse.recharge;


import android.content.Context;
import android.media.Image;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;

/***
 * Created by wangyong951 on 2019/6/27.
 */
public class Recharge_PayType_Adpter extends
        BaseQuickAdapter<String, QuickViewHolder> {
  String payType = "支付宝";
  @Override
  protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable String orderListBean) {
    TextView tv = quickViewHolder.findView(R.id.activity_mine_purse_recharge_paytype_item_tv);
    ImageView iconIv = quickViewHolder.findView(R.id.activity_mine_purse_recharge_paytype_item_icon);
    ImageView stateIv = quickViewHolder.findView(R.id.activity_mine_purse_recharge_paytype_item_state_icon);
    tv.setText(orderListBean);
    stateIv.setSelected(payType.equals(orderListBean));
    switch (orderListBean) {
      case "支付宝":case "支付宝1":case "支付宝2":
        iconIv.setImageResource(R.mipmap.recharge_index_zfb);
        break;
      case "微信":
        iconIv.setImageResource(R.mipmap.recharge_index_wx);
        break;
      case "银行卡":
        iconIv.setImageResource(R.mipmap.recharge_index_szrmb);
        break;
      case "USDT":
        iconIv.setImageResource(R.mipmap.recharge_index_bi);
        break;
    }
//    tv.setText(orderListBean);
//    tv.setSelected(selectStr.equals(orderListBean));

  }

  @NonNull
  @Override
  protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
    return new QuickViewHolder(R.layout.activity_mine_purse_recharge_paytype_item,viewGroup);
  }
}
