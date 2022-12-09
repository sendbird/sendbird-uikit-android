package com.sendbird.uikit.customsample.openchannel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.Sender;
import com.sendbird.uikit.activities.viewholder.MessageViewHolder;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.databinding.ViewOpenChannelHighlightMessageHolderBinding;
import com.sendbird.uikit.customsample.utils.DrawableUtils;
import com.sendbird.uikit.model.MessageListUIParams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ViewHolder to draw the highlight message for <code>OpenChannel</code>.
 */
public class HighlightOpenChannelMessageViewHolder extends MessageViewHolder {
    private final ViewOpenChannelHighlightMessageHolderBinding binding;
    private final int operatorAppearance;
    private final int nicknameAppearance;

    public HighlightOpenChannelMessageViewHolder(@NonNull ViewOpenChannelHighlightMessageHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        TypedArray a = binding.getRoot().getContext().getTheme().obtainStyledAttributes(null, com.sendbird.uikit.R.styleable.MessageView, 0, 0);
        try {
            nicknameAppearance = a.getResourceId(com.sendbird.uikit.R.styleable.MessageView_sb_message_sender_name_text_appearance, com.sendbird.uikit.R.style.SendbirdCaption1OnLight02);
            operatorAppearance = a.getResourceId(com.sendbird.uikit.R.styleable.MessageView_sb_message_operator_name_text_appearance, com.sendbird.uikit.R.style.SendbirdCaption1Secondary300);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageListUIParams params) {
        OpenChannel openChannel;
        if (channel instanceof OpenChannel) {
            openChannel = (OpenChannel) channel;
        } else {
            return;
        }

        Context context = binding.getRoot().getContext();
        DrawableUtils.drawStatus(binding.ivStatus, message);

        binding.ivProfileView.setVisibility(View.VISIBLE);
        binding.tvNickname.setVisibility(View.VISIBLE);
        binding.tvSentAt.setVisibility(View.VISIBLE);
        String sentAt = DateUtils.formatDateTime(context, message.getCreatedAt(), DateUtils.FORMAT_SHOW_TIME);
        binding.tvSentAt.setText(sentAt);

        if (openChannel.isOperator(message.getSender())) {
            binding.tvNickname.setTextAppearance(context, operatorAppearance);
        } else {
            binding.tvNickname.setTextAppearance(context, nicknameAppearance);
        }

        Sender sender = message.getSender();
        String nickname = sender == null || TextUtils.isEmpty(sender.getNickname()) ?
                context.getString(R.string.sb_text_channel_list_title_unknown) :
                sender.getNickname();
        binding.tvNickname.setText(nickname);

        String url = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
        }

        Drawable errorIcon = DrawableUtils.createOvalIcon(binding.getRoot().getContext(),
                R.color.background_300, R.drawable.icon_user, R.color.ondark_01);
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(errorIcon)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivProfileView);

        binding.tvMessage.setText(message.getMessage());
    }

    @NonNull
    @Override
    public Map<String, View> getClickableViewMap() {
        return new ConcurrentHashMap<String, View>() {{
            put(ClickableViewIdentifier.Chat.name(), binding.contentPanel);
            put(ClickableViewIdentifier.Profile.name(), binding.ivProfileView);
        }};
    }
}
