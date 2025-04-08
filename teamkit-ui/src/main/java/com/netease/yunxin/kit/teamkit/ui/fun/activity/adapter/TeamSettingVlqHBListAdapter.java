package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import org.w3c.dom.Text;

import java.util.List;

public class TeamSettingVlqHBListAdapter extends BaseQuickAdapter<CustomMsgBean, QuickViewHolder> {

    public int opt_type = 0;
    public List<GroupInfoBean> contacts;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable CustomMsgBean bean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_vlq_hb_list_bg_iv);
        TextView greetTv = quickViewHolder.getView(R.id.cell_fun_team_setting_vlq_hb_list_greeting_tv);
        TextView typeTv = quickViewHolder.getView(R.id.cell_fun_team_setting_vlq_hb_list_type_tv);
        TextView timeTv = quickViewHolder.getView(R.id.cell_fun_team_setting_vlq_hb_list_time_tv);
        TextView moneyTv = quickViewHolder.getView(R.id.cell_fun_team_setting_vlq_hb_list_money_tv);

        if (bean.type == 21) {
            greetTv.setText(bean.title);
            typeTv.setText("专属红包");
        } else if (bean.type == 22){
            greetTv.setText(bean.title);
            typeTv.setText("红包");
        } else {
            greetTv.setText(bean.title);
            typeTv.setText("拼手气红包");
        }
        timeTv.setText(TimeUtil.stampToDate(bean.createTime));
        moneyTv.setText("¥" + NumberUtil.formartMoney(bean.amount));
        iv.setImageResource( (bean.type == 21) ? com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_redpacket_purple_bg_no_open: com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_red_packet_cell_bg_no_open);

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_vlq_hb_list, viewGroup);
    }
}

