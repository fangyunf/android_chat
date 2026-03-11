package com.turunsi.yaoxin.main.mine.purse.bill.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bankcard.bean.BankCardListBean;
import com.turunsi.yaoxin.main.mine.purse.bill.bean.BillDetailBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.List;

public class BillDetailListAdapter extends BaseQuickAdapter<BillDetailBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable BillDetailBean bean) {
        quickViewHolder.setText(R.id.item_purse_bill_detail_list_title_tv, bean.name)
                .setText(R.id.item_purse_bill_detail_list_content_tv, TimeUtil.stampToDate(bean.createTime))
                .setText(R.id.item_purse_bill_detail_list_blance, "余额:" + NumberUtil.formartMoney((bean.balance + bean.amount) + ""))
                .setText(R.id.item_purse_bill_detail_list_money_tv, (bean.amount > 0 ? "+" : "-") + NumberUtil.formartMoney(Math.abs(bean.amount) + ""))
                .setTextColor(R.id.item_purse_bill_detail_list_money_tv, getContext().getResources().getColor(bean.amount > 0 ? R.color.color_FF3A3A : R.color.color_24CE70));
        ;

        ImageView headIv = quickViewHolder.getView(R.id.item_purse_bill_detail_list_icon);
//        GlideUtil.yh_loadImageRoundedCorner(this,headIv,bean.);

        headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_bohui);
        switch (bean.moduleType) {
            case 23:
            case 26:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_lingqu_coupon);
                break;
            case 21:
            case 24:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_send_zhuanshu);
                break;
            case 22:
            case 25:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_geren_coupon);
                break;
            case 0:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_chongzhi);
                break;
            case 1:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_tixian);
                break;
            case 27:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_tuihui);
                break;
            case 5:
                headIv.setImageResource(R.mipmap.mine_purse_bill_detail_list_bohui);
                break;
            default:
                break;

        }
    }

    private void returnImgRes(int type) {
        /*
        *
        String[] strs = {"全部","发送群红包","领取群红包","发送专属红包","领取专属红包","发送个人红包","领取个人红包","充值","提现","红包退回","提现驳回","购物支出"};
        int[] types = {-1,23,26,21,24,22,25,0,1,27,5,0,100};
        * */

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_purse_bill_detail_list, viewGroup);
    }
}

