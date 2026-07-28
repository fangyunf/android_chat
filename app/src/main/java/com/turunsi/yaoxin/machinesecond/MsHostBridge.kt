package com.turunsi.yaoxin.machinesecond

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.machinesecond.host.FanUser
import com.machinesecond.host.HostBridge
import com.machinesecond.host.RedInbound
import com.machinesecond.util.DiagLogStore
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam
import com.netease.yunxin.kit.chatkit.repo.ChatRepo
import com.netease.yunxin.kit.chatkit.repo.ContactRepo
import com.netease.yunxin.kit.chatkit.repo.TeamRepo
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import com.turunsi.yaoxin.R
import com.yaoxin.appbase.model.CustomMsgBean
import com.yaoxin.appbase.model.GroupInfoBean
import com.yaoxin.appbase.model.NetData
import com.yaoxin.appbase.model.RegisterBean
import com.yaoxin.appbase.net.CommonCallback
import com.yaoxin.appbase.net.Constant
import com.yaoxin.appbase.net.HttpUtil
import com.yaoxin.appbase.utils.AESUtil
import com.yaoxin.appbase.utils.BaseEvent
import com.yaoxin.appbase.utils.DataUtil
import com.yaoxin.appbase.utils.DeviceUtils
import com.yaoxin.appbase.utils.ToastUtils
import org.greenrobot.eventbus.EventBus
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * MachineSecond 宿主桥接。
 * 网络走现网 HttpUtil（AES + Authorization）；红包 path 见规格 §17.4；
 * 授权 URL 见规格 §11.1。
 */
@Suppress("UNCHECKED_CAST")
class MsHostBridge(context: Context) : HostBridge {

    private val appContext = context.applicationContext
    private val gson = Gson()

    override fun currentUserId(): String = DataUtil.getUserid() ?: ""

    override fun postRed(
        path: String,
        body: Map<String, Any?>,
        silent: Boolean,
        onOk: (JsonObject) -> Unit,
        onFail: (Throwable) -> Unit
    ) {
        val req = HashMap<String, Any>()
        for ((k, v) in body) {
            if (v != null) req[k] = v
        }
        val amount = req["amount"]
        if (amount is String) {
            val s = amount.trim()
            if (!s.contains(".")) {
                s.toIntOrNull()?.let { req["amount"] = it }
            }
        }

        val url = if (path.startsWith("http")) {
            path
        } else {
            Constant.BASE_URL + path.removePrefix("/")
        }

        HttpUtil.apiW().ms_postRed(url, req).enqueue(object : Callback<NetData<*>> {
            override fun onResponse(call: Call<NetData<*>>, response: Response<NetData<*>>) {
                val netData = response.body()
                if (!response.isSuccessful || netData == null) {
                    if (!silent) ToastUtils.toastMsg("网络错误")
                    onFail(RuntimeException("network error"))
                    return
                }
                val root = JsonObject()
                root.addProperty("code", netData.code)
                root.addProperty("msg", netData.msg ?: "")
                try {
                    if (netData.code == 200 && netData.data != null) {
                        val decrypted = AESUtil.aseDecrypt(netData.data.toString())
                        root.add("data", JsonParser.parseString(decrypted))
                    } else {
                        root.add("data", JsonObject())
                        if (!silent && !netData.msg.isNullOrEmpty()) {
                            ToastUtils.toastMsg(netData.msg)
                        }
                    }
                    onOk(root)
                } catch (e: Exception) {
                    if (!silent) ToastUtils.toastMsg("解析失败")
                    onFail(e)
                }
            }

            override fun onFailure(call: Call<NetData<*>>, t: Throwable) {
                if (!silent) {
                    ToastUtils.toastMsg(if (t.message.isNullOrEmpty()) "网络错误" else t.message)
                }
                onFail(t)
            }
        })
    }

    override fun isSessionPinned(sessionId: String): Boolean {
        return try {
            val team = NIMClient.getService(MsgService::class.java)
                .queryRecentContact(sessionId, SessionTypeEnum.Team)
            val p2p = team ?: NIMClient.getService(MsgService::class.java)
                .queryRecentContact(sessionId, SessionTypeEnum.P2P)
            p2p != null && p2p.tag != 0L
        } catch (_: Exception) {
            false
        }
    }

    override fun markMessageClaimed(redPacketId: String) {
        DiagLogStore.append(appContext, "Host", "markMessageClaimed rid=$redPacketId")
    }

    override fun updateMessageUI(messageRef: Any) {
        val message = messageRef as? IMMessage ?: return
        try {
            val ext = HashMap<String, Any>()
            message.localExtension?.let { ext.putAll(it) }
            ext["userId"] = currentUserId()
            ext["hasDragDown"] = 1
            ext["msClaimed"] = true
            message.localExtension = ext
            NIMClient.getService(MsgService::class.java).updateIMMessage(message)
            EventBus.getDefault().post(BaseEvent("ms_red_packet_claimed").apply {
                put("uuid", message.uuid ?: "")
            })
            DiagLogStore.append(appContext, "Host", "updateMessageUI uuid=${message.uuid ?: ""}")
        } catch (e: Exception) {
            DiagLogStore.append(appContext, "Host", "updateMessageUI failed ${e.message ?: ""}")
        }
    }

    override fun fetchGroupMembers(groupId: String, cb: (List<FanUser>) -> Unit) {
        DiagLogStore.append(appContext, "Host", "fetchGroupMembers start group=$groupId")
        // 业务接口有 memberCode，NIM 群成员头像更完整；两个来源合并后再保存，避免修加好友后丢头像。
        fetchGroupMembersByBusinessApi(groupId) { businessUsers ->
            fetchGroupMembersByNim(groupId) { nimUsers ->
                val users = mergeFanUsers(businessUsers, nimUsers)
                DiagLogStore.append(
                    appContext,
                    "Host",
                    "members merged group=$groupId business=${businessUsers.size} nim=${nimUsers.size} result=${users.size} avatars=${users.count { it.avatarUrl.orEmpty().isNotBlank() }} memberCodes=${users.count { it.memberCode.orEmpty().isNotBlank() }}"
                )
                cb(users)
            }
        }
    }

    private fun normalizeAvatarUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank() || value.equals("null", ignoreCase = true)) return ""
        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("content://", ignoreCase = true) ||
            value.startsWith("file://", ignoreCase = true)
        ) {
            return value
        }
        if (value.startsWith("//")) return "https:$value"
        return Constant.BASE_URL.trimEnd('/') + "/" + value.removePrefix("/")
    }

    private fun mergeFanUsers(businessUsers: List<FanUser>, nimUsers: List<FanUser>): List<FanUser> {
        if (businessUsers.isEmpty()) return nimUsers
        if (nimUsers.isEmpty()) return businessUsers
        val nimById = nimUsers.associateBy { it.userId }
        val merged = ArrayList<FanUser>()
        val seen = HashSet<String>()
        for (business in businessUsers) {
            if (!seen.add(business.userId)) continue
            val nim = nimById[business.userId]
            merged.add(
                business.copy(
                    name = business.name.ifBlank { nim?.name ?: business.userId },
                    avatarUrl = normalizeAvatarUrl(nim?.avatarUrl).ifBlank { normalizeAvatarUrl(business.avatarUrl) },
                    memberCode = business.memberCode.orEmpty().ifBlank { nim?.memberCode.orEmpty() }
                )
            )
        }
        for (nim in nimUsers) {
            if (seen.add(nim.userId)) merged.add(nim)
        }
        return merged
    }

    private fun fetchGroupMembersByNim(groupId: String, cb: (List<FanUser>) -> Unit) {
        try {
            TeamRepo.getMemberList(groupId, object : FetchCallback<List<UserInfoWithTeam>> {
                override fun onSuccess(param: List<UserInfoWithTeam>?) {
                    val users = param.toFanUsers()
                    DiagLogStore.append(appContext, "Host", "NIM members group=$groupId raw=${param?.size ?: 0} mapped=${users.size}")
                    cb(users)
                }

                override fun onFailed(code: Int) {
                    DiagLogStore.append(appContext, "Host", "NIM members failed group=$groupId code=$code")
                    cb(emptyList())
                }

                override fun onException(exception: Throwable?) {
                    DiagLogStore.append(appContext, "Host", "NIM members exception group=$groupId error=${exception?.message ?: ""}")
                    cb(emptyList())
                }
            })
        } catch (e: Exception) {
            DiagLogStore.append(appContext, "Host", "NIM members throw group=$groupId error=${e.message ?: ""}")
            cb(emptyList())
        }
    }

    private fun List<UserInfoWithTeam>?.toFanUsers(): List<FanUser> {
        if (this.isNullOrEmpty()) return emptyList()
        val result = ArrayList<FanUser>()
        val seen = HashSet<String>()
        val groupCache = DataUtil.getGroupMemberList()
        for (item in this) {
            val account = item.teamInfo.account ?: item.userInfo?.account ?: continue
            if (account.isEmpty() || !seen.add(account)) continue
            val cached = groupCache.firstOrNull { it.userId == account }
            val name = try {
                ChatUserCache.getName(item)
            } catch (_: Exception) {
                null
            }.takeUnless { it.isNullOrBlank() }
                ?: cached?.userGroupName?.takeUnless { it.isBlank() }
                ?: cached?.name?.takeUnless { it.isBlank() }
                ?: item.userInfo?.name?.takeUnless { it.isBlank() }
                ?: item.teamInfo.teamNick?.takeUnless { it.isBlank() }
                ?: account
            val avatar = item.userInfo?.avatar?.takeUnless { it.isBlank() }
                ?: cached?.portrait?.takeUnless { it.isBlank() }
                ?: cached?.avatar?.takeUnless { it.isBlank() }
                ?: cached?.avatarUrl?.takeUnless { it.isBlank() }
                ?: cached?.head?.takeUnless { it.isBlank() }
                ?: ""
            result.add(FanUser(account, name, normalizeAvatarUrl(avatar), cached?.memberCode ?: ""))
        }
        return result
    }

    private fun fetchGroupMembersByBusinessApi(groupId: String, cb: (List<FanUser>) -> Unit) {
        DiagLogStore.append(appContext, "Host", "business members start group=$groupId")
        val bean = RegisterBean()
        bean.groupId = groupId
        bean.page = "1"
        bean.pageNo = "500"
        HttpUtil.apiW().group_groupUserListPost(bean).enqueue(object : CommonCallback<NetData<*>>() {
            override fun Successful(call: Call<NetData<*>>?, response: Response<NetData<*>>?, body: NetData<*>) {
                val result = ArrayList<FanUser>()
                try {
                    val list: List<GroupInfoBean>? = gson.fromJson(
                        body.data.toString(),
                        object : TypeToken<List<GroupInfoBean>>() {}.type
                    )
                    if (!list.isNullOrEmpty()) {
                        DataUtil.setGroupMemberInfoList(list)
                    }
                    list?.forEach { m ->
                        if (!m.userId.isNullOrEmpty()) {
                            val name = when {
                                !m.userGroupName.isNullOrEmpty() -> m.userGroupName
                                !m.name.isNullOrEmpty() -> m.name
                                else -> m.userId
                            }
                            val avatar = m.portrait?.takeUnless { it.isBlank() }
                                ?: m.avatar?.takeUnless { it.isBlank() }
                                ?: m.avatarUrl?.takeUnless { it.isBlank() }
                                ?: m.head?.takeUnless { it.isBlank() }
                                ?: ""
                            result.add(FanUser(m.userId, name, normalizeAvatarUrl(avatar), m.memberCode ?: ""))
                        }
                    }
                } catch (e: Exception) {
                    DiagLogStore.append(appContext, "Host", "business members parse error group=$groupId error=${e.message ?: ""}")
                }
                DiagLogStore.append(appContext, "Host", "business members group=$groupId count=${result.size}")
                cb(result)
            }

            override fun Failure(call: Call<NetData<*>>?, t: Throwable?) {
                DiagLogStore.append(appContext, "Host", "business members failed group=$groupId error=${t?.message ?: ""}")
                cb(emptyList())
            }
        })
    }

    override fun friendIdsForMass(): List<String> {
        return DataUtil.getFriendInfoList()?.mapNotNull { it.userId }?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    override fun sendMass(
        toUserId: String,
        text: String,
        imagePath: String?,
        cb: (Boolean) -> Unit
    ) {
        try {
            val textMsg = MessageBuilder.createTextMessage(toUserId, SessionTypeEnum.P2P, text)
            ChatRepo.sendMessage(textMsg, true, null)
            val imageFile = imagePath?.takeIf { it.isNotBlank() }?.let { File(it) }
            if (imageFile != null && imageFile.exists() && imageFile.length() > 0) {
                val imageMsg = MessageBuilder.createImageMessage(toUserId, SessionTypeEnum.P2P, imageFile)
                ChatRepo.sendMessage(imageMsg, true, null)
                DiagLogStore.append(appContext, "Mass", "send text+image to=$toUserId path=${imageFile.name}")
            } else {
                DiagLogStore.append(appContext, "Mass", "send text to=$toUserId image=${imagePath ?: ""}")
            }
            cb(true)
        } catch (e: Exception) {
            DiagLogStore.append(appContext, "Mass", "send failed to=$toUserId error=${e.message ?: ""}")
            cb(false)
        }
    }

    override fun isFriend(userId: String): Boolean {
        return try {
            NIMClient.getService(FriendService::class.java).isMyFriend(userId)
        } catch (_: Exception) {
            DataUtil.getFriendInfoList()?.any { it.userId == userId } == true
        }
    }

    override fun addFriendBusiness(
        userId: String,
        memberCode: String?,
        greeting: String,
        cb: (Boolean, String?) -> Unit
    ) {
        val resolvedMemberCode = resolveMemberCode(userId, memberCode)
        val bean = RegisterBean()
        bean.memberCode = resolvedMemberCode
        bean.msg = if (greeting.isEmpty()) "加我通过下" else greeting
        DiagLogStore.append(appContext, "Fan", "bizAdd start uid=$userId memberCode=$resolvedMemberCode")
        HttpUtil.apiW().friends_addFriends(bean).enqueue(object : CommonCallback<NetData<*>>() {
            override fun Successful(call: Call<NetData<*>>?, response: Response<NetData<*>>?, body: NetData<*>) {
                DiagLogStore.append(appContext, "Fan", "bizAdd ok uid=$userId memberCode=$resolvedMemberCode msg=${body.msg ?: ""}")
                cb(true, body.msg)
            }

            override fun Failure(call: Call<NetData<*>>?, t: Throwable?) {
                DiagLogStore.append(appContext, "Fan", "bizAdd fail uid=$userId memberCode=$resolvedMemberCode error=${t?.message ?: ""}")
                cb(false, t?.message)
            }
        })
    }

    private fun resolveMemberCode(userId: String, suppliedMemberCode: String?): String {
        suppliedMemberCode?.takeIf { it.isNotBlank() }?.let { return it }
        DataUtil.getGroupMemberList().firstOrNull { it.userId == userId }
            ?.memberCode?.takeIf { it.isNotBlank() }?.let { return it }
        DataUtil.getFriendInfoList().firstOrNull { it.userId == userId }
            ?.memberCode?.takeIf { it.isNotBlank() }?.let { return it }
        return userId
    }

    override fun addFriendIm(
        userId: String,
        greeting: String,
        cb: (Boolean, String?) -> Unit
    ) {
        try {
            ContactRepo.addFriend(
                userId,
                FriendVerifyType.VerifyRequest,
                object : FetchCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        cb(true, null)
                    }

                    override fun onFailed(code: Int) {
                        cb(false, "code=$code")
                    }

                    override fun onException(exception: Throwable?) {
                        cb(false, exception?.message)
                    }
                }
            )
        } catch (e: Exception) {
            cb(false, e.message)
        }
    }

    override fun appDisplayName(): String {
        return try {
            appContext.getString(R.string.app_name)
        } catch (_: Exception) {
            "助手"
        }
    }

    override fun parseInbound(rawMessage: Any): RedInbound? {
        if (rawMessage !is IMMessage) return null
        if (rawMessage.msgType != MsgTypeEnum.custom) return null
        val attach = rawMessage.attachStr ?: return null
        return try {
            val outer = gson.fromJson(attach, CustomMsgBean::class.java) ?: return null
            val type = outer.type
            if (type != 21 && type != 22 && type != 23 && type != 28 && type != 503) {
                return null
            }
            val data: CustomMsgBean = when {
                outer.result != null -> outer.result
                !outer.data.isNullOrEmpty() -> gson.fromJson(outer.data, CustomMsgBean::class.java)
                else -> outer
            } ?: return null

            val redId = when {
                !data.redPacketId.isNullOrEmpty() -> data.redPacketId
                !data.id.isNullOrEmpty() -> data.id
                else -> return null
            }
            val claimed = data.hasOpened || try {
                data.vos != null && data.isHasOpened
            } catch (_: Exception) {
                false
            }

            RedInbound(
                customType = type,
                redPacketId = redId,
                createTime = data.createTime ?: "",
                sessionId = rawMessage.sessionId ?: "",
                title = data.title ?: "",
                amountYuan = yuanFromServer(data.amount ?: data.sendAmount),
                fromUserId = data.fromUserId ?: data.sendUserId ?: rawMessage.fromAccount ?: "",
                toUserId = data.toUserId,
                claimed = claimed,
                senderDisplayName = data.sendName ?: data.sendUserName,
                senderAvatarUrl = normalizeAvatarUrl(data.sendAvatar ?: data.avatar),
                senderMemberCode = data.memberCode ?: outer.memberCode,
                rawMessageRef = rawMessage
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun deviceUuid(): String = DeviceUtils.getDeviceId(appContext) ?: ""

    /** 规格 §11.1 默认授权 URL */
    override fun authLoginUrl(): String = "https://w.eydata.net/12E69306C833B66E"

    override fun authExpireUrl(): String = "https://w.eydata.net/89A0FBACA03027BD"

    /** 规格 §17.0：整数=分；带小数=元 */
    private fun yuanFromServer(raw: String?): Double {
        if (raw.isNullOrBlank()) return 0.0
        return try {
            if (raw.contains(".")) raw.toDouble() else raw.toLong() / 100.0
        } catch (_: Exception) {
            0.0
        }
    }
}
