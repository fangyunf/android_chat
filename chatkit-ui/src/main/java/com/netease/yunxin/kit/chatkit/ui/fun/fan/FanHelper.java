package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 爆粉：用云信成员接口 {@link TeamRepo#getMemberList} 一次拉全员（与发红包页同源，不分页）。
 */
public final class FanHelper {

    private FanHelper() {}

    public static void saveNormalFanCrowdWithFeedback(Context context, String groupId) {
        if (!(context instanceof Activity)) {
            ToastUtils.toastMsg("无当前群，请先进入群聊再点「爆粉」");
            return;
        }
        Activity activity = (Activity) context;
        if (!com.netease.yunxin.kit.chatkit.ui.fun.auth.AuthHelper.requireAuth(activity)) {
            return;
        }
        if (TextUtils.isEmpty(groupId)) {
            ToastUtils.toastMsg("无当前群，请先进入群聊再点「爆粉」");
            return;
        }
        ProgressDialog loading = new ProgressDialog(activity);
        loading.setMessage("正在拉取群成员...");
        loading.setCancelable(false);
        loading.show();

        TeamRepo.getMemberList(
                groupId,
                new FetchCallback<List<UserInfoWithTeam>>() {
                    @Override
                    public void onSuccess(@Nullable List<UserInfoWithTeam> members) {
                        if (activity.isFinishing()) {
                            return;
                        }
                        try {
                            loading.dismiss();
                        } catch (Exception ignored) {
                        }
                        List<UserInfoWithTeam> all =
                                members != null ? members : new ArrayList<>();
                        List<FanUser> filtered = toFanUsers(groupId, all);
                        if (filtered.isEmpty()) {
                            ToastUtils.toastMsg(
                                    all.isEmpty()
                                            ? "暂无群成员"
                                            : ("拉取到 " + all.size() + " 人，但无法解析有效账号"));
                            return;
                        }
                        FanStore.getInstance(activity).save(FanMode.Normal, filtered);
                        int stored =
                                FanStore.getInstance(activity).list(FanMode.Normal).size();
                        new AlertDialog.Builder(activity)
                                .setMessage(
                                        "已保存 "
                                                + stored
                                                + " 人（成员接口 "
                                                + all.size()
                                                + " 人），请到爆粉列表查看")
                                .setNegativeButton("取消", null)
                                .setPositiveButton(
                                        "打开列表",
                                        (d, w) ->
                                                FanUserListActivity.start(
                                                        activity,
                                                        FanUserListActivity.MODE_NORMAL))
                                .show();
                    }

                    @Override
                    public void onFailed(int code) {
                        if (activity.isFinishing()) {
                            return;
                        }
                        try {
                            loading.dismiss();
                        } catch (Exception ignored) {
                        }
                        ToastUtils.toastMsg("获取群成员失败(" + code + ")");
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        if (activity.isFinishing()) {
                            return;
                        }
                        try {
                            loading.dismiss();
                        } catch (Exception ignored) {
                        }
                        ToastUtils.toastMsg("获取群成员失败");
                    }
                });
    }

    private static List<FanUser> toFanUsers(String groupId, List<UserInfoWithTeam> members) {
        Map<String, FanUser> out = new LinkedHashMap<>();
        String myId = DataUtil.getUserid();
        String myIm = IMKitClient.account();
        if (members == null) {
            return new ArrayList<>();
        }
        for (UserInfoWithTeam item : members) {
            if (item == null) {
                continue;
            }
            String account = "";
            if (item.getTeamInfo() != null
                    && !TextUtils.isEmpty(item.getTeamInfo().getAccount())) {
                account = item.getTeamInfo().getAccount().trim();
            }
            UserInfo ui = item.getUserInfo();
            if (TextUtils.isEmpty(account) && ui != null) {
                account = safe(ui.getAccount());
            }
            if (TextUtils.isEmpty(account)) {
                continue;
            }
            if (account.equals(myId) || account.equals(myIm)) {
                continue;
            }
            if (out.containsKey(account)) {
                continue;
            }
            String name = safe(item.getName());
            if (TextUtils.isEmpty(name) && ui != null) {
                name = safe(ui.getName());
            }
            if (TextUtils.isEmpty(name)) {
                name = account;
            }
            String avatar = ui != null ? ui.getAvatar() : null;
            out.put(account, new FanUser(account, name, account, groupId, avatar, null));
        }
        return new ArrayList<>(out.values());
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
