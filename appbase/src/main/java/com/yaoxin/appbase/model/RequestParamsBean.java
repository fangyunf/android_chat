package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

public class RequestParamsBean {
    private String phone;
    public String id;
    public String payChannel;
    public String zfb;
    public String certNo;
    public String usdt;
    public String name;
    public String configId;
    public String payWay;
    public int amount;
    public int summary;
    private String phone1;
    public String type;
    private String deviceId;
    private String clientType;
    public RequestParamsBean(String phone1,String name1, String type1) {
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
