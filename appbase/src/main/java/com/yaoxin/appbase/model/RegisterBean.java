package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

import java.util.List;

public class RegisterBean {

    public String allDisturb;
    public String caiDanId  ;
    public String sound;
    public String check;
    public String shake;
    public String phoneAdd;
    public String idAdd;
    public String cardAdd;
    public String qrAdd;
    public String zfbNo;
    public String phone;
    public String zfbUrl;
    public String zfb;
    public String payPassword;
    public String phoneNo;
    public String name;
    public String avatar;
    public String date;
    public String requestId;
    public String certifyId;
    public String validate;
    public String jqr;
    public String param;
    public String password;
    public String captcha;
    public String deviceId;
    public String clientType;
    public String phoneAndCode;
    public String memberCode;
    public String msg;
    public String alias;
    public String remark;
    public String pageNo;
    public String page;
    public String userId;
    public String groupId;
    public String head;
    public String announcement;
    public String newGroupUserId;
    public String nickName;
    public String title;
    public String token;
    public int amount;
    public int num;
    public String tradePassword;
    public String toUserId;
    public String metaInfos;
    public String certName;
    public String certNo;
    public String redpacketId;
    public String groupName;
    public String groupHead;
    public List members;
    public int type;
    public int id;
    public int moudleType;
    public String inviteState;
    public int state;
    public String addFriendsState;
    public String shutupState;
    public String nonCollectionState;
    public String grade;

    public RegisterBean(String param) {
        this.param = param;
    }

    public RegisterBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
