package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/** 云信 SDK 群成员禁言 */
public final class TeamNimMuteHelper {

  private TeamNimMuteHelper() {}

  public static boolean isMemberMuted(String teamId, String account) {
    if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(account)) {
      return false;
    }
    List<TeamMember> muted =
        NIMClient.getService(TeamService.class).queryMutedTeamMembers(teamId);
    if (muted == null || muted.isEmpty()) {
      return false;
    }
    for (TeamMember member : muted) {
      if (member != null && account.equals(member.getAccount())) {
        return true;
      }
    }
    return false;
  }
}
