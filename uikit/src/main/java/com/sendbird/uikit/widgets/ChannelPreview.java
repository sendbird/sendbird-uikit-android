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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DateUtils;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.MessageUtils;

import java.util.List;

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
    private ImageView ivLastMessageStatus;

    private boolean useTypingIndicator = false;
    private boolean useMessageReceiptStatus = false;

    public ChannelPreview(@NonNull Context context) {
        this(context, null);
    }

    public ChannelPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_channel_preview);
    }

    public ChannelPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChannelPreview, defStyle, 0);
        try {
            this.layout = LayoutInflater.from(getContext()).inflate(R.layout.sb_view_channel_list_item, this, false);
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
            this.ivLastMessageStatus = layout.findViewById(R.id.ivLastMessageStatus);

            int background = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_background, R.drawable.selector_rectangle_light);
            int titleAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_title_appearance, R.style.SendbirdSubtitle1OnLight01);
            int memberCountAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_member_count_appearance, R.style.SendbirdCaption1OnLight02);
            int updatedAtAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_updated_at_appearance, R.style.SendbirdCaption2OnLight02);
            int unReadCountAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_unread_count_appearance, R.style.SendbirdCaption1OnDark01);
            int lastMessageAppearance = a.getResourceId(R.styleable.ChannelPreview_sb_channel_preview_last_message_appearance, R.style.SendbirdBody3OnLight03);

            this.layout.findViewById(R.id.root).setBackgroundResource(background);
            this.tvTitle.setTextAppearance(context, titleAppearance);
            this.tvMemberCount.setTextAppearance(context, memberCountAppearance);
            this.tvUpdatedAt.setTextAppearance(context, updatedAtAppearance);
            this.tvUnreadCount.setTextAppearance(context, unReadCountAppearance);
            this.tvLastMessage.setTextAppearance(context, lastMessageAppearance);
        } finally {
            a.recycle();
        }
    }

    public void drawChannel(@NonNull GroupChannel channel) {
        Context context = getContext();
        final BaseMessage lastMessage = channel.getLastMessage();
        final int unreadMessageCount = channel.getUnreadMessageCount();
        ivPushEnabled.setVisibility(ChannelUtils.isChannelPushOff(channel) ? View.VISIBLE : View.GONE);
        int pushEnabledTint = SendbirdUIKit.getDefaultThemeMode().getMonoTintResId();
        ivPushEnabled.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_notifications_off_filled, pushEnabledTint));
        tvTitle.setText(ChannelUtils.makeTitleText(context, channel));
        tvUnreadCount.setText(unreadMessageCount > 99 ? context.getString(R.string.sb_text_channel_list_unread_count_max) : String.valueOf(unreadMessageCount));
        tvUnreadCount.setVisibility(unreadMessageCount > 0 ? View.VISIBLE : View.GONE);
        tvUnreadCount.setBackgroundResource(SendbirdUIKit.isDarkMode() ? R.drawable.sb_shape_unread_message_count_dark : R.drawable.sb_shape_unread_message_count);
        ivFrozen.setVisibility(channel.isFrozen() ? View.VISIBLE : View.GONE);
        ivBroadcast.setVisibility(channel.isBroadcast() ? View.VISIBLE : View.GONE);
        ChannelUtils.loadChannelCover(coverView, channel);

        if (channel.isBroadcast()) {
            ColorStateList broadcastTint = SendbirdUIKit.getDefaultThemeMode().getSecondaryTintColorStateList(context);
            ivBroadcast.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_broadcast, broadcastTint));
        }

        if (channel.isFrozen()) {
            ColorStateList frozenTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(context);
            ivFrozen.setImageDrawable(DrawableUtils.setTintList(context, R.drawable.icon_freeze, frozenTint));
        }

        int memberCount = channel.getMemberCount();
        tvMemberCount.setVisibility(memberCount > 2 ? View.VISIBLE : View.GONE);
        tvMemberCount.setText(ChannelUtils.makeMemberCountText(channel.getMemberCount()));

        tvUpdatedAt.setText(DateUtils.formatDateTime(context, lastMessage != null ? lastMessage.getCreatedAt() : channel.getCreatedAt()));
        setLastMessage(tvLastMessage, channel, useTypingIndicator);

        ivLastMessageStatus.setVisibility(useMessageReceiptStatus ? View.VISIBLE : View.GONE);
        if (useMessageReceiptStatus) {
            if (lastMessage != null && MessageUtils.isMine(lastMessage) && !channel.isSuper() && channel.isGroupChannel()) {
                ivLastMessageStatus.setVisibility(View.VISIBLE);
                int unreadMemberCount = channel.getUnreadMemberCount(lastMessage);
                int unDeliveredMemberCount = channel.getUndeliveredMemberCount(lastMessage);
                if (unreadMemberCount == 0) {
                    int readColor = SendbirdUIKit.getDefaultThemeMode().getSecondaryTintResId();
                    ivLastMessageStatus.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_done_all, readColor));
                } else if (unDeliveredMemberCount == 0) {
                    ivLastMessageStatus.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_done_all, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()));
                } else {
                    ivLastMessageStatus.setImageDrawable(DrawableUtils.setTintList(getContext(), R.drawable.icon_done, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()));
                }
            } else {
                ivLastMessageStatus.setVisibility(View.GONE);
            }
        }
    }

    public void setUseMessageReceiptStatus(boolean useMessageReceiptStatus) {
        this.useMessageReceiptStatus = useMessageReceiptStatus;
    }

    public void setUseTypingIndicator(boolean useTypingIndicator) {
        this.useTypingIndicator = useTypingIndicator;
    }

    @NonNull
    public View getLayout() {
        return layout;
    }

    private static void setLastMessage(@NonNull TextView textView, @NonNull GroupChannel channel, boolean useTypingIndicator) {
        String message = "";
        final BaseMessage lastMessage = channel.getLastMessage();

        if (useTypingIndicator) {
            final List<User> typingUsers = channel.getTypingUsers();
            if (!typingUsers.isEmpty()) {
                message = ChannelUtils.makeTypingText(textView.getContext(), typingUsers);
                textView.setText(message);
                return;
            }
        }
        if (lastMessage instanceof UserMessage) {
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            message = lastMessage.getMessage();
        } else if (lastMessage instanceof FileMessage) {
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            message = ((FileMessage) lastMessage).getName();
        }
        textView.setText(message);
    }
}
