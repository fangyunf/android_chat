package com.yaoxin.appbase.utils;

import com.orhanobut.hawk.Hawk;
import com.yaoxin.appbase.model.UserBean;

/**
 * Created by will
 * on 2018/6/19.
 */
public class DataUtil {

    private static final String TOKEN = "token";
    private static final String USERID = "user_id";
    private static final String KEFU_ID = "kefu_id";
    private static final String XIAOZHUSHOU_ID = "xiaozhushou_id";

    public static void putToken(String token) {
        Hawk.put(TOKEN, token);
    }

    public static String getToken() {
        return Hawk.get(TOKEN,"");
    }

    public static void putUserInfo(UserBean userBean) {
        Hawk.put(USERID, userBean);
    }
    public static void putKeFuId(String kefuId) {
        Hawk.put(KEFU_ID, kefuId);
    }
    public static void putXiaoZhuShouId(String kefuId) {
        Hawk.put(XIAOZHUSHOU_ID, kefuId);
    }

    public static String getKeFuId() {
        return Hawk.get(KEFU_ID);
    }
    public static String getXiaoZhuShouId() {
        return Hawk.get(XIAOZHUSHOU_ID);
    }

    public static UserBean getUserInfo() {
        UserBean userBean = Hawk.get(USERID);
        if (userBean != null) {
            return userBean;
        }
        return new UserBean();
    }

    public static String getUserid() {
        if (getUserInfo() != null) {

            return getUserInfo().userId;
        }
        return "";
    }
    public static void deleteData() {
        DataUtil.putToken("");
        DataUtil.putUserInfo(null);
    }

}
