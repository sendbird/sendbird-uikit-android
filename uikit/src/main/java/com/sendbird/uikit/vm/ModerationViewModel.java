package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.log.Logger;

/**
 * ViewModel preparing and managing data related with the moderation for a channel
 *
 * @since 3.0.0
 */
public class ModerationViewModel extends BaseViewModel {
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_MODERATION" + System.currentTimeMillis();
    private final String channelUrl;
    private GroupChannel channel;
    private final MutableLiveData<BaseChannel> frozenStateChanges = new MutableLiveData<>();
    private final MutableLiveData<Member.MemberState> myMemberStateChanges = new MutableLiveData<>();
    private final MutableLiveData<Member.Role> myRoleChanges = new MutableLiveData<>();
    private final MutableLiveData<String> isChannelDeleted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBanned = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isShowLoadingDialog = new MutableLiveData<>();

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @since 3.0.0
     */
    public ModerationViewModel(@NonNull String channelUrl) {
        super();
        this.channelUrl = channelUrl;
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel channel, BaseMessage message) {
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        ModerationViewModel.this.myMemberStateChanges.setValue(channel.getMyMemberState());
                    }
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ModerationFragment::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    ModerationViewModel.this.isChannelDeleted.setValue(channelUrl);
                }
            }

            @Override
            public void onChannelFrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onChannelFrozen(%s)", channel.isFrozen());
                    ModerationViewModel.this.frozenStateChanges.setValue(channel);
                }
            }

            @Override
            public void onChannelUnfrozen(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ModerationFragment::onChannelUnfrozen(%s)", channel.isFrozen());
                    ModerationViewModel.this.frozenStateChanges.setValue(channel);
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl()) && channel instanceof GroupChannel) {
                    if (((GroupChannel) channel).getMyRole() != Member.Role.OPERATOR) {
                        Logger.i(">> ModerationFragment::onOperatorUpdated()");
                        Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                        ModerationViewModel.this.myRoleChanges.setValue(((GroupChannel) channel).getMyRole());
                    }
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> ModerationFragment::onUserBanned()");
                    ModerationViewModel.this.isBanned.setValue(true);
                }
            }
        });
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
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

    @Override
    protected void onCleared() {
        super.onCleared();
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    /**
     * Returns LiveData that can be observed if the channel has been frozen.
     *
     * @return LiveData holding the latest {@code BaseChannel}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<BaseChannel> getFrozenStateChanges() {
        return frozenStateChanges;
    }

    /**
     * Returns LiveData that can be observed if the state of the current user is changed.
     *
     * @return LiveData holding the latest {@code Member.MemberState}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Member.MemberState> getMyMemberStateChanges() {
        return myMemberStateChanges;
    }

    /**
     * Returns LiveData that can be observed if the role of the current user is changed.
     *
     * @return LiveData holding the latest {@code Member.Role}
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Member.Role> getMyRoleChanges() {
        return myRoleChanges;
    }

    /**
     * Returns LiveData that can be observed if the channel has been deleted.
     *
     * @return LiveData holding the URL of the deleted channel
     * @since 3.0.0
     */
    @NonNull
    public LiveData<String> getIsChannelDeleted() {
        return isChannelDeleted;
    }

    /**
     * Returns LiveData that can be observed whether the current user is banned.
     *
     * @return LiveData holding the URL of the deleted channel
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getIsBanned() {
        return isBanned;
    }

    /**
     * Returns LiveData that can be observed if the loading dialog is showing.
     *
     * @return LiveData holding whether the loading dialog is showing
     * @since 3.0.0
     */
    @NonNull
    public LiveData<Boolean> getIsShowLoadingDialog() {
        return isShowLoadingDialog;
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
                    ModerationViewModel.this.channel = channel;
                    if (e1 != null) {
                        handler.onAuthenticationFailed();
                    } else {
                        handler.onAuthenticated();
                    }
                });
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    /**
     * Freezes the channel.
     *
     * @since 3.0.0
     */
    @UiThread
    public void freezeChannel() {
        this.isShowLoadingDialog.setValue(true);
        channel.freeze(e -> this.isShowLoadingDialog.setValue(false));
    }

    /**
     * Unfreezes the channel.
     *
     * @since 3.0.0
     */
    @UiThread
    public void unfreezeChannel() {
        this.isShowLoadingDialog.setValue(true);
        channel.unfreeze(e -> this.isShowLoadingDialog.setValue(false));
    }
}
