package com.sendbird.uikit.customsample.openchannel.community;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.activities.OpenChannelActivity;


/**
 * Displays an open channel screen used for community.
 */
public class CommunityActivity extends OpenChannelActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        final Intent intent = getIntent();
        final Bundle args = intent != null && intent.getExtras() != null ? intent.getExtras() : new Bundle();
        CommunityChannelFragment fragment = new CommunityChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
