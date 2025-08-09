package com.sendbird.uikit.fragments;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Feedback;
import com.sendbird.android.message.FeedbackRating;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MessageForm;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.params.MessageListParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.user.MutedState;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.activities.MessageThreadActivity;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.activities.adapter.SuggestedMentionListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.consts.ThreadReplySelectType;
import com.sendbird.uikit.consts.TypingIndicatorType;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.MessageDisplayDataProvider;
import com.sendbird.uikit.interfaces.OnConsumableClickListener;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnInputModeChangedListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
import com.sendbird.uikit.internal.model.VoicePlayerManager;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.Action;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit.modules.ChannelModule;
import com.sendbird.uikit.modules.components.ChannelHeaderComponent;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.modules.components.MessageListComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.providers.ModuleProviders;
import com.sendbird.uikit.providers.ViewModelProviders;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.ChannelViewModel;
import com.sendbird.uikit.vm.FileDownloader;
import com.sendbird.uikit.widgets.MentionEditText;
import com.sendbird.uikit.widgets.MessageInputView;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that provides chat in {@code GroupChannel}
 */
public class ChannelFragment extends BaseMessageListFragment<MessageListAdapter, MessageListComponent, ChannelModule, ChannelViewModel> {
    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> quoteReplyMessageClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> threadInfoClickListener;
    @Nullable
    private View.OnClickListener replyModeCloseButtonClickListener;
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nullable
    @Deprecated
    private View.OnClickListener scrollBottomButtonClickListener;
    @Nullable
    private OnConsumableClickListener scrollFirstButtonClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnInputTextChangedListener editModeTextChangedListener;
    @Nullable
    private View.OnClickListener inputRightButtonClickListener;
    @Nullable
    private View.OnClickListener editModeCancelButtonClickListener;
    @Nullable
    private View.OnClickListener editModeSaveButtonClickListener;
    @Nullable
    private OnInputModeChangedListener inputModeChangedListener;
    @Nullable
    private View.OnClickListener tooltipClickListener;
    @Nullable
    private View.OnClickListener unreadCountTooltipClickListener;
    @Nullable
    private View.OnClickListener onVoiceRecorderButtonClickListener;
    @Nullable
    private MessageListParams params;
    @NonNull
    private final AtomicBoolean tryAnimateWhenMessageLoaded = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean anchorDialogShowing = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean isInitCallFinished = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean isThreadRedirected = new AtomicBoolean(false);

    @Nullable
    private OnMessageTemplateActionHandler messageTemplateActionHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null && args.containsKey(StringSet.KEY_ANCHOR_MESSAGE_ID)) {
            isThreadRedirected.set(true);
        }
    }

    @NonNull
    @Override
    protected ChannelModule onCreateModule(@NonNull Bundle args) {
        return ModuleProviders.getChannel().provide(requireContext(), args);
    }

    @NonNull
    @Override
    protected ChannelViewModel onCreateViewModel() {
        return ViewModelProviders.getChannel().provide(this, getChannelUrl(), params, channelConfig);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelModule module, @NonNull ChannelViewModel viewModel) {
        Logger.d(">> ChannelFragment::onBeforeReady()");
        super.onBeforeReady(status, module, viewModel);
        final GroupChannel channel = viewModel.getChannel();
        onBindChannelHeaderComponent(module.getHeaderComponent(), viewModel, channel);
        onBindMessageListComponent(module.getMessageListComponent(), viewModel, channel);
        onBindMessageInputComponent(module.getMessageInputComponent(), viewModel, channel);
        onBindStatusComponent(module.getStatusComponent(), viewModel, channel);
    }

    @Override
    protected void onReady(@NonNull ReadyStatus status, @NonNull ChannelModule module, @NonNull ChannelViewModel viewModel) {
        shouldDismissLoadingDialog();
        final GroupChannel channel = viewModel.getChannel();
        if (status == ReadyStatus.ERROR || channel == null || channel.isChatNotification()) {
            Logger.e("status: " + status + ", channel is null: " + (channel == null) + ", isChatNotification: " + (channel != null ? channel.isChatNotification() : "channel null"));
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }

        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getMessageListComponent().notifyChannelChanged(channel);
        module.getMessageInputComponent().notifyChannelChanged(channel);

        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), channelUrl -> shouldActivityFinish());
        final MessageListComponent messageListComponent = module.getMessageListComponent();
        final long startingPoint = messageListComponent.getParams().getInitialStartingPoint();
        loadInitial(startingPoint);
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().setIsChatScreenVisible(true);
        getViewModel().markAsRead();
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewModel().setIsChatScreenVisible(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VoicePlayerManager.disposeAll();
        if (!isInitCallFinished.get()) {
            shouldDismissLoadingDialog();
        }

        ChannelViewModel.ChannelMessageData channelMessageData = getViewModel().getMessageList().getValue();
        if (channelMessageData != null) {
            MessageExtensionsKt.clearLastValidations(channelMessageData.getMessages());
        }

        MessageExtensionsKt.setNewLineMessageId(null);
    }

    /**
     * Called to bind events to the ChannelHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindChannelHeaderComponent(@NonNull ChannelHeaderComponent headerComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindChannelHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            if (channel == null) return;
            Intent intent = ChannelSettingsActivity.newIntent(requireContext(), channel.getUrl());
            startActivity(intent);
        });

        if (channelConfig.getEnableTypingIndicator() && channelConfig.getTypingIndicatorTypes().contains(TypingIndicatorType.TEXT)) {
            viewModel.getTypingMembers().observe(getViewLifecycleOwner(), typingMembers -> {
                String description = null;
                if (typingMembers != null && getContext() != null) {
                    description = ChannelUtils.makeTypingText(getContext(), typingMembers);
                }
                headerComponent.notifyHeaderDescriptionChanged(description);
            });
        }
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), groupChannel -> {
            if (groupChannel != null) {
                headerComponent.notifyChannelChanged(groupChannel);
            }
        });
    }

    /**
     * Called to bind events to the MessageListComponent and also bind ChannelViewModel.
     * This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param messageListComponent The component to which the event will be bound
     * @param viewModel            A view model that provides the data needed for the fragment
     * @param channel              The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMessageListComponent(@NonNull MessageListComponent messageListComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindMessageListComponent()");
        if (channel == null) return;
        messageListComponent.setOnMessageClickListener(this::onMessageClicked);
        messageListComponent.setOnMessageProfileLongClickListener(this::onMessageProfileLongClicked);
        messageListComponent.setOnMessageProfileClickListener(this::onMessageProfileClicked);
        messageListComponent.setOnMessageLongClickListener(this::onMessageLongClicked);
        messageListComponent.setOnMessageMentionClickListener(this::onMessageMentionClicked);
        messageListComponent.setOnEmojiReactionClickListener(emojiReactionClickListener != null ? emojiReactionClickListener : (view, position, message, reactionKey) -> toggleReaction(view, message, reactionKey));
        messageListComponent.setOnEmojiReactionLongClickListener(emojiReactionLongClickListener != null ? emojiReactionLongClickListener : (view, position, message, reactionKey) -> showEmojiReactionDialog(message, position));
        messageListComponent.setOnEmojiReactionMoreButtonClickListener(emojiReactionMoreButtonClickListener != null ? emojiReactionMoreButtonClickListener : (view, position, message) -> showEmojiListDialog(message));
        messageListComponent.setSuggestedRepliesClickListener((view, position, data) -> onSuggestedRepliesClicked(data));
        messageListComponent.setFormSubmitButtonClickListener(this::onFormSubmitButtonClicked);
        messageListComponent.setOnFeedbackRatingClickListener(this::onFeedbackRatingClicked);
        messageListComponent.setOnTooltipClickListener(tooltipClickListener != null ? tooltipClickListener : this::onMessageTooltipClicked);
        messageListComponent.setOnUnreadTooltipClickListener(unreadCountTooltipClickListener != null ? unreadCountTooltipClickListener : this::onUnreadCountTooltipClicked);

        messageListComponent.setOnQuoteReplyMessageLongClickListener(this::onQuoteReplyMessageLongClicked);
        messageListComponent.setOnQuoteReplyMessageClickListener(this::onQuoteReplyMessageClicked);
        messageListComponent.setOnThreadInfoClickListener(this::onThreadInfoClicked);
        messageListComponent.setOnScrollBottomButtonClickListener(scrollBottomButtonClickListener);
        messageListComponent.setOnScrollFirstButtonClickListener(scrollFirstButtonClickListener != null ? scrollFirstButtonClickListener : view -> {
            if (viewModel.hasNext()) {
                loadInitial(Long.MAX_VALUE);
                return true;
            }
            return false;
        });
        messageListComponent.setMessageTemplateActionHandler(this.messageTemplateActionHandler != null ? this.messageTemplateActionHandler : this::handleTemplateMessageAction);

        if (messageListComponent.getRecyclerView() != null) {
            messageListComponent.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        boolean isScrollAtBottom = layoutManager.findFirstVisibleItemPosition() == 0;
                        viewModel.onScrollChanged(isScrollAtBottom);
                    }

                    if (UIKitConfig.getGroupChannelConfig().getEnableMarkAsUnread()) {
                        boolean isNewLineVisible = messageListComponent.updateTooltipByNewLine(channel);
                        if (isNewLineVisible) viewModel.onNewLineSeen();
                    }
                }
            });
        }

        final ChannelModule module = getModule();
        viewModel.getMessageList().observeAlways(getViewLifecycleOwner(), receivedMessageData -> {
            boolean isInitialCallFinished = isInitCallFinished.getAndSet(true);
            if (!isInitialCallFinished && isFragmentAlive()) {
                shouldDismissLoadingDialog();
            }
            if (isThreadRedirected.get() && isFragmentAlive()) {
                redirectMessageThreadIfNeeded(getArguments());
            }

            final List<BaseMessage> messageList = receivedMessageData.getMessages();
            Logger.d("++ result messageList size : %s, source = %s", messageList.size(), receivedMessageData.getTraceName());

            final String eventSource = receivedMessageData.getTraceName();
            // The callback coming from setItems is worked asynchronously. So `isInitCallFinished` flag has to mark in advance.
            messageListComponent.notifyDataSetChanged(messageList, channel, messages -> {
                if (!isFragmentAlive()) return;

                if (eventSource != null) {
                    Logger.d("++ ChannelFragment Message action : %s", eventSource);
                    final RecyclerView recyclerView = messageListComponent.getRecyclerView();

                    final MessageListAdapter adapter = messageListComponent.getAdapter();
                    if (recyclerView == null || adapter == null) return;

                    final Context context = recyclerView.getContext();
                    switch (eventSource) {
                        case StringSet.ACTION_FAILED_MESSAGE_ADDED:
                        case StringSet.ACTION_PENDING_MESSAGE_ADDED:
                            module.getMessageInputComponent().requestInputMode(MessageInputView.Mode.DEFAULT);
                            scrollToFirst();
                            break;
                        case StringSet.EVENT_MESSAGE_RECEIVED:
                        case StringSet.EVENT_MESSAGE_SENT:
                            messageListComponent.notifyOtherMessageReceived(anchorDialogShowing.get());
                            if (eventSource.equals(StringSet.EVENT_MESSAGE_SENT)) {
                                final MessageListParams messageListParams = viewModel.getMessageListParams();
                                final BaseMessage latestMessage = adapter.getItem(messageListParams != null && messageListParams.getReverse() ? 0 : adapter.getItemCount() - 1);
                                if (latestMessage instanceof FileMessage) {
                                    // Download from files already sent for quick image loading.
                                    FileDownloader.downloadThumbnail(context, (FileMessage) latestMessage);
                                }
                            }
                            break;
                        case StringSet.MESSAGE_CHANGELOG:
                        case StringSet.MESSAGE_FILL:
                            messageListComponent.notifyMessagesFilled(!anchorDialogShowing.get());
                            break;
                        case StringSet.EVENT_TYPING_STATUS_UPDATED:
                            messageListComponent.notifyTypingIndicatorUpdated(!anchorDialogShowing.get());
                            break;
                        case StringSet.ACTION_INIT_FROM_REMOTE:
                            refreshNewLineVisibility();
                            messageListComponent.notifyMessagesFilled(!anchorDialogShowing.get());
                            break;
                        case StringSet.EVENT_USER_MARKED_UNREAD:
                            refreshNewLineVisibility();
                            break;
                    }
                }
                if (!isInitialCallFinished) {
                    BaseMessage willAnimateMessage = null;
                    if (tryAnimateWhenMessageLoaded.getAndSet(false)) {
                        final List<BaseMessage> founded = viewModel.getMessagesByCreatedAt(viewModel.getStartingPoint());
                        Logger.i("++ founded=%s, startingPoint=%s", founded, viewModel.getStartingPoint());
                        if (founded.size() == 1) {
                            willAnimateMessage = founded.get(0);
                        } else {
                            toastError(R.string.sb_text_error_original_message_not_found);
                        }
                    }
                    messageListComponent.moveToFocusedMessage(viewModel.getStartingPoint(), willAnimateMessage);
                }
            });
        });

        viewModel.getHugeGapDetected().observe(getViewLifecycleOwner(), detected -> {
            Logger.d(">> onHugeGapDetected()");
            final long currentStartingPoint = viewModel.getStartingPoint();
            if (currentStartingPoint == 0 || currentStartingPoint == Long.MAX_VALUE) {
                loadInitial(currentStartingPoint);
            } else {
                final RecyclerView recyclerView = messageListComponent.getRecyclerView();
                if (recyclerView != null) {
                    if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        int position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        MessageListAdapter adapter = messageListComponent.getAdapter();
                        if (position >= 0 && adapter != null) {
                            final BaseMessage message = adapter.getItem(position);
                            Logger.d("++ founded first visible message = %s", message);
                            loadInitial(message.getCreatedAt());
                        }
                    }
                }
            }
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), groupChannel -> {
            if (UIKitConfig.getGroupChannelConfig().getEnableMarkAsUnread()) {
                messageListComponent.updateTooltipByNewLine(groupChannel);
            }

            messageListComponent.notifyChannelChanged(groupChannel);
        });
        viewModel.onMessagesDeleted().observe(getViewLifecycleOwner(), deletedMessages -> {
            for (final BaseMessage deletedMessage : deletedMessages) {
                if (deletedMessage instanceof FileMessage && MessageUtils.isVoiceMessage((FileMessage) deletedMessage)) {
                    final String key = MessageUtils.getVoiceMessageKey((FileMessage) deletedMessage);
                    if (key.equals(VoicePlayerManager.getCurrentKey())) {
                        VoicePlayerManager.pause();
                    }
                }
            }
        });

        viewModel.onFeedbackSubmitted().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            final BaseMessage message = result.first;
            final SendbirdException e = result.second;
            if (e == null) {
                if (message != null) {
                    showUpdateFeedbackCommentDialog(message);
                }
            } else {
                toastError(R.string.sb_text_toast_failure_feedback_submit);
            }
        });

        viewModel.onFeedbackUpdated().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            final SendbirdException e = result.second;

            if (e == null) {
                toastSuccess(R.string.sb_text_toast_success_feedback_update);
            } else {
                toastError(R.string.sb_text_toast_failure_feedback_update);
            }
        });

        viewModel.onFeedbackDeleted().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            final SendbirdException e = result.second;
            if (e != null) {
                toastError(R.string.sb_text_toast_failure_feedback_delete);
            }
        });

        viewModel.onNewLineUpdated().observe(getViewLifecycleOwner(), newLineData -> {
            MessageListAdapter adapter = messageListComponent.getAdapter();
            if (adapter == null) return;

            Integer prevPosition = newLineData.getPrevPosition();
            if (prevPosition != null) {
                adapter.notifyItemChanged(prevPosition);
            }

            Integer currentPosition = newLineData.getCurrentPosition();
            if (currentPosition != null) {
                adapter.notifyItemChanged(currentPosition);
            }
        });
    }

    /**
     * Called to bind events to the MessageInputComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param inputComponent The component to which the event will be bound
     * @param viewModel      A view model that provides the data needed for the fragment
     * @param channel        The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindMessageInputComponent(@NonNull MessageInputComponent inputComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindMessageInputComponent()");
        if (channel == null) return;
        inputComponent.setOnInputLeftButtonClickListener(inputLeftButtonClickListener != null ? inputLeftButtonClickListener : v -> showMediaSelectDialog());
        inputComponent.setOnInputRightButtonClickListener(inputRightButtonClickListener != null ? inputRightButtonClickListener : this::onInputRightButtonClicked);
        inputComponent.setOnEditModeSaveButtonClickListener(editModeSaveButtonClickListener != null ? editModeSaveButtonClickListener : v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                if (null != targetMessage) {
                    UserMessageUpdateParams params = new UserMessageUpdateParams(inputText.getText().toString());
                    if (inputText instanceof MentionEditText) {
                        final List<User> mentionedUsers = ((MentionEditText) inputText).getMentionedUsers();
                        final CharSequence mentionedTemplate = ((MentionEditText) inputText).getMentionedTemplate();
                        Logger.d("++ mentioned template text=%s", mentionedTemplate);
                        params.setMentionedMessageTemplate(mentionedTemplate.toString());
                        params.setMentionedUsers(mentionedUsers);
                    }
                    updateUserMessage(targetMessage.getMessageId(), params);
                } else {
                    Logger.d("Target message for update is missing");
                }
            }
            inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
        });

        inputComponent.setOnEditModeTextChangedListener(editModeTextChangedListener != null ? editModeTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnEditModeCancelButtonClickListener(editModeCancelButtonClickListener != null ? editModeCancelButtonClickListener : v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnInputTextChangedListener(inputTextChangedListener != null ? inputTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnInputModeChangedListener(inputModeChangedListener != null ? inputModeChangedListener : this::onInputModeChanged);
        inputComponent.setOnQuoteReplyModeCloseButtonClickListener(replyModeCloseButtonClickListener != null ? replyModeCloseButtonClickListener : v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnVoiceRecorderButtonClickListener((onVoiceRecorderButtonClickListener != null) ? onVoiceRecorderButtonClickListener : v -> takeVoiceRecorder());

        if (channelConfig.getEnableMention()) {
            inputComponent.bindUserMention(SendbirdUIKit.getUserMentionConfig(), text -> viewModel.loadMemberList(text != null ? text.toString() : null));

            // observe suggestion list
            viewModel.getMentionSuggestion().observe(getViewLifecycleOwner(), suggestion -> inputComponent.notifySuggestedMentionDataChanged(suggestion.getSuggestionList()));
        }

        viewModel.onMessagesDeleted().observe(getViewLifecycleOwner(), deletedMessages -> {
            if (targetMessage != null && deletedMessages.contains(targetMessage)) {
                targetMessage = null;
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });

        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), openChannel -> {
            inputComponent.notifyChannelChanged(openChannel);
            boolean isOperator = channel.getMyRole() == Role.OPERATOR;
            boolean isMuted = channel.getMyMutedState() == MutedState.MUTED;
            boolean isFrozen = channel.isFrozen() && !isOperator;
            if (isMuted || isFrozen) {
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)}  regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindStatusComponent()");
        statusComponent.setOnActionButtonClickListener(v -> {
            statusComponent.notifyStatusChanged(StatusFrameView.Status.LOADING);
            shouldAuthenticate();
        });
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    /**
     * Called when the quoted message of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageClickListener != null) {
            quoteReplyMessageClickListener.onItemClick(view, position, message);
            return;
        }
        if (channelConfig.getReplyType() == ReplyType.THREAD &&
            channelConfig.getThreadReplySelectType() == ThreadReplySelectType.THREAD) {
            startMessageThreadActivity(message);
        } else {
            jumpToParentMessage(message);
        }
    }

    /**
     * Called when the quoted message of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onQuoteReplyMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageLongClickListener != null)
            quoteReplyMessageLongClickListener.onItemLongClick(view, position, message);
    }

    /**
     * Called when the thread info of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.3.0
     */
    protected void onThreadInfoClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (threadInfoClickListener != null) {
            threadInfoClickListener.onItemClick(view, position, message);
            return;
        }
        startMessageThreadActivity(message);
    }

    /**
     * Called when the a suggested replies view is clicked
     *
     * @param suggestedReply Clicked suggested reply data.
     * since 3.10.0
     */
    protected void onSuggestedRepliesClicked(@NonNull String suggestedReply) {
        UserMessageCreateParams params = new UserMessageCreateParams(suggestedReply);
        sendUserMessage(params);
    }

    /**
     * Called when the Form submit button is clicked.
     *
     * @param message The message that contains the form
     * @param form The form to be submitted
     * since 3.12.1
     */
    protected void onFormSubmitButtonClicked(@NonNull BaseMessage message, @NonNull MessageForm form) {
        message.submitMessageForm((e) -> {
            if (e != null) {
                showConfirmDialog(getString(R.string.sb_forms_submit_failed));
            }
        });
    }

    /**
     * Called when the feedback rating of the message is clicked.
     *
     * @param message The message that contains feedback
     * @param feedbackRating The clicked feedback rating
     * since 3.13.0
     */
    protected void onFeedbackRatingClicked(@NonNull BaseMessage message, @NonNull FeedbackRating feedbackRating) {
        Feedback currentFeedback = message.getMyFeedback();
        if (currentFeedback != null) {
            DialogListItem[] dialogListItems = {
                new DialogListItem(R.string.sb_text_feedback_edit_comment),
                new DialogListItem(R.string.sb_text_feedback_remove_comment, 0, true)
            };

            DialogUtils.showListBottomDialog(
                requireContext(),
                dialogListItems,
                (view, position, data) -> {
                    if (position == 0) {
                        showUpdateFeedbackCommentDialog(message);
                    } else if (position == 1) {
                        getViewModel().removeFeedback(message);
                    }
                }
            );
        } else {
            getViewModel().submitFeedback(message, feedbackRating, null);
        }
    }

    /**
     * Find the same message as the message ID and move it to the matching message.
     *
     * @param messageId the message id to move
     * @param withAnimation {@code true} animate the message after focusing on it
     * @return {@code true} if there is a message to move, {@code false} otherwise
     * since 3.7.0
     */
    public boolean moveToMessage(long messageId, boolean withAnimation) {
        Logger.d(">> ChannelFragment::moveToMessage(%s), withAnimation=%s", messageId, withAnimation);
        final MessageListComponent messageListComponent = getModule().getMessageListComponent();
        final BaseMessage message = getViewModel().getMessageById(messageId);
        if (message != null) {
            final BaseMessage animateMessage = withAnimation ? message : null;
            messageListComponent.moveToFocusedMessage(message.getCreatedAt(), animateMessage);
            Logger.d("-- jumpToMessage return (true)");
            return true;
        }
        Logger.d("-- return (couldn't find the message)");
        return false;
    }

    private void refreshNewLineVisibility() {
        if (!UIKitConfig.getGroupChannelConfig().getEnableMarkAsUnread()) return;

        final MessageListComponent messageListComponent = getModule().getMessageListComponent();
        final GroupChannel channel = getViewModel().getChannel();

        getViewModel().updateNewLine();

        boolean isNewLineVisible = messageListComponent.updateTooltipByNewLine(channel);
        if (isNewLineVisible) {
            getViewModel().onNewLineSeen();
        }
    }

    private void onMessageTooltipClicked(@NonNull View view) {
        scrollToFirst();
    }

    private void onUnreadCountTooltipClicked(@NonNull View view) {
        getViewModel().markAsRead();
    }

    private void onInputRightButtonClicked(@NonNull View view) {
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        final EditText inputText = inputComponent.getEditTextView();
        if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
            final Editable editableText = inputText.getText();
            UserMessageCreateParams params = new UserMessageCreateParams(editableText.toString());
            if (targetMessage != null && channelConfig.getReplyType() != ReplyType.NONE) {
                params.setParentMessageId(targetMessage.getMessageId());
                params.setReplyToChannel(true);
            }
            if (channelConfig.getEnableMention()) {
                if (inputText instanceof MentionEditText) {
                    final List<User> mentionedUsers = ((MentionEditText) inputText).getMentionedUsers();
                    final CharSequence mentionedTemplate = ((MentionEditText) inputText).getMentionedTemplate();
                    Logger.d("++ mentioned template text=%s", mentionedTemplate);
                    params.setMentionedMessageTemplate(mentionedTemplate.toString());
                    params.setMentionedUsers(mentionedUsers);
                }
            }

            sendUserMessage(params);
        }
    }

    private void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current) {
        final GroupChannel channel = getViewModel().getChannel();
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        if (channel == null) return;

        switch (current) {
            case QUOTE_REPLY:
            case EDIT:
                inputComponent.notifyDataChanged(targetMessage, channel);
                break;
            default:
                if (before == MessageInputView.Mode.QUOTE_REPLY && targetMessage == null) {
                    final EditText input = inputComponent.getEditTextView();
                    final String defaultText = input != null && !TextUtils.isEmpty(input.getText())
                        ? inputComponent.getEditTextView().getText().toString() : "";
                    inputComponent.notifyDataChanged(null, channel, defaultText);
                } else {
                    inputComponent.notifyDataChanged(null, channel);
                }
                targetMessage = null;
        }
    }

    @NonNull
    @Override
    protected List<DialogListItem> makeMessageContextMenu(@NonNull BaseMessage message) {
        final List<DialogListItem> items = new ArrayList<>();
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return items;

        MessageType type = MessageViewHolderFactory.getMessageType(message);
        DialogListItem copy = new DialogListItem(R.string.sb_text_channel_anchor_copy, R.drawable.icon_copy);
        DialogListItem edit = new DialogListItem(R.string.sb_text_channel_anchor_edit, R.drawable.icon_edit);
        DialogListItem save = new DialogListItem(R.string.sb_text_channel_anchor_save, R.drawable.icon_download);
        DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete, false, MessageUtils.hasThread(message));
        int replyStringRes = channelConfig.getReplyType() == ReplyType.THREAD ? R.string.sb_text_channel_anchor_reply_in_thread : R.string.sb_text_channel_anchor_reply;
        int replyDrawableRes = channelConfig.getReplyType() == ReplyType.THREAD ? R.drawable.icon_thread : R.drawable.icon_reply;
        DialogListItem reply = new DialogListItem(replyStringRes, replyDrawableRes, false, MessageUtils.hasParentMessage(message));
        DialogListItem retry = new DialogListItem(R.string.sb_text_channel_anchor_retry, 0);
        DialogListItem deleteFailed = new DialogListItem(R.string.sb_text_channel_anchor_delete, 0);
        DialogListItem markAsUnread = UIKitConfig.getGroupChannelConfig().getEnableMarkAsUnread()
            ? new DialogListItem(R.string.sb_text_channel_anchor_mark_as_unread, R.drawable.icon_mark_as_unread)
            : null;

        DialogListItem[] actions = null;
        final ReplyType replyType = channelConfig.getReplyType();
        switch (type) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                if (status == SendingStatus.SUCCEEDED) {
                    if (replyType == ReplyType.NONE) {
                        actions = new DialogListItem[]{copy, edit, markAsUnread};
                    } else {
                        actions = new DialogListItem[]{copy, edit, markAsUnread, reply};
                    }
                } else if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                }
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                if (replyType == ReplyType.NONE) {
                    actions = new DialogListItem[]{copy, markAsUnread};
                } else {
                    actions = new DialogListItem[]{copy, markAsUnread, reply};
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    if (replyType == ReplyType.NONE) {
                        actions = new DialogListItem[]{save, markAsUnread, delete};
                    } else {
                        actions = new DialogListItem[]{save, markAsUnread, delete, reply};
                    }
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                if (replyType == ReplyType.NONE) {
                    actions = new DialogListItem[]{save, markAsUnread};
                } else {
                    actions = new DialogListItem[]{save, markAsUnread, reply};
                }
                break;
            case VIEW_TYPE_MULTIPLE_FILES_MESSAGE_ME:
            case VIEW_TYPE_VOICE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    if (replyType == ReplyType.NONE) {
                        actions = new DialogListItem[]{markAsUnread, delete};
                    } else {
                        actions = new DialogListItem[]{markAsUnread, delete, reply};
                    }
                }
                break;
            case VIEW_TYPE_MULTIPLE_FILES_MESSAGE_OTHER:
            case VIEW_TYPE_VOICE_MESSAGE_OTHER:
                if (replyType != ReplyType.NONE) {
                    actions = new DialogListItem[]{markAsUnread, reply};
                } else {
                    actions = new DialogListItem[]{markAsUnread};
                }
                break;
            case VIEW_TYPE_UNKNOWN_MESSAGE_ME:
                actions = new DialogListItem[]{markAsUnread, delete};
            default:
                break;
        }

        if (actions != null) {
            for (DialogListItem action : actions) {
                if (action != null) {
                    items.add(action);
                }
            }
        }

        return items;
    }

    @Override
    protected boolean onMessageContextMenuItemClicked(@NonNull BaseMessage message, @NonNull View view, int position, @NonNull DialogListItem item) {
        final MessageInputComponent inputComponent = getModule().getMessageInputComponent();
        int key = item.getKey();
        if (key == R.string.sb_text_channel_anchor_copy) {
            copyTextToClipboard(message.getMessage());
            return true;
        } else if (key == R.string.sb_text_channel_anchor_edit) {
            targetMessage = message;
            inputComponent.requestInputMode(MessageInputView.Mode.EDIT);
            return true;
        } else if (key == R.string.sb_text_channel_anchor_delete) {
            if (MessageUtils.isFailed(message)) {
                Logger.dev("delete");
                deleteMessage(message);
            } else {
                showWarningDialog(message);
            }
            return true;
        } else if (key == R.string.sb_text_channel_anchor_save) {
            if (message instanceof FileMessage) {
                saveFileMessage((FileMessage) message);
            }
            return true;
        } else if (key == R.string.sb_text_channel_anchor_reply) {
            this.targetMessage = message;
            inputComponent.requestInputMode(MessageInputView.Mode.QUOTE_REPLY);
            return true;
        } else if (key == R.string.sb_text_channel_anchor_reply_in_thread) {
            startMessageThreadActivity(message);
            return true;
        } else if (key == R.string.sb_text_channel_anchor_retry) {
            resendMessage(message);
            return true;
        } else if (key == R.string.sb_text_channel_anchor_mark_as_unread) {
            getViewModel().markAsUnread(message, true);
            return true;
        }
        return false;
    }

    @Override
    void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {
        int size = items.size();
        final DialogListItem[] actions = items.toArray(new DialogListItem[size]);
        if (getViewModel().getChannel() != null && !(ChannelConfig.canSendReactions(channelConfig, getViewModel().getChannel()))) {
            final RecyclerView messageListView = getModule().getMessageListComponent().getRecyclerView();
            if (getContext() == null || messageListView == null || size == 0) return;
            MessageAnchorDialog messageAnchorDialog = new MessageAnchorDialog.Builder(anchorView, messageListView, actions)
                .setOnItemClickListener(createMessageActionListener(message))
                .setOnDismissListener(() -> anchorDialogShowing.set(false))
                .build();
            messageAnchorDialog.show();
            anchorDialogShowing.set(true);
        } else if (MessageUtils.isUnknownType(message) || !MessageUtils.isSucceed(message)) {
            if (getContext() == null || size == 0) return;
            hideKeyboard();
            DialogUtils.showListBottomDialog(requireContext(), actions, createMessageActionListener(message));
        } else {
            showEmojiActionsDialog(message, actions);
        }
    }

    private synchronized void loadInitial(long startingPoint) {
        isInitCallFinished.set(false);
        getViewModel().loadInitial(startingPoint);
    }

    private void scrollToFirst() {
        final MessageListComponent messageListComponent = getModule().getMessageListComponent();
        if (getViewModel().hasNext()) {
            loadInitial(Long.MAX_VALUE);
        } else {
            messageListComponent.scrollToFirst();
        }
    }

    private void jumpToParentMessage(@NonNull BaseMessage message) {
        // ClickableViewType.Reply

        final BaseMessage parentMessage = message.getParentMessage();
        final long parentMessageCreatedAt = parentMessage == null ? 0L : parentMessage.getCreatedAt();
        if (parentMessageCreatedAt > 0) {
            final MessageListComponent messageListComponent = getModule().getMessageListComponent();
            if (getViewModel().hasMessageById(message.getParentMessageId())) {
                messageListComponent.moveToFocusedMessage(parentMessageCreatedAt, parentMessage);
            } else {
                tryAnimateWhenMessageLoaded.set(true);
                loadInitial(parentMessageCreatedAt);
            }
        } else {
            toastError(R.string.sb_text_error_original_message_not_found);
        }
    }

    private void startMessageThreadActivity(@NonNull BaseMessage message) {
        BaseMessage parentMessage;
        long startingPoint = 0;
        if (MessageUtils.hasParentMessage(message) && message.getParentMessage() != null) {
            parentMessage = getViewModel().getMessageById(message.getParentMessageId());
            startingPoint = message.getCreatedAt();
            if (parentMessage == null) {
                parentMessage = message.getParentMessage();
            }
        } else {
            parentMessage = message;
        }

        final GroupChannel channel = getViewModel().getChannel();
        if (channel != null && parentMessage.getCreatedAt() < channel.getMessageOffsetTimestamp()) {
            toastError(R.string.sb_text_error_original_message_not_found);
        } else {
            startActivity(new MessageThreadActivity
                .IntentBuilder(requireContext(), getChannelUrl(), parentMessage)
                .setStartingPoint(startingPoint)
                .build(), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        }
    }

    private void redirectMessageThreadIfNeeded(@Nullable Bundle args) {
        if (args == null || channelConfig.getReplyType() != ReplyType.THREAD) return;

        if (args.containsKey(StringSet.KEY_ANCHOR_MESSAGE_ID)) {
            long messageId = args.getLong(StringSet.KEY_ANCHOR_MESSAGE_ID);
            final BaseMessage anchorMessage = getViewModel().getMessageById(messageId);
            if (anchorMessage != null && MessageUtils.hasParentMessage(anchorMessage)) {
                Logger.i(">> ChannelFragment::redirectMessageThreadIfNeeded(), startMessageThreadActivity()");
                isThreadRedirected.set(false);
                args.remove(StringSet.KEY_ANCHOR_MESSAGE_ID);
                startMessageThreadActivity(anchorMessage);
            }
        }
    }

    private void showUpdateFeedbackCommentDialog(@NonNull BaseMessage message) {
        final boolean hasFeedbackComment = message.getMyFeedback() != null && message.getMyFeedback().getComment() != null;
        final String positiveButtonText = hasFeedbackComment ? getString(R.string.sb_text_button_save) : getString(R.string.sb_text_button_submit);
        final OnEditTextResultListener listener = text -> {
            final Feedback feedback = message.getMyFeedback();
            if (feedback == null) return;
            getViewModel().submitFeedback(message, feedback.getRating(), text);
        };

        final DialogEditTextParams params = new DialogEditTextParams(getString(R.string.sb_text_feedback_comment_hint));
        final Feedback currentFeedback = message.getMyFeedback();
        if (currentFeedback != null) {
            params.setText(currentFeedback.getComment());
        }
        params.setEnableSingleLine(true);
        DialogUtils.showInputDialog(
            requireContext(),
            getString(R.string.sb_text_feedback_comment_title),
            params,
            listener,
            positiveButtonText,
            null,
            getString(R.string.sb_text_button_cancel),
            null
        );
    }

    private void handleTemplateMessageAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        switch (action.type) {
            case StringSet.web:
                handleWebAction(view, action, message);
                break;
            case StringSet.custom:
                handleCustomAction(view, action, message);
                break;
            default:
                break;
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * since 3.16.0
     */
    protected void handleWebAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> ChannelFragment::handleWebAction() action=%s", action);
        final Intent intent = IntentUtils.getWebViewerIntent(action.data);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Logger.e(e);
        }
    }

    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param action the registered Action data
     * @param message a clicked message
     * since 3.16.0
     */
    protected void handleCustomAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message) {
        Logger.d(">> ChannelFragment::handleCustomAction() action=%s", action);
        try {
            final String data = action.data;
            if (TextUtils.isNotEmpty(data)) {
                final Uri uri = Uri.parse(data);
                Logger.d("++ uri = %s", uri);
                final String scheme = uri.getScheme();
                Logger.d("++ scheme=%s", scheme);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                boolean hasIntent = IntentUtils.hasIntent(requireContext(), intent);
                if (!hasIntent) {
                    final String alterData = action.alterData;
                    if (alterData != null) {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alterData));
                    }
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            Logger.w(e);
        }
    }

    @SuppressWarnings("unused")
    public static class Builder {
        @NonNull
        private final Bundle bundle;
        @Nullable
        private MessageListAdapter adapter;
        @Nullable
        private View.OnClickListener headerLeftButtonClickListener;
        @Nullable
        private View.OnClickListener headerRightButtonClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> messageClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> messageProfileClickListener;
        @Nullable
        private OnItemClickListener<User> emojiReactionUserListProfileClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> quoteReplyMessageClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageLongClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
        @Nullable
        private OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener;
        @Nullable
        private View.OnClickListener inputLeftButtonListener;
        @Nullable
        private OnEmojiReactionClickListener emojiReactionClickListener;
        @Nullable
        private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
        @Nullable
        private MessageListParams params;
        @Nullable
        private LoadingDialogHandler loadingDialogHandler;
        @Nullable
        private OnInputTextChangedListener inputTextChangedListener;
        @Nullable
        private OnInputTextChangedListener editModeTextChangedListener;
        @Nullable
        private SuggestedMentionListAdapter suggestedMentionListAdapter;
        @Nullable
        private View.OnClickListener inputRightButtonClickListener;
        @Nullable
        private View.OnClickListener editModeCancelButtonClickListener;
        @Nullable
        private View.OnClickListener editModeSaveButtonClickListener;
        @Nullable
        private View.OnClickListener replyModeCloseButtonClickListener;
        @Nullable
        private OnInputModeChangedListener inputModeChangedListener;
        @Nullable
        private View.OnClickListener tooltipClickListener;
        @Nullable
        private View.OnClickListener unreadCountTooltipClickListener;
        @Nullable
        @Deprecated
        private View.OnClickListener scrollBottomButtonClickListener;
        @Nullable
        private OnConsumableClickListener scrollFirstButtonClickListener;
        @Nullable
        private OnItemClickListener<BaseMessage> threadInfoClickListener;
        @Nullable
        private View.OnClickListener voiceRecorderButtonClickListener;
        @Nullable
        private OnItemClickListener<User> messageMentionClickListener;

        @Nullable
        private OnMessageTemplateActionHandler messageTemplateActionHandler;

        @Nullable
        private ChannelFragment customFragment;


        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, 0);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendbirdUIKit.ThemeMode}
         */
        public Builder(@NonNull String channelUrl, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            this(channelUrl, themeMode.getResId());
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme. The theme resource id must be `sb_module_channel`.
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            this.bundle = new Bundle();
            if (customThemeResId != 0) {
                this.bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            }
            this.bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom fragment. It must inherit {@link ChannelFragment}.
         *
         * @param fragment custom fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.0
         */
        @NonNull
        public <T extends ChannelFragment> Builder setCustomFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder withArguments(@NonNull Bundle args) {
            this.bundle.putAll(args);
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeader(boolean useHeader) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER, useHeader);
            return this;
        }

        /**
         * Sets whether the right button of the header is used.
         *
         * @param useHeaderRightButton <code>true</code> if the right button of the header is used,
         *                             <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeaderRightButton(boolean useHeaderRightButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useHeaderRightButton);
            return this;
        }

        /**
         * Sets whether the left button of the header is used.
         *
         * @param useHeaderLeftButton <code>true</code> if the left button of the header is used,
         *                            <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets whether the typing indicator is used.
         *
         * @param useTypingIndicator <code>true</code> if the typing indicator is used,
         *                           <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @deprecated 3.6.0
         * <p> Use {@link #setChannelConfig(ChannelConfig)} instead.</p>
         * @see ChannelConfig#setEnableTypingIndicator(boolean)
         */
        @NonNull
        @Deprecated
        public Builder setUseTypingIndicator(boolean useTypingIndicator) {
            bundle.putBoolean(StringSet.KEY_USE_TYPING_INDICATOR, useTypingIndicator);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.1
         */
        @NonNull
        public Builder setHeaderTitle(@NonNull String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderLeftButtonIconResId(@DrawableRes int resId) {
            return setHeaderLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setHeaderLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setHeaderRightButtonIconResId(@DrawableRes int resId) {
            return setHeaderRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets whether the left button of the input is used.
         *
         * @param useInputLeftButton <code>true</code> if the left button of the input is used,
         *                           <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.1
         */
        @NonNull
        public Builder setUseInputLeftButton(boolean useInputLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_INPUT_LEFT_BUTTON, useInputLeftButton);
            return this;
        }

        /**
         * Sets the icon on the left button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setInputLeftButtonIconResId(@DrawableRes int resId) {
            return setInputLeftButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the left button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setInputLeftButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the icon on the right button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setInputRightButtonIconResId(@DrawableRes int resId) {
            return setInputRightButtonIcon(resId, null);
        }

        /**
         * Sets the icon on the right button of the input.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setInputRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets whether showing the right button of the input always.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.2
         */
        @NonNull
        public Builder showInputRightButtonAlways() {
            bundle.putBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS, true);
            return this;
        }

        /**
         * Sets the hint of the input text.
         *
         * @param hint text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setInputHint(@NonNull String hint) {
            bundle.putString(StringSet.KEY_INPUT_HINT, hint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerLeftButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnHeaderRightButtonClickListener(@NonNull View.OnClickListener listener) {
            this.headerRightButtonClickListener = listener;
            return this;
        }

        /**
         * Sets the message list adapter.
         *
         * @param adapter the adapter for the message list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        @NonNull
        public Builder setMessageListAdapter(@Nullable MessageListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the message list adapter and the message display data provider.
         * The message display data provider is used to generate the data to display the message.
         *
         * @param adapter the adapter for the message list.
         * @param provider the provider for the message display data.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.7
         */
        @NonNull
        public Builder setMessageListAdapter(@Nullable MessageListAdapter adapter, @Nullable MessageDisplayDataProvider provider) {
            this.adapter = adapter;
            if (this.adapter != null) this.adapter.setMessageDisplayDataProvider(provider);
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageClickListener(@NonNull OnItemClickListener<BaseMessage> itemClickListener) {
            this.messageClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> itemLongClickListener) {
            this.messageLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param quoteReplyMessageClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnQuoteReplyMessageClickListener(@NonNull OnItemClickListener<BaseMessage> quoteReplyMessageClickListener) {
            this.quoteReplyMessageClickListener = quoteReplyMessageClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param quoteReplyMessageLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnQuoteReplyMessageLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener) {
            this.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param threadInfoClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setOnThreadInfoClickListener(@NonNull OnItemClickListener<BaseMessage> threadInfoClickListener) {
            this.threadInfoClickListener = threadInfoClickListener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the input.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnInputLeftButtonClickListener(@NonNull View.OnClickListener listener) {
            this.inputLeftButtonListener = listener;
            return this;
        }

        /**
         * Sets the message list params for this channel.
         * The reverse and the nextResultSize properties in the MessageListParams are used in the UIKit. Even though you set that property it will be ignored.
         *
         * @param params The MessageListParams instance that you want to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.0.5
         */
        @NonNull
        public Builder setMessageListParams(@NonNull MessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Sets the click listener on the emoji reaction of the message.
         *
         * @param emojiReactionClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.1.0
         */
        @NonNull
        public Builder setEmojiReactionClickListener(@NonNull OnEmojiReactionClickListener emojiReactionClickListener) {
            this.emojiReactionClickListener = emojiReactionClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the emoji reaction of the message.
         *
         * @param emojiReactionLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.1.0
         */
        @NonNull
        public Builder setEmojiReactionLongClickListener(@NonNull OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
            this.emojiReactionLongClickListener = emojiReactionLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the emoji reaction more button.
         *
         * @param emojiReactionMoreButtonClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.1.0
         */
        @NonNull
        public Builder setEmojiReactionMoreButtonClickListener(@NonNull OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
            this.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
            return this;
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.1
         */
        @NonNull
        public Builder setUseMessageGroupUI(boolean useMessageGroupUI) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, useMessageGroupUI);
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageProfileClickListener(@NonNull OnItemClickListener<BaseMessage> profileClickListener) {
            this.messageProfileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets the click listener on the profile of emoji reaction user list.
         *
         * @param emojiReactionUserListProfileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.9.2
         */
        @NonNull
        public Builder setOnEmojiReactionUserListProfileClickListener(@NonNull OnItemClickListener<User> emojiReactionUserListProfileClickListener) {
            this.emojiReactionUserListProfileClickListener = emojiReactionUserListProfileClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the profile of message.
         *
         * @param messageProfileLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnMessageProfileLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
            this.messageProfileLongClickListener = messageProfileLongClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 1.2.2
         */
        @NonNull
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * The message input displays as a dialog type. (Refer to {@link KeyboardDisplayType})
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.0
         */
        @NonNull
        public Builder setKeyboardDisplayType(@NonNull KeyboardDisplayType type) {
            bundle.putSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE, type);
            return this;
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 1.2.5
         */
        @NonNull
        public Builder setLoadingDialogHandler(@NonNull LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.2
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId) {
            return setEmptyIcon(resId, null);
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param resId the resource identifier of the drawable.
         * @param tint  Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setEmptyIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_EMPTY_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_EMPTY_ICON_TINT, tint);
            return this;
        }

        /**
         * Sets the text when the data is not exists
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.0.2
         */
        @NonNull
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the text when error occurs
         *
         * @param resId the resource identifier of text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setErrorText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_ERROR_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is edited.
         *
         * @param editModeTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setOnEditModeTextChangedListener(@NonNull OnInputTextChangedListener editModeTextChangedListener) {
            this.editModeTextChangedListener = editModeTextChangedListener;
            return this;
        }

        /**
         * Sets the input text
         *
         * @param inputText the message text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setInputText(@NonNull String inputText) {
            bundle.putString(StringSet.KEY_INPUT_TEXT, inputText);
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is changed..
         *
         * @param inputTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@NonNull OnInputTextChangedListener inputTextChangedListener) {
            this.inputTextChangedListener = inputTextChangedListener;
            return this;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startTimemillis The timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public Builder setStartingPoint(long startTimemillis) {
            bundle.putLong(StringSet.KEY_STARTING_POINT, startTimemillis);
            return this;
        }

        /**
         * Sets whether the profile image of the header is used.
         *
         * @param useHeaderProfileImage <code>true</code> if the profile image of the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.2.4
         */
        @NonNull
        public Builder setUseHeaderProfileImage(boolean useHeaderProfileImage) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE, useHeaderProfileImage);
            return this;
        }

        /**
         * Sets the suggested mention list adapter.
         *
         * @param adapter the adapter for the mentionable user list.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setSuggestedMentionListAdapter(@Nullable SuggestedMentionListAdapter adapter) {
            this.suggestedMentionListAdapter = adapter;
            return this;
        }

        /**
         * Sets the UI configuration of mentioned text.
         *
         * @param configSentFromMe     the UI configuration of mentioned text in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of mentioned text in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setMentionUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_MENTION_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of edited text mark.
         *
         * @param configSentFromMe     the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers the UI configuration of edited text mark in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setEditedTextMarkUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_EDITED_MARK_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message text.
         *
         * @param configSentFromMe       the UI configuration of the message text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message text that was sent from others.\
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setMessageTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_MESSAGE_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message sentAt text.
         *
         * @param configSentFromMe       the UI configuration of the message sentAt text that was sent from me.
         * @param configSentFromOthers   the UI configuration of the message sentAt text that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setSentAtTextUIConfig(@Nullable TextUIConfig configSentFromMe, @Nullable TextUIConfig configSentFromOthers) {
            if (configSentFromMe != null)
                bundle.putParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_ME, configSentFromMe);
            if (configSentFromOthers != null)
                bundle.putParcelable(StringSet.KEY_SENT_AT_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of sender nickname text.
         *
         * @param configSentFromOthers   the UI configuration of the sender nickname text that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setNicknameTextUIConfig(@NonNull TextUIConfig configSentFromOthers) {
            bundle.putParcelable(StringSet.KEY_NICKNAME_TEXT_UI_CONFIG_SENT_FROM_OTHERS, configSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of the replied parent message text.
         *
         * @param configRepliedMessage the UI configuration of the replied parent message text.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.1
         */
        @NonNull
        public Builder setRepliedMessageTextUIConfig(@NonNull TextUIConfig configRepliedMessage) {
            bundle.putParcelable(StringSet.KEY_REPLIED_MESSAGE_TEXT_UI_CONFIG, configRepliedMessage);
            return this;
        }

        /**
         * Sets the UI configuration of message input text.
         *
         * @param textUIConfig the UI configuration of the message input text.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.1
         */
        @NonNull
        public Builder setMessageInputTextUIConfig(@NonNull TextUIConfig textUIConfig) {
            bundle.putParcelable(StringSet.KEY_MESSAGE_INPUT_TEXT_UI_CONFIG, textUIConfig);
            return this;
        }

        /**
         * Sets the UI configuration of message background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the message background that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the message background that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setMessageBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_MESSAGE_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of message reaction list background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the message reaction list background drawable that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the message reaction list background drawable that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setReactionListBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_REACTION_LIST_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of ogtag message background drawable.
         *
         * @param drawableResSentFromMe     the UI configuration of the ogtag message background drawable that was sent from me.
         * @param drawableResSentFromOthers the UI configuration of the ogtag message background drawable that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setOgtagBackground(@DrawableRes int drawableResSentFromMe, @DrawableRes int drawableResSentFromOthers) {
            bundle.putInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_ME, drawableResSentFromMe);
            bundle.putInt(StringSet.KEY_OGTAG_BACKGROUND_SENT_FROM_OTHERS, drawableResSentFromOthers);
            return this;
        }

        /**
         * Sets the UI configuration of the linked text color in the message text.
         *
         * @param colorRes  the UI configuration of the linked text color.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.1.1
         */
        @NonNull
        public Builder setLinkedTextColor(@ColorRes int colorRes) {
            bundle.putInt(StringSet.KEY_LINKED_TEXT_COLOR, colorRes);
            return this;
        }

        /**
         * Register a callback to be invoked when the right button of the input is clicked.
         *
         * @param inputRightButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnInputRightButtonClickListener(@Nullable View.OnClickListener inputRightButtonClickListener) {
            this.inputRightButtonClickListener = inputRightButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the cancel button is clicked, when the input is the edited mode.
         *
         * @param editModeCancelButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnEditModeCancelButtonClickListener(@Nullable View.OnClickListener editModeCancelButtonClickListener) {
            this.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the save button is clicked, when the input is the edited mode.
         *
         * @param editModeSaveButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnEditModeSaveButtonClickListener(@Nullable View.OnClickListener editModeSaveButtonClickListener) {
            this.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the close button is clicked, when the input is the quote reply mode.
         *
         * @param replyModeCloseButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnQuoteReplyModeCloseButtonClickListener(@Nullable View.OnClickListener replyModeCloseButtonClickListener) {
            this.replyModeCloseButtonClickListener = replyModeCloseButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the input mode is changed.
         *
         * @param inputModeChangedListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnInputModeChangedListener(@Nullable OnInputModeChangedListener inputModeChangedListener) {
            this.inputModeChangedListener = inputModeChangedListener;
            return this;
        }

        /**
         * Sets whether to use divider in suggested mention list.
         *
         * @param useDivider If <code>true</code> the divider will be used at suggested mention list, <code>false</code> other wise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setUseSuggestedMentionListDivider(boolean useDivider) {
            bundle.putBoolean(StringSet.KEY_USE_SUGGESTED_MENTION_LIST_DIVIDER, useDivider);
            return this;
        }

        /**
         * Register a callback to be invoked when the tooltip view is clicked.
         *
         * @param tooltipClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         */
        @NonNull
        public Builder setOnTooltipClickListener(@Nullable View.OnClickListener tooltipClickListener) {
            this.tooltipClickListener = tooltipClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the unread count tooltip is clicked.
         *
         * @param unreadCountTooltipClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.24.0
         */
        @NonNull
        public Builder setOnUnreadCountTooltipClickListener(@Nullable View.OnClickListener unreadCountTooltipClickListener) {
            this.unreadCountTooltipClickListener = unreadCountTooltipClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the button to scroll to the bottom is clicked.
         *
         * @param scrollBottomButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.0.0
         * @deprecated 3.2.2
         * This method is no longer acceptable to invoke event.
         * <p> Use {@link #setOnScrollFirstButtonClickListener(OnConsumableClickListener)} instead.
         */
        @NonNull
        @Deprecated
        public Builder setOnScrollBottomButtonClickListener(@Nullable View.OnClickListener scrollBottomButtonClickListener) {
            this.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
            return this;
        }

        /**
         * Register a callback to be invoked when the button to scroll to the first position is clicked.
         *
         * @param scrollFirstButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.2.2
         */
        @NonNull
        public Builder setOnScrollFirstButtonClickListener(@Nullable OnConsumableClickListener scrollFirstButtonClickListener) {
            this.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
            return this;
        }

        /**
         * Sets whether the message list banner is used.
         *
         * @param useBanner <code>true</code> if the message list banner is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public Builder setUseMessageListBanner(boolean useBanner) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_LIST_BANNER, useBanner);
            return this;
        }

        /**
         * Register a callback to be invoked when the button to show voice recorder is clicked.
         *
         * @param voiceRecorderButtonClickListener The callback that will run
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.4.0
         */
        @NonNull
        public Builder setOnVoiceRecorderButtonClickListener(@Nullable View.OnClickListener voiceRecorderButtonClickListener) {
            this.voiceRecorderButtonClickListener = voiceRecorderButtonClickListener;
            return this;
        }

        /**
         * Sets the click listener on the mentioned user of message.
         *
         * @param mentionClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.5.3
         */
        @NonNull
        public Builder setOnMessageMentionClickListener(@NonNull OnItemClickListener<User> mentionClickListener) {
            this.messageMentionClickListener = mentionClickListener;
            return this;
        }

        public Builder setMessageTemplateActionHandler(@NonNull OnMessageTemplateActionHandler messageTemplateActionHandler) {
            this.messageTemplateActionHandler = messageTemplateActionHandler;
            return this;
        }

        /**
         * Sets channel configuration for this fragment.
         * Use {@code UIKitConfig.groupChannelConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val fragment = ChannelFragment.Builder(CHANNEL_URL)
         *     .setChannelConfig(
         *         UIKitConfig.groupChannelConfig.clone().apply {
         *            this.enableMention = true
         *         }
         *     )
         *     .build()
         * </pre>
         *
         * @param channelConfig The channel config.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.6.0
         */
        @NonNull
        public Builder setChannelConfig(@NonNull ChannelConfig channelConfig) {
            this.bundle.putParcelable(StringSet.KEY_CHANNEL_CONFIG, channelConfig);
            return this;
        }

        /**
         * Creates an {@link ChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public ChannelFragment build() {
            final ChannelFragment fragment = customFragment != null ? customFragment : new ChannelFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.setOnMessageClickListener(messageClickListener);
            fragment.setOnMessageLongClickListener(messageLongClickListener);
            fragment.inputLeftButtonClickListener = inputLeftButtonListener;
            fragment.emojiReactionClickListener = emojiReactionClickListener;
            fragment.emojiReactionLongClickListener = emojiReactionLongClickListener;
            fragment.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
            fragment.setOnMessageProfileClickListener(messageProfileClickListener);
            fragment.setOnEmojiReactionUserListProfileClickListener(emojiReactionUserListProfileClickListener);
            fragment.setOnMessageProfileLongClickListener(messageProfileLongClickListener);
            fragment.setOnLoadingDialogHandler(loadingDialogHandler);
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.editModeTextChangedListener = editModeTextChangedListener;
            fragment.quoteReplyMessageClickListener = quoteReplyMessageClickListener;
            fragment.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
            fragment.setSuggestedMentionListAdapter(suggestedMentionListAdapter);
            fragment.inputRightButtonClickListener = inputRightButtonClickListener;
            fragment.editModeCancelButtonClickListener = editModeCancelButtonClickListener;
            fragment.editModeSaveButtonClickListener = editModeSaveButtonClickListener;
            fragment.replyModeCloseButtonClickListener = replyModeCloseButtonClickListener;
            fragment.inputModeChangedListener = inputModeChangedListener;
            fragment.tooltipClickListener = tooltipClickListener;
            fragment.unreadCountTooltipClickListener = unreadCountTooltipClickListener;
            fragment.scrollBottomButtonClickListener = scrollBottomButtonClickListener;
            fragment.scrollFirstButtonClickListener = scrollFirstButtonClickListener;
            fragment.setAdapter(adapter);
            fragment.params = params;
            fragment.threadInfoClickListener = threadInfoClickListener;
            fragment.onVoiceRecorderButtonClickListener = voiceRecorderButtonClickListener;
            fragment.setOnMessageMentionClickListener(messageMentionClickListener);
            fragment.messageTemplateActionHandler = messageTemplateActionHandler;

            // set animation flag to TRUE to animate searched text.
            if (bundle.containsKey(StringSet.KEY_TRY_ANIMATE_WHEN_MESSAGE_LOADED)) {
                fragment.tryAnimateWhenMessageLoaded.set(true);
            }
            return fragment;
        }
    }
}
