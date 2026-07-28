package com.machinesecond.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.machinesecond.MsSdk
import com.machinesecond.R
import com.machinesecond.fan.FanMode
import com.machinesecond.ui.fan.FanUserListActivity
import com.machinesecond.ui.fan.GroupMemberPickerActivity
import com.machinesecond.ui.log.DiagLogActivity
import com.machinesecond.ui.log.GrabLogActivity
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.ui.widget.MsInputDialog
import com.machinesecond.util.MsToast

class SettingsActivity : AppCompatActivity(), SettingsTabFragment.Callback {

    private lateinit var viewPager: ViewPager2
    private val fragments = mutableListOf<SettingsTabFragment>()
    private val iconItems = mutableListOf<View>()
    private var selectedTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar())
        root.addView(buildIconBar())
        val tabStrip = buildTabStrip()
        root.addView(tabStrip.first)
        viewPager = ViewPager2(this).apply {
            adapter = object : FragmentStateAdapter(this@SettingsActivity) {
                override fun getItemCount(): Int = 5
                override fun createFragment(position: Int): Fragment {
                    val f = SettingsTabFragment.newInstance(position)
                    f.setCallback(this@SettingsActivity)
                    fragments.add(f)
                    return f
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateIconSelection(position)
                }
            })
        }
        TabLayoutMediator(tabStrip.second, viewPager) { tab, pos ->
            val label = TextView(this).apply {
                text = SettingsDataSource.tabTitles()[pos]
                gravity = Gravity.CENTER
                setTextColor(MsColors.white)
                includeFontPadding = false
                maxLines = 1
                setPadding(dp(1), dp(6), dp(1), dp(6))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                // 窄屏自动缩字，避免「秒抢设置」被截断
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this, 9, 13, 1, TypedValue.COMPLEX_UNIT_DIP
                )
            }
            tab.customView = label
        }.attach()
        // 同步选中加粗
        tabStrip.second.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab?) {
                (t?.customView as? TextView)?.typeface =
                    Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
            override fun onTabUnselected(t: TabLayout.Tab?) {
                (t?.customView as? TextView)?.typeface = Typeface.DEFAULT
            }
            override fun onTabReselected(t: TabLayout.Tab?) {}
        })
        (tabStrip.second.getTabAt(0)?.customView as? TextView)?.typeface =
            Typeface.create("sans-serif-medium", Typeface.NORMAL)
        root.addView(viewPager, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        updateIconSelection(0)
    }

    private fun buildTopBar(): View {
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
            setOnClickListener {
                MsSdk.getConfig().reloadFromDisk()
                finish()
            }
        }
        val confirm = TextView(this).apply {
            text = "确定"
            setTextColor(MsColors.actionOrange)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            includeFontPadding = false
            gravity = Gravity.CENTER
            setOnClickListener {
                MsSdk.getConfig().synchronize()
                MsSdk.getHost().syncBackgroundKeepAlive(MsSdk.getConfig().backgroundGrab)
                finish()
            }
        }
        val titleOverride = intent.getStringExtra(EXTRA_TITLE)
        val title = TextView(this).apply {
            text = MsSdk.getConfig().settingsTitle(titleOverride)
            setTextColor(MsColors.black)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            includeFontPadding = false
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
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

    private fun buildIconBar(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(MsColors.pageBg)
            setPadding(dp(6), dp(4), dp(6), dp(4))
        }
        val icons = listOf(
            R.drawable.ms_tab_settings,
            R.drawable.ms_tab_bolt,
            R.drawable.ms_tab_mail,
            R.drawable.ms_tab_group,
            null
        )
        iconItems.clear()
        for (i in icons.indices) {
            val cell = FrameLayout(this).apply {
                setOnClickListener {
                    if (::viewPager.isInitialized) viewPager.currentItem = i
                }
            }
            val circle = View(this).apply { tag = "bg" }
            cell.addView(circle, FrameLayout.LayoutParams(dp(40), dp(40)).apply {
                gravity = Gravity.CENTER
            })
            if (icons[i] != null) {
                val iv = ImageView(this).apply {
                    tag = "icon"
                    setImageResource(icons[i]!!)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
                cell.addView(iv, FrameLayout.LayoutParams(dp(20), dp(20)).apply {
                    gravity = Gravity.CENTER
                })
            } else {
                val tv = TextView(this).apply {
                    tag = "pay"
                    text = "赔"
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
                    setTypeface(typeface, Typeface.BOLD)
                }
                cell.addView(tv, FrameLayout.LayoutParams(dp(40), dp(40)).apply {
                    gravity = Gravity.CENTER
                })
            }
            iconItems.add(cell)
            row.addView(cell, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        return row
    }

    private fun updateIconSelection(position: Int) {
        selectedTab = position
        iconItems.forEachIndexed { i, cell ->
            val selected = i == position
            val bgView = cell.findViewWithTag<View>("bg") ?: return@forEachIndexed
            val isPay = i == 4
            val onBg = if (isPay) MsColors.actionOrange else MsColors.themeGold
            val offBg = if (isPay) Color.parseColor("#C7C0BA") else Color.TRANSPARENT
            val onFg = MsColors.white
            val offFg = if (isPay) Color.parseColor("#6B645C") else Color.parseColor("#7A736B")
            bgView.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                if (selected) {
                    setColor(onBg)
                } else if (isPay) {
                    setColor(offBg)
                    setStroke(dp(1), Color.parseColor("#9E948C"))
                } else {
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(2), Color.parseColor("#B8B0A8"))
                }
            }
            cell.alpha = if (selected) 1f else 0.82f
            cell.scaleX = if (selected) 1.06f else 1f
            cell.scaleY = if (selected) 1.06f else 1f

            cell.findViewWithTag<ImageView>("icon")?.let { iv ->
                val d = ContextCompat.getDrawable(this, when (i) {
                    0 -> R.drawable.ms_tab_settings
                    1 -> R.drawable.ms_tab_bolt
                    2 -> R.drawable.ms_tab_mail
                    else -> R.drawable.ms_tab_group
                })?.mutate()
                if (d != null) {
                    DrawableCompat.setTint(d, if (selected) onFg else offFg)
                    iv.setImageDrawable(d)
                }
            }
            cell.findViewWithTag<TextView>("pay")?.setTextColor(if (selected) onFg else offFg)
        }
    }

    private fun buildTabStrip(): Pair<View, TabLayout> {
        val tab = TabLayout(this).apply {
            setBackgroundColor(MsColors.tabStripBg)
            setSelectedTabIndicatorColor(MsColors.white)
            setSelectedTabIndicatorHeight(dp(2))
            tabMode = TabLayout.MODE_FIXED
            tabGravity = TabLayout.GRAVITY_FILL
            minimumHeight = dp(40)
        }
        return tab to tab
    }

    override fun onRowChanged(row: SettingsRow) {
        if (row.key == "authCode") return
    }

    override fun onRowAction(row: SettingsRow) {
        when (row.key) {
            "grabLog" -> startActivity(Intent(this, GrabLogActivity::class.java))
            "diagLog" -> startActivity(Intent(this, DiagLogActivity::class.java))
            "normalFan" -> startActivity(Intent(this, FanUserListActivity::class.java).apply {
                putExtra(FanUserListActivity.EXTRA_MODE, FanMode.Normal.name)
                putExtra(FanUserListActivity.EXTRA_TITLE, "开始爆粉")
            })
            "preciseFan" -> startActivity(Intent(this, FanUserListActivity::class.java).apply {
                putExtra(FanUserListActivity.EXTRA_MODE, FanMode.Precise.name)
                putExtra(FanUserListActivity.EXTRA_TITLE, "查看红包用户")
            })
            "customFan" -> startActivity(Intent(this, FanUserListActivity::class.java).apply {
                putExtra(FanUserListActivity.EXTRA_MODE, FanMode.Custom.name)
                putExtra(FanUserListActivity.EXTRA_TITLE, "自选人群爆粉列表")
            })
        }
    }

    override fun onAuthRequest() = showAuthDialog()

    override fun onUnauthorizedUse() {
        AlertDialog.Builder(this)
            .setMessage("暂无授权，不可使用")
            .setPositiveButton("确定") { _, _ -> showAuthDialog() }
            .show()
    }

    override fun onPickUsers(row: SettingsRow, grabMode: Boolean) {
        val intent = Intent(this, GroupMemberPickerActivity::class.java).apply {
            putExtra(GroupMemberPickerActivity.EXTRA_GRAB_MODE, grabMode)
            putExtra(GroupMemberPickerActivity.EXTRA_GROUP_ID, MsSdk.getSendEngine().activeGroupId)
        }
        startActivityForResult(intent, if (grabMode) REQ_PICK_GRAB else REQ_PICK_SKIP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        val ids = data.getStringExtra(GroupMemberPickerActivity.EXTRA_IDS) ?: ""
        val names = data.getStringExtra(GroupMemberPickerActivity.EXTRA_NAMES) ?: ""
        val config = MsSdk.getConfig()
        when (requestCode) {
            REQ_PICK_GRAB -> {
                config.grabSpecifiedUsers = ids
                config.grabSpecifiedUsersDisplay = names
            }
            REQ_PICK_SKIP -> {
                config.skipSpecifiedUsers = ids
                config.skipSpecifiedUsersDisplay = names
            }
        }
        config.synchronize()
        fragments.forEach { it.reloadRows() }
    }

    private fun showAuthDialog() {
        MsInputDialog.show(
            context = this,
            title = "提示",
            message = "请输入激活码",
            hint = "请输入..."
        ) { code ->
            if (code.isEmpty()) {
                MsToast.show(this, "授权码不能为空")
                return@show
            }
            MsSdk.getAuthRepository().login(code) { ok, msg ->
                if (ok) {
                    fragments.forEach { it.reloadRows() }
                } else {
                    MsToast.show(this, msg ?: "授权失败")
                }
            }
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    companion object {
        const val EXTRA_TITLE = "settings_title"
        private const val REQ_PICK_GRAB = 1001
        private const val REQ_PICK_SKIP = 1002
    }
}
