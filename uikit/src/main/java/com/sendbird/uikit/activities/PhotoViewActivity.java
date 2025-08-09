package com.sendbird.uikit.activities;

import static com.sendbird.uikit.internal.extensions.ViewExtensionsKt.setInsetMarginAndStatusBarColor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MultipleFilesMessage;
import com.sendbird.android.message.UploadedFileInfo;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.PhotoViewFragment;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
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
    public static Intent newIntent(@NonNull Context context, @NonNull ChannelType channelType, @NonNull MultipleFilesMessage message, int index) {
        return newIntent(context, channelType, message, index, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull ChannelType channelType, @NonNull FileMessage message, @StyleRes int themeResId) {
        return newIntent(
            context,
            channelType,
            message.getMessageId(),
            message.getChannelUrl(),
            message.getUrl(),
            message.getPlainUrl(),
            message.getRequestId(),
            message.getName(),
            message.getType(),
            message.getCreatedAt(),
            message.getSender() == null ? "0" : message.getSender().getUserId(),
            message.getSender() == null ? "" : message.getSender().getNickname(),
            MessageUtils.isDeletableMessage(message),
            themeResId
        );
    }

    @NonNull
    public static Intent newIntent(
        @NonNull Context context,
        @NonNull ChannelType channelType,
        @NonNull MultipleFilesMessage message,
        int index, // Assuming that index is always in the range of message.files.
        @StyleRes int themeResId
    ) {
        UploadedFileInfo uploadedFileInfo = message.getFiles().get(index);
        return newIntent(
            context,
            channelType,
            message.getMessageId(),
            message.getChannelUrl(),
            uploadedFileInfo.getUrl(),
            uploadedFileInfo.getPlainUrl(),
            MessageExtensionsKt.getCacheKey(message, index), // Since the request id is used for the cache key of image, it should be unique among the images in the same message.
            uploadedFileInfo.getFileName(),
            uploadedFileInfo.getFileType(),
            message.getCreatedAt(),
            message.getSender() == null ? "0" : message.getSender().getUserId(),
            message.getSender() == null ? "" : message.getSender().getNickname(),
            false, // Currently, each file of MultipleFilesMessage cannot be deleted.
            themeResId
        );
    }

    @NonNull
    private static Intent newIntent(
        @NonNull Context context,
        @NonNull ChannelType channelType,
        long messageId,
        @NonNull String channelUrl,
        @NonNull String imageUrl,
        @NonNull String plainUrl,
        @NonNull String requestId,
        @NonNull String fileName,
        @NonNull String fileType,
        long createdAt,
        @NonNull String senderId,
        @NonNull String senderName,
        boolean isDeletable,
        @StyleRes int themeResId
    ) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(StringSet.KEY_MESSAGE_ID, messageId);
        intent.putExtra(StringSet.KEY_MESSAGE_FILENAME, fileName);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        intent.putExtra(StringSet.KEY_IMAGE_URL, imageUrl);
        intent.putExtra(StringSet.KEY_IMAGE_PLAIN_URL, plainUrl);
        intent.putExtra(StringSet.KEY_REQUEST_ID, requestId);
        intent.putExtra(StringSet.KEY_MESSAGE_MIMETYPE, fileType);
        intent.putExtra(StringSet.KEY_MESSAGE_CREATEDAT, createdAt);
        intent.putExtra(StringSet.KEY_SENDER_ID, senderId);
        intent.putExtra(StringSet.KEY_MESSAGE_SENDER_NAME, senderName);
        intent.putExtra(StringSet.KEY_CHANNEL_TYPE, channelType);
        intent.putExtra(StringSet.KEY_DELETABLE_MESSAGE, isDeletable);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = getIntent().getIntExtra(StringSet.KEY_THEME_RES_ID, SendbirdUIKit.getDefaultThemeMode().getResId());
        setTheme(themeResId);
        setContentView(R.layout.sb_activity);

        View activityContainer = findViewById(R.id.sb_activity_container);
        View fragmentContainer = findViewById(R.id.sb_fragment_container);
        setInsetMarginAndStatusBarColor(activityContainer, fragmentContainer, getWindow());

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
