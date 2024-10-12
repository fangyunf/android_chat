package com.yaoxin.appbase.net;

import com.google.gson.Gson;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;

import retrofit2.Call;

public class HttpUtil {
    public static ServiceW apiW() {
        return NetManager.getInstance().create(ServiceW.class);
    }
    public static ServiceW api8444() {
        return NetManager.getInstance1().create(ServiceW.class);
    }
    public static ServiceW api8446() {
        return NetManager.getInstance2().create(ServiceW.class);
    }
    public static ServiceW apiWSaveToken() {
        return NetManager.getTokenInstance().create(ServiceW.class);
    }
    private static boolean isTokenExpired() {
        // TODO: 实现 Token 过期的判断逻辑
        long currentTimeMillis = System.currentTimeMillis();
        long lastTimeMillis = DataUtil.getLastRefreshTime();
        if (currentTimeMillis - lastTimeMillis > 30 * 1000 && lastTimeMillis != 0) {
            return true;
        }
        return false;
    }
    // 模拟异步请求 Token
    public static void fetchTokenAsync() {
        if (!DataUtil.isLogin() || !isTokenExpired()) {
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.account = DataUtil.getUserInfo().account;
        bean.password = DataUtil.getUserInfo().password;
        DataUtil.putToken("");
        HttpUtil.apiW().customer_login(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, retrofit2.Response<NetData> response, NetData body) {

                        Gson gson = new Gson();
                        UserBean userBean = gson.fromJson(gson.toJson(body.data), UserBean.class);
                        DataUtil.putToken(userBean.access_token);
                        DataUtil.setLastRefreshTime(System.currentTimeMillis());
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                });
    }
}
