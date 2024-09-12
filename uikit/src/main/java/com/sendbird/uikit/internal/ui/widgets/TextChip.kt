package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.extensions.intToDp

internal class TextChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Chip(context, attrs, defStyleAttr) {
    private val isDarkMode = SendbirdUIKit.isDarkMode()
    private val chipEnabledBackgroundColor: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.selector_form_chip_background_dark else R.color.selector_form_chip_background_light)
    private val chipDisabledBackgroundColor: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.onlight_chip_disabled else R.color.ondark_chip_disabled)
    private val chipEnabledTextColor: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.selector_form_chip_text_dark else R.color.selector_form_chip_text_light)
    private val chipDisabledTextColor: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.selector_form_chip_disabled_text_dark else R.color.selector_form_chip_disabled_text_light)
    private val chipEnabledStrokeColor: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.selector_form_chip_stroke_dark else R.color.selector_form_chip_stroke_light)
    private val closeSubmittedIconTint: ColorStateList? = ContextCompat.getColorStateList(context, if (isDarkMode) R.color.secondary_200 else R.color.secondary_300)

    var isChipEnabled: Boolean = true
        set(value) {
            chipBackgroundColor = if (value) chipEnabledBackgroundColor else chipDisabledBackgroundColor
            setTextColor(if (value) chipEnabledTextColor else chipDisabledTextColor)
            closeIcon = if (value) null else ResourcesCompat.getDrawable(resources, R.drawable.icon_done, null)
            chipStrokeColor = if (value) chipEnabledStrokeColor else null
            chipStrokeWidth = if (value) resources.intToDp(1).toFloat() else 0f
            closeIconTint = if (value) null else closeSubmittedIconTint
            closeIconStartPadding = if (value) 0f else resources.intToDp(4).toFloat()
            isEnabled = value
            field = value
        }

    var isChipSelected: Boolean = false
        set(value) {
            isChecked = value
            isCloseIconVisible = value
            field = value
        }

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setTypeface(typeface, Typeface.BOLD)
        textStartPadding = 0f
        textEndPadding = 0f
        ellipsize = TextUtils.TruncateAt.END
        chipStartPadding = resources.intToDp(12).toFloat()
        chipEndPadding = resources.intToDp(12).toFloat()
        checkedIcon = null
        isCheckable = true
        isClickable = true
        setEnsureMinTouchTargetSize(false)
    }
}
