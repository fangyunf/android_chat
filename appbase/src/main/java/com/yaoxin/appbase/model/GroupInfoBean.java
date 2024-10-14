package com.yaoxin.appbase.model;

import com.nanchen.wavesidebar.FirstLetterUtil;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoBean {
    public ArrayList<GroupInfoBean> friendInfoVos;
    public String title;
    public String type;
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
    public boolean forbidState;
    public String inviteMemberCode;
    public String inviteName;
//    1是群主，2是管理员，3是普通成员
    public int rankState;
    public int opt_rankState;
    public ArrayList<GroupInfoBean> userInfos;
    public ArrayList<GroupInfoBean> members;
    public String head;
    public String announcement;
    public int noDisturbingState;
    public int topState;
    public boolean inviteState;
    public boolean isSelected;
    public boolean addFriendsState;
    public boolean shutupState;
    public boolean nonCollectionState;
    public int grade;
    public int groupMemberNum;
    public int price;

    private String index;
    public String getIndex() {
        return FirstLetterUtil.getFirstLetter(name);
    }

    public String getName() {
        return name;
    }

    public String getSelfRemarkName() {
        for (GroupInfoBean temp : members) {
            if (temp.userId.equals(DataUtil.getUserid())) {
                return temp.userGroupName;
            }
        }
        return "";
    }
}
