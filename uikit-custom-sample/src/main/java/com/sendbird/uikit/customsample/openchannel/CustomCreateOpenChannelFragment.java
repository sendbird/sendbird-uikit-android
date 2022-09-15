package com.sendbird.uikit.customsample.openchannel;

import static android.app.Activity.RESULT_OK;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.activities.OpenChannelActivity;
import com.sendbird.uikit.customsample.openchannel.community.CommunityActivity;
import com.sendbird.uikit.fragments.CreateOpenChannelFragment;

public class CustomCreateOpenChannelFragment extends CreateOpenChannelFragment {
    @Override
    protected void onNewChannelCreated(@NonNull OpenChannel channel) {
        if (isFragmentAlive() && getActivity() != null) {
            startActivity(OpenChannelActivity.newIntent(requireContext(), CommunityActivity.class, channel.getUrl()));
            getActivity().setResult(RESULT_OK);
            shouldActivityFinish();
        }
    }
}
