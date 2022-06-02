package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.interfaces.OnCompleteHandler;
import com.sendbird.uikit.log.Logger;

/**
 * ViewModel preparing and managing data related with the push setting of a channel
 *
 * @since 3.0.0
 */
public class ChannelPushSettingViewModel extends BaseViewModel {
    @NonNull
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_PUSH_SETTING" + System.currentTimeMillis();
    @NonNull
    private final String channelUrl;
    @Nullable
    private GroupChannel channel;
    @NonNull
    private final MutableLiveData<GroupChannel> channelUpdated = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> shouldFinish = new MutableLiveData<>();

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @since 3.0.0
     */
    public ChannelPushSettingViewModel(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
    }

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * @since 3.0.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                GroupChannel.getChannel(channelUrl, (channel, e1) -> {
                    ChannelPushSettingViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        registerChannelHandler();
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterChannelHandler();
    }

    /**
     * Returns {@code GroupChannel}. If the authentication failed, {@code null} is returned.
     *
     * @return {@code GroupChannel} this view model is currently associated with
     * @since 3.0.0
     */
    @Nullable
    public GroupChannel getChannel() {
        return channel;
    }

    /**
     * Returns URL of GroupChannel.
     *
     * @return The URL of a channel this view model is currently associated with
     * @since 3.0.0
     */
    @NonNull
    public String getChannelUrl() {
        return channelUrl;
    }

    /**
     * Returns LiveData that can be observed if the channel has been updated.
     *
     * @return LiveData holding the updated {@code GroupChannel}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<GroupChannel> getChannelUpdated() {
        return channelUpdated;
    }

    /**
     * Returns LiveData that can be observed if the Activity or Fragment should be finished.
     *
     * @return LiveData holding the event for whether the Activity or Fragment should be finished
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> shouldFinish() {
        return shouldFinish;
    }

    private void registerChannelHandler() {
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {}

            @Override
            public void onUserJoined(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserJoined()");
                    Logger.d("++ joind user : " + user);
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        shouldFinish.postValue(true);
                        return;
                    }
                    notifyChannelUpdated(channel);
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsViewModel::onChannelChanged()");
                    notifyChannelUpdated((GroupChannel) channel);
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ChannelSettingsViewModel::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    shouldFinish.postValue(true);
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> ChannelSettingsViewModel::onUserBanned()");
                    shouldFinish.postValue(true);
                }
            }
        });
    }

    private void unregisterChannelHandler() {
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        if (channel == null) return false;
        return channelUrl.equals(channel.getUrl());
    }

    private void notifyChannelUpdated(@NonNull GroupChannel channel) {
        this.channel = channel;
        channelUpdated.setValue(channel);
    }

    /**
     * Sets the push notification to on, off, or mentions only for the current channel.
     *
     * @param option pushTriggerOption `PushTriggerOption`. Refer to {@link com.sendbird.android.GroupChannel.PushTriggerOption}.
     * @param handler Callback handler called when this method is completed.
     * @since 3.0.0
     */
    public void requestPushOption(@NonNull GroupChannel.PushTriggerOption option, @Nullable OnCompleteHandler handler) {
        if (channel == null) {
            if (handler != null) handler.onComplete(new SendBirdException("Couldn't retrieve the channel"));
            return;
        }
        channel.setMyPushTriggerOption(option, e -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ toggle notification");
        });
    }
}
