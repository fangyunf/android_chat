package com.yaoxin.appbase.model;

import com.google.gson.annotations.SerializedName;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.List;

public class CustomMsgBean {
    public CustomMsgBean result;
    public String caidanLs;//需要流水
    public String groupLs;//当前流水
    public String fafId;
    public String data;
    public String sendUserId;
    public String sendUserName;
    public String receiveUserName;
    public String receiveUserId;
    public int type;
    public String amount;
    public String createTime;
    public String fromUserId;
    public String id;
    public String img;
    public String price;
    public String level;
    public String sendAvatar;
    public String sendName;
    public String title;
    public String toUserId;
    public String toUserName;
    public String redPacketId;
    public String sendAmount;
    public String totalNum;
    public List<CustomMsgBean> vos;
    public String userId;
    public String avatar;
    public String memberCode;
    public String name;
    public String reciveTime;
    public String reciveAmount;
    public String lootAll;
    public String sendLevel;
    public List<String> adminIds;

    //type == 21 专属 || type == 22 个人 || type == 23 群
    public int redpacketType;
    public String groupId;
    public String groupName;
    public int num;
    public String sendId;
    public String sendTime;
    public boolean isBest;
    public boolean hasOpened;

    public String transcationId;

    public boolean isHasOpened() {
        for (CustomMsgBean tempBean :
                vos) {
            if (tempBean.userId.equals(DataUtil.getUserid())) {
                return true;
            }
        }
        return false;
    }

    /*
     * {"data":"{\"amount\":1,\"createTime\":1718249162000,\"fromUserId\":\"1800528223122104320\",\"id\":\"1801093644191670272\",\"level\":0,\"sendAvatar\":\"https://ao/defaultAvatar/15.png\",\"sendName\":\"YM\",\"title\":\"大吉大利\",\"toUserId\":\"1800527828803002368\",\"toUserName\":\"FZYM\"}","type":22}
     * */

}
