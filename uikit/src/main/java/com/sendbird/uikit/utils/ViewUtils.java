package com.sendbird.uikit.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.OGMetaData;
import com.sendbird.android.Sender;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.vm.PendingMessageRepository;
import com.sendbird.uikit.widgets.BaseQuotedMessageView;
import com.sendbird.uikit.widgets.EmojiReactionListView;
import com.sendbird.uikit.widgets.OgtagView;
import com.sendbird.uikit.widgets.RoundCornerView;

import java.util.List;

/**
 * The helper class for the drawing views in the UIKit.
 * It is used to draw common UI from each custom component.
 */
public class ViewUtils {
    private final static int MINIMUM_THUMBNAIL_WIDTH = 100;
    private final static int MINIMUM_THUMBNAIL_HEIGHT = 100;

    private static void drawUnknownMessage(TextView view, boolean isMine) {
        int unknownHintAppearance;
        if (isMine) {
            unknownHintAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnLight02 : R.style.SendbirdBody3OnDark02;
        } else {
            unknownHintAppearance = SendBirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnDark03 : R.style.SendbirdBody3OnLight02;
        }

        final int sizeOfFirstLine = 23;
        String unknownHintText = view.getContext().getResources().getString(R.string.sb_text_channel_unknown_type_text);
        final Spannable spannable = new SpannableString(unknownHintText);
        spannable.setSpan(new TextAppearanceSpan(view.getContext(), unknownHintAppearance), sizeOfFirstLine, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

    public static void drawTextMessage(@NonNull TextView textView, BaseMessage message, @StyleRes int editedTextAppearance) {
        drawTextMessage(textView, message, editedTextAppearance, null, 0, 0);
    }

    public static void drawTextMessage(@NonNull TextView textView, BaseMessage message, @StyleRes int editedTextAppearance, HighlightMessageInfo highlightMessageInfo, @ColorRes int backgroundColor, @ColorRes int foregroundColor) {
        if (message == null) {
            return;
        }

        if (MessageUtils.isUnknownType(message)) {
            drawUnknownMessage(textView, MessageUtils.isMine(message));
            return;
        }

        CharSequence text = message.getMessage();
        if (highlightMessageInfo != null && highlightMessageInfo.getMessageId() == message.getMessageId() && highlightMessageInfo.getUpdatedAt() == message.getUpdatedAt()) {
            SpannableStringBuilder builder = new SpannableStringBuilder(textView.getContext(), text);
            builder.addHighlightTextSpan(text.toString(), text.toString(), backgroundColor, foregroundColor);
            text = builder.build();
        }
        textView.setText(text);
        if (message.getUpdatedAt() <= 0L) {
            return;
        }

        String edited = textView.getResources().getString(R.string.sb_text_channel_message_badge_edited);
        final Spannable spannable = new SpannableString(edited);
        spannable.setSpan(new TextAppearanceSpan(textView.getContext(), editedTextAppearance),
                0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(spannable);
    }

    public static void drawOgtag(@NonNull ViewGroup parent, OGMetaData ogMetaData) {
        if (ogMetaData == null) {
            return;
        }

        parent.removeAllViews();
        OgtagView ogtagView = OgtagView.inflate(parent.getContext(), parent);
        ogtagView.drawOgtag(ogMetaData);
        parent.setOnClickListener(v -> {
            if (ogMetaData.getUrl() == null) {
                return;
            }

            Intent intent = IntentUtils.getWebViewerIntent(ogMetaData.getUrl());
            try {
                ogtagView.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Logger.e(e);
            }
        });
    }

    public static void drawReactionEnabled(EmojiReactionListView view, BaseChannel channel) {
        boolean canSendReaction = ReactionUtils.canSendReaction(channel);
        view.setClickable(canSendReaction);
        if (view.useMoreButton() != canSendReaction) {
            view.setUseMoreButton(canSendReaction);
            view.refresh();
        }
    }

    public static void drawNickname(TextView tvNickname, BaseMessage message) {
        if (message == null) {
            return;
        }

        Sender sender = message.getSender();
        String nickname = UserUtils.getDisplayName(tvNickname.getContext(), sender);
        tvNickname.setText(nickname);
    }

    public static void drawProfile(ImageView ivProfile, BaseMessage message) {
        if (message == null) {
            return;
        }
        Sender sender = message.getSender();

        String url = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
        }

        drawProfile(ivProfile, url);
    }

    public static void drawProfile(ImageView ivProfile, String url) {
        int iconTint = SendBirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = R.color.background_300;
        Drawable errorDrawable = DrawableUtils.createOvalIcon(ivProfile.getContext(),
                backgroundTint, R.drawable.icon_user, iconTint);

        Glide.with(ivProfile.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(url))
                .error(errorDrawable)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfile);
    }

    public static void drawThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message) {
        drawThumbnail(view, message, null, R.dimen.sb_size_48);
    }

    public static void drawQuotedMessageThumbnail(@NonNull RoundCornerView view,
                                                  @NonNull FileMessage message,
                                                  @Nullable RequestListener<Drawable> requestListener) {
        drawThumbnail(view, message, requestListener, R.dimen.sb_size_24);
    }

    private static void drawThumbnail(@NonNull RoundCornerView view,
                                      @NonNull FileMessage message,
                                      @Nullable RequestListener<Drawable> requestListener,
                                      @DimenRes int iconSize
                                      ) {
        String url = message.getUrl();
        if (TextUtils.isEmpty(url) && message.getMessageParams() != null &&
                message.getMessageParams().getFile() != null) {
            url = message.getMessageParams().getFile().getAbsolutePath();
        }
        Context context = view.getContext();
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        RequestBuilder<Drawable> builder = Glide.with(context)
                .asDrawable()
                .apply(options);

        Pair<Integer, Integer> defaultResizingSize = SendBirdUIKit.getResizingSize();
        int width = defaultResizingSize.first / 2;
        int height = defaultResizingSize.second / 2;
        FileInfo fileInfo = PendingMessageRepository.getInstance().getFileInfo(message);
        if (fileInfo != null) {
            width = fileInfo.getThumbnailWidth();
            height = fileInfo.getThumbnailHeight();
            builder = builder.override(width, height);
            if (!TextUtils.isEmpty(fileInfo.getThumbnailPath())) {
                url = fileInfo.getThumbnailPath();
            }
        } else {
            List<FileMessage.Thumbnail> thumbnails = message.getThumbnails();
            FileMessage.Thumbnail thumbnail = null;
            if (thumbnails.size() > 0) {
                thumbnail = thumbnails.get(0);
            }
            if (thumbnail != null && !TextUtils.isEmpty(thumbnail.getUrl())) {
                Logger.dev("++ thumbnail width : %s, thumbnail height : %s", thumbnail.getRealWidth(), thumbnail.getRealHeight());
                width = Math.max(MINIMUM_THUMBNAIL_WIDTH, thumbnail.getRealWidth());
                height = Math.max(MINIMUM_THUMBNAIL_HEIGHT, thumbnail.getRealHeight());
                url = thumbnail.getUrl();
                builder = builder.override(width, height);
            } else {
                final int size = Math.min(Math.max(MINIMUM_THUMBNAIL_WIDTH, width), Math.max(MINIMUM_THUMBNAIL_HEIGHT, height));
                builder = builder.override(size);
            }
        }

        if (message.getType().toLowerCase().contains(StringSet.image) && !message.getType().toLowerCase().contains(StringSet.gif)) {
            view.getContent().setScaleType(ImageView.ScaleType.CENTER);
            int thumbnailIconTint = SendBirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
            builder = builder
                    .placeholder(DrawableUtils.setTintList(
                            ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_photo), iconSize, iconSize),
                            AppCompatResources.getColorStateList(context, thumbnailIconTint)))
                    .error(DrawableUtils.setTintList(
                            ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_thumbnail_none), iconSize, iconSize),
                            AppCompatResources.getColorStateList(context, thumbnailIconTint)));
        }

        Logger.d("-- will load thumbnail url : %s", url);
        builder.load(url).centerCrop().thumbnail(0.3f).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (requestListener != null) {
                    requestListener.onLoadFailed(e, model, target, isFirstResource);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                view.getContent().setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (requestListener != null) {
                    requestListener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                }
                return false;
            }
        }).into(view.getContent());
    }

    public static void drawThumbnailIcon(ImageView imageView, FileMessage fileMessage) {
        String type = fileMessage.getType();
        Context context = imageView.getContext();
        int backgroundTint = R.color.ondark_01;
        int iconTint = R.color.onlight_02;
        if (type.toLowerCase().contains(StringSet.gif)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_gif, iconTint));
        } else if (type.toLowerCase().contains(StringSet.video)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_play, iconTint));
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }
    }

    public static void drawFileIcon(ImageView imageView, FileMessage fileMessage) {
        Context context = imageView.getContext();
        int backgroundTint = SendBirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50;
        int iconTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_4);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);
        if ((fileMessage.getType().toLowerCase().startsWith(StringSet.audio))) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawFileMessageIconToReply(ImageView imageView, FileMessage fileMessage) {
        String type = fileMessage.getType();
        Context context = imageView.getContext();
        int backgroundTint = SendBirdUIKit.isDarkMode() ? R.color.background_500 : R.color.background_100;
        int iconTint = SendBirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_8);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);

        if ((fileMessage.getType().toLowerCase().startsWith(StringSet.audio))) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else if ((type.startsWith(StringSet.image) && !type.contains(StringSet.svg)) ||
                type.toLowerCase().contains(StringSet.gif) ||
                type.toLowerCase().contains(StringSet.video)) {
            imageView.setImageResource(android.R.color.transparent);
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawQuotedMessage(@NonNull BaseQuotedMessageView replyPanel, @NonNull BaseMessage message) {
        final boolean hasParentMessage = message.getParentMessageId() != 0L;
        replyPanel.setVisibility(hasParentMessage ? View.VISIBLE : View.GONE);
        replyPanel.drawQuotedMessage(message);
    }
}
