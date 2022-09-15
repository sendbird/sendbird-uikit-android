package com.sendbird.uikit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.utils.DrawableUtils

internal class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ProgressBar(context, attrs) {
    init {
        val loadingTint = SendbirdUIKit.getDefaultThemeMode().primaryTintResId
        val loading = DrawableUtils.setTintList(context, R.drawable.sb_progress, loadingTint)
        this.indeterminateDrawable = loading
    }
}
