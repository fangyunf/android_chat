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
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

public class TeamSettingUserMingDanListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int opt_type = 0;
    public List<GroupInfoBean> contacts;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_head_iv);

        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_tv_index);
        // 优先显示备注名，如果没有备注名则显示本名
        String displayName = getDisplayName(infoBean);
        quickViewHolder.setText(R.id.cell_fun_team_setting_users_mingdan_name_tv, displayName);
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 22);

        TextView state_tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_state_tv);
        state_tv.setSelected(infoBean.forbidState == 1);
        state_tv.setTextColor(getContext().getResources().getColor(infoBean.forbidState == 1 ?R.color.color_white:R.color.color_999999));
        state_tv.setText(infoBean.forbidState == 0 ? "禁止": "禁领中");
        if (position == 0 || !contacts.get(position-1).getIndex().equals(infoBean.getIndex())) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(infoBean.getIndex());
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users_mingdan, viewGroup);
    }
    
    /**
     * 获取显示名称，优先使用备注名
     */
    private String getDisplayName(GroupInfoBean infoBean) {
        // 使用ChatUserCache获取正确的显示名称，它会优先使用好友备注名
        String displayName = ChatUserCache.getName(null, infoBean.userId);
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        // 如果ChatUserCache没有找到，使用群内备注名
        if (infoBean.userGroupName != null && !infoBean.userGroupName.trim().isEmpty()) {
            return infoBean.userGroupName;
        }
        // 最后使用本名
        return infoBean.name != null ? infoBean.name : "";
    }
}

