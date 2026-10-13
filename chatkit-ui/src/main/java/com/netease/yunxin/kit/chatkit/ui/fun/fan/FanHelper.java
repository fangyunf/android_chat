package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import android.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

/** 爆粉：拉取群成员写入本地名单（对齐 qiangbao saveNormalFanCrowd）。 */
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
        loading.setMessage("正在保存群成员...");
        loading.setCancelable(false);
        loading.show();

        fetchAllMembers(
                groupId,
                new ArrayList<>(),
                1,
                (members, err) -> {
                    if (activity.isFinishing()) {
                        return;
                    }
                    try {
                        loading.dismiss();
                    } catch (Exception ignored) {
                    }
                    if (err != null) {
                        ToastUtils.toastMsg(err);
                        return;
                    }
                    String myId = DataUtil.getUserid();
                    Set<String> seen = new HashSet<>();
                    List<FanUser> filtered = new ArrayList<>();
                    for (GroupInfoBean m : members) {
                        if (m == null) {
                            continue;
                        }
                        String memberCode =
                                !TextUtils.isEmpty(m.memberCode) ? m.memberCode.trim() : "";
                        String hostUid = !TextUtils.isEmpty(m.userId) ? m.userId.trim() : "";
                        String displayId =
                                !TextUtils.isEmpty(memberCode) ? memberCode : hostUid;
                        if (TextUtils.isEmpty(displayId)) {
                            continue;
                        }
                        if (!TextUtils.isEmpty(myId)
                                && (myId.equals(hostUid) || myId.equals(memberCode))) {
                            continue;
                        }
                        if (!seen.add(displayId)) {
                            continue;
                        }
                        String name =
                                !TextUtils.isEmpty(m.name)
                                        ? m.name
                                        : (!TextUtils.isEmpty(m.userGroupName)
                                                ? m.userGroupName
                                                : displayId);
                        filtered.add(
                                new FanUser(
                                        displayId,
                                        name,
                                        hostUid,
                                        groupId,
                                        m.avatar));
                    }
                    if (filtered.isEmpty()) {
                        ToastUtils.toastMsg("暂无群成员");
                        return;
                    }
                    int[] result =
                            FanStore.getInstance(activity)
                                    .mergeGroupMembers(FanMode.Normal, groupId, filtered);
                    if (result[1] <= 0 && result[0] <= 0) {
                        ToastUtils.toastMsg("暂无群成员");
                        return;
                    }
                    new AlertDialog.Builder(activity)
                            .setMessage("已保存到本地,请到爆粉列表查看")
                            .setNegativeButton("取消", null)
                            .setPositiveButton(
                                    "打开列表",
                                    (d, w) ->
                                            FanUserListActivity.start(
                                                    activity, FanUserListActivity.MODE_NORMAL))
                            .show();
                });
    }

    private interface MembersCallback {
        void onDone(List<GroupInfoBean> members, String err);
    }

    private static void fetchAllMembers(
            String groupId, List<GroupInfoBean> acc, int page, MembersCallback cb) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = String.valueOf(page);
        bean.pageNo = "100";
        HttpUtil.apiW()
                .group_groupUserListPost(bean)
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                if (body == null || body.data == null) {
                                    cb.onDone(acc, "暂无群成员");
                                    return;
                                }
                                try {
                                    Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                                    List<GroupInfoBean> pageList =
                                            new Gson().fromJson(body.data.toString(), type);
                                    if (pageList != null && !pageList.isEmpty()) {
                                        acc.addAll(pageList);
                                        if (pageList.size() >= 100) {
                                            fetchAllMembers(groupId, acc, page + 1, cb);
                                            return;
                                        }
                                    }
                                    cb.onDone(acc, null);
                                } catch (Exception e) {
                                    cb.onDone(acc, "解析群成员失败");
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                cb.onDone(acc, "获取群成员失败");
                            }
                        });
    }
}
