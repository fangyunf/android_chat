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
    /** 云信 AppKey，接口 /customer/yunxinId 出参 */
    public String key;
    public String deviceId;
    public String clientType;
    public int page;
    public ParamsBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
