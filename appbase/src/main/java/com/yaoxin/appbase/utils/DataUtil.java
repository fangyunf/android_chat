package com.yaoxin.appbase.utils;

import com.orhanobut.hawk.Hawk;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.UserBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will
 * on 2018/6/19.
 */
public class DataUtil {

    private static final String TOKEN = "token";
    private static final String USERID = "user_id";
    private static final String USERInfoList = "USERInfoList2";
    private static final String FriendList = "FriendList";
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
    public static List<UserBean> getLoginUserInfoList() {
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null || arrayList.isEmpty()) {
//            arrayList = new ArrayList<>();
            addLoginUserInfoList(DataUtil.getUserInfo());
            arrayList = Hawk.get(USERInfoList);
        }
        return arrayList;
    }
    public static void addLoginUserInfoList(UserBean userBean) {
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null || arrayList.isEmpty()) {
            arrayList = new ArrayList<>();
        }
        boolean hasUser = false;
        for (UserBean userInfo : arrayList) {
            if (userInfo.userId.equals(userBean.userId)) {
                hasUser = true;
                break;
            }
        }
        if (!hasUser) {
            arrayList.add(userBean);
            Hawk.put(USERInfoList, arrayList);
        }
    }
    public static void deleteLoginUserInfoList(UserBean userBean) {
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null) {
            return;
        }
        boolean hasUser = false;
        for (UserBean userInfo : arrayList) {
            if (userInfo.userId.equals(userBean.userId)) {
                hasUser = true;
                arrayList.remove(userInfo);
                Hawk.put(USERInfoList, arrayList);
                return;
            }
        }
    }

    public static void setFriendInfoList(List<GroupInfoBean> friendInfoList) {
        Hawk.put(FriendList, friendInfoList);
    }
    public static List<GroupInfoBean> getFriendInfoList() {
        return Hawk.get(FriendList);
    }

    public static void updateLoginUserInfoList(UserBean userBean) {
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null) {
            return;
        }
        for (UserBean userInfo : arrayList) {
            if (userInfo.userId.equals(userBean.userId)) {
                arrayList.remove(userInfo);
                arrayList.add(userBean);
                Hawk.put(USERInfoList, arrayList);
                return;
            }
        }
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
        Hawk.put(FriendList, new ArrayList<>());

    }

    public static void setStringValue(String jsonStr,String key) {
        Hawk.put(key, jsonStr);
    }
    public static String getStringValue(String key) {
        return Hawk.get(key);
    }

}
