package com.sendbird.uikit_messaging_android.openchannel.livestream;

import androidx.annotation.Nullable;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit_messaging_android.consts.StringSet;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListFragment;

/**
 * Displays an open channel list screen used for live stream.
 */
public class LiveStreamListFragment extends OpenChannelListFragment {
    public LiveStreamListFragment() {
        super(new LiveStreamListAdapter());
        setCustomTypeFilter(StringSet.SB_LIVE_TYPE);
    }

    @Override
    protected void clickOpenChannelItem(@Nullable OpenChannel openChannel) {
        if (getContext() == null || openChannel == null) return;
        startActivity(LiveStreamActivity.newIntent(getContext(), openChannel.getUrl()));
    }
}
