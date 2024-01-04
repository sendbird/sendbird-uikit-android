package com.sendbird.uikit.internal.extensions

import com.sendbird.uikit.activities.adapter.MessageListAdapter
import com.sendbird.uikit.internal.interfaces.OnSubmitButtonClickListener

private var submitButtonClickListener: OnSubmitButtonClickListener? = null
internal var MessageListAdapter.submitButtonClickListener: OnSubmitButtonClickListener?
    get() = com.sendbird.uikit.internal.extensions.submitButtonClickListener
    set(value) {
        com.sendbird.uikit.internal.extensions.submitButtonClickListener = value
    }
