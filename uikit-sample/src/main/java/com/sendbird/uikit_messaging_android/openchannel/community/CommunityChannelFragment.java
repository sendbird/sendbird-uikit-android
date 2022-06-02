package com.sendbird.uikit_messaging_android.openchannel.community;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.modules.OpenChannelModule;
import com.sendbird.uikit.modules.components.OpenChannelHeaderComponent;

/**
 * Displays an open channel screen used for community.
 */
public class CommunityChannelFragment extends OpenChannelFragment {
    @Override
    protected void onConfigureParams(@NonNull OpenChannelModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);
        OpenChannelModule.Params params = module.getParams();
        params.setUseHeader(true);

        OpenChannelHeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        headerParams.setUseLeftButton(true);
    }

    @NonNull
    @Override
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString("CHANNEL_URL", "");
    }
}
