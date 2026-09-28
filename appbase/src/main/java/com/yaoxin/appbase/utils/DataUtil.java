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


    public static List<String> adminIds = new ArrayList<>();
    public static String qunzhuId = "";
    private static final String TOKEN = "token";
    private static final String USERID = "user_id";
    private static final String USERInfoList = "USERInfoList2";
    private static final String FriendList = "FriendList";
    private static final String KEFU_ID = "kefu_id";
    private static final String KEFU_MEMBER_CODE = "kefu_member_code";
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
    public static void putKeFuMemberCode(String memberCode) {
        Hawk.put(KEFU_MEMBER_CODE, memberCode);
    }
    public static void putXiaoZhuShouId(String kefuId) {
        Hawk.put(XIAOZHUSHOU_ID, kefuId);
    }

    public static String getKeFuId() {
        return Hawk.get(KEFU_ID);
    }
    public static String getKeFuMemberCode() {
        return Hawk.get(KEFU_MEMBER_CODE);
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
        if (userBean == null || userBean.userId == null || userBean.userId.isEmpty()) {
            return;
        }
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null || arrayList.isEmpty()) {
            arrayList = new ArrayList<>();
        }
        for (int i = 0; i < arrayList.size(); i++) {
            UserBean userInfo = arrayList.get(i);
            if (userInfo != null && userBean.userId.equals(userInfo.userId)) {
                // 刷新资料时保留本地已有的登录凭证，避免 getToken 回包没带 imToken 把切换账号凭证冲掉
                if (isEmpty(userBean.imToken) && !isEmpty(userInfo.imToken)) {
                    userBean.imToken = userInfo.imToken;
                }
                if (isEmpty(userBean.token) && !isEmpty(userInfo.token)) {
                    userBean.token = userInfo.token;
                }
                if (isEmpty(userBean.avatar) && !isEmpty(userInfo.avatar)) {
                    userBean.avatar = userInfo.avatar;
                }
                if (isEmpty(userBean.name) && !isEmpty(userInfo.name)) {
                    userBean.name = userInfo.name;
                }
                if (isEmpty(userBean.username) && !isEmpty(userInfo.username)) {
                    userBean.username = userInfo.username;
                }
                if (isEmpty(userBean.memberCode) && !isEmpty(userInfo.memberCode)) {
                    userBean.memberCode = userInfo.memberCode;
                }
                arrayList.set(i, userBean);
                Hawk.put(USERInfoList, arrayList);
                return;
            }
        }
        arrayList.add(userBean);
        Hawk.put(USERInfoList, arrayList);
    }

    public static void deleteLoginUserInfoList(UserBean userBean) {
        if (userBean == null || userBean.userId == null) {
            return;
        }
        ArrayList<UserBean> arrayList = Hawk.get(USERInfoList);
        if (arrayList == null) {
            return;
        }
        for (int i = 0; i < arrayList.size(); i++) {
            UserBean userInfo = arrayList.get(i);
            if (userInfo != null && userBean.userId.equals(userInfo.userId)) {
                arrayList.remove(i);
                Hawk.put(USERInfoList, arrayList);
                return;
            }
        }
    }

    public static void setFriendInfoList(List<GroupInfoBean> friendInfoList) {
        Hawk.put(FriendList, friendInfoList);
    }
    public static List<GroupInfoBean> getFriendInfoList() {
        List<GroupInfoBean> tempList = Hawk.get(FriendList);
        return tempList != null ? tempList : new ArrayList<>();
    }

    public static void setGroupMemberInfoList(List<GroupInfoBean> friendInfoList) {
        Hawk.put("GroupMemberInfoList", friendInfoList);
    }
    public static List<GroupInfoBean> getGroupMemberList() {
        List<GroupInfoBean> tempList = Hawk.get("GroupMemberInfoList");
        return tempList != null ? tempList : new ArrayList<>();
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

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

}
