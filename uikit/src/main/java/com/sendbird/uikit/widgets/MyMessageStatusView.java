package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewMyMessageStatusBinding;
import com.sendbird.uikit.utils.DrawableUtils;

public class MyMessageStatusView extends FrameLayout {
    private SbViewMyMessageStatusBinding binding;

    public MyMessageStatusView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyMessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyMessageStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.sb_view_my_message_status, this, true);
    }

    public void drawError() {
        setProgress(false);
        int alertColor = SendBirdUIKit.isDarkMode() ? R.color.error_200 : R.color.error_300;
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_error, alertColor));
    }

    public void drawRead() {
        setProgress(false);
        int readColor = SendBirdUIKit.getDefaultThemeMode().getSecondaryTintResId();
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done_all, readColor));
    }

    public void drawSent() {
        setProgress(false);
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done, SendBirdUIKit.getDefaultThemeMode().getMonoTintResId()));
    }

    public void drawDelivered() {
        setProgress(false);
        this.binding.ivStatus.setImageDrawable(DrawableUtils.setTintList(getContext(),
                R.drawable.icon_done_all, SendBirdUIKit.getDefaultThemeMode().getMonoTintResId()));
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

    public void drawStatus(BaseMessage message, BaseChannel channel) {
        if (message == null) {
            return;
        }

        BaseMessage.SendingStatus status = message.getSendingStatus();

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
