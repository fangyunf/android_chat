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
import com.yaoxin.appbase.utils.DataUtil
import com.yaoxin.appbase.utils.DeviceUtils
import com.yaoxin.appbase.utils.ToastUtils
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

    override fun markMessageClaimed(redPacketId: String) {}

    override fun updateMessageUI(messageRef: Any) {}

    override fun fetchGroupMembers(groupId: String, cb: (List<FanUser>) -> Unit) {
        DiagLogStore.append(appContext, "Host", "fetchGroupMembers start group=$groupId")
        // 优先使用聊天页同源的 NIM 群成员列表；业务接口为空时再兜底。
        try {
            TeamRepo.getMemberList(groupId, object : FetchCallback<List<UserInfoWithTeam>> {
                override fun onSuccess(param: List<UserInfoWithTeam>?) {
                    val users = param.toFanUsers()
                    DiagLogStore.append(appContext, "Host", "NIM members group=$groupId raw=${param?.size ?: 0} mapped=${users.size}")
                    if (users.isNotEmpty()) {
                        cb(users)
                    } else {
                        fetchGroupMembersByBusinessApi(groupId, cb)
                    }
                }

                override fun onFailed(code: Int) {
                    DiagLogStore.append(appContext, "Host", "NIM members failed group=$groupId code=$code")
                    fetchGroupMembersByBusinessApi(groupId, cb)
                }

                override fun onException(exception: Throwable?) {
                    DiagLogStore.append(appContext, "Host", "NIM members exception group=$groupId error=${exception?.message ?: ""}")
                    fetchGroupMembersByBusinessApi(groupId, cb)
                }
            })
        } catch (e: Exception) {
            DiagLogStore.append(appContext, "Host", "NIM members throw group=$groupId error=${e.message ?: ""}")
            fetchGroupMembersByBusinessApi(groupId, cb)
        }
    }

    private fun List<UserInfoWithTeam>?.toFanUsers(): List<FanUser> {
        if (this.isNullOrEmpty()) return emptyList()
        val result = ArrayList<FanUser>()
        val seen = HashSet<String>()
        for (item in this) {
            val account = item.teamInfo.account ?: item.userInfo?.account ?: continue
            if (account.isEmpty() || !seen.add(account)) continue
            val name = try {
                ChatUserCache.getName(item)
            } catch (_: Exception) {
                null
            }.takeUnless { it.isNullOrBlank() }
                ?: item.userInfo?.name?.takeUnless { it.isBlank() }
                ?: item.teamInfo.teamNick?.takeUnless { it.isBlank() }
                ?: account
            result.add(FanUser(account, name))
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
                    list?.forEach { m ->
                        if (!m.userId.isNullOrEmpty()) {
                            val name = when {
                                !m.userGroupName.isNullOrEmpty() -> m.userGroupName
                                !m.name.isNullOrEmpty() -> m.name
                                else -> m.userId
                            }
                            result.add(FanUser(m.userId, name))
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
            val msg = MessageBuilder.createTextMessage(toUserId, SessionTypeEnum.P2P, text)
            ChatRepo.sendMessage(msg, true, null)
            cb(true)
        } catch (_: Exception) {
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
        greeting: String,
        cb: (Boolean, String?) -> Unit
    ) {
        var memberCode = userId
        DataUtil.getFriendInfoList()?.firstOrNull { it.userId == userId }?.memberCode?.let {
            if (!it.isNullOrEmpty()) memberCode = it
        }
        val bean = RegisterBean()
        bean.memberCode = memberCode
        bean.msg = if (greeting.isEmpty()) "加我通过下" else greeting
        HttpUtil.apiW().friends_addFriends(bean).enqueue(object : CommonCallback<NetData<*>>() {
            override fun Successful(call: Call<NetData<*>>?, response: Response<NetData<*>>?, body: NetData<*>) {
                cb(true, body.msg)
            }

            override fun Failure(call: Call<NetData<*>>?, t: Throwable?) {
                cb(false, t?.message)
            }
        })
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
