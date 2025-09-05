package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.List;

public class TeamSettingUserInfoAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    List<GroupInfoBean> userInfoList;

    boolean isManager = false;
    public TeamSettingUserInfoAdapter(boolean isM, List<GroupInfoBean> userInfo) {
        isManager = isM;
        userInfoList = userInfo;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean bean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_head_iv);

        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_name_tv_role);
        if (i >= userInfoList.size()) {
            if (i == userInfoList.size()) {
                iv.setImageResource(com.yaoxin.appbase.R.drawable.team_setting_add_user);
                quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, "邀请成员");
            } else {
                iv.setImageResource(com.yaoxin.appbase.R.drawable.team_setting_delete_user);
                quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, "删除成员");
            }
        } else {
            GroupInfoBean infoBean = userInfoList.get(i);
            // 优先显示备注名，如果没有备注名则显示本名
            String displayName = getDisplayName(infoBean);
            quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, displayName);
            GlideUtil.yh_loadImageRoundedCorner(getContext(),iv,infoBean.avatar,26);
            tv.setVisibility(View.VISIBLE);
            if (infoBean.rankState == 1) {
                tv.setText("群主");
            } else if (infoBean.rankState == 2) {
                tv.setText("管理");
            } else {
                tv.setVisibility(View.GONE);
            }

        }
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users,viewGroup);
    }
    protected int getItemCount(@NonNull List<? extends GroupInfoBean> items) {
        if (isManager) {
            return userInfoList.size() + 2;
        }
        return userInfoList.size() + 1;
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

