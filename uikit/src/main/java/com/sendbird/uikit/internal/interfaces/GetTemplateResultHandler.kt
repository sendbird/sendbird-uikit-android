package com.sendbird.uikit.internal.interfaces

import com.sendbird.android.exception.SendbirdException
import com.sendbird.message.template.model.TemplateParams

internal fun interface GetTemplateResultHandler {
    fun onResult(templateKey: String, templateParams: TemplateParams?, e: SendbirdException?)
}
