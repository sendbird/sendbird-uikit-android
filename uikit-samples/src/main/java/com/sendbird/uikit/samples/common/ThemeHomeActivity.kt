package com.sendbird.uikit.samples.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils

abstract class ThemeHomeActivity : AppCompatActivity() {
    abstract val binding: ViewBinding
    val isDarkTheme: Boolean
        get() = PreferenceUtils.themeMode.isUsingDarkTheme()
    private var currentThemeMode = PreferenceUtils.themeMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(if (PreferenceUtils.themeMode.isUsingDarkTheme()) R.style.AppTheme_Dark else R.style.AppTheme)
        setContentView(binding.root)
        applyTheme()
    }

    override fun onResume() {
        super.onResume()
        if (currentThemeMode != PreferenceUtils.themeMode) {
            currentThemeMode = PreferenceUtils.themeMode
            recreate()
        }
    }

    open fun applyTheme() {
        val backgroundRedId = if (isDarkTheme) R.color.background_600 else R.color.background_50
        binding.apply {
            root.setBackgroundResource(backgroundRedId)
        }
    }
}
