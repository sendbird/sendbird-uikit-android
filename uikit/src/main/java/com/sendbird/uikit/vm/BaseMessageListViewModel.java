package com.sendbird.uikit.vm;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.collection.CollectionEventSource;
import com.sendbird.android.collection.MessageContext;
import com.sendbird.android.collection.Traceable;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.message.UserMessage;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.interfaces.OnCompleteHandler;
import com.sendbird.uikit.interfaces.OnPagedDataLoader;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.LiveDataEx;
import com.sendbird.uikit.model.MentionSuggestion;
import com.sendbird.uikit.model.MessageList;
import com.sendbird.uikit.model.MutableLiveDataEx;

import java.io.File;
import java.util.List;

abstract public class BaseMessageListViewModel extends BaseViewModel implements OnPagedDataLoader<List<BaseMessage>> {
    @Nullable
    GroupChannel channel;
    @NonNull
    private final String channelUrl;
    @NonNull
    private final MemberFinder memberFinder;
    @NonNull
    final MessageList cachedMessages = new MessageList();
    @NonNull
    final MutableLiveDataEx<ChannelViewModel.ChannelMessageData> messageList = new MutableLiveDataEx<>();

    public BaseMessageListViewModel(@NonNull String channelUrl) {
        super();
        this.channel = null;
        this.channelUrl = channelUrl;
        this.memberFinder = new MemberFinder(channelUrl, SendbirdUIKit.getUserMentionConfig());
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
     * Returns LiveData that can be observed for the list of messages.
     *
     * @return LiveData holding the latest {@link ChannelViewModel.ChannelMessageData}
     * @since 3.0.0
     */
    @NonNull
    public LiveDataEx<ChannelViewModel.ChannelMessageData> getMessageList() {
        return messageList;
    }

    /**
     * Returns LiveData that can be observed for suggested information from mention.
     *
     * @return LiveData holding {@link MentionSuggestion} for this view model
     * @since 3.0.0
     */
    @NonNull
    public LiveData<MentionSuggestion> getMentionSuggestion() {
        return memberFinder.getMentionSuggestion();
    }

    @Override
    abstract public boolean hasNext();

    @Override
    abstract public boolean hasPrevious();

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.dev("-- onCleared ChannelViewModel");
        memberFinder.dispose();
    }

    /**
     * Sets whether the current user is typing.
     *
     * @param isTyping {@code true} if the current user is typing, {@code false} otherwise
     */
    public void setTyping(boolean isTyping) {
        if (channel != null) {
            if (isTyping) {
                channel.startTyping();
            } else {
                channel.endTyping();
            }
        }
    }

    /**
     * Sends a text message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @since 3.0.0
     */
    public void sendUserMessage(@NonNull UserMessageCreateParams params) {
        Logger.i("++ request send message : %s", params);
        if (channel != null) {
            channel.sendUserMessage(params, (message, e) -> {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                Logger.i("++ sent message : %s", message);
            });
        }
    }

    /**
     * Sends a file message to the channel.
     *
     * @param params Parameters to be applied to the message
     * @param fileInfo File information to send to the channel
     * @since 3.0.0
     */
    public void sendFileMessage(@NonNull FileMessageCreateParams params, @NonNull FileInfo fileInfo) {
        Logger.i("++ request send file message : %s", params);
        if (channel != null) {
            FileMessage pendingFileMessage = channel.sendFileMessage(params, (message, ee) -> {
                if (ee != null) {
                    Logger.e(ee);
                    return;
                }
                Logger.i("++ sent message : %s", message);
            });
            if (pendingFileMessage != null) {
                PendingMessageRepository.getInstance().addFileInfo(pendingFileMessage, fileInfo);
            }
        }

    }

    /**
     * Resends a message to the channel.
     *
     * @param message Message to resend
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void resendMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        if (message instanceof UserMessage) {
            channel.resendMessage((UserMessage) message, (message12, e) -> {
                if (handler != null) handler.onComplete(e);
                Logger.i("__ resent message : %s", message12);
            });
        } else if (message instanceof FileMessage) {
            FileInfo info = PendingMessageRepository.getInstance().getFileInfo(message);
            final File file = info == null ? null : info.getFile();
            channel.resendMessage((FileMessage) message, file, (message1, e1) -> {
                if (handler != null) handler.onComplete(e1);
                Logger.i("__ resent file message : %s", message1);
            });
        }
    }

    /**
     * Updates a text message with {@code messageId}.
     *
     * @param messageId ID of message to be updated
     * @param params Parameters to be applied to the message
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        channel.updateUserMessage(messageId, params, (message, e) -> {
            if (handler != null) handler.onComplete(e);
            Logger.i("++ updated message : %s", message);
        });
    }

    /**
     * Deletes a message.
     *
     * @param message Message to be deleted
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void deleteMessage(@NonNull BaseMessage message, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.SUCCEEDED) {
            channel.deleteMessage(message, e -> {
                if (handler != null) handler.onComplete(e);
                Logger.i("++ deleted message : %s", message);
            });
        }
    }

    /**
     * Adds the reaction with {@code key} if the current user doesn't add it, otherwise the reaction will be deleted
     *
     * @param view View displaying the reaction with {@code key}
     * @param message Message to which the reaction will be applieds
     * @param key Key of reaction
     * @param handler Callback handler called when this method is completed
     * @since 3.0.0
     */
    public void toggleReaction(@NonNull View view, @NonNull BaseMessage message, @NonNull String key, @Nullable OnCompleteHandler handler) {
        if (channel == null) return;
        if (!view.isSelected()) {
            Logger.i("__ add reaction : %s", key);
            channel.addReaction(message, key, (reactionEvent, e) -> {
                if (handler != null) {
                    Logger.e(e);
                    handler.onComplete(e);
                }
            });
        } else {
            Logger.i("__ delete reaction : %s", key);
            channel.deleteReaction(message, key, (reactionEvent, e) -> {
                if (handler != null) {
                    Logger.e(e);
                    handler.onComplete(e);
                }
            });
        }
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
                    this.channel = channel;
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
     * Loads the list of members whose nickname starts with startWithFilter.
     *
     * @param startWithFilter The filter to be used to load a list of members with nickname that starts with a specific text.
     * @since 3.0.0
     */
    public synchronized void loadMemberList(@Nullable String startWithFilter) {
        memberFinder.find(startWithFilter);
    }

    void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
        if (messages.isEmpty()) return;

        if (context.getMessagesSendingStatus() == SendingStatus.SUCCEEDED || context.getMessagesSendingStatus() == SendingStatus.NONE) {
            cachedMessages.addAll(messages);
            notifyDataSetChanged(context);
        } else if (context.getMessagesSendingStatus() == SendingStatus.PENDING) {
            notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_ADDED);
        }
    }

    void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
        if (messages.isEmpty()) return;

        if (context.getMessagesSendingStatus() == SendingStatus.SUCCEEDED) {
            // if the source was MESSAGE_SENT, we should remove the message from the pending message datasource.
            if (context.getCollectionEventSource() == CollectionEventSource.EVENT_MESSAGE_SENT) {
                PendingMessageRepository.getInstance().clearAllFileInfo(messages);
                cachedMessages.addAll(messages);
            } else {
                cachedMessages.updateAll(messages);
            }
            notifyDataSetChanged(context);
        } else if (context.getMessagesSendingStatus() == SendingStatus.PENDING) {
            notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_ADDED);
        } else if (context.getMessagesSendingStatus() == SendingStatus.FAILED) {
            notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_ADDED);
        } else if (context.getMessagesSendingStatus() == SendingStatus.CANCELED) {
            notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_ADDED);
        }
    }

    void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel groupChannel, @NonNull List<BaseMessage> messages) {
        if (messages.isEmpty()) return;

        if (context.getMessagesSendingStatus() == SendingStatus.SUCCEEDED) {
            // Remove the succeeded message from the succeeded message datasource.
            cachedMessages.deleteAll(messages);
            notifyDataSetChanged(context);
        } else if (context.getMessagesSendingStatus() == SendingStatus.PENDING) {
            // Remove the pending message from the pending message datasource.
            notifyDataSetChanged(StringSet.ACTION_PENDING_MESSAGE_REMOVED);
        } else if (context.getMessagesSendingStatus() == SendingStatus.FAILED) {
            // Remove the failed message from the pending message datasource.
            notifyDataSetChanged(StringSet.ACTION_FAILED_MESSAGE_REMOVED);
        }
    }

    @UiThread
    synchronized void notifyDataSetChanged(@NonNull Traceable trace) {
        notifyDataSetChanged(trace.getTraceName());
    }

    @UiThread
    synchronized void notifyDataSetChanged(@NonNull String traceName) {}
}

