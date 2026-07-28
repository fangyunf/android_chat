package com.machinesecond.host

import com.google.gson.JsonObject

interface HostBridge {
    fun currentUserId(): String
    fun postRed(
        path: String,
        body: Map<String, Any?>,
        silent: Boolean,
        onOk: (JsonObject) -> Unit,
        onFail: (Throwable) -> Unit
    )
    fun isSessionPinned(sessionId: String): Boolean
    fun markMessageClaimed(redPacketId: String)
    fun updateMessageUI(messageRef: Any)
    fun fetchGroupMembers(groupId: String, cb: (List<FanUser>) -> Unit)
    fun friendIdsForMass(): List<String>
    fun sendMass(toUserId: String, text: String, imagePath: String?, cb: (Boolean) -> Unit)
    fun isFriend(userId: String): Boolean
    fun addFriendBusiness(userId: String, memberCode: String?, greeting: String, cb: (Boolean, String?) -> Unit)
    fun addFriendIm(userId: String, greeting: String, cb: (Boolean, String?) -> Unit)
    fun appDisplayName(): String
    fun parseInbound(rawMessage: Any): RedInbound?
    fun syncBackgroundKeepAlive(enabled: Boolean) {}
    fun deviceUuid(): String
    fun authLoginUrl(): String
    fun authExpireUrl(): String
}

data class RedInbound(
    val customType: Int,
    val redPacketId: String,
    val createTime: String,
    val sessionId: String,
    val title: String,
    val amountYuan: Double,
    val fromUserId: String,
    val toUserId: String?,
    val claimed: Boolean,
    val senderDisplayName: String?,
    val senderAvatarUrl: String? = null,
    val senderMemberCode: String? = null,
    val rawMessageRef: Any? = null
)

data class FanUser(
    val userId: String,
    val name: String,
    val avatarUrl: String? = "",
    val memberCode: String? = ""
)
