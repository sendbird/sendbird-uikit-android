package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.message.FileMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.PhotoViewFragment;
import com.sendbird.uikit.utils.MessageUtils;

/**
 * Activity displays a image file.
 */
public class PhotoViewActivity extends AppCompatActivity {
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull ChannelType channelType, @NonNull FileMessage message) {
        return newIntent(context, channelType, message, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull ChannelType channelType, @NonNull FileMessage message, @StyleRes int themeResId) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(StringSet.KEY_MESSAGE_ID, message.getMessageId());
        intent.putExtra(StringSet.KEY_MESSAGE_FILENAME, message.getName());
        intent.putExtra(StringSet.KEY_CHANNEL_URL, message.getChannelUrl());
        intent.putExtra(StringSet.KEY_IMAGE_URL, message.getUrl());
        intent.putExtra(StringSet.KEY_IMAGE_PLAIN_URL, message.getPlainUrl());
        intent.putExtra(StringSet.KEY_REQUEST_ID, message.getRequestId());
        intent.putExtra(StringSet.KEY_MESSAGE_MIMETYPE, message.getType());
        intent.putExtra(StringSet.KEY_MESSAGE_CREATEDAT, message.getCreatedAt());
        intent.putExtra(StringSet.KEY_SENDER_ID, message.getSender() == null ? 0 : message.getSender().getUserId());
        intent.putExtra(StringSet.KEY_MESSAGE_SENDER_NAME, message.getSender().getNickname());
        intent.putExtra(StringSet.KEY_CHANNEL_TYPE, channelType);
        intent.putExtra(StringSet.KEY_DELETABLE_MESSAGE, MessageUtils.isDeletableMessage(message));
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = getIntent().getIntExtra(StringSet.KEY_THEME_RES_ID, SendbirdUIKit.getDefaultThemeMode().getResId());
        setTheme(themeResId);
        setContentView(R.layout.sb_activity);


        final Intent intent = getIntent();
        final long messageId = intent.getLongExtra(StringSet.KEY_MESSAGE_ID, 0L);
        final String senderId = intent.getStringExtra(StringSet.KEY_SENDER_ID);
        final String channelUrl = intent.getStringExtra(StringSet.KEY_CHANNEL_URL);
        final String fileName = intent.getStringExtra(StringSet.KEY_MESSAGE_FILENAME);
        final String url = intent.getStringExtra(StringSet.KEY_IMAGE_URL);
        final String plainUrl = intent.getStringExtra(StringSet.KEY_IMAGE_PLAIN_URL);
        final String requestId = intent.getStringExtra(StringSet.KEY_REQUEST_ID);
        final String mimeType = intent.getStringExtra(StringSet.KEY_MESSAGE_MIMETYPE);
        final String senderNickname = intent.getStringExtra(StringSet.KEY_MESSAGE_SENDER_NAME);
        final long createdAt = intent.getLongExtra(StringSet.KEY_MESSAGE_CREATEDAT, 0L);
        final ChannelType channelType = (ChannelType) intent.getSerializableExtra(StringSet.KEY_CHANNEL_TYPE);
        final boolean isDeletable = intent.getBooleanExtra(StringSet.KEY_DELETABLE_MESSAGE, MessageUtils.isMine(senderId));

        final PhotoViewFragment fragment = new PhotoViewFragment.Builder(senderId, fileName,
                channelUrl, url, plainUrl, requestId, mimeType, senderNickname, createdAt,
                messageId, channelType, SendbirdUIKit.getDefaultThemeMode(), isDeletable)
                .build();

        final FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();
    }
}
