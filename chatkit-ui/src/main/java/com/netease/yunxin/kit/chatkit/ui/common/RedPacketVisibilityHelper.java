package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.List;

/**
 * 红包消息可见性：专属红包仅目标人 / 发包人 / 群主 / 群管理可见。
 */
public final class RedPacketVisibilityHelper {

    private RedPacketVisibilityHelper() {}

    /**
     * @return true 表示当前用户可见该红包消息
     */
    public static boolean canSee(CustomMsgBean wrap, String sessionId, SessionTypeEnum sessionType) {
        if (wrap == null) {
            return true;
        }
        CustomMsgBean body = wrap.result != null ? wrap.result : wrap;
        int type = wrap.type;
        String uid = DataUtil.getUserid();
        if (TextUtils.isEmpty(uid)) {
            return true;
        }
        // 拼手气群红包：全员可见
        if (type == 23) {
            return true;
        }
        // 个人红包：收发双方
        if (type == 22) {
            return TextUtils.equals(uid, body.toUserId) || TextUtils.equals(uid, body.fromUserId);
        }
        // 专属红包：目标人 / 发包人 / 消息 adminIds / 本群群主或管理员
        if (type == 21) {
            if (TextUtils.equals(uid, body.toUserId) || TextUtils.equals(uid, body.fromUserId)) {
                return true;
            }
            List<String> adminIds = body.adminIds;
            if (adminIds != null && adminIds.contains(uid)) {
                return true;
            }
            if (sessionType == SessionTypeEnum.Team && !TextUtils.isEmpty(sessionId)) {
                return isTeamOwnerOrManager(sessionId, uid);
            }
            return false;
        }
        return true;
    }

    public static boolean isTeamOwnerOrManager(String teamId, String account) {
        try {
            TeamMember member =
                    NIMClient.getService(TeamService.class).queryTeamMemberBlock(teamId, account);
            if (member == null || member.getType() == null) {
                return false;
            }
            TeamMemberType type = member.getType();
            return type == TeamMemberType.Owner || type == TeamMemberType.Manager;
        } catch (Exception e) {
            return false;
        }
    }
}
