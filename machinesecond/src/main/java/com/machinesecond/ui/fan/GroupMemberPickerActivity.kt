package com.machinesecond.ui.fan

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.machinesecond.MsSdk
import com.machinesecond.host.FanUser
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.DiagLogStore
import com.machinesecond.util.MsToast

class GroupMemberPickerActivity : AppCompatActivity() {

    private val allMembers = mutableListOf<FanUser>()
    private val filtered = mutableListOf<FanUser>()
    private val selected = mutableSetOf<String>()
    private lateinit var listContainer: LinearLayout
    private lateinit var stateText: TextView
    private lateinit var countText: TextView
    private var grabMode = true
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        grabMode = intent.getBooleanExtra(EXTRA_GRAB_MODE, true)
        groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: MsSdk.getSendEngine().activeGroupId
        DiagLogStore.append(this, "Picker", "open mode=${if (grabMode) "grab" else "skip"} group=${groupId ?: ""} cached=${MsSdk.getSendEngine().activeGroupId ?: ""}")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar())
        root.addView(buildSearchCard())

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setPadding(dp(8), dp(4), dp(8), dp(12))
            clipToPadding = false
        }
        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(
            listContainer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        setContentView(root)

        val cfg = MsSdk.getConfig()
        selected.addAll(if (grabMode) cfg.grabSpecifiedUserSet() else cfg.skipSpecifiedUserSet())
        renderLoading()
        loadMembers()
    }

    private fun buildSearchCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(10).toFloat()
            }
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(8), dp(8), dp(8), dp(6)) }
        }
        countText = TextView(this).apply {
            text = "正在获取群成员..."
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            includeFontPadding = false
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val search = EditText(this).apply {
            hint = "搜索昵称或ID"
            isSingleLine = true
            setTextColor(MsColors.fieldText)
            setHintTextColor(MsColors.fieldHint)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            includeFontPadding = false
            setPadding(dp(12), 0, dp(12), 0)
            background = GradientDrawable().apply {
                setColor(MsColors.white)
                cornerRadius = dp(8).toFloat()
            }
            setOnTextChangedListener { filter(it?.toString() ?: "") }
        }
        card.addView(countText)
        card.addView(search, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(40)
        ).apply { topMargin = dp(8) })
        return card
    }

    private fun loadMembers() {
        val gid = groupId
        if (gid.isNullOrBlank()) {
            DiagLogStore.append(this, "Picker", "missing groupId")
            renderEmpty("请先从群聊界面打开设置，再选择群成员")
            return
        }
        countText.text = "正在获取群成员..."
        DiagLogStore.append(this, "Picker", "fetch members group=$gid")
        MsSdk.getHost().fetchGroupMembers(gid) { members ->
            runOnUiThread {
                allMembers.clear()
                allMembers.addAll(members.distinctBy { it.userId })
                DiagLogStore.append(this, "Picker", "members loaded group=$gid count=${allMembers.size}")
                if (allMembers.isEmpty()) {
                    renderEmpty("没有获取到群成员，请确认当前群聊成员已加载")
                } else {
                    countText.text = "共 ${allMembers.size} 人，已选择 ${selected.size} 人"
                    filter("")
                }
            }
        }
    }

    private fun renderLoading() {
        listContainer.removeAllViews()
        stateText = stateLabel("正在加载群成员...")
        listContainer.addView(stateText)
    }

    private fun renderEmpty(message: String) {
        countText.text = message
        listContainer.removeAllViews()
        listContainer.addView(stateLabel(message))
    }

    private fun stateLabel(message: String): TextView = TextView(this).apply {
        text = message
        gravity = Gravity.CENTER
        setTextColor(MsColors.descText)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        includeFontPadding = false
        setPadding(dp(20), dp(60), dp(20), dp(60))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun EditText.setOnTextChangedListener(block: (String?) -> Unit) {
        addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                block(s?.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filter(query: String) {
        filtered.clear()
        val q = query.trim().lowercase()
        filtered.addAll(if (q.isEmpty()) allMembers else allMembers.filter {
            it.name.lowercase().contains(q) || it.userId.lowercase().contains(q)
        })
        renderList()
    }

    private fun renderList() {
        listContainer.removeAllViews()
        if (filtered.isEmpty()) {
            listContainer.addView(stateLabel("没有匹配的群成员"))
            return
        }
        for (user in filtered) {
            listContainer.addView(memberRow(user))
        }
        countText.text = "共 ${allMembers.size} 人，已选择 ${selected.size} 人"
    }

    private fun memberRow(user: FanUser): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(64)
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(10).toFloat()
            }
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dp(6)) }
        }
        val avatar = TextView(this).apply {
            text = user.name.take(1).ifBlank { "?" }
            gravity = Gravity.CENTER
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            background = GradientDrawable().apply {
                setColor(MsColors.themeGold)
                shape = GradientDrawable.OVAL
            }
        }
        row.addView(avatar, LinearLayout.LayoutParams(dp(42), dp(42)))

        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        textCol.addView(TextView(this).apply {
            text = user.name
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
        textCol.addView(TextView(this).apply {
            text = "ID：${user.userId}"
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
            includeFontPadding = false
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        })
        row.addView(textCol, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(10)
            marginEnd = dp(10)
        })

        val check = TextView(this).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
        }
        fun paint() {
            val chosen = user.userId in selected
            check.text = if (chosen) "已选" else "选择"
            check.setTextColor(if (chosen) MsColors.white else MsColors.rowTitle)
            check.background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                if (chosen) {
                    setColor(MsColors.themeGold)
                } else {
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(1), MsColors.themeGold)
                }
            }
        }
        paint()
        row.addView(check, LinearLayout.LayoutParams(dp(54), dp(28)))
        row.setOnClickListener {
            if (user.userId in selected) selected.remove(user.userId) else selected.add(user.userId)
            paint()
            countText.text = "共 ${allMembers.size} 人，已选择 ${selected.size} 人"
        }
        return row
    }

    private fun buildTopBar(): FrameLayout {
        val bar = FrameLayout(this).apply {
            minimumHeight = dp(44)
            setBackgroundColor(MsColors.pageBg)
        }
        val cancel = TextView(this).apply {
            text = "取消"
            setTextColor(MsColors.actionOrange)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }
        val confirm = TextView(this).apply {
            text = "确定"
            setTextColor(MsColors.actionOrange)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setOnClickListener { finishWithSelection() }
        }
        val title = TextView(this).apply {
            text = if (grabMode) "抢指定人" else "不抢此人"
            setTextColor(MsColors.black)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        bar.addView(cancel, FrameLayout.LayoutParams(dp(60), dp(44)).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            leftMargin = dp(12)
        })
        bar.addView(confirm, FrameLayout.LayoutParams(dp(60), dp(44)).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            rightMargin = dp(12)
        })
        bar.addView(title, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, dp(44)
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(72)
            rightMargin = dp(72)
        })
        return bar
    }

    private fun finishWithSelection() {
        val picked = allMembers.filter { it.userId in selected }
        val ids = picked.joinToString(",") { it.userId }
        val names = picked.joinToString("、") { it.name }
        setResult(RESULT_OK, intent.apply {
            putExtra(EXTRA_IDS, ids)
            putExtra(EXTRA_NAMES, names)
        })
        MsToast.show(this, "已选择 ${picked.size} 人")
        finish()
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    companion object {
        const val EXTRA_GRAB_MODE = "grab_mode"
        const val EXTRA_GROUP_ID = "group_id"
        const val EXTRA_IDS = "ids"
        const val EXTRA_NAMES = "names"
    }
}
