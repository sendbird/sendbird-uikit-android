package com.sendbird.uikit.internal.interfaces

import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.model.Form

internal fun interface OnSubmitButtonClickListener {
    fun onClicked(message: BaseMessage, form: Form)
}
