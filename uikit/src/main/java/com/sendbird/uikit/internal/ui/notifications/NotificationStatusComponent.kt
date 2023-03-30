package com.sendbird.uikit.internal.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.modules.components.StatusComponent

@JvmSuppressWildcards
internal class NotificationStatusComponent(
    private val uiConfig: NotificationConfig? = null
) : StatusComponent() {

    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        val layout = super.onCreateView(context, inflater, parent, args)
        uiConfig?.let {
            val themeMode = it.themeMode
            it.theme.listTheme.apply {
                layout.setBackgroundColor(backgroundColor.getColor(themeMode))
            }
        }
        return layout
    }
}
