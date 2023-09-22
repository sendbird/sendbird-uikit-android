package com.sendbird.uikit_messaging_android.openchannel.livestream

import android.view.View
import com.sendbird.android.channel.OpenChannel
import com.sendbird.uikit.fragments.OpenChannelListFragment

/**
 * Displays an open channel list screen used for live stream.
 */
class LiveStreamListFragment : OpenChannelListFragment() {
    override fun onItemClicked(view: View, position: Int, channel: OpenChannel) {
        startActivity(LiveStreamActivity.newIntent(requireContext(), channel.url))
    }
}
