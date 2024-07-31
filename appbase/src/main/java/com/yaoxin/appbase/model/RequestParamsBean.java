package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

public class RequestParamsBean {
    public String certNo;
    public String usdt;
    private String phone;
    public String id;
    public int amount;
    private String phone1;
    private String name;
    private int type;
    private String deviceId;
    private String clientType;
    public RequestParamsBean(String phone1,String name1, int type1) {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
        phone = phone1;
        name = name1;
        type = type1;
    }

    public RequestParamsBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
