package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.FileMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.PhotoViewFragment;

/**
 * Activity displays a image file.
 */
public class PhotoViewActivity extends AppCompatActivity {
    public static Intent newIntent(@NonNull Context context, @NonNull BaseChannel.ChannelType channelType, @NonNull FileMessage message) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(StringSet.KEY_MESSAGE_ID, message.getMessageId());
        intent.putExtra(StringSet.KEY_MESSAGE_FILENAME, message.getName());
        intent.putExtra(StringSet.KEY_CHANNEL_URL, message.getChannelUrl());
        intent.putExtra(StringSet.KEY_IMAGE_URL, message.getUrl());
        intent.putExtra(StringSet.KEY_MESSAGE_MIMETYPE, message.getType());
        intent.putExtra(StringSet.KEY_MESSAGE_CREATEDAT, message.getCreatedAt());
        intent.putExtra(StringSet.KEY_SENDER_ID, message.getSender().getUserId());
        intent.putExtra(StringSet.KEY_MESSAGE_SENDER_NAME, message.getSender().getNickname());
        intent.putExtra(StringSet.KEY_CHANNEL_TYPE, channelType);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.sb_activity);

        Intent intent = getIntent();
        long messageId = intent.getLongExtra(StringSet.KEY_MESSAGE_ID, 0L);
        String senderId = intent.getStringExtra(StringSet.KEY_SENDER_ID);
        String channelUrl = intent.getStringExtra(StringSet.KEY_CHANNEL_URL);
        String fileName = intent.getStringExtra(StringSet.KEY_MESSAGE_FILENAME);
        String url = intent.getStringExtra(StringSet.KEY_IMAGE_URL);
        String mimeType = intent.getStringExtra(StringSet.KEY_MESSAGE_MIMETYPE);
        String senderNickname = intent.getStringExtra(StringSet.KEY_MESSAGE_SENDER_NAME);
        long createdAt = intent.getLongExtra(StringSet.KEY_MESSAGE_CREATEDAT, 0L);
        BaseChannel.ChannelType channelType = (BaseChannel.ChannelType) intent.getSerializableExtra(StringSet.KEY_CHANNEL_TYPE);

        PhotoViewFragment fragment = new PhotoViewFragment.Builder(senderId, fileName,
                channelUrl, url, mimeType, senderNickname, createdAt,
                messageId, channelType, SendBirdUIKit.getDefaultThemeMode())
                .build();

        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();
    }
}
