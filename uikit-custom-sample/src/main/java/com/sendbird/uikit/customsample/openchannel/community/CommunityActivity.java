package com.sendbird.uikit.customsample.openchannel.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.utils.ContextUtils;


/**
 * Displays an open channel screen used for community.
 */
public class CommunityActivity extends AppCompatActivity {

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        Intent intent = new Intent(context, CommunityActivity.class);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        String url = getIntent().getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(url)) {
            ContextUtils.toastError(this, R.string.sb_text_error_get_channel);
        } else {
            OpenChannel.getChannel(url, (openChannel, e) -> {
                if (e != null) {
                    ContextUtils.toastError(CommunityActivity.this, R.string.sb_text_error_get_channel);
                    return;
                }

                OpenChannelFragment fragment = createOpenChannelFragment(openChannel.getUrl());
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStack();
                manager.beginTransaction()
                        .replace(R.id.sb_fragment_container, fragment)
                        .commit();
            });
        }
    }

    @NonNull
    protected OpenChannelFragment createOpenChannelFragment(@NonNull String channelUrl) {
        final Bundle args = new Bundle();
        args.putString("CHANNEL_URL", channelUrl);

        CommunityChannelFragment fragment = new CommunityChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
