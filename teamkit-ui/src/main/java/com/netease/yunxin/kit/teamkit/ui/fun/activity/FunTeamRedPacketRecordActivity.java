package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunZhuanZhangResultActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.teamkit.ui.databinding.ActivityFunTeamRedPacketRecordBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamRedPacketRecordAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class FunTeamRedPacketRecordActivity extends BaseActivity implements View.OnClickListener {
    private static final int PAGE_SIZE = 20;

    private ActivityFunTeamRedPacketRecordBinding binding;
    private final List<TeamRedPacketRecordAdapter.ItemData> records = new ArrayList<>();
    private final Set<String> seenMessageIds = new HashSet<>();
    private final TeamRedPacketRecordAdapter adapter = new TeamRedPacketRecordAdapter();
    private final Gson gson = new Gson();
    private String groupId;
    private IMMessage anchorMessage;
    private boolean loading = false;
    private boolean noMore = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        _getParams();
        if (extras != null) {
            groupId = extras.getString("groupId");
        }
        super.onCreate(savedInstanceState);
        binding = ActivityFunTeamRedPacketRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityFunTeamRedPacketRecordNav);
        _initView();
        refreshFromStart();
    }

    @Override
    protected void _initView() {
        binding.activityFunTeamRedPacketRecordNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunTeamRedPacketRecordRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityFunTeamRedPacketRecordRv.setAdapter(adapter);
        adapter.setOnItemClickListener(this::openDetailPage);
        binding.activityFunTeamRedPacketRecordRefreshLayout.setEnableRefresh(true);
        binding.activityFunTeamRedPacketRecordRefreshLayout.setEnableLoadMore(true);
        binding.activityFunTeamRedPacketRecordRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        binding.activityFunTeamRedPacketRecordRefreshLayout.setOnRefreshListener(refreshLayout -> refreshFromStart());
        binding.activityFunTeamRedPacketRecordRefreshLayout.setOnLoadMoreListener(refreshLayout -> loadMoreFromYunXin());
    }

    @Override
    protected void _requestData() {
        if (binding == null) {
            return;
        }
        if (TextUtils.isEmpty(groupId)) {
            ToastUtils.toastMsg("群ID为空");
            return;
        }
        refreshFromStart();
    }

    private void refreshFromStart() {
        if (loading) {
            binding.activityFunTeamRedPacketRecordRefreshLayout.finishRefresh();
            return;
        }
        records.clear();
        seenMessageIds.clear();
        adapter.setItems(records);
        adapter.notifyDataSetChanged();
        anchorMessage = null;
        noMore = false;
        binding.activityFunTeamRedPacketRecordRefreshLayout.resetNoMoreData();
        loadMoreFromYunXin();
    }

    private void loadMoreFromYunXin() {
        if (loading || noMore) {
            if (noMore) {
                binding.activityFunTeamRedPacketRecordRefreshLayout.finishLoadMoreWithNoMoreData();
            }
            return;
        }
        loading = true;
        if (anchorMessage == null) {
            anchorMessage = MessageBuilder.createEmptyMessage(groupId, SessionTypeEnum.Team, System.currentTimeMillis());
        }
        NIMClient.getService(MsgService.class)
                .queryMessageListEx(anchorMessage, QueryDirectionEnum.QUERY_OLD, PAGE_SIZE, true)
                .setCallback(new RequestCallback<List<IMMessage>>() {
                    @Override
                    public void onSuccess(List<IMMessage> messages) {
                        loading = false;
                        if (messages == null || messages.isEmpty()) {
                            noMore = true;
                            finishRefreshLoadMore(true);
                            showEmptyIfNeeded();
                            return;
                        }
                        anchorMessage = messages.get(messages.size() - 1);
                        appendRecords(messages);
                        if (messages.size() < PAGE_SIZE) {
                            noMore = true;
                        }
                        finishRefreshLoadMore(noMore);
                    }

                    @Override
                    public void onFailed(int code) {
                        loading = false;
                        finishRefreshLoadMore(false);
                        showEmptyIfNeeded();
                    }

                    @Override
                    public void onException(Throwable exception) {
                        loading = false;
                        finishRefreshLoadMore(false);
                        showEmptyIfNeeded();
                    }
                });
    }

    private void finishRefreshLoadMore(boolean reachNoMore) {
        if (binding.activityFunTeamRedPacketRecordRefreshLayout.isRefreshing()) {
            binding.activityFunTeamRedPacketRecordRefreshLayout.finishRefresh();
        }
        if (binding.activityFunTeamRedPacketRecordRefreshLayout.isLoading()) {
            if (reachNoMore) {
                binding.activityFunTeamRedPacketRecordRefreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                binding.activityFunTeamRedPacketRecordRefreshLayout.finishLoadMore();
            }
        } else if (reachNoMore) {
            binding.activityFunTeamRedPacketRecordRefreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    private void appendRecords(List<IMMessage> messages) {
        for (IMMessage message : messages) {
            if (message == null
                    || message.getSessionType() != SessionTypeEnum.Team
                    || !TextUtils.equals(groupId, message.getSessionId())
                    || message.getMsgType() != MsgTypeEnum.custom
                    || TextUtils.isEmpty(message.getAttachStr())) {
                continue;
            }
            CustomMsgBean wrap = parseWrapBean(message.getAttachStr());
            if (wrap == null || !isSupportedRedPacketType(wrap.type)) {
                continue;
            }
            CustomMsgBean body = parseBodyBean(wrap.data);
            TeamRedPacketRecordAdapter.ItemData item = buildItemData(wrap, body, message);
            if (item != null) {
                String uniqueId = readMessageUniqueId(message);
                if (TextUtils.isEmpty(uniqueId) || seenMessageIds.contains(uniqueId)) {
                    continue;
                }
                seenMessageIds.add(uniqueId);
                records.add(item);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            records.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        }
        adapter.setItems(records);
        adapter.notifyDataSetChanged();
        showEmptyIfNeeded();
    }

    private String readMessageUniqueId(IMMessage message) {
        if (!TextUtils.isEmpty(message.getUuid())) {
            return message.getUuid();
        }
        return message.getSessionId() + "_" + message.getFromAccount() + "_" + message.getTime();
    }

    private CustomMsgBean parseWrapBean(String attachStr) {
        try {
            return gson.fromJson(attachStr, CustomMsgBean.class);
        } catch (Exception e) {
            return null;
        }
    }

    private CustomMsgBean parseBodyBean(String data) {
        if (TextUtils.isEmpty(data)) {
            return new CustomMsgBean();
        }
        try {
            return gson.fromJson(data, CustomMsgBean.class);
        } catch (Exception e) {
            return new CustomMsgBean();
        }
    }

    private boolean isSupportedRedPacketType(int type) {
        return type == 21 || type == 22 || type == 23 || type == 28;
    }

    private TeamRedPacketRecordAdapter.ItemData buildItemData(CustomMsgBean wrap, CustomMsgBean body, IMMessage message) {
        TeamRedPacketRecordAdapter.ItemData item = new TeamRedPacketRecordAdapter.ItemData();
        item.timestamp = message.getTime();
        item.dayLabel = formatDayLabel(message.getTime());
        item.timeLabel = formatTimeLabel(message.getTime());
        item.senderName = resolveSenderName(body, message);
        item.avatarUrl = resolveAvatar(body);
        item.amountLabel = formatAmount(body.amount);
        item.title = resolveTitle(wrap.type, item.senderName);
        item.subTitle = resolveSubTitle(wrap.type, body);
        item.customType = wrap.type;
        item.sendName = item.senderName;
        item.sendAvatar = item.avatarUrl;
        item.amount = body.amount;
        item.toUserId = body.toUserId;
        item.createTime = body.createTime;
        item.redpacketId = resolveRedpacketId(wrap, body);
        return item;
    }

    private String resolveRedpacketId(CustomMsgBean wrap, CustomMsgBean body) {
        if (!TextUtils.isEmpty(body.redPacketId)) {
            return body.redPacketId;
        }
        if (!TextUtils.isEmpty(body.id)) {
            return body.id;
        }
        if (!TextUtils.isEmpty(wrap.redPacketId)) {
            return wrap.redPacketId;
        }
        return wrap.id;
    }

    private String resolveSenderName(CustomMsgBean body, IMMessage message) {
        if (!TextUtils.isEmpty(body.sendName)) {
            return body.sendName;
        }
        if (!TextUtils.isEmpty(body.fromUserId) && TextUtils.equals(body.fromUserId, DataUtil.getUserid())) {
            return "你";
        }
        if (!TextUtils.isEmpty(message.getFromNick())) {
            return message.getFromNick();
        }
        return message.getFromAccount();
    }

    private String resolveAvatar(CustomMsgBean body) {
        if (!TextUtils.isEmpty(body.sendAvatar)) {
            return body.sendAvatar;
        }
        if (!TextUtils.isEmpty(body.avatar)) {
            return body.avatar;
        }
        return "";
    }

    private String resolveTitle(int type, String senderName) {
        if (type == 28) {
            return senderName + "发起转账";
        }
        if (type == 21) {
            return senderName + "发了一个专属红包";
        }
        return senderName + "发了一个拼手气红包";
    }

    private String resolveSubTitle(int type, CustomMsgBean body) {
        if (type == 28) {
            return "已被接收";
        }
        if (type == 21) {
            return "给专属的人";
        }
        if (!TextUtils.isEmpty(body.title)) {
            return body.title;
        }
        return "拼手气红包";
    }

    private String formatAmount(String amount) {
        if (TextUtils.isEmpty(amount)) {
            return "¥0.00";
        }
        return "¥" + amount;
    }

    private String formatDayLabel(long time) {
        Date date = new Date(time);
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
        java.util.Calendar target = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
        target.setTime(date);
        if (isSameDay(cal, target)) {
            return "今天";
        }
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(cal, target)) {
            return "昨天";
        }
        int nowYear = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA).get(java.util.Calendar.YEAR);
        if (target.get(java.util.Calendar.YEAR) == nowYear) {
            return target.get(java.util.Calendar.MONTH) + 1 + "月" + target.get(java.util.Calendar.DAY_OF_MONTH) + "日";
        }
        return target.get(java.util.Calendar.YEAR) + "年" + (target.get(java.util.Calendar.MONTH) + 1) + "月" + target.get(java.util.Calendar.DAY_OF_MONTH) + "日";
    }

    private boolean isSameDay(java.util.Calendar a, java.util.Calendar b) {
        return a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR)
                && a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private String formatTimeLabel(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(time));
    }

    private void showEmptyIfNeeded() {
        binding.activityFunTeamRedPacketRecordEmptyTv.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openDetailPage(TeamRedPacketRecordAdapter.ItemData item) {
        if (item == null) {
            return;
        }
        if (!TextUtils.isEmpty(item.redpacketId)) {
            HashMap map = new HashMap();
            map.put("redpacketId", item.redpacketId);
            FunRedPacketResultActivity.start(FunRedPacketResultActivity.class, this, map);
            return;
        }
        if (item.customType == 28) {
            CustomMsgBean bean = new CustomMsgBean();
            bean.sendName = item.sendName;
            bean.sendAvatar = item.sendAvatar;
            bean.amount = item.amount;
            bean.toUserId = item.toUserId;
            HashMap map = new HashMap();
            map.put("bean", new Gson().toJson(bean));
            FunZhuanZhangResultActivity.start(FunZhuanZhangResultActivity.class, this, map);
            return;
        }
        ToastUtils.toastMsg("红包详情暂不可用");
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunTeamRedPacketRecordNav.addCloseImageButton()) {
            finish();
        }
    }
}
