package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

public class ParamsBean {
    public String appType;
    public String version;
    public String phone;
    public String name;
    public String type;
    public String upMsg;
    public String downloadUrl;
    public String absolutePathUrl;
    public String deviceId;
    public String clientType;
    public String amount;


    public String goodsTitle;
    public String goodsDesc;
    public String description;
    public String token_no;
    public String in_member_id;
    public String out_member_id;

    public String configId;
    public String userId;
    public ParamsBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
