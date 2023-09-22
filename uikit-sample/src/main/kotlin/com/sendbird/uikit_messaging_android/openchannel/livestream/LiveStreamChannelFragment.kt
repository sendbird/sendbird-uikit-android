package com.sendbird.uikit_messaging_android.openchannel.livestream

import android.content.res.Configuration
import android.os.Bundle
import com.sendbird.uikit.consts.KeyboardDisplayType
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.uikit.model.ReadyStatus
import com.sendbird.uikit.modules.OpenChannelModule
import com.sendbird.uikit.vm.OpenChannelViewModel

/**
 * Displays an open channel screen used for live stream.
 */
class LiveStreamChannelFragment : OpenChannelFragment() {
    private var inputText: String? = null

    override fun onConfigureParams(module: OpenChannelModule, args: Bundle) {
        super.onConfigureParams(module, args)
        val creatorName = args.getString("DESCRIPTION")
        inputText = args.getString("INPUT_TEXT")
        val moduleParams = module.params
        moduleParams.setUseOverlayMode(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        moduleParams.setUseHeader(true)
        val headerParams = module.headerComponent.params
        headerParams.description = creatorName
        val inputParams = module.messageInputComponent.params
        inputParams.inputText = inputText
        inputParams.keyboardDisplayType = KeyboardDisplayType.Dialog
    }

    public override fun onReady(status: ReadyStatus, module: OpenChannelModule, viewModel: OpenChannelViewModel) {
        super.onReady(status, module, viewModel)
        getModule().messageInputComponent.setOnInputTextChangedListener { s: CharSequence, _: Int, _: Int, _: Int ->
            inputText = s.toString()
        }
    }

    override fun getChannelUrl(): String {
        val args = if (arguments == null) Bundle() else requireArguments()
        return args.getString("CHANNEL_URL", "")
    }
}
