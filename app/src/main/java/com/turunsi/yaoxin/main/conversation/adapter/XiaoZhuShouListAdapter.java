package com.turunsi.yaoxin.main.conversation.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class XiaoZhuShouListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {
        // 标题
        quickViewHolder.setText(R.id.item_xiaozhushou_list_title_tv, titleText(infoBean.state));
        
        // 副标题（只在红包退回时显示）
        TextView subtitleTv = quickViewHolder.getView(R.id.item_xiaozhushou_list_subtitle_tv);
        if (infoBean.state == 106) {
            subtitleTv.setVisibility(View.VISIBLE);
            subtitleTv.setText("您有一笔收入");
        } else {
            subtitleTv.setVisibility(View.GONE);
        }
        
        // 金额（格式：+金额，居中显示）
        String moneyStr = NumberUtil.formartMoney(infoBean.money);
        quickViewHolder.setText(R.id.item_xiaozhushou_list_money_tv, "+" + moneyStr);
        
        // 交易账户（可能为空）
        TextView accountValueTv = quickViewHolder.getView(R.id.item_xiaozhushou_list_account_value_tv);
        if (!TextUtils.isEmpty(infoBean.payMsg)) {
            accountValueTv.setText(infoBean.payMsg);
        } else {
            accountValueTv.setText("");
        }
        
        // 交易时间（格式：yyyy年MM月dd日 HH:mm:ss）
        String formattedTime = formatTime(infoBean.createTime);
        quickViewHolder.setText(R.id.item_xiaozhushou_list_time_tv, formattedTime);
        
        // 交易单号（使用 msg 或其他字段）
        TextView orderValueTv = quickViewHolder.getView(R.id.item_xiaozhushou_list_order_value_tv);
        if (!TextUtils.isEmpty(infoBean.msg)) {
            orderValueTv.setText(infoBean.msg);
        } else if (!TextUtils.isEmpty(infoBean.title)) {
            orderValueTv.setText(infoBean.title);
        } else {
            orderValueTv.setText("");
        }
    }
    
    /**
     * 格式化时间为：yyyy年MM月dd日 HH:mm:ss
     */
    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            long lt = Long.parseLong(timestamp);
            Date date = new Date(lt);
            return outputFormat.format(date);
        } catch (Exception e) {
            // 如果解析失败，返回原始格式
            return TimeUtil.stampToDate(timestamp);
        }
    }

    String titleText(int type) {
        String result = "";
        switch (type) {
            case 101:
                result = "小助手消息";
                break;
            case 102:
                result = "充值成功";
                break;
            case 103:
                result = "提现审核";
                break;
            case 104:
                result = "提现成功";
                break;
            case 105:
                result = "提现失败";
                break;
            case 106:
                result = "红包退回";
                break;
            case 107:
                result = "系统消息";
                break;
        }
        return result;
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_xiaozhushou_list, viewGroup);
    }
}

