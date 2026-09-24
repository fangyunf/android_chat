package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

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
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;

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
            quickViewHolder.setText(
                    R.id.cell_fun_team_setting_users_name_tv, displayMemberName(infoBean));

            GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 26);
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

    /** 优先好友备注，其次成员自身 remark，再回退昵称 */
    public static String displayMemberName(GroupInfoBean infoBean) {
        if (infoBean == null) {
            return "";
        }
        String friendRemark = findFriendRemark(infoBean.userId);
        if (!TextUtils.isEmpty(friendRemark)) {
            return friendRemark;
        }
        if (!TextUtils.isEmpty(infoBean.remark)) {
            return infoBean.remark;
        }
        return TextUtils.isEmpty(infoBean.name) ? "" : infoBean.name;
    }

    private static String findFriendRemark(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return "";
        }
        try {
            List<GroupInfoBean> friendList = DataUtil.getFriendInfoList();
            if (friendList == null) {
                return "";
            }
            for (GroupInfoBean friend : friendList) {
                if (friend != null
                        && userId.equals(friend.userId)
                        && !TextUtils.isEmpty(friend.remark)) {
                    return friend.remark;
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users, viewGroup);
    }

    protected int getItemCount(@NonNull List<? extends GroupInfoBean> items) {
        if (isManager) {
            return userInfoList.size() + 2;
        }
        return userInfoList.size() + 1;
    }
}
