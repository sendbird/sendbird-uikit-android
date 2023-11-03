package com.sendbird.uikit.internal.extensions
import com.sendbird.uikit.internal.interfaces.OnSubmitButtonClickListener
import com.sendbird.uikit.modules.components.MessageListComponent

private var submitButtonClickListener: OnSubmitButtonClickListener? = null
internal var MessageListComponent.submitButtonClickListener: OnSubmitButtonClickListener?
    get() = com.sendbird.uikit.internal.extensions.submitButtonClickListener
    set(value) {
        com.sendbird.uikit.internal.extensions.submitButtonClickListener = value
    }
