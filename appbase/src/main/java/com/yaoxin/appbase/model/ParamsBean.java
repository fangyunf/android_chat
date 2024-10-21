package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

import java.util.List;

public class ParamsBean {
    public String appType;
    public String version;
    public String phone;
    public String name;
    public String type;
    public String payPassword;
    public String sessionId;
    public int splitCount;
    public String upMsg;
    public String downloadUrl;
    public String absolutePathUrl;
    public String deviceId;
    public String clientType;

    public String title;
    public int amount;
    public List receiverIds;
    public ParamsBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
