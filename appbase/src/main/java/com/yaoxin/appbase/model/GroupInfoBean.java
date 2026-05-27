package com.yaoxin.appbase.model;

import com.nanchen.wavesidebar.FirstLetterUtil;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoBean {
    public String title;
    public String money;
    public String payTerm;
    public String payMsg;
    public String msg;
    public String createTime;
    public int state;
    public int check;
    public int phoneAdd;
    public int idAdd;
    public int cardAdd;
    public int qrAdd;
    public int addState;
    public int addGroupState;
    public int friendApplyNum;
    public int groupApplyNum;
    public String content;
    public String remark;
    public String groupId;
    public List<GroupInfoBean> data;
    public String avatar;
    public String name;
    public String userGroupName;
    public String memberCode;
    public String userId;
    public int forbidState;
    /** 单人禁言：0 未禁言，1 已禁言（来自云信 TeamMember.isMute） */
    public int muteState;
    public String inviteMemberCode;
    public String inviteName;
//    1是群主，2是管理员，3是普通成员
    public int rankState;
    public int opt_rankState;
    public ArrayList<GroupInfoBean> userInfos;
    public String head;
    public String announcement;
    public int noDisturbingState;
    public int topState;
    public int inviteState;
    public boolean isSelected;
    public int addFriendsState;
    public int shutupState;
    public int nonCollectionState;
    public int grade;
    public int groupMemberNum;
    public int price;
    public String price1;
    public String gradeName;
    public String maxMembers;
    public String discount;
    public String mallDiscount;

    private String index;
    public String getIndex() {
        String displayName = name;
        if (remark != null && !remark.isEmpty()) {
            displayName = remark;
        }
        return FirstLetterUtil.getFirstLetter(displayName);
    }

    public String getName() {
        return name;
    }

    public String getSelfRemarkName() {
        for (GroupInfoBean temp : userInfos) {
            if (temp.userId.equals(DataUtil.getUserid())) {
                return temp.userGroupName;
            }
        }
        return "";
    }
}
