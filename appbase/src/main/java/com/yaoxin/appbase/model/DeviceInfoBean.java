package com.yaoxin.appbase.model;

import com.yaoxin.appbase.utils.DataUtil;

import java.util.List;

public class DeviceInfoBean {

    public long id;
    public String userId;
    public String ua;
    public String deviceId;
    public String uaDevice;

    public String ip;

    public long createTime;

    public long updateTime;


    public String getDeviceName() {
        // 如果 ua 为空，返回空字符串
        if (ua == null || ua.isEmpty()) {
            return "";
        }

        // 如果 ua 包含 = 号，提取设备名称部分（第一个 = 号之前的内容）
        if (ua.contains("=")) {
            int index = ua.indexOf("=");
            return ua.substring(0, index);
        }

        // 如果 ua 不包含 = 号，直接返回 ua
        return ua;
    }

}
