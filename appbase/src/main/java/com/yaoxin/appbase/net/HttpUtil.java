package com.yaoxin.appbase.net;

public class HttpUtil {
    public static ServiceW apiW() {
        return NetManager.getInstance().create(ServiceW.class);
    }
    public static ServiceW api8444() {
        return NetManager.getInstance1().create(ServiceW.class);
    }
    public static ServiceW apiWSaveToken() {
        return NetManager.getTokenInstance().create(ServiceW.class);
    }

}
