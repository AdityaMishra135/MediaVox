package org.skywaves.mediavox.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import com.google.android.material.textview.MaterialTextView
import org.skywaves.mediavox.R
import org.skywaves.mediavox.core.extensions.getProperPrimaryColor
import org.skywaves.mediavox.core.extensions.getProperTextColor

class SettingsCategoryItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val mTitle: MaterialTextView
    private val mText: MaterialTextView
    private lateinit var mIcon: ImageView

    init {
        val paddingDef = DimensionsUtil.getDimensionPixelSize(context, 12f)
        setPadding(
            DimensionsUtil.getDimensionPixelSize(context, 8f),
            paddingDef,
            paddingDef,
            paddingDef
        )

        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)

        val view = inflate(context, R.layout.settings_category_list_item, this)
        mTitle = view.findViewById(R.id.settings_list_item_title)
        mText = view.findViewById(R.id.settings_list_item_text)
        mTitle.setTextColor(context.getProperTextColor())
        mText.setTextColor(context.getProperTextColor())

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsCategoryItemView)

        if (mTitle.typeface != null && typedArray.hasValue(R.styleable.SettingsCategoryItemView_android_textStyle)) {
            mTitle.setTypeface(
                mTitle.typeface,
                typedArray.getInteger(
                    R.styleable.SettingsCategoryItemView_android_textStyle,
                    Typeface.BOLD
                )
            )
        }

        if (typedArray.hasValue(R.styleable.SettingsCategoryItemView_settingItemIcon)) {
            mIcon = view.findViewById(R.id.settings_list_item_icon)
            mIcon.setImageDrawable(typedArray.getDrawable(R.styleable.SettingsCategoryItemView_settingItemIcon))

            var iconColor =
                typedArray.getColor(R.styleable.SettingsCategoryItemView_settingItemIconColor, context.getProperPrimaryColor())

            val isColoredIcon = typedArray.getBoolean(
                R.styleable.SettingsCategoryItemView_settingItemColoredIcon,
                false
            )
            var iconBackgroundColor = if (isColoredIcon) iconColor else 0

            if (isColoredIcon) {
                iconColor =
                    context.resources.getColor(context.getProperPrimaryColor())
                iconBackgroundColor = mixColors(iconBackgroundColor, Color.WHITE, 0.4f)
            }


            mIcon.backgroundTintList = ColorStateList.valueOf(iconBackgroundColor)
            mIcon.imageTintList = ColorStateList.valueOf(iconColor)
        }

        mTitle.text = typedArray.getText(R.styleable.SettingsCategoryItemView_settingItemTitle)
        mText.text = typedArray.getText(R.styleable.SettingsCategoryItemView_settingItemText)

        typedArray.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        mTitle.isEnabled = enabled
        mText.isEnabled = enabled
        if (null != mIcon) mIcon.isEnabled = enabled
        super.setEnabled(enabled)
    }


    companion object {
        @ColorInt
        fun mixColors(@ColorInt color1: Int, @ColorInt color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val r = Color.red(color1) * ratio + Color.red(color2) * inverseRatio
            val g = Color.green(color1) * ratio + Color.green(color2) * inverseRatio
            val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }
}