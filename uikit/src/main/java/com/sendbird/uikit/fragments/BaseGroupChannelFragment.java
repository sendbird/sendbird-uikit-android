package com.sendbird.uikit.fragments;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.utils.TextUtils;

abstract class BaseGroupChannelFragment extends BaseFragment {
    protected GroupChannel channel;

    abstract protected void onReadyFailure();
    abstract protected void onConfigure();
    abstract protected void onDrawPage();

    @Override
    public void onReady(User user, ReadyStatus status) {
        Logger.i(">> BaseGroupChannelFragment::onReady(%s)", status);
        Logger.i("++ user : %s", user);
        if (status == ReadyStatus.ERROR) {
            onReadyFailure();
            return;
        }

        String channelUrl = getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (!TextUtils.isEmpty(channelUrl)) {
            GroupChannel.getChannel(channelUrl, (channel, e) -> {
                if (!isActive()) return;
                if (e != null) {
                    toastError(R.string.sb_text_error_get_channel);
                    finish();
                    return;
                }
                this.channel = channel;
                onConfigure();
                onDrawPage();
            });
            return;
        }
        onConfigure();
        onDrawPage();
    }
}
