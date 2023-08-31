package com.sendbird.uikit_messaging_android.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.utils.DrawableUtils.setTintList
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils.isUsingDarkTheme

/**
 * View displaying icon and badge in tabs.
 */
class CustomTabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var tintColorRedId = 0
    private lateinit var badgeView: TextView
    private lateinit var iconView: ImageView
    private lateinit var titleView: TextView

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val inflater = LayoutInflater.from(getContext())
        val view = inflater.inflate(R.layout.view_custom_tab, this, true)
        badgeView = view.findViewById(R.id.badge)
        iconView = view.findViewById(R.id.ivIcon)
        titleView = view.findViewById(R.id.tvTitle)
        val isDarkMode = isUsingDarkTheme
        tintColorRedId = if (isDarkMode) R.color.selector_tab_tint_dark else R.color.selector_tab_tint
        val badgeTextAppearance =
            if (isDarkMode) R.style.SendbirdCaption3OnLight01 else R.style.SendbirdCaption3OnDark01
        val badgeBackgroundRes =
            if (isDarkMode) R.drawable.shape_badge_background_dark else R.drawable.shape_badge_background
        val titleTextAppearance =
            if (isDarkMode) R.style.SendbirdCaption2Primary200 else R.style.SendbirdCaption2Primary300
        badgeView.setTextAppearance(context, badgeTextAppearance)
        badgeView.setBackgroundResource(badgeBackgroundRes)
        titleView.setTextAppearance(context, titleTextAppearance)
        titleView.setTextColor(AppCompatResources.getColorStateList(context, tintColorRedId))
    }

    fun setBadgeVisibility(visibility: Int) {
        badgeView.visibility = visibility
    }

    fun setBadgeCount(countString: String?) {
        badgeView.text = countString
    }

    fun setIcon(@DrawableRes iconResId: Int) {
        iconView.setImageDrawable(setTintList(context, iconResId, tintColorRedId))
    }

    fun setTitle(title: String?) {
        titleView.text = title
    }
}
