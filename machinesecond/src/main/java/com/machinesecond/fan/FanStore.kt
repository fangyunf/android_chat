package com.machinesecond.fan

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.machinesecond.host.FanUser

enum class FanMode { Normal, Precise, Custom }

class FanStore(private val context: Context) {

    companion object {
        private const val KEY_NORMAL = "MachineSecond_Fan_normal"
        private const val KEY_PRECISE = "MachineSecond_Fan_precise"
        private const val KEY_CUSTOM = "MachineSecond_Fan_custom"
        private val gson = Gson()
    }

    private val prefs = context.getSharedPreferences("MachineSecond_fan", Context.MODE_PRIVATE)

    fun load(mode: FanMode): List<FanUser> {
        val key = keyFor(mode)
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<FanUser>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(mode: FanMode, users: List<FanUser>) {
        prefs.edit().putString(keyFor(mode), gson.toJson(users)).apply()
    }

    fun appendPrecise(userId: String, name: String, avatarUrl: String = "", memberCode: String = "") {
        val list = load(FanMode.Precise).toMutableList()
        val index = list.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            val old = list[index]
            val nextAvatar = old.avatarUrl.orEmpty().ifBlank { avatarUrl }
            val nextMemberCode = old.memberCode.orEmpty().ifBlank { memberCode }
            if (nextAvatar != old.avatarUrl.orEmpty() || nextMemberCode != old.memberCode.orEmpty()) {
                list[index] = old.copy(avatarUrl = nextAvatar, memberCode = nextMemberCode)
                save(FanMode.Precise, list)
            }
            return
        }
        list.add(FanUser(userId, name, avatarUrl, memberCode))
        save(FanMode.Precise, list)
    }

    fun appendCustom(userId: String, name: String, avatarUrl: String = "", memberCode: String = "") {
        val list = load(FanMode.Custom).toMutableList()
        val index = list.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            val old = list[index]
            val nextAvatar = old.avatarUrl.orEmpty().ifBlank { avatarUrl }
            val nextMemberCode = old.memberCode.orEmpty().ifBlank { memberCode }
            if (nextAvatar != old.avatarUrl.orEmpty() || nextMemberCode != old.memberCode.orEmpty()) {
                list[index] = old.copy(avatarUrl = nextAvatar, memberCode = nextMemberCode)
                save(FanMode.Custom, list)
            }
            return
        }
        list.add(FanUser(userId, name, avatarUrl, memberCode))
        save(FanMode.Custom, list)
    }

    fun saveNormalFromMembers(members: List<FanUser>) {
        save(FanMode.Normal, members)
    }

    fun clear(mode: FanMode) {
        prefs.edit().remove(keyFor(mode)).apply()
    }

    private fun keyFor(mode: FanMode): String = when (mode) {
        FanMode.Normal -> KEY_NORMAL
        FanMode.Precise -> KEY_PRECISE
        FanMode.Custom -> KEY_CUSTOM
    }
}
