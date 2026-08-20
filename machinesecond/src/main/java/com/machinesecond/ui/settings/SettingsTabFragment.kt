package com.machinesecond.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.machinesecond.MsSdk
import com.machinesecond.ui.theme.MsColors

class SettingsTabFragment : Fragment() {

    interface Callback {
        fun onRowChanged(row: SettingsRow)
        fun onRowAction(row: SettingsRow)
        fun onAuthRequest()
        fun onUnauthorizedUse()
        fun onPickUsers(row: SettingsRow, grabMode: Boolean)
    }

    private var tabIndex: Int = 0
    private var callback: Callback? = null
    private val rows = mutableListOf<SettingsRow>()
    private var listContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabIndex = arguments?.getInt(ARG_TAB) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(MsColors.pageBg)
            isFillViewport = true
        }
        listContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        scroll.addView(listContainer)
        reloadRows()
        return scroll
    }

    fun setCallback(cb: Callback) {
        callback = cb
    }

    /**
     * 延后到下一帧再重建：焦点/触摸分发过程中直接 removeAllViews 会让正在获取焦点的
     * 子 View 脱离视图树，触发 requestChildFocus 的
     * "parameter must be a descendant of this view" 崩溃。
     */
    fun reloadRows() {
        val container = listContainer ?: return
        container.post { reloadRowsNow() }
    }

    private fun reloadRowsNow() {
        if (!isAdded) return
        val config = MsSdk.getConfig()
        rows.clear()
        rows.addAll(SettingsDataSource.rowsForTab(config, tabIndex))
        rebuildViews()
    }

    private fun rebuildViews() {
        val container = listContainer ?: return
        // 先清焦点，避免被移除的 EditText 仍持有焦点导致父容器 offsetDescendantRect 崩溃
        container.clearFocus()
        container.removeAllViews()
        val ctx = context ?: return
        for (row in rows) {
            val view = SettingsRowInflater.inflate(
                ctx, row,
                onChanged = { changed ->
                    if (changed.key !in NO_APPLY_KEYS) {
                        SettingsApplier.applyRow(MsSdk.getConfig(), changed)
                        MsSdk.getConfig().synchronize()
                    }
                    callback?.onRowChanged(changed)
                },
                onAction = { callback?.onRowAction(it) },
                onAuth = { callback?.onAuthRequest() },
                onUnauthorized = {
                    callback?.onUnauthorizedUse()
                    reloadRows()
                },
                onPickUsers = { r, grab -> callback?.onPickUsers(r, grab) }
            )
            container.addView(view)
        }
    }

    companion object {
        private const val ARG_TAB = "tab"
        private val NO_APPLY_KEYS = setOf(
            "desc", "compensationMatrix", "discountMatrix",
            "normalFan", "preciseFan", "customFan", "grabLog", "diagLog", "ios14AuthTip"
        )

        fun newInstance(tabIndex: Int): SettingsTabFragment =
            SettingsTabFragment().apply {
                arguments = Bundle().apply { putInt(ARG_TAB, tabIndex) }
            }
    }
}
