package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewMyMessageStatusBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class MyMessageStatusView extends FrameLayout {
    private final SbViewMyMessageStatusBinding binding;

    public MyMessageStatusView(@NonNull Context context) {
        this(context, null);
    }

    public MyMessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.binding = SbViewMyMessageStatusBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void drawError() {
        setProgress(false);
        final int errorColor = SendbirdUIKit.getDefaultThemeMode().getErrorColorResId();
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_error, errorColor));
    }

    public void drawRead() {
        setProgress(false);
        int readColor = SendbirdUIKit.getDefaultThemeMode().getSecondaryTintResId();
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done_all, readColor));
    }

    public void drawSent() {
        setProgress(false);
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()));
    }

    public void drawDelivered() {
        setProgress(false);
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done_all, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()));
    }

    public void drawProgress() {
        setProgress(true);
    }

    private void setProgress(boolean isProgress) {
        this.setVisibility(VISIBLE);
        if (isProgress) {
            this.binding.ivStatus.setVisibility(GONE);
            this.binding.mpvProgressStatus.setVisibility(VISIBLE);
        } else {
            this.binding.mpvProgressStatus.setVisibility(GONE);
            this.binding.ivStatus.setVisibility(VISIBLE);
        }
    }

    public void drawStatus(@NonNull BaseMessage message, @NonNull BaseChannel channel) {
        final SendingStatus status = message.getSendingStatus();

        switch (status) {
            case CANCELED:
            case FAILED:
                setVisibility(View.VISIBLE);
                drawError();
                break;
            case SUCCEEDED:
                if (channel.isGroupChannel()) {
                    GroupChannel groupChannel = (GroupChannel) channel;
                    if (groupChannel.isSuper() || groupChannel.isBroadcast()) {
                        setVisibility(View.GONE);
                        return;
                    }
                }

                if (channel.isGroupChannel()) {
                    setVisibility(View.VISIBLE);
                    GroupChannel groupChannel = (GroupChannel) channel;
                    int unreadMemberCount = groupChannel.getUnreadMemberCount(message);
                    int unDeliveredMemberCount = groupChannel.getUndeliveredMemberCount(message);

                    if (unreadMemberCount == 0) {
                        drawRead();
                    } else if (unDeliveredMemberCount == 0) {
                        drawDelivered();
                    } else {
                        drawSent();
                    }
                } else {
                    setVisibility(View.GONE);
                }
                break;
            case PENDING:
                drawProgress();
            default:
                break;
        }
    }
}
