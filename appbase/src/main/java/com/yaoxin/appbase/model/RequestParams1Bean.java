package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

public class RequestParams1Bean {
    public String phone;
    public String id;
    public String userId;
    public String zfb;
    public String name;
    public String usdt;
    public String certNo;
    public int type;
    private String deviceId;
    private String clientType;

    public RequestParams1Bean(String phone1, String name1, int type1) {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
        phone = phone1;
        name = name1;
        type = type1;
    }

    public RequestParams1Bean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
