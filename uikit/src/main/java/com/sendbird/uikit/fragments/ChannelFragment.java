package com.sendbird.uikit.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.Emoji;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.Reaction;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.activities.PhotoViewActivity;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.activities.adapter.SuggestedMentionListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.model.TextUIConfig;
import com.sendbird.uikit.modules.ChannelModule;
import com.sendbird.uikit.modules.components.ChannelHeaderComponent;
import com.sendbird.uikit.modules.components.MessageInputComponent;
import com.sendbird.uikit.modules.components.MessageListComponent;
import com.sendbird.uikit.modules.components.StatusComponent;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.ReactionUtils;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.ChannelViewModel;
import com.sendbird.uikit.vm.FileDownloader;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.EmojiListView;
import com.sendbird.uikit.widgets.EmojiReactionUserListView;
import com.sendbird.uikit.widgets.MentionEditText;
import com.sendbird.uikit.widgets.MessageInputView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment that provides chat in {@code GroupChannel}
 */
public class ChannelFragment extends BaseModuleFragment<ChannelModule, ChannelViewModel> {
    private static final int CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 2002;
    private static final int PICK_FILE_PERMISSIONS_REQUEST_CODE = 2003;
    private static final int PERMISSION_REQUEST_ALL = 2005;
    private static final int PERMISSION_REQUEST_STORAGE = 2006;

    @Nullable
    private View.OnClickListener headerLeftButtonClickListener;
    @Nullable
    private View.OnClickListener headerRightButtonClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> quoteReplyMessageClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener;
    @Nullable
    private View.OnClickListener inputLeftButtonClickListener;
    @Nullable
    private OnEmojiReactionClickListener emojiReactionClickListener;
    @Nullable
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnInputTextChangedListener editModeTextChangedListener;
    @Nullable
    private MessageListAdapter adapter;
    @Nullable
    private MessageListParams params;
    @Nullable
    private SuggestedMentionListAdapter suggestedMentionListAdapter;

    @Nullable
    private BaseMessage targetMessage;
    @NonNull
    private final AtomicBoolean isInitCallFinished = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean shouldAnimate = new AtomicBoolean(false);
    @NonNull
    private final AtomicBoolean anchorDialogShowing = new AtomicBoolean(false);
    @Nullable
    private Uri mediaUri;

    @NonNull
    @Override
    protected ChannelModule onCreateModule(@NonNull Bundle args) {
        return new ChannelModule(requireContext());
    }

    @Override
    protected void onConfigureParams(@NonNull ChannelModule module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @NonNull
    @Override
    protected ChannelViewModel onCreateViewModel() {
        return new ViewModelProvider(this, new ViewModelFactory(getChannelUrl(), params)).get(getChannelUrl(), ChannelViewModel.class);
    }

    @Override
    public void onDestroy() {
        Logger.i(">> ChannelFragment::onDestroy()");
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
        if (!isInitCallFinished.get()) {
            shouldDismissLoadingDialog();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldShowLoadingDialog();
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull ChannelModule module, @NonNull ChannelViewModel viewModel) {
        Logger.d(">> ChannelFragment::onBeforeReady()");
        module.getMessageListComponent().setPagedDataLoader(viewModel);
        if (this.adapter != null) {
            module.getMessageListComponent().setAdapter(adapter);
        }
        module.getMessageInputComponent().setSuggestedMentionListAdapter(suggestedMentionListAdapter == null ? new SuggestedMentionListAdapter() : suggestedMentionListAdapter);
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
        if (status == ReadyStatus.ERROR || channel == null) {
            if (isFragmentAlive()) {
                toastError(R.string.sb_text_error_get_channel);
                shouldActivityFinish();
            }
            return;
        }

        viewModel.onChannelDeleted().observe(getViewLifecycleOwner(), channelUrl -> shouldActivityFinish());

        module.getMessageListComponent().notifyChannelChanged(channel);
        module.getHeaderComponent().notifyChannelChanged(channel);
        module.getMessageInputComponent().notifyChannelChanged(channel);

        final MessageListComponent messageListComponent = module.getMessageListComponent();
        final long startingPoint = messageListComponent.getParams().getInitialStartingPoint();
        loadInitial(startingPoint);
    }

    /**
     * Called to bind events to the ChannelHeaderComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param headerComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindChannelHeaderComponent(@NonNull ChannelHeaderComponent headerComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindChannelHeaderComponent()");
        headerComponent.setOnLeftButtonClickListener(headerLeftButtonClickListener != null ? headerLeftButtonClickListener : v -> shouldActivityFinish());
        headerComponent.setOnRightButtonClickListener(headerRightButtonClickListener != null ? headerRightButtonClickListener : v -> {
            if (channel == null) return;
            Intent intent = ChannelSettingsActivity.newIntent(requireContext(), channel.getUrl());
            startActivity(intent);
        });

        viewModel.getTypingMembers().observe(getViewLifecycleOwner(), typingMembers -> {
            String description = null;
            if (typingMembers != null && getContext() != null) {
                description = ChannelUtils.makeTypingText(getContext(), typingMembers);
            }
            headerComponent.notifyHeaderDescriptionChanged(description);
        });
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), headerComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the MessageListComponent and also bind ChannelViewModel.
     * This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param messageListComponent The component to which the event will be bound
     * @param viewModel            A view model that provides the data needed for the fragment
     * @param channel              The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindMessageListComponent(@NonNull MessageListComponent messageListComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindMessageListComponent()");
        if (channel == null) return;
        messageListComponent.setOnMessageClickListener(this::onMessageClicked);
        messageListComponent.setOnMessageProfileLongClickListener(this::onMessageProfileLongClicked);
        messageListComponent.setOnQuoteReplyMessageLongClickListener(this::onQuoteReplyMessageLongClicked);
        messageListComponent.setOnMessageProfileClickListener(this::onMessageProfileClicked);
        messageListComponent.setOnQuoteReplyMessageClickListener(this::onQuoteReplyMessageClicked);
        messageListComponent.setOnMessageLongClickListener(this::onMessageLongClicked);
        messageListComponent.setOnEmojiReactionClickListener(emojiReactionClickListener != null ? emojiReactionClickListener : (view, position, message, reactionKey) -> toggleReaction(view, message, reactionKey));
        messageListComponent.setOnEmojiReactionLongClickListener(emojiReactionLongClickListener != null ? emojiReactionLongClickListener : (view, position, message, reactionKey) -> showEmojiReactionDialog(message, position));
        messageListComponent.setOnEmojiReactionMoreButtonClickListener(emojiReactionMoreButtonClickListener != null ? emojiReactionMoreButtonClickListener : (view, position, message) -> showEmojiListDialog(message));
        messageListComponent.setOnTooltipClickListener(v -> scrollToBottom());
        messageListComponent.setOnScrollBottomButtonClickListener(v -> scrollToBottom());

        final ChannelModule module = getModule();
        viewModel.getMessageList().observe(getViewLifecycleOwner(), receivedMessageData -> {
            boolean isInitialCallFinished = isInitCallFinished.getAndSet(true);
            if (!isInitialCallFinished && isFragmentAlive()) shouldDismissLoadingDialog();

            final List<BaseMessage> messageList = receivedMessageData.getMessages();
            Logger.d("++ result messageList size : %s, source = %s", messageList.size(), receivedMessageData.getTraceName());

            if (messageList.isEmpty()) return;
            final String eventSource = receivedMessageData.getTraceName();
            // The callback coming from setItems is worked asynchronously. So `isInitCallFinished` flag has to mark in advance.
            messageListComponent.notifyDataSetChanged(messageList, channel, messages -> {
                if (!isFragmentAlive()) return;

                if (eventSource != null) {
                    Logger.d("++ Message action : %s", eventSource);
                    final RecyclerView recyclerView = messageListComponent.getRecyclerView();

                    final MessageListAdapter adapter = messageListComponent.getAdapter();
                    if (recyclerView == null || adapter == null) return;

                    final Context context = recyclerView.getContext();
                    switch (eventSource) {
                        case StringSet.ACTION_FAILED_MESSAGE_ADDED:
                        case StringSet.ACTION_PENDING_MESSAGE_ADDED:
                            module.getMessageInputComponent().requestInputMode(MessageInputView.Mode.DEFAULT);
                            scrollToBottom();
                            break;
                        case StringSet.EVENT_MESSAGE_RECEIVED:
                        case StringSet.EVENT_MESSAGE_SENT:
                            messageListComponent.notifyOtherMessageReceived(anchorDialogShowing.get());
                            if (eventSource.equals(StringSet.EVENT_MESSAGE_SENT)) {
                                final MessageListParams messageListParams = viewModel.getMessageListParams();
                                final BaseMessage latestMessage = adapter.getItem(messageListParams.shouldReverse() ? 0 : adapter.getItemCount() - 1);
                                if (latestMessage instanceof FileMessage) {
                                    // Download from files already sent for quick image loading.
                                    FileDownloader.downloadThumbnail(context, (FileMessage) latestMessage);
                                }
                            }
                            break;
                        case StringSet.ACTION_INIT_FROM_REMOTE:
                        case StringSet.MESSAGE_CHANGELOG:
                        case StringSet.MESSAGE_FILL:
                            messageListComponent.notifyMessagesFilled(!anchorDialogShowing.get());
                            break;
                    }
                }
                if (!isInitialCallFinished) {
                    BaseMessage willAnimateMessage = null;
                    if (shouldAnimate.getAndSet(false)) {
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
        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), messageListComponent::notifyChannelChanged);
    }

    /**
     * Called to bind events to the MessageInputComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param inputComponent The component to which the event will be bound
     * @param viewModel      A view model that provides the data needed for the fragment
     * @param channel        The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindMessageInputComponent(@NonNull MessageInputComponent inputComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindMessageInputComponent()");
        if (channel == null) return;
        inputComponent.setOnInputLeftButtonClickListener(inputLeftButtonClickListener != null ? inputLeftButtonClickListener : v -> showMediaSelectDialog());
        inputComponent.setOnInputRightButtonClickListener(v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                final Editable editableText = inputText.getText();
                UserMessageParams params = new UserMessageParams(editableText.toString());
                if (targetMessage != null && SendbirdUIKit.getReplyType() == ReplyType.QUOTE_REPLY) {
                    params.setParentMessageId(targetMessage.getMessageId());
                    params.setReplyToChannel(true);
                }
                if (SendbirdUIKit.isUsingUserMention()) {
                    setMentionInfo(inputText, params);
                }
                sendUserMessage(params);
            }
        });
        inputComponent.setOnEditModeSaveButtonClickListener(v -> {
            final EditText inputText = inputComponent.getEditTextView();
            if (inputText != null && !TextUtils.isEmpty(inputText.getText())) {
                if (null != targetMessage) {
                    UserMessageParams params = new UserMessageParams(inputText.getText().toString());
                    setMentionInfo(inputText, params);
                    updateUserMessage(targetMessage.getMessageId(), params);
                } else {
                    Logger.d("Target message for update is missing");
                }
            }
            inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
        });

        inputComponent.setOnEditModeTextChangedListener(editModeTextChangedListener != null ? editModeTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnEditModeCancelButtonClickListener(v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnQuoteReplyModeCloseButtonClickListener(v -> inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT));
        inputComponent.setOnInputTextChangedListener(inputTextChangedListener != null ? inputTextChangedListener : (s, start, before, count) -> viewModel.setTyping(s.length() > 0));
        inputComponent.setOnInputModeChangedListener((before, current) -> {
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
        });

        if (SendbirdUIKit.isUsingUserMention()) {
            inputComponent.bindUserMention(SendbirdUIKit.getUserMentionConfig(), text -> viewModel.loadMemberList(text != null ? text.toString() : null));

            // observe suggestion list
            viewModel.getMentionSuggestion().observe(getViewLifecycleOwner(), suggestion -> {
                inputComponent.notifySuggestedMentionDataChanged(suggestion.getSuggestionList());
            });
        }

        viewModel.onMessagesDeleted().observe(getViewLifecycleOwner(), deletedMessages -> {
            if (targetMessage != null && deletedMessages.contains(targetMessage)) {
                targetMessage = null;
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });

        viewModel.onChannelUpdated().observe(getViewLifecycleOwner(), openChannel -> {
            inputComponent.notifyChannelChanged(openChannel);
            boolean isOperator = channel.getMyRole() == Member.Role.OPERATOR;
            boolean isMuted = channel.getMyMutedState() == Member.MutedState.MUTED;
            boolean isFrozen = channel.isFrozen() && !isOperator;
            if (isMuted || isFrozen) {
                inputComponent.requestInputMode(MessageInputView.Mode.DEFAULT);
            }
        });
    }

    /**
     * Called to bind events to the StatusComponent. This is called from {@link #onBeforeReady(ReadyStatus, ChannelModule, ChannelViewModel)} regardless of the value of {@link ReadyStatus}.
     *
     * @param statusComponent The component to which the event will be bound
     * @param viewModel       A view model that provides the data needed for the fragment
     * @param channel         The {@code GroupChannel} that contains the data needed for this fragment
     * @since 3.0.0
     */
    protected void onBindStatusComponent(@NonNull StatusComponent statusComponent, @NonNull ChannelViewModel viewModel, @Nullable GroupChannel channel) {
        Logger.d(">> ChannelFragment::onBindStatusComponent()");
        viewModel.getStatusFrame().observe(getViewLifecycleOwner(), statusComponent::notifyStatusChanged);
    }

    private void scrollToBottom() {
        final MessageListComponent messageListComponent = getModule().getMessageListComponent();
        if (getViewModel().hasNext()) {
            loadInitial(Long.MAX_VALUE);
        } else {
            messageListComponent.scrollToBottom();
        }
    }

    /**
     * Make context menu items that are shown when the message is long clicked.
     *
     * @param message A clicked message.
     * @return Collection of {@link DialogListItem}
     * @since 2.2.3
     */
    @NonNull
    protected List<DialogListItem> makeMessageContextMenu(@NonNull BaseMessage message) {
        final List<DialogListItem> items = new ArrayList<>();
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.PENDING) return items;

        MessageType type = MessageViewHolderFactory.getMessageType(message);
        DialogListItem copy = new DialogListItem(R.string.sb_text_channel_anchor_copy, R.drawable.icon_copy);
        DialogListItem edit = new DialogListItem(R.string.sb_text_channel_anchor_edit, R.drawable.icon_edit);
        DialogListItem save = new DialogListItem(R.string.sb_text_channel_anchor_save, R.drawable.icon_download);
        DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete, false, MessageUtils.hasThread(message));
        DialogListItem reply = new DialogListItem(R.string.sb_text_channel_anchor_reply, R.drawable.icon_reply, false, MessageUtils.hasParentMessage(message));
        DialogListItem retry = new DialogListItem(R.string.sb_text_channel_anchor_retry, 0);
        DialogListItem deleteFailed = new DialogListItem(R.string.sb_text_channel_anchor_delete, 0);

        DialogListItem[] actions = null;
        final ReplyType replyType = SendbirdUIKit.getReplyType();
        switch (type) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                if (status == BaseMessage.SendingStatus.SUCCEEDED) {
                    if (replyType == ReplyType.NONE) {
                        actions = new DialogListItem[]{copy, edit, delete};
                    } else {
                        actions = new DialogListItem[]{copy, edit, delete, reply};
                    }
                } else if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                }
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                if (replyType == ReplyType.NONE) {
                    actions = new DialogListItem[]{copy};
                } else {
                    actions = new DialogListItem[]{copy, reply};
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    if (replyType == ReplyType.NONE) {
                        actions = new DialogListItem[]{delete, save};
                    } else {
                        actions = new DialogListItem[]{delete, save, reply};
                    }
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                if (replyType == ReplyType.NONE) {
                    actions = new DialogListItem[]{save};
                } else {
                    actions = new DialogListItem[]{save, reply};
                }
                break;
            case VIEW_TYPE_UNKNOWN_MESSAGE_ME:
                actions = new DialogListItem[]{delete};
            default:
                break;
        }

        if (actions != null) {
            items.addAll(Arrays.asList(actions));
        }
        return items;
    }

    private void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {
        int size = items.size();
        final DialogListItem[] actions = items.toArray(new DialogListItem[size]);
        if (!ReactionUtils.canSendReaction(getViewModel().getChannel())) {
            final RecyclerView messageListView = getModule().getMessageListComponent().getRecyclerView();
            if (getContext() != null && messageListView != null) {
                MessageAnchorDialog messageAnchorDialog = new MessageAnchorDialog.Builder(anchorView, messageListView, actions)
                        .setOnItemClickListener(createMessageActionListener(message))
                        .setOnDismissListener(() -> anchorDialogShowing.set(false))
                        .build();
                messageAnchorDialog.show();
                anchorDialogShowing.set(true);
            }
        } else if (MessageUtils.isUnknownType(message)) {
            if (getContext() == null) return;
            DialogUtils.showListBottomDialog(requireContext(), actions, createMessageActionListener(message));
        } else {
            showEmojiActionsDialog(message, actions);
        }
    }

    /**
     * It will be called when the message context menu was clicked.
     *
     * @param message  A clicked message.
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param item     {@link DialogListItem} that was clicked.
     * @return <code>true</code> if long click event was handled, <code>false</code> otherwise.
     * @since 2.2.3
     */
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
        } else if (key == R.string.sb_text_channel_anchor_retry) {
            resendMessage(message);
            return true;
        }
        return false;
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageClickListener != null) {
            messageClickListener.onItemClick(view, position, message);
            return;
        }
        if (message.getSendingStatus() == BaseMessage.SendingStatus.SUCCEEDED) {
            MessageType type = MessageViewHolderFactory.getMessageType(message);
            switch (type) {
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                    startActivity(PhotoViewActivity.newIntent(requireContext(), BaseChannel.ChannelType.GROUP, (FileMessage) message));
                    break;
                case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                case VIEW_TYPE_FILE_MESSAGE_ME:
                case VIEW_TYPE_FILE_MESSAGE_OTHER:
                    final FileMessage fileMessage = (FileMessage) message;
                    FileDownloader.downloadFile(requireContext(), fileMessage, new OnResultHandler<File>() {
                        @Override
                        public void onResult(@NonNull File file) {
                            showFile(file, fileMessage.getType());
                        }

                        @Override
                        public void onError(@Nullable SendBirdException e) {
                            toastError(R.string.sb_text_error_download_file);
                        }
                    });
                    break;
                default:
            }
        } else {
            if (MessageUtils.isMine(message) && (message instanceof UserMessage || message instanceof FileMessage)) {
                resendMessage(message);
            }
        }
    }

    /**
     * Called when the profile view of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileClickListener != null) {
            messageProfileClickListener.onItemClick(view, position, message);
            return;
        }

        showUserProfile(message.getSender());
    }

    /**
     * Called when the quoted message of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * @since 3.0.0
     */
    protected void onQuoteReplyMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageClickListener != null) {
            quoteReplyMessageClickListener.onItemClick(view, position, message);
            return;
        }
        jumpToParentMessage(message);
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageLongClickListener != null) {
            messageLongClickListener.onItemLongClick(view, position, message);
            return;
        }
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.PENDING) return;
        showMessageContextMenu(view, message, makeMessageContextMenu(message));
    }

    /**
     * Called when the profile view of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onMessageProfileLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileLongClickListener != null) {
            messageProfileLongClickListener.onItemLongClick(view, position, message);
        }
    }

    /**
     * Called when the quoted message of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * @since 3.0.0
     */
    protected void onQuoteReplyMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (quoteReplyMessageLongClickListener != null)
            quoteReplyMessageLongClickListener.onItemLongClick(view, position, message);
    }

    @NonNull
    private OnItemClickListener<DialogListItem> createMessageActionListener(@NonNull BaseMessage message) {
        return (view, position, item) -> onMessageContextMenuItemClicked(message, view, position, item);
    }

    private void download(@NonNull FileMessage fileMessage) {
        toastSuccess(R.string.sb_text_toast_success_start_download_file);
        TaskQueue.addTask(new JobResultTask<Boolean>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            @NonNull
            public Boolean call() throws Exception {
                if (getContext() == null) return false;
                FileDownloader.getInstance().saveFile(getContext(), fileMessage.getUrl(),
                        fileMessage.getType(), fileMessage.getName());
                return true;
            }

            @Override
            public void onResultForUiThread(@Nullable Boolean result, @Nullable SendBirdException e) {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_download_file);
                    return;
                }
                toastSuccess(R.string.sb_text_toast_success_download_file);
            }
        });
    }

    private void copyTextToClipboard(@NonNull String text) {
        if (!isFragmentAlive()) return;
        ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(StringSet.LABEL_COPY_TEXT, text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
            toastSuccess(R.string.sb_text_toast_success_copy);
        } else {
            toastError(R.string.sb_text_error_copy_message);
        }
    }

    private void showWarningDialog(@NonNull BaseMessage message) {
        if (getContext() == null) return;
        DialogUtils.showWarningDialog(
                requireContext(),
                getString(R.string.sb_text_dialog_delete_message),
                getString(R.string.sb_text_button_delete),
                delete -> {
                    Logger.dev("delete");
                    deleteMessage(message);
                },
                getString(R.string.sb_text_button_cancel),
                cancel -> Logger.dev("cancel"));
    }

    private void showEmojiActionsDialog(@NonNull BaseMessage message, @NonNull DialogListItem[] actions) {
        if (getContext() == null) {
            return;
        }

        List<Emoji> emojiList = EmojiManager.getInstance().getAllEmojis();

        int shownEmojiSize = emojiList.size();
        boolean showMoreButton = false;
        if (emojiList.size() > 6) {
            showMoreButton = true;
            shownEmojiSize = 5;
        }
        emojiList = emojiList.subList(0, shownEmojiSize);

        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiListView emojiListView = new EmojiListView.Builder(contextThemeWrapper)
                .setEmojiList(emojiList)
                .setReactionList(message.getReactions())
                .setShowMoreButton(showMoreButton)
                .create();

        hideKeyboard();
        final AlertDialog dialog = DialogUtils.showContentViewAndListDialog(requireContext(), emojiListView, actions, createMessageActionListener(message));

        emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
            dialog.dismiss();
            getViewModel().toggleReaction(view, message, emojiKey, e -> {
                if (e != null)
                    toastError(view.isSelected() ? R.string.sb_text_error_delete_reaction : R.string.sb_text_error_add_reaction);
            });
        });

        emojiListView.setMoreButtonClickListener(v -> {
            dialog.dismiss();
            showEmojiListDialog(message);
        });
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
                shouldAnimate.set(true);
                loadInitial(parentMessageCreatedAt);
            }
        } else {
            toastError(R.string.sb_text_error_original_message_not_found);
        }
    }

    private void showUserProfile(@NonNull User sender) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendbirdUIKit.shouldUseDefaultUserProfile());
        if (getContext() == null || !useUserProfile) return;
        hideKeyboard();
        DialogUtils.showUserProfileDialog(getContext(), sender, true, null, null);
    }

    @NonNull
    private static Map<Reaction, List<User>> getReactionUserInfo(@NonNull GroupChannel channel, @NonNull List<Reaction> reactionList) {
        final Map<Reaction, List<User>> result = new HashMap<>();
        final Map<String, User> userMap = new HashMap<>();

        for (Member member : channel.getMembers()) {
            userMap.put(member.getUserId(), member);
        }

        for (Reaction reaction : reactionList) {
            final List<User> userList = new ArrayList<>();
            final List<String> userIds = reaction.getUserIds();
            for (String userId : userIds) {
                final User user = userMap.get(userId);
                userList.add(user);
            }
            result.put(reaction, userList);
        }

        return result;
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    private synchronized void loadInitial(long startingPoint) {
        isInitCallFinished.set(false);
        final GroupChannel channel = getViewModel().getChannel();
        if (channel != null) {
            getViewModel().loadInitial(startingPoint);
        }
    }

    private void toggleReaction(@NonNull View view, @NonNull BaseMessage message, @NonNull String reactionKey) {
        getViewModel().toggleReaction(view, message, reactionKey, e -> {
            if (e != null && isFragmentAlive()) {
                toastError(view.isSelected() ? R.string.sb_text_error_delete_reaction : R.string.sb_text_error_add_reaction);
            }
        });
    }

    private void showEmojiReactionDialog(@NonNull BaseMessage message, int position) {
        if (getContext() == null) {
            return;
        }

        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiReactionUserListView emojiReactionUserListView = new EmojiReactionUserListView(contextThemeWrapper);
        final GroupChannel channel = getViewModel().getChannel();
        if (channel != null) {
            emojiReactionUserListView.setEmojiReactionUserData(this,
                    position,
                    message.getReactions(),
                    getReactionUserInfo(channel, message.getReactions()));
        }
        hideKeyboard();
        DialogUtils.showContentDialog(requireContext(), emojiReactionUserListView);
    }

    private void showEmojiListDialog(@NonNull BaseMessage message) {
        if (getContext() == null) {
            return;
        }

        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiListView emojiListView = new EmojiListView.Builder(contextThemeWrapper)
                .setEmojiList(EmojiManager.getInstance().getAllEmojis())
                .setReactionList(message.getReactions())
                .setShowMoreButton(false)
                .create();
        hideKeyboard();
        final AlertDialog dialog = DialogUtils.showContentDialog(requireContext(), emojiListView);

        emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
            dialog.dismiss();
            getViewModel().toggleReaction(view, message, emojiKey, e -> {
                if (e != null)
                    toastError(view.isSelected() ? R.string.sb_text_error_delete_reaction : R.string.sb_text_error_add_reaction);
            });
        });
    }

    private void showFile(@NonNull File file, @NonNull String mimeType) {
        TaskQueue.addTask(new JobResultTask<Intent>() {
            @Override
            @Nullable
            public Intent call() {
                if (!isFragmentAlive()) return null;
                Uri uri = FileUtils.fileToUri(requireContext(), file);
                return IntentUtils.getFileViewerIntent(uri, mimeType);
            }

            @Override
            public void onResultForUiThread(@Nullable Intent intent, @Nullable SendBirdException e) {
                if (!isFragmentAlive()) return;
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_open_file);
                    return;
                }
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void setMentionInfo(@NonNull final EditText inputText,
                                @NonNull final UserMessageParams params) {
        if (inputText instanceof MentionEditText) {
            final List<User> mentionedUsers = ((MentionEditText) inputText).getMentionedUsers();
            final CharSequence mentionedTemplate = ((MentionEditText) inputText).getMentionedTemplate();
            Logger.d("++ mentioned template text=%s", mentionedTemplate);
            params.setMentionedMessageTemplate(mentionedTemplate.toString());
            params.setMentionedUsers(mentionedUsers);
        }
    }

    /**
     * It will be called when the input message's left button is clicked.
     * The default behavior is showing the menu, like, taking camera, gallery, and file.
     *
     * @since 2.0.1
     */
    protected void showMediaSelectDialog() {
        if (getContext() == null) return;
        DialogListItem[] items = {
                new DialogListItem(R.string.sb_text_channel_input_camera, R.drawable.icon_camera),
                new DialogListItem(R.string.sb_text_channel_input_gallery, R.drawable.icon_photo),
                new DialogListItem(R.string.sb_text_channel_input_document, R.drawable.icon_document)
        };
        hideKeyboard();
        DialogUtils.showListBottomDialog(requireContext(), items, (view, position, item) -> {
            final int key = item.getKey();
            try {
                if (key == R.string.sb_text_channel_input_camera) {
                    takeCamera();
                } else if (key == R.string.sb_text_channel_input_gallery) {
                    takePhoto();
                } else {
                    takeFile();
                }
            } catch (Exception e) {
                Logger.e(e);
                if (key == R.string.sb_text_channel_input_camera) {
                    toastError(R.string.sb_text_error_open_camera);
                } else if (key == R.string.sb_text_channel_input_gallery) {
                    toastError(R.string.sb_text_error_open_gallery);
                } else {
                    toastError(R.string.sb_text_error_open_file);
                }
            }
        });
    }

    /**
     * Call taking camera application.
     *
     * @since 2.0.1
     */
    public void takeCamera() {
        SendBird.setAutoBackgroundDetection(false);
        checkPermission(PERMISSION_REQUEST_ALL, new PermissionFragment.IPermissionHandler() {
            @Override
            @NonNull
            public String[] getPermissions(int requestCode) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    return new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE};
                }
                return new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            }

            @Override
            public void onPermissionGranted(int requestCode) {
                if (getContext() == null) return;
                mediaUri = FileUtils.createPictureImageUri(getContext());
                if (mediaUri == null) return;
                Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
                if (IntentUtils.hasIntent(getContext(), intent)) {
                    startActivityForResult(intent, CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE);
                }
            }
        });
    }

    /**
     * Call taking gallery application.
     *
     * @since 2.0.1
     */
    public void takePhoto() {
        SendBird.setAutoBackgroundDetection(false);
        checkPermission(PERMISSION_REQUEST_STORAGE, new PermissionFragment.IPermissionHandler() {
            @Override
            @NonNull
            public String[] getPermissions(int requestCode) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                }
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            }

            @Override
            public void onPermissionGranted(int requestCode) {
                Intent intent = IntentUtils.getGalleryIntent();
                startActivityForResult(intent, PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            }
        });
    }

    /**
     * Call taking file chooser application.
     *
     * @since 2.0.1
     */
    public void takeFile() {
        SendBird.setAutoBackgroundDetection(false);
        checkPermission(PERMISSION_REQUEST_STORAGE, new PermissionFragment.IPermissionHandler() {
            @Override
            @NonNull
            public String[] getPermissions(int requestCode) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                }
                return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
            }

            @Override
            public void onPermissionGranted(int requestCode) {
                Intent intent = IntentUtils.getFileChooserIntent();
                startActivityForResult(intent, PICK_FILE_PERMISSIONS_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendBird.setAutoBackgroundDetection(true);

        if (resultCode != RESULT_OK) return;

        Uri mediaUri = this.mediaUri;
        switch (requestCode) {
            case CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE:
                break;
            case PICK_IMAGE_PERMISSIONS_REQUEST_CODE:
            case PICK_FILE_PERMISSIONS_REQUEST_CODE:
                if (data != null) {
                    mediaUri = data.getData();
                }
                break;
        }

        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    protected boolean shouldShowLoadingDialog() {
        return getModule().shouldShowLoadingDialog();
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     * @since 1.0.4
     */
    protected void onBeforeSendUserMessage(@NonNull UserMessageParams params) {
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageParams}.
     * @since 1.0.4
     */
    protected void onBeforeSendFileMessage(@NonNull FileMessageParams params) {
    }

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     * @since 1.0.4
     */
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageParams params) {
    }

    /**
     * Sends a user message.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     * @since 1.0.4
     */
    protected void sendUserMessage(@NonNull UserMessageParams params) {
        final CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeSendUserMessage(params);
        }
        onBeforeSendUserMessage(params);
        getViewModel().sendUserMessage(params);
    }

    /**
     * Sends a file with given file information.
     *
     * @param uri A file Uri
     * @since 1.0.4
     */
    protected void sendFileMessage(@NonNull Uri uri) {
        if (getContext() != null) {
            FileInfo.fromUri(getContext(), uri, SendbirdUIKit.shouldUseImageCompression(), new OnResultHandler<FileInfo>() {
                @Override
                public void onResult(@NonNull FileInfo info) {
                    ChannelFragment.this.mediaUri = null;
                    final FileMessageParams params = info.toFileParams();
                    final CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
                    if (customHandler != null) {
                        customHandler.onBeforeSendFileMessage(params);
                    }
                    onBeforeSendFileMessage(params);
                    if (targetMessage != null && SendbirdUIKit.getReplyType() == ReplyType.QUOTE_REPLY) {
                        params.setParentMessageId(targetMessage.getMessageId());
                        params.setReplyToChannel(true);
                    }
                    getViewModel().sendFileMessage(params, info);
                }

                @Override
                public void onError(@Nullable SendBirdException e) {
                    Logger.w(e);
                    toastError(R.string.sb_text_error_send_message);
                    ChannelFragment.this.mediaUri = null;
                }
            });
        }
    }

    /**
     * Updates a <code>UserMessage</code> that was previously sent in the channel.
     *
     * @param messageId The ID of the message. This must be a message that exists in the channel's history,
     *                  or an error will be returned.
     * @param params    Params of a message. Refer to {@link UserMessageParams}.
     * @since 1.0.4
     */
    protected void updateUserMessage(long messageId, @NonNull UserMessageParams params) {
        CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeUpdateUserMessage(params);
        }
        onBeforeUpdateUserMessage(params);
        getViewModel().updateUserMessage(messageId, params, e -> {
            if (e != null) toastError(R.string.sb_text_error_update_user_message);
        });
    }

    /**
     * Delete a message
     *
     * @param message Message to delete.
     * @since 1.0.4
     */
    protected void deleteMessage(@NonNull BaseMessage message) {
        getViewModel().deleteMessage(message, e -> {
            if (e != null) toastError(R.string.sb_text_error_delete_message);
        });
    }

    /**
     * Resends a failed message.
     *
     * @param message Failed message to resend.
     */
    protected void resendMessage(@NonNull BaseMessage message) {
        if (message.isResendable()) {
            getViewModel().resendMessage(message, e -> {
                if (e != null) toastError(R.string.sb_text_error_resend_message);
            });
        } else {
            toastError(R.string.sb_text_error_not_possible_resend_message);
        }
    }

    /**
     * Download {@link FileMessage} into external storage.
     * It needs to have a permission.
     * If current application needs permission, the request of permission will call automatically.
     * After permission is granted, the download will be also called automatically.
     *
     * @param message A file message to download contents.
     * @since 2.2.3
     */
    protected void saveFileMessage(@NonNull FileMessage message) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            download(message);
        } else {
            checkPermission(PERMISSION_REQUEST_STORAGE, new PermissionFragment.IPermissionHandler() {
                @Override
                @NonNull
                public String[] getPermissions(int requestCode) {
                    return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE};
                }

                @Override
                public void onPermissionGranted(int requestCode) {
                    download(message);
                }
            });
        }
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * @since 3.0.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
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

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendbirdUIKit.getDefaultThemeMode());
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
            this.bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            this.bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets arguments to this fragment.
         *
         * @param args the arguments supplied when the fragment was instantiated.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
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
         */
        @NonNull
        public Builder setUseTypingIndicator(boolean useTypingIndicator) {
            bundle.putBoolean(StringSet.KEY_USE_TYPING_INDICATOR, useTypingIndicator);
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.1
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
         * @since 2.1.0
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
         * @since 2.1.0
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
         * @since 2.0.1
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
         * @since 2.1.0
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
         * @since 2.1.0
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
         * @since 2.1.2
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
         * @since 3.0.0
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
         * @since 3.0.0
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
         * Sets the click listener on the item of message list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
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
         * @since 3.0.0
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
         * @since 3.0.0
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
         * @since 3.0.0
         */
        @NonNull
        public Builder setOnQuoteReplyMessageLongClickListener(@NonNull OnItemLongClickListener<BaseMessage> quoteReplyMessageLongClickListener) {
            this.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the input.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
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
         * @since 1.0.5
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
         * @since 1.1.0
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
         * @since 1.1.0
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
         * @since 1.1.0
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
         * @since 1.2.1
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
         * @since 3.0.0
         */
        @NonNull
        public Builder setOnMessageProfileClickListener(@NonNull OnItemClickListener<BaseMessage> profileClickListener) {
            this.messageProfileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the profile of message.
         *
         * @param messageProfileLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
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
         * @since 1.2.2
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
         * @since 2.0.0
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
         * @since 1.2.5
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
         * @since 2.0.2
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
         * @since 2.1.0
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
         * @since 2.0.2
         */
        @NonNull
        public Builder setEmptyText(@StringRes int resId) {
            bundle.putInt(StringSet.KEY_EMPTY_TEXT_RES_ID, resId);
            return this;
        }

        /**
         * Sets the listener invoked when a text of message input is edited.
         *
         * @param editModeTextChangedListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
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
         * @since 2.1.0
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
         * @since 2.1.0
         */
        @NonNull
        public Builder setOnInputTextChangedListener(@NonNull OnInputTextChangedListener inputTextChangedListener) {
            this.inputTextChangedListener = inputTextChangedListener;
            return this;
        }

        /**
         * Sets the information of the message to highlight.
         *
         * @param highlightMessageInfo The information of the message to highlight.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        @NonNull
        public Builder setHighlightMessageInfo(@NonNull HighlightMessageInfo highlightMessageInfo) {
            bundle.putParcelable(StringSet.KEY_HIGHLIGHT_MESSAGE_INFO, highlightMessageInfo);
            return this;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startTimemillis The timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
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
         * @since 2.2.4
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
         * @since 3.0.0
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
         * @since 3.0.0
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
         * Sets the UI configuration of searched text.
         *
         * @param searchedTextUIConfig the UI configuration of searched text.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        @NonNull
        public Builder setSearchedTextUIConfig(@NonNull TextUIConfig searchedTextUIConfig) {
            bundle.putParcelable(StringSet.KEY_SEARCHED_TEXT_UI_CONFIG, searchedTextUIConfig);
            return this;
        }

        /**
         * Sets the UI configuration of edited text mark.
         *
         * @param configSentFromMe       the UI configuration of edited text mark in the message that was sent from me.
         * @param configSentFromOthers   the UI configuration of edited text mark in the message that was sent from others.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
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
         * Creates an {@link ChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelFragment} applied to the {@link Bundle}.
         */
        @NonNull
        public ChannelFragment build() {
            final ChannelFragment fragment = new ChannelFragment();
            fragment.setArguments(bundle);
            fragment.headerLeftButtonClickListener = headerLeftButtonClickListener;
            fragment.headerRightButtonClickListener = headerRightButtonClickListener;
            fragment.messageClickListener = messageClickListener;
            fragment.messageLongClickListener = messageLongClickListener;
            fragment.inputLeftButtonClickListener = inputLeftButtonListener;
            fragment.emojiReactionClickListener = emojiReactionClickListener;
            fragment.emojiReactionLongClickListener = emojiReactionLongClickListener;
            fragment.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
            fragment.messageProfileClickListener = messageProfileClickListener;
            fragment.messageProfileLongClickListener = messageProfileLongClickListener;
            fragment.loadingDialogHandler = loadingDialogHandler;
            fragment.inputTextChangedListener = inputTextChangedListener;
            fragment.editModeTextChangedListener = editModeTextChangedListener;
            fragment.quoteReplyMessageClickListener = quoteReplyMessageClickListener;
            fragment.quoteReplyMessageLongClickListener = quoteReplyMessageLongClickListener;
            fragment.suggestedMentionListAdapter = suggestedMentionListAdapter;
            fragment.adapter = adapter;
            fragment.params = params;
            return fragment;
        }
    }
}
