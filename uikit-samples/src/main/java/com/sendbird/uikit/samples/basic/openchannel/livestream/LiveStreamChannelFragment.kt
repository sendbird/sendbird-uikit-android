package com.sendbird.uikit.samples.basic.openchannel.livestream

import android.content.res.Configuration
import android.os.Bundle
import com.sendbird.uikit.fragments.OpenChannelFragment
import com.sendbird.uikit.modules.OpenChannelModule

/**
 * Displays an open channel screen used for live stream.
 */
class LiveStreamChannelFragment : OpenChannelFragment() {
    override fun onConfigureParams(module: OpenChannelModule, args: Bundle) {
        super.onConfigureParams(module, args)
        val moduleParams = module.params
        moduleParams.setUseOverlayMode(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        moduleParams.setUseHeader(true)
    }
}
