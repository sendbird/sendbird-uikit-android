package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.UserMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbViewMyQuotedMessageBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.UserUtils;
import com.sendbird.uikit.utils.ViewUtils;

public class MyQuotedMessageView extends BaseQuotedMessageView {
    private final SbViewMyQuotedMessageBinding binding;

    public MyQuotedMessageView(@NonNull Context context) {
        this(context, null);
    }

    public MyQuotedMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_my_message);
    }

    public MyQuotedMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyleAttr, 0);
        try {
            this.binding = SbViewMyQuotedMessageBinding.inflate(LayoutInflater.from(getContext()), this, true);
            int backgroundResId = a.getResourceId(R.styleable.MessageView_sb_quoted_message_me_background, R.drawable.sb_shape_chat_bubble);
            ColorStateList backgroundTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_background_tint);
            int titleIconId = a.getResourceId(R.styleable.MessageView_sb_quoted_message_me_title_icon, R.drawable.icon_reply_filled);
            ColorStateList titleIconTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_title_icon_tint);
            int titleTextAppearance = a.getResourceId(R.styleable.MessageView_sb_quoted_message_me_title_text_appearance, R.style.SendbirdCaption1OnLight01);
            ColorStateList messageIconTint = a.getColorStateList(R.styleable.MessageView_sb_quoted_message_me_file_icon_tint);
            int messageTextAppearance = a.getResourceId(R.styleable.MessageView_sb_quoted_message_me_text_appearance, R.style.SendbirdCaption2OnLight03);

            if (backgroundTint != null) {
                binding.quoteReplyMessagePanel.setBackground(DrawableUtils.setTintList(context, backgroundResId, backgroundTint.withAlpha(0x80)));
            } else {
                binding.quoteReplyMessagePanel.setBackgroundResource(backgroundResId);
            }
            binding.ivQuoteReplyIcon.setImageResource(titleIconId);
            binding.ivQuoteReplyIcon.setImageTintList(titleIconTint);
            binding.tvQuoteReplyTitle.setTextAppearance(context, titleTextAppearance);
            binding.tvQuoteReplyMessage.setTextAppearance(context, messageTextAppearance);
            binding.ivQuoteReplyMessageIcon.setImageTintList(messageIconTint);

            int bg = SendbirdUIKit.isDarkMode() ? R.drawable.sb_shape_quoted_message_thumbnail_background_dark : R.drawable.sb_shape_quoted_message_thumbnail_background;
            binding.ivQuoteReplyThumbnail.setBackgroundResource(bg);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void drawQuotedMessage(@Nullable BaseMessage message) {
        binding.quoteReplyPanel.setVisibility(GONE);
        if (message == null) return;
        if (message.getParentMessage() == null) return;

        final BaseMessage parentMessage = message.getParentMessage();

        binding.quoteReplyPanel.setVisibility(VISIBLE);
        binding.quoteReplyMessagePanel.setVisibility(GONE);
        binding.ivQuoteReplyMessageIcon.setVisibility(GONE);
        binding.quoteReplyThumbnailPanel.setVisibility(GONE);
        binding.tvQuoteReplyTitle.setText(String.format(getContext().getString(R.string.sb_text_replied_to),
                getContext().getString(R.string.sb_text_you), UserUtils.getDisplayName(getContext(), parentMessage.getSender(), true)));

        binding.ivQuoteReplyThumbnailOveray.setVisibility(GONE);
        RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                binding.ivQuoteReplyThumbnailOveray.setVisibility(GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                binding.ivQuoteReplyThumbnailOveray.setVisibility(VISIBLE);
                return false;
            }
        };

        if (parentMessage instanceof UserMessage) {
            binding.quoteReplyMessagePanel.setVisibility(VISIBLE);
            binding.tvQuoteReplyMessage.setText(parentMessage.getMessage());
            binding.tvQuoteReplyMessage.setSingleLine(false);
            binding.tvQuoteReplyMessage.setMaxLines(2);
            binding.tvQuoteReplyMessage.setEllipsize(TextUtils.TruncateAt.END);
        } else if (parentMessage instanceof FileMessage) {
            final FileMessage parentFileMessage = (FileMessage) parentMessage;
            String type = parentFileMessage.getType();
            binding.ivQuoteReplyThumbnail.setRadius(getResources().getDimensionPixelSize(R.dimen.sb_size_8));
            binding.tvQuoteReplyMessage.setSingleLine(true);
            binding.tvQuoteReplyMessage.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            if (type.toLowerCase().contains(StringSet.gif)) {
                binding.quoteReplyThumbnailPanel.setVisibility(VISIBLE);
                binding.ivQuoteReplyThumbnailIcon.setImageDrawable(DrawableUtils.createOvalIcon(getContext(), R.color.background_50, R.drawable.icon_gif, R.color.onlight_03));
                ViewUtils.drawQuotedMessageThumbnail(binding.ivQuoteReplyThumbnail, parentFileMessage, requestListener);
            } else if (type.toLowerCase().contains(StringSet.video)) {
                binding.quoteReplyThumbnailPanel.setVisibility(VISIBLE);
                binding.ivQuoteReplyThumbnailIcon.setImageDrawable(DrawableUtils.createOvalIcon(getContext(), R.color.background_50, R.drawable.icon_play, R.color.onlight_03));
                ViewUtils.drawQuotedMessageThumbnail(binding.ivQuoteReplyThumbnail, parentFileMessage, requestListener);
            } else if ((type.toLowerCase().startsWith(StringSet.audio))) {
                binding.quoteReplyMessagePanel.setVisibility(VISIBLE);
                binding.ivQuoteReplyMessageIcon.setVisibility(VISIBLE);
                binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_audio);
                binding.tvQuoteReplyMessage.setText(parentFileMessage.getName());
            } else if (type.startsWith(StringSet.image) && !type.contains(StringSet.svg)) {
                binding.quoteReplyThumbnailPanel.setVisibility(VISIBLE);
                binding.ivQuoteReplyThumbnailIcon.setImageResource(android.R.color.transparent);
                ViewUtils.drawQuotedMessageThumbnail(binding.ivQuoteReplyThumbnail, parentFileMessage, requestListener);
            } else {
                binding.quoteReplyMessagePanel.setVisibility(VISIBLE);
                binding.ivQuoteReplyMessageIcon.setVisibility(VISIBLE);
                binding.ivQuoteReplyMessageIcon.setImageResource(R.drawable.icon_file_document);
                binding.tvQuoteReplyMessage.setText(parentFileMessage.getName());
            }
        }
    }
}
