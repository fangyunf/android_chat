package com.machinesecond.ui.mass

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.machinesecond.MsSdk
import com.machinesecond.ui.theme.MsColors
import com.machinesecond.util.MsToast
import java.io.File
import java.io.FileOutputStream

/**
 * 对齐 iOS MSMassMessageConfigViewController：群发文本 + 可选配图。
 */
class MassMessageConfigActivity : AppCompatActivity() {

    private lateinit var textField: EditText
    private lateinit var imageButton: FrameLayout
    private lateinit var imagePreview: ImageView
    private lateinit var imageHint: TextView
    private lateinit var clearImageBtn: TextView
    private var imagePath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val dest = copyUriToCache(uri) ?: return@registerForActivityResult
        imagePath = dest.absolutePath
        refreshImageUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(MsColors.pageBg)
        }
        root.addView(buildTopBar())
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setPadding(dp(8), dp(8), dp(8), dp(12))
            clipToPadding = false
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(MsColors.darkRow)
                cornerRadius = dp(12).toFloat()
            }
        }

        card.addView(sectionTitle("群发文本"))
        textField = EditText(this).apply {
            hint = "请输入要群发的文本内容"
            minHeight = dp(168)
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setTextColor(MsColors.fieldText)
            setHintTextColor(MsColors.fieldHint)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            includeFontPadding = false
            background = GradientDrawable().apply {
                setColor(MsColors.white)
                cornerRadius = dp(8).toFloat()
            }
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setText(MsSdk.getMassSender().loadText())
        }
        card.addView(textField, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(168)
        ).apply { topMargin = dp(8) })

        card.addView(sectionTitle("配图（可选）").apply {
            setPadding(0, dp(18), 0, 0)
        })

        imagePreview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = android.view.View.GONE
        }
        imageHint = TextView(this).apply {
            text = "＋ 添加图片"
            gravity = Gravity.CENTER
            setTextColor(MsColors.fieldHint)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            includeFontPadding = false
        }
        imageButton = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(MsColors.white)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), MsColors.themeGold)
            }
            clipToOutline = false
            addView(imagePreview, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ))
            addView(imageHint, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ))
            setOnClickListener { pickImage.launch("image/*") }
        }
        card.addView(imageButton, LinearLayout.LayoutParams(dp(118), dp(118)).apply {
            topMargin = dp(8)
        })

        clearImageBtn = TextView(this).apply {
            text = "清除图片"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(MsColors.white)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            background = roundedBg(MsColors.actionOrange, 8)
            visibility = android.view.View.GONE
            setOnClickListener {
                imagePath = null
                refreshImageUi()
            }
        }
        card.addView(clearImageBtn, LinearLayout.LayoutParams(
            dp(92), dp(32)
        ).apply { topMargin = dp(8) })

        card.addView(TextView(this).apply {
            text = "确定后返回通讯录，点「群发」将按好友列表逐人发送（间隔约 0.2 秒）。"
            setTextColor(MsColors.descText)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            includeFontPadding = false
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, dp(18), 0, 0)
        })
        content.addView(card, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        scroll.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)

        imagePath = MsSdk.getMassSender().loadImagePath()
        refreshImageUi()
    }

    private fun refreshImageUi() {
        val path = imagePath
        if (!path.isNullOrBlank() && File(path).exists()) {
            val bmp = BitmapFactory.decodeFile(path)
            imagePreview.setImageDrawable(BitmapDrawable(resources, bmp))
            imagePreview.visibility = android.view.View.VISIBLE
            imageHint.visibility = android.view.View.GONE
            clearImageBtn.visibility = android.view.View.VISIBLE
        } else {
            imagePreview.setImageDrawable(null)
            imagePreview.visibility = android.view.View.GONE
            imageHint.visibility = android.view.View.VISIBLE
            clearImageBtn.visibility = android.view.View.GONE
        }
    }

    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val dir = File(cacheDir, "ms_mass").apply { mkdirs() }
            val dest = File(dir, "mass_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest
        } catch (_: Exception) {
            null
        }
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
            setOnClickListener {
                val text = textField.text?.toString()?.trim() ?: ""
                if (text.isEmpty()) {
                    AlertDialog.Builder(this@MassMessageConfigActivity)
                        .setMessage("请输入文本")
                        .setPositiveButton("确定", null)
                        .show()
                    return@setOnClickListener
                }
                val sender = MsSdk.getMassSender()
                sender.saveText(text)
                sender.saveImagePath(imagePath)
                MsToast.show(this@MassMessageConfigActivity, "群发信息已保存")
                finish()
            }
        }
        val title = TextView(this).apply {
            text = "设置群发信息"
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

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(MsColors.rowTitle)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        includeFontPadding = false
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun roundedBg(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
}
