package com.machinesecond.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GrabLogStore {

    private const val KEY = "MachineSecond_GrabLog"
    private const val MAX_ENTRIES = 500
    private val gson = Gson()

    data class Entry(
        val time: Long = System.currentTimeMillis(),
        val message: String,
        val success: Boolean = true
    )

    fun append(context: Context, message: String, success: Boolean = true) {
        val prefs = context.getSharedPreferences("MachineSecond_logs", Context.MODE_PRIVATE)
        val list = load(context).toMutableList()
        list.add(0, Entry(message = message, success = success))
        while (list.size > MAX_ENTRIES) list.removeAt(list.size - 1)
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun load(context: Context): List<Entry> {
        val prefs = context.getSharedPreferences("MachineSecond_logs", Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Entry>>() {}.type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences("MachineSecond_logs", Context.MODE_PRIVATE)
        prefs.edit().remove(KEY).apply()
    }
}
