// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.team.constant.TeamAllMuteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;

import java.util.List;
import java.util.Map;

/**
 * 群通知消息文案构造类，将通知转换为消息展示文案。
 *
 * <p>各类通知里的 {@code fromUser}（操作者）均可能为空，此时应传入 {@code fromAccount}（一般为消息的
 * {@code getFromAccount()}），本类会按账号走群昵称缓存展示；两者皆空时，操作者位置展示为空串，其余文案仍尽量拼接。
 */
public class TeamNotificationHelper {

    /** 全员禁言开关在聊天里下发的通知文案；详情页需折叠不占高度时用于识别。 */
    public static boolean isAllMuteOnlyConversationTipText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        String s = text.toString().trim();
        return s.equals(IMKitClient.getApplicationContext().getString(R.string.chat_team_cancel_all_mute))
                || s.equals(IMKitClient.getApplicationContext().getString(R.string.chat_team_full_mute));
    }

    public static String getTeamNotificationText(IMMessageInfo message) {
        if (message == null || message.getMessage() == null) {
            return "";
        }
        String fromAccount = message.getMessage().getFromAccount();
        return buildNotification(
                message.getMessage().getSessionId(),
                message.getFromUser(),
                fromAccount,
                (NotificationAttachment) message.getMessage().getAttachment());
    }

    /**
     * 会话列表等：用摘要里的 attachment 生成预览。{@code fromUser}、{@code fromAccount} 可只填其一。
     */
    public static String getNotificationPreviewFromAttachment(
            String teamId, UserInfo fromUser, NotificationAttachment attachment) {
        return getNotificationPreviewFromAttachment(teamId, fromUser, null, attachment);
    }

    public static String getNotificationPreviewFromAttachment(
            String teamId, UserInfo fromUser, String fromAccount, NotificationAttachment attachment) {
        if (attachment == null || TextUtils.isEmpty(teamId)) {
            return "";
        }
        // 踢人、禁言等可不依赖操作者；退群、改群名等 fromUser/fromAccount 皆空时前缀为空，仍返回其余文案
        try {
            return buildNotification(teamId, fromUser, fromAccount, attachment);
        } catch (Exception e) {
            return "";
        }
    }

    private static String buildNotification(
            String tid, UserInfo fromUser, String fromAccount, NotificationAttachment attachment) {
        String text;
        switch (attachment.getType()) {
            case InviteMember:
            case SUPER_TEAM_INVITE:
                text = buildInviteMemberNotification(tid, ((MemberChangeAttachment) attachment), fromUser, fromAccount);
                break;
            case KickMember:
            case SUPER_TEAM_KICK:
                text = buildKickMemberNotification(tid, ((MemberChangeAttachment) attachment));
                break;
            case LeaveTeam:
            case SUPER_TEAM_LEAVE:
                text = buildLeaveTeamNotification(tid, fromUser, fromAccount);
                break;
            case DismissTeam:
            case SUPER_TEAM_DISMISS:
                text = buildDismissTeamNotification(tid, fromUser, fromAccount);
                break;
            case UpdateTeam:
            case SUPER_TEAM_UPDATE_T_INFO:
                text = buildUpdateTeamNotification(tid, fromUser, fromAccount, (UpdateTeamAttachment) attachment);
                break;
            case PassTeamApply:
            case SUPER_TEAM_APPLY_PASS:
                text = buildManagerPassTeamApplyNotification(tid, (MemberChangeAttachment) attachment);
                break;
            case TransferOwner:
            case SUPER_TEAM_CHANGE_OWNER:
                text = buildTransferOwnerNotification(tid, fromUser, fromAccount, (MemberChangeAttachment) attachment);
                break;
            case AddTeamManager:
            case SUPER_TEAM_ADD_MANAGER:
                text = buildAddTeamManagerNotification(tid, (MemberChangeAttachment) attachment);
                break;
            case RemoveTeamManager:
            case SUPER_TEAM_REMOVE_MANAGER:
                text = buildRemoveTeamManagerNotification(tid, (MemberChangeAttachment) attachment);
                break;
            case AcceptInvite:
            case SUPER_TEAM_INVITE_ACCEPT:
                text = buildAcceptInviteNotification(tid, fromUser, fromAccount, (MemberChangeAttachment) attachment);
                break;
            case MuteTeamMember:
            case SUPER_TEAM_MUTE_TLIST:
                text = buildMuteTeamNotification(tid, (MuteMemberAttachment) attachment);
                break;
            default:
                text = operatorDisplayName(tid, fromUser, fromAccount) + ": unknown message";
                break;
        }

        return text;
    }

    /** 操作者展示名：优先 UserInfo，否则用账号查群缓存昵称。 */
    private static String operatorDisplayName(String tid, UserInfo fromUser, String fromAccount) {
        if (fromUser != null) {
            return getTeamMemberDisplayName(tid, fromUser);
        }
        if (!TextUtils.isEmpty(fromAccount)) {
            return getTeamMemberDisplayName(tid, fromAccount);
        }
        return "";
    }

    private static String getTeamMemberDisplayName(String tid, String account) {
        return MessageHelper.getTeamMemberDisplayNameYou(tid, account);
    }

    private static String getTeamMemberDisplayName(String tid, UserInfo userInfo) {
        return MessageHelper.getTeamMemberDisplayName(tid, userInfo);
    }

    /**
     * @param fromUser 可为 null；{@code fromAccount} 为操作者账号，用于在仅有账号时排除邀请人自己。
     */
    private static String buildMemberListString(
            String tid, List<String> members, UserInfo fromUser, String fromAccount) {
        if (members == null || members.isEmpty()) {
            return "";
        }
        String skipAccount = fromUser != null ? fromUser.getAccount() : fromAccount;
        StringBuilder sb = new StringBuilder();
        for (String account : members) {
            if (TextUtils.isEmpty(account)) {
                continue;
            }
            if (!TextUtils.isEmpty(skipAccount) && TextUtils.equals(account, skipAccount)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(getTeamMemberDisplayName(tid, account));
        }
        return sb.toString();
    }

    private static String buildInviteMemberNotification(
            String tid, MemberChangeAttachment a, UserInfo fromUser, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_invite));
        sb.append(buildMemberListString(tid, a.getTargets(), fromUser, fromAccount));
        Team team = getTeam(tid);
        if (team != null && IMKitUtils.isTeamGroup(team)) {
            sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_discuss_team));
        } else {
            sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_team));
        }

        return sb.toString();
    }

    private static String buildKickMemberNotification(String tid, MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildMemberListString(tid, a.getTargets(), null, null));
        Team team = getTeam(tid);
        if (team != null && IMKitUtils.isTeamGroup(team)) {
            sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_discuss_team));
        } else {
            sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_team));
        }

        return sb.toString();
    }

    private static String buildLeaveTeamNotification(String tid, UserInfo fromUser, String fromAccount) {
        String tip;
        Team team = getTeam(tid);
        if (team != null && IMKitUtils.isTeamGroup(team)) {
            tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_discuss_team);
        } else {
            tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_team);
        }
        return operatorDisplayName(tid, fromUser, fromAccount) + tip;
    }

    private static Team getTeam(String teamId) {
        Team team = ChatRepo.getCurrentTeam();
        if (team == null || !TextUtils.equals(teamId, team.getId())) {
            team = TeamRepo.getTeamInfo(teamId);
        }
        return team;
    }

    private static String buildDismissTeamNotification(String tid, UserInfo fromUser, String fromAccount) {
        return operatorDisplayName(tid, fromUser, fromAccount)
                + IMKitClient.getApplicationContext().getString(R.string.chat_dismiss_team);
    }

    private static String buildUpdateTeamNotification(
            String tid, UserInfo fromUser, String fromAccount, UpdateTeamAttachment a) {
        StringBuilder sb = new StringBuilder();
        boolean showContent = true;
        for (Map.Entry<TeamFieldEnum, Object> field : a.getUpdatedFields().entrySet()) {
            StringBuilder subStr = new StringBuilder();
            subStr.append(operatorDisplayName(tid, fromUser, fromAccount)).append(" ");
            if (field.getKey() == TeamFieldEnum.Name) {
                subStr.append(
                        String.format(
                                IMKitClient.getApplicationContext().getString(R.string.chat_name_update),
                                field.getValue()));
            } else if (field.getKey() == TeamFieldEnum.Introduce) {
                subStr.append(
                        IMKitClient.getApplicationContext().getString(R.string.chat_team_introduce_update));
            } else if (field.getKey() == TeamFieldEnum.Announcement) {
                subStr
                        .append(operatorDisplayName(tid, fromUser, fromAccount))
                        .append(
                                IMKitClient.getApplicationContext().getString(R.string.chat_team_notice_update));
            } else if (field.getKey() == TeamFieldEnum.VerifyType) {
                VerifyTypeEnum type = (VerifyTypeEnum) field.getValue();
                String auth =
                        IMKitClient.getApplicationContext().getString(R.string.chat_team_verify_update);
                if (type == VerifyTypeEnum.Free) {
                    subStr
                            .append(auth)
                            .append(
                                    IMKitClient.getApplicationContext()
                                            .getString(R.string.chat_team_allow_anyone_join));
                } else if (type == VerifyTypeEnum.Apply) {
                    subStr
                            .append(auth)
                            .append(
                                    IMKitClient.getApplicationContext()
                                            .getString(R.string.chat_team_need_authentication));
                } else {
                    subStr
                            .append(auth)
                            .append(
                                    IMKitClient.getApplicationContext()
                                            .getString(R.string.chat_team_not_allow_anyone_join));
                }
            } else if (field.getKey() == TeamFieldEnum.ICON) {
                subStr.append(
                        IMKitClient.getApplicationContext().getString(R.string.chat_team_avatar_update));
            } else if (field.getKey() == TeamFieldEnum.InviteMode) {
                subStr.append(
                        IMKitClient.getApplicationContext()
                                .getString(R.string.chat_team_invitation_permission_update));
                TeamInviteModeEnum inviteModeEnum = (TeamInviteModeEnum) field.getValue();
                if (inviteModeEnum == TeamInviteModeEnum.All) {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_invitation_permission_all));
                } else {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_invitation_permission_manager));
                }
            } else if (field.getKey() == TeamFieldEnum.TeamUpdateMode) {
                subStr.append(
                        IMKitClient.getApplicationContext()
                                .getString(R.string.chat_team_modify_resource_permission_update));
                TeamUpdateModeEnum updateModeEnum = (TeamUpdateModeEnum) field.getValue();
                if (updateModeEnum == TeamUpdateModeEnum.All) {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_modify_permission_all));
                } else {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_modify_permission_manager));
                }
            } else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
                subStr.append(
                        IMKitClient.getApplicationContext()
                                .getString(R.string.chat_team_invited_id_verify_permission_update));
                subStr.append(
                        IMKitClient.getApplicationContext()
                                .getString(R.string.chat_team_invited_id_verify_permission_update));
                TeamBeInviteModeEnum inviteModeEnum = (TeamBeInviteModeEnum) field.getValue();
                if (inviteModeEnum == TeamBeInviteModeEnum.NeedAuth) {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_invited_permission_need));
                } else {
                    subStr.append(
                            IMKitClient.getApplicationContext()
                                    .getString(R.string.chat_team_invited_permission_no));
                }
            } else if (field.getKey() == TeamFieldEnum.AllMute) {
                TeamAllMuteModeEnum teamAllMuteModeEnum = (TeamAllMuteModeEnum) field.getValue();
                subStr.delete(0, subStr.length());
                if (teamAllMuteModeEnum == TeamAllMuteModeEnum.Cancel) {
                    subStr.append(
                            IMKitClient.getApplicationContext().getString(R.string.chat_team_cancel_all_mute));
                } else {
                    subStr.append(
                            IMKitClient.getApplicationContext().getString(R.string.chat_team_full_mute));
                }
            } else if (field.getKey() == TeamFieldEnum.Extension
                    || field.getKey() == TeamFieldEnum.Ext_Server_Only
                    || field.getKey() == TeamFieldEnum.TeamExtensionUpdateMode) {
                showContent = false;
                continue;
            } else {
                subStr.append(
                        String.format(
                                IMKitClient.getApplicationContext().getString(R.string.chat_team_update),
                                field.getKey(),
                                field.getValue()));
            }
            sb.append(subStr);
            sb.append("\r\n");
        }
        if (sb.length() < 2 && !showContent) {
            return "";
        }
        if (sb.length() < 2) {
            return IMKitClient.getApplicationContext().getString(R.string.chat_team_unknown_notification);
        }
        return sb.delete(sb.length() - 2, sb.length()).toString();
    }

    private static String buildManagerPassTeamApplyNotification(
            String tid, MemberChangeAttachment a) {

        return String.format(
                IMKitClient.getApplicationContext()
                        .getString(R.string.chat_team_manager_pass_ones_application),
                buildMemberListString(tid, a.getTargets(), null, null));
    }

    private static String buildTransferOwnerNotification(
            String tid, UserInfo fromUser, String fromAccount, MemberChangeAttachment a) {

        return operatorDisplayName(tid, fromUser, fromAccount)
                + IMKitClient.getApplicationContext().getString(R.string.chat_team_remove_to_another)
                + buildMemberListString(tid, a.getTargets(), null, null);
    }

    private static String buildAddTeamManagerNotification(String tid, MemberChangeAttachment a) {

        return String.format(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_appoint_manager),
                buildMemberListString(tid, a.getTargets(), null, null));
    }

    private static String buildRemoveTeamManagerNotification(String tid, MemberChangeAttachment a) {

        return String.format(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_removed_manager),
                buildMemberListString(tid, a.getTargets(), null, null));
    }

    private static String buildAcceptInviteNotification(
            String tid, UserInfo fromUser, String fromAccount, MemberChangeAttachment a) {

        return operatorDisplayName(tid, fromUser, fromAccount)
                + String.format(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_accept_ones_invent),
                buildMemberListString(tid, a.getTargets(), null, null));
    }

    private static String buildMuteTeamNotification(String tid, MuteMemberAttachment a) {

        return buildMemberListString(tid, a.getTargets(), null, null)
                + IMKitClient.getApplicationContext().getString(R.string.chat_team_operate_by_manager)
                + (a.isMute()
                ? IMKitClient.getApplicationContext().getString(R.string.chat_team_mute)
                : IMKitClient.getApplicationContext().getString(R.string.chat_team_un_mute));
    }
}
