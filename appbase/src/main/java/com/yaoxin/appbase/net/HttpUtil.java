package com.yaoxin.appbase.net;

public class HttpUtil {
    public static ServiceW apiW() {
        return NetManager.getInstance().create(ServiceW.class);
    }
    public static ServiceW apiWSaveToken() {
        return NetManager.getTokenInstance().create(ServiceW.class);
    }

}
