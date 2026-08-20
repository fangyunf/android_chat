package com.machinesecond.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagLogStore {

    private const val KEY = "MachineSecond_DiagLog"
    private const val MAX_ENTRIES = 1000
    private val gson = Gson()
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    data class Entry(
        val time: Long = System.currentTimeMillis(),
        val tag: String,
        val message: String
    )

    fun formatTime(timeMs: Long): String = timeFormat.format(Date(timeMs))

    fun append(context: Context, tag: String, message: String) {
        val prefs = context.getSharedPreferences("MachineSecond_logs", Context.MODE_PRIVATE)
        val list = load(context).toMutableList()
        val now = System.currentTimeMillis()
        val stamped = "${formatTime(now)} $message"
        list.add(0, Entry(time = now, tag = tag, message = stamped))
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

    fun copyText(context: Context): String =
        load(context).joinToString("\n") { "[${it.tag}] ${it.message}" }
}
