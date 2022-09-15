package com.sendbird.uikit.customsample.openchannel.livestream;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.fragments.OpenChannelListFragment;

/**
 * Displays an open channel list screen used for live stream.
 */
public class LiveStreamListFragment extends OpenChannelListFragment {
    @Override
    protected void onItemClicked(@NonNull View view, int position, @NonNull OpenChannel channel) {
        startActivity(LiveStreamActivity.newIntent(requireContext(), channel.getUrl()));
    }
}
