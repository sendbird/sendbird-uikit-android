package com.sendbird.uikit.interfaces

import android.view.View
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.model.Action

/**
 * Interface definition for a callback to be invoked when a item is invoked with an event.
 *
 * @since 3.16.0
 */
fun interface OnMessageTemplateActionHandler {
    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param view the view that was clicked.
     * @param action the registered Action data
     * @param message the clicked message
     * @since 3.16.0
     */
    fun onHandleAction(view: View, action: Action, message: BaseMessage)
}
