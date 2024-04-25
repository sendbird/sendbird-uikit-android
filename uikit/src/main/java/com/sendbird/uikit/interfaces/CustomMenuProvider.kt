package com.sendbird.uikit.interfaces

import android.content.Context
import android.view.View

/**
 * A provider interface for customizing the menu view.
 *
 * @since 3.16.0
 */
fun interface MenuViewProvider {
    /**
     * Provide menu view.
     *
     * @param context The context in which the theme is set
     * @param position The position of the current custom menu item
     * @return The menu view
     * @since 3.16.0
     */
    fun provideMenuView(context: Context, position: Int): View
}
