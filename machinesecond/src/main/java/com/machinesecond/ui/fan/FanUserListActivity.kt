package com.machinesecond.ui.fan

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.method.DigitsKeyListener
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.machinesecond.MsSdk
import com.machinesecond.fan.FanAddStatus
import com.machinesecond.fan.FanMode
import com.machinesecond.host.FanUser
import com.machinesecond.ui.theme.MsColors

class FanUserListActivity : AppCompatActivity() {

    private lateinit var mode: FanMode
    private lateinit var recyclerView: RecyclerView
    private lateinit var fanAdapter: FanUserAdapter
    private val selected = mutableSetOf<String>()
    private var users = listOf<FanUser>()
    private lateinit var intervalField: EditText
    private lateinit var greetingField: EditText
    private lateinit var actionBtn: TextView
    private lateinit var selectAllBtn: TextView
    private lateinit var invertBtn: TextView
    private lateinit var clearBtn: TextView
    private lateinit var progressOverlay: FrameLayout
    private lateinit var modalProgress: TextView
    private lateinit var modalCountdown: TextView
    private lateinit var modalTitle: TextView
    private var addRunning = false
    private var stoppedByUser = false
    private val statusById = mutableMapOf<String, FanAddStatus>()
    private val statusMessageById = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = FanMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: FanMode.Normal.name)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "开始爆粉"
        val config = MsSdk.getConfig()
        users = MsSdk.getFanStore().load(mode)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar(title))
        fanAdapter = FanUserAdapter()
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@FanUserListActivity)
            adapter = fanAdapter
            setPadding(dp(8), dp(6), dp(8), dp(8))
            clipToPadding = false
            setBackgroundColor(MsColors.pageBg)
            itemAnimator = null
        }
        root.addView(recyclerView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        root.addView(buildBottomBar())

        val wrapper = FrameLayout(this)
        wrapper.addView(root, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        progressOverlay = buildProgressOverlay()
        progressOverlay.visibility = View.GONE
        wrapper.addView(progressOverlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        setContentView(wrapper)
        renderList()

        intervalField.setText(maxOf(5, config.fanIntervalSec).toString())
        greetingField.setText(config.fanGreeting.ifBlank { "加我通过下" })

        val queue = MsSdk.getFanAddQueue()
        queue.onRunningChanged = { running, total ->
            runOnUiThread {
                addRunning = running
                if (running) {
                    stoppedByUser = false
                    showProgressOverlay(total)
                } else {
                    hideProgressOverlay()
                }
                setControlsEnabled(!running)
                updateActionButton()
            }
        }
        queue.onProgress = { cur, total, _ ->
            runOnUiThread {
                modalProgress.text = "添加中：$cur/$total"
            }
        }
        queue.onCountdown = { seconds ->
            runOnUiThread {
                modalCountdown.text = if (seconds > 0) {
                    "下次添加 ${seconds}s（平台限速）"
                } else {
                    ""
                }
            }
        }
        queue.onItemStatus = { userId, status, message ->
            runOnUiThread {
                statusById[userId] = status
                statusMessageById[userId] = message
                updateRowStatus(userId)
            }
        }
        queue.onFinished = {
            runOnUiThread {
                hideProgressOverlay()
                addRunning = false
                setControlsEnabled(true)
                updateActionButton()
                if (stoppedByUser) {
                    stoppedByUser = false
                } else {
                    AlertDialog.Builder(this)
                        .setMessage("添加好友已结束，到通讯录去查看吧，有可能有延迟...")
                        .setPositiveButton("确定", null)
                        .show()
                }
            }
        }
    }

    private fun buildProgressOverlay(): FrameLayout {
        val overlay = FrameLayout(this).apply {
            setBackgroundColor(0x73000000)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(14).toFloat()
            }
        }
        modalTitle = TextView(this).apply {
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        card.addView(modalTitle, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        val status = TextView(this).apply {
            text = "添加中"
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }
        card.addView(status, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        modalProgress = TextView(this).apply {
            text = "添加中"
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }
        card.addView(modalProgress, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        modalCountdown = TextView(this).apply {
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(10))
        }
        card.addView(modalCountdown, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        val stopBtn = TextView(this).apply {
            text = "停止"
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            background = roundedBg(MsColors.stopRed, 8)
            setOnClickListener { stopAdding() }
        }
        card.addView(stopBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(38)
        ))
        overlay.addView(card, FrameLayout.LayoutParams(
            dp(320).coerceAtMost(resources.displayMetrics.widthPixels - dp(32)),
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))
        return overlay
    }

    private fun showProgressOverlay(total: Int) {
        val intervalSec = maxOf(5, MsSdk.getConfig().fanIntervalSec)
        modalTitle.text =
            "添加进行中，请耐心等待，结束会有提示，成员越多时间越久...平台限制，每${intervalSec}秒添加1个好友"
        modalProgress.text = if (total > 0) "添加中：0/$total" else "添加中"
        modalCountdown.text = ""
        progressOverlay.visibility = View.VISIBLE
    }

    private fun hideProgressOverlay() {
        progressOverlay.visibility = View.GONE
    }

    private fun stopAdding() {
        stoppedByUser = true
        MsSdk.getFanAddQueue().stop()
        hideProgressOverlay()
        addRunning = false
        setControlsEnabled(true)
        updateActionButton()
        AlertDialog.Builder(this)
            .setMessage("已停止添加")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun setControlsEnabled(enabled: Boolean) {
        intervalField.isEnabled = enabled
        greetingField.isEnabled = enabled
        selectAllBtn.isEnabled = enabled
        selectAllBtn.alpha = if (enabled) 1f else 0.5f
        invertBtn.isEnabled = enabled
        invertBtn.alpha = if (enabled) 1f else 0.5f
        clearBtn.isEnabled = enabled
        clearBtn.alpha = if (enabled) 1f else 0.5f
        fanAdapter.notifyDataSetChanged()
    }

    private fun updateActionButton() {
        if (addRunning) {
            actionBtn.visibility = View.GONE
        } else {
            actionBtn.visibility = View.VISIBLE
            actionBtn.text = "打招呼添加"
            actionBtn.background = roundedBg(MsColors.actionOrange, 8)
        }
    }

    private fun buildTopBar(title: String): FrameLayout {
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
        val tv = TextView(this).apply {
            text = title
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
        bar.addView(tv, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, dp(44)
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(72)
            rightMargin = dp(72)
        })
        return bar
    }

    private fun buildBottomBar(): LinearLayout {
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(12))
            setBackgroundColor(MsColors.pageBg)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(12).toFloat()
            }
        }
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        selectAllBtn = orangeBtn("全选") {
            selected.clear()
            selected.addAll(users.map { it.userId })
            renderList()
        }
        row1.addView(selectAllBtn, LinearLayout.LayoutParams(0, dp(34), 1f).apply { marginEnd = dp(4) })
        invertBtn = orangeBtn("反选") {
            if (users.isEmpty()) return@orangeBtn
            val current = selected.toSet()
            selected.clear()
            users.forEach { user ->
                if (user.userId !in current) selected.add(user.userId)
            }
            renderList()
        }
        row1.addView(invertBtn, LinearLayout.LayoutParams(0, dp(34), 1f).apply {
            marginStart = dp(4)
            marginEnd = dp(4)
        })
        clearBtn = orangeBtn("清空") {
            if (users.isEmpty()) return@orangeBtn
            AlertDialog.Builder(this)
                .setTitle("清空名单")
                .setMessage("将清空本页全部 ${users.size} 人本地名单，不可恢复。是否继续？")
                .setPositiveButton("确定") { _, _ ->
                    MsSdk.getFanStore().clear(mode)
                    users = emptyList()
                    selected.clear()
                    renderList()
                }
                .setNegativeButton("取消", null)
                .show()
        }
        row1.addView(clearBtn, LinearLayout.LayoutParams(0, dp(34), 1f).apply { marginStart = dp(4) })
        card.addView(row1)
        val intervalRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        intervalRow.addView(TextView(this).apply {
            text = "爆粉间隔"
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(dp(70), dp(38)))
        intervalField = styledField("默认加人间隔5秒").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            keyListener = DigitsKeyListener.getInstance("0123456789")
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val v = text?.toString()?.toIntOrNull() ?: 5
                    MsSdk.getConfig().fanIntervalSec = maxOf(5, v)
                    MsSdk.getConfig().synchronize()
                }
            }
        }
        intervalRow.addView(intervalField, LinearLayout.LayoutParams(0, dp(38), 1f).apply {
            marginStart = dp(4)
        })
        card.addView(intervalRow, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(38)
        ).apply { topMargin = dp(8) })
        greetingField = styledField("打招呼内容").apply {
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    MsSdk.getConfig().fanGreeting = text?.toString()?.ifBlank { "加我通过下" } ?: "加我通过下"
                    MsSdk.getConfig().synchronize()
                }
            }
        }
        card.addView(greetingField, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(38)
        ).apply { topMargin = dp(8) })
        actionBtn = orangeBtn("打招呼添加") { startAdd() }
        card.addView(actionBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(42)
        ).apply { topMargin = dp(8) })
        bottom.addView(card, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        return bottom
    }

    private fun styledField(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        isSingleLine = true
        setTextColor(MsColors.fieldText)
        setHintTextColor(MsColors.fieldHint)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        includeFontPadding = false
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), 0, dp(12), 0)
        background = GradientDrawable().apply {
            setColor(MsColors.white)
            cornerRadius = dp(8).toFloat()
        }
    }

    private fun orangeBtn(text: String, click: () -> Unit): TextView =
        TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            background = GradientDrawable().apply {
                setColor(MsColors.actionOrange)
                cornerRadius = dp(8).toFloat()
            }
            setOnClickListener { click() }
        }

    private fun renderList() {
        fanAdapter.notifyDataSetChanged()
    }

    private fun usableAvatarUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        return if (value.isBlank() || value.equals("null", ignoreCase = true)) "" else value
    }

    private inner class FanUserAdapter : RecyclerView.Adapter<FanUserViewHolder>() {
        override fun getItemCount(): Int = if (users.isEmpty()) 1 else users.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FanUserViewHolder {
            val holder = FrameLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return FanUserViewHolder(holder)
        }

        override fun onBindViewHolder(holder: FanUserViewHolder, position: Int) {
            holder.container.removeAllViews()
            val child = if (users.isEmpty()) {
                emptyView()
            } else {
                userRow(position, users[position])
            }
            holder.container.addView(child, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(if (users.isEmpty()) 8 else 6)
            })
        }
    }

    private class FanUserViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)

    private fun emptyView(): TextView = TextView(this).apply {
        text = emptyMessage()
        gravity = Gravity.CENTER
        setTextColor(MsColors.descText)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        includeFontPadding = false
        setLineSpacing(dp(3).toFloat(), 1f)
        setPadding(dp(24), dp(70), dp(24), dp(70))
        background = GradientDrawable().apply {
            setColor(MsColors.darkRow)
            cornerRadius = dp(12).toFloat()
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, dp(4), 0, dp(8)) }
    }

    private fun userRow(index: Int, user: FanUser): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(66)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(12).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dp(6)) }
        }
        val avatar = avatarView(user)
        row.addView(avatar, LinearLayout.LayoutParams(dp(42), dp(42)))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        info.addView(TextView(this).apply {
            text = "${index + 1}. ${user.name}"
            setTextColor(MsColors.rowTitle)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
        info.addView(TextView(this).apply {
            text = "昵称 ID：${user.userId}"
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
        row.addView(info, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(10)
            marginEnd = dp(8)
        })

        val rightCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val status = statusById[user.userId] ?: FanAddStatus.Idle
        val statusMsg = statusMessageById[user.userId].orEmpty()
        rightCol.addView(statusText(user.userId, status, statusMsg))
        val selectedBtn = TextView(this).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            minWidth = dp(54)
        }
        fun paint() {
            val chosen = user.userId in selected
            selectedBtn.text = if (chosen) "已选" else "选择"
            selectedBtn.setTextColor(if (chosen) MsColors.white else MsColors.rowTitle)
            selectedBtn.background = GradientDrawable().apply {
                cornerRadius = dp(13).toFloat()
                if (chosen) {
                    setColor(MsColors.themeGold)
                } else {
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(1), MsColors.themeGold)
                }
            }
        }
        paint()
        rightCol.addView(selectedBtn, LinearLayout.LayoutParams(dp(54), dp(26)).apply { topMargin = dp(5) })
        row.addView(rightCol, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        row.setOnClickListener {
            if (addRunning) return@setOnClickListener
            if (user.userId in selected) selected.remove(user.userId) else selected.add(user.userId)
            paint()
        }
        row.isEnabled = !addRunning
        row.alpha = if (addRunning) 0.62f else 1f
        return row
    }

    private fun avatarView(user: FanUser): FrameLayout {
        val holder = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(MsColors.themeGold)
                shape = GradientDrawable.OVAL
            }
            clipToOutline = true
        }
        val fallback = TextView(this).apply {
            text = user.name.take(1).ifBlank { "?" }
            gravity = Gravity.CENTER
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            includeFontPadding = false
        }
        holder.addView(fallback, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        val avatarUrl = usableAvatarUrl(user.avatarUrl)
        if (avatarUrl.isNotBlank()) {
            val image = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                visibility = View.GONE
            }
            holder.addView(image, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            loadAvatar(avatarUrl, image)
        }
        return holder
    }

    private fun loadAvatar(url: String, image: ImageView) {
        image.visibility = View.VISIBLE
        Glide.with(image)
            .load(url)
            .centerCrop()
            .into(image)
    }

    private fun statusText(userId: String, status: FanAddStatus, message: String): TextView = TextView(this).apply {
        text = if (status == FanAddStatus.Idle || message.isEmpty()) "待选择" else message
        setTextColor(if (status == FanAddStatus.Idle || message.isEmpty()) MsColors.descText else statusColor(status))
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
        includeFontPadding = false
        gravity = Gravity.CENTER
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        tag = statusTag(userId)
    }

    private fun statusTag(userId: String): String = "status_$userId"

    private fun statusColor(status: FanAddStatus): Int = when (status) {
        FanAddStatus.Adding -> MsColors.actionOrange
        FanAddStatus.Success, FanAddStatus.AlreadyFriend -> Color.parseColor("#68D391")
        FanAddStatus.Failed -> Color.parseColor("#FF7A7A")
        FanAddStatus.Pending, FanAddStatus.NeedProvider -> MsColors.descText
        FanAddStatus.Idle -> MsColors.descText
    }

    private fun updateRowStatus(userId: String) {
        val index = users.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            fanAdapter.notifyItemChanged(index)
        }
    }

    private fun emptyMessage(): String = when (mode) {
        FanMode.Normal -> "暂无用户数据\n请先在群里保存爆粉人群"
        FanMode.Precise -> "暂无红包用户\n打开「添加精准人群」…"
        FanMode.Custom -> "请先「添加到自选爆粉区」"
    }

    private fun startAdd() {
        if (MsSdk.getFanAddQueue().isRunning()) return
        val greeting = greetingField.text?.toString()?.trim() ?: ""
        if (greeting.isEmpty()) {
            AlertDialog.Builder(this).setMessage("请输入打招呼内容").setPositiveButton("确定", null).show()
            return
        }
        val picked = users.filter { it.userId in selected }
        if (picked.isEmpty()) {
            AlertDialog.Builder(this).setMessage("请选择").setPositiveButton("确定", null).show()
            return
        }
        val config = MsSdk.getConfig()
        if (!config.masterSwitch || !config.isAuthorized()) return
        config.fanGreeting = greeting
        config.fanIntervalSec = maxOf(5, intervalField.text?.toString()?.toIntOrNull() ?: 5)
        config.synchronize()
        MsSdk.getFanAddQueue().start(picked, greeting)
    }

    private fun roundedBg(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_TITLE = "title"
    }
}
