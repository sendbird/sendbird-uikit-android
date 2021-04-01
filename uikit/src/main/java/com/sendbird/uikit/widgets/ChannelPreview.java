package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.UserMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;

public class ChannelPreview extends FrameLayout {
    private View layout;
    private ChannelCoverView coverView;
    private TextView tvTitle;
    private TextView tvMemberCount;
    private TextView tvUpdatedAt;
    private TextView tvLastMessage;
    private TextView tvUnreadCount;
    private ImageView ivPushEnabled;
    private ImageView ivBroadcast;
    private ImageView ivFrozen;

    public ChannelPreview(Context context) {
        this(context, null);
    }

    public ChannelPreview(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.sb_channel_preview_style);
    }

    public ChannelPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelPreview, defStyle, 0);
        try {
            this.layout = LayoutInflater.from(getContext()).inflate(R.layout.sb_view_channel_list_item, null);
            addView(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            this.coverView = layout.findViewById(R.id.ivChannelCover);
            this.tvTitle = layout.findViewById(R.id.tvTitle);
            this.tvMemberCount = layout.findViewById(R.id.tvMemberCount);
            this.ivPushEnabled = layout.findViewById(R.id.ivPushEnabledIcon);
            this.tvUpdatedAt = layout.findViewById(R.id.tvUpdatedAt);
            this.tvLastMessage = layout.findViewById(R.id.tvLastMessage);
            this.tvUnreadCount = layout.findViewById(R.id.tvUnreadCount);
            this.ivBroadcast = layout.findViewById(R.id.ivBroadcastIcon);
            this.ivFrozen = layout.findViewById(R.id.ivFrozenIcon);

            int titleAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_title_appearance, R.style.SendbirdSubtitle1OnLight01);
            int memberCountAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_member_count_appearance, R.style.SendbirdCaption1OnLight02);
            int updatedAtAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_updated_at_appearance, R.style.SendbirdCaption2OnLight02);
            int unReadCountAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_unread_count_appearance, R.style.SendbirdCaption1OnDark01);
            int lastMessageAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_last_message_appearance, R.style.SendbirdBody3OnLight03);

            this.tvTitle.setTextAppearance(context, titleAppearance);
            this.tvMemberCount.setTextAppearance(context, memberCountAppearance);
            this.tvUpdatedAt.setTextAppearance(context, updatedAtAppearance);
            this.tvUnreadCount.setTextAppearance(context, unReadCountAppearance);
            this.tvLastMessage.setTextAppearance(context, lastMessageAppearance);
        } finally {
            a.recycle();
        }
    }

    public void setChannel(GroupChannel channel) {
        drawChannel(this, channel);
    }

    @BindingAdapter("bind_channel")
    public static void drawChannel(ChannelPreview channelPreview, GroupChannel channel) {
        final BaseMessage lastMessage = channel.getLastMessage();
        final int unreadMessageCount = channel.getUnreadMessageCount();
        channelPreview.ivPushEnabled.setVisibility(ChannelUtils.isChannelPushOff(channel) ? View.VISIBLE : View.GONE);
        int pushEnabledTint = SendBirdUIKit.getDefaultThemeMode().getMonoTintResId();
        channelPreview.ivPushEnabled.setImageDrawable(DrawableUtils.setTintList(channelPreview.getContext(), R.drawable.icon_notifications_off_filled, pushEnabledTint));
        channelPreview.tvTitle.setText(ChannelUtils.makeTitleText(channelPreview.getContext(), channel));
        channelPreview.tvUnreadCount.setText(unreadMessageCount > 99 ? channelPreview.getContext().getString(R.string.sb_text_channel_list_unread_count_max) : String.valueOf(unreadMessageCount));
        channelPreview.tvUnreadCount.setVisibility(unreadMessageCount > 0 ? View.VISIBLE : View.GONE);
        channelPreview.tvUnreadCount.setBackgroundResource(SendBirdUIKit.isDarkMode() ? R.drawable.sb_shape_unread_message_count_dark : R.drawable.sb_shape_unread_message_count);
        channelPreview.ivFrozen.setVisibility(channel.isFrozen() ? View.VISIBLE : View.GONE);
        channelPreview.ivBroadcast.setVisibility(channel.isBroadcast() ? View.VISIBLE : View.GONE);
        ChannelUtils.loadChannelCover(channelPreview.coverView, channel);

        Context context = channelPreview.getContext();

        if (channel.isBroadcast()) {
            ColorStateList broadcastTint = SendBirdUIKit.getDefaultThemeMode().getSecondaryTintColorStateList(context);
            channelPreview.ivBroadcast.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_broadcast, broadcastTint));
        }

        if (channel.isFrozen()) {
            ColorStateList frozenTint = SendBirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(context);
            channelPreview.ivFrozen.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_freeze, frozenTint));
        }

        int memberCount = channel.getMemberCount();
        channelPreview.tvMemberCount.setVisibility(memberCount > 2 ? View.VISIBLE : View.GONE);
        channelPreview.tvMemberCount.setText(ChannelUtils.makeMemberCountText(channel.getMemberCount()));

        channelPreview.tvUpdatedAt.setText(String.valueOf(DateUtils.formatDateTime(channelPreview.getContext(), lastMessage != null ? lastMessage.getCreatedAt() : channel.getCreatedAt())));
        setLastMessage(channelPreview.tvLastMessage, channel);
    }

    public View getLayout() {
        return layout;
    }

    private static void setLastMessage(TextView textView, GroupChannel channel) {
        String message = "";
        final BaseMessage lastMessage = channel.getLastMessage();
        if (lastMessage instanceof UserMessage) {
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            message = lastMessage.getMessage();
        } else if (lastMessage instanceof FileMessage) {
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            message = ((FileMessage)lastMessage).getName();
        }
        textView.setText(message);
    }
}
