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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.databinding.DataBindingUtil;
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
import com.sendbird.android.MessagePayloadFilter;
import com.sendbird.android.Reaction;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.handlers.GroupChannelContext;
import com.sendbird.android.handlers.MessageCollectionHandler;
import com.sendbird.android.handlers.MessageContext;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelSettingsActivity;
import com.sendbird.uikit.activities.PhotoViewActivity;
import com.sendbird.uikit.activities.adapter.MessageListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.MessageLoadState;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentChannelBinding;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnEmojiReactionClickListener;
import com.sendbird.uikit.interfaces.OnEmojiReactionLongClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemLongClickListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.HighlightMessageInfo;
import com.sendbird.uikit.model.TimelineMessage;
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
import com.sendbird.uikit.widgets.MessageInputView;
import com.sendbird.uikit.widgets.PagerRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment displaying the list of messages in the channel.
 */
public class ChannelFragment extends BaseGroupChannelFragment implements OnIdentifiableItemClickListener<BaseMessage>,
        OnIdentifiableItemLongClickListener<BaseMessage>,
        LoadingDialogHandler {

    private static final int CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 2002;
    private static final int PICK_FILE_PERMISSIONS_REQUEST_CODE = 2003;
    private static final int GROUP_CHANNEL_SETTINGS_REQUEST_CODE = 2004;
    private static final int PERMISSION_REQUEST_ALL = 2005;
    private static final int PERMISSION_REQUEST_STORAGE = 2006;

    private SbFragmentChannelBinding binding;
    private ChannelViewModel viewModel;
    private MessageListAdapter adapter;
    private String inputHint;
    private boolean anchorDialogShowing = false;

    private final AtomicInteger tooltipMessageCount = new AtomicInteger();
    private Uri mediaUri;

    @Nullable
    private BaseMessage targetMessage;

    private View.OnClickListener headerLeftButtonListener;
    private View.OnClickListener headerRightButtonListener;
    private OnItemClickListener<BaseMessage> profileClickListener;
    private OnItemClickListener<BaseMessage> itemClickListener;
    private OnItemLongClickListener<BaseMessage> itemLongClickListener;
    private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
    private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;
    private View.OnClickListener inputLeftButtonListener;
    private MessageListParams params;

    private OnEmojiReactionClickListener emojiReactionClickListener;
    private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
    private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
    private LoadingDialogHandler loadingDialogHandler;
    private OnInputTextChangedListener inputTextChangedListener;
    private OnInputTextChangedListener editModeTextChangedListener;
    final AtomicBoolean isInitCallFinished = new AtomicBoolean(false);
    final AtomicBoolean shouldAnimate = new AtomicBoolean(false);
    private String headerTitle = null;

    private final ReplyType replyType = SendBirdUIKit.getReplyType();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> ChannelFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID, SendBirdUIKit.getDefaultThemeMode().getResId());
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }

        if (loadingDialogHandler == null) {
            loadingDialogHandler = this;
        }
    }

    @Override
    public void onDestroy() {
        Logger.i(">> ChannelFragment::onDestroy()");
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
        if (!isInitCallFinished.get()) {
            loadingDialogHandler.shouldDismissLoadingDialog();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_channel, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeaderOnCreated();
        loadingDialogHandler.shouldShowLoadingDialog();
    }

    @Override
    protected void onReadyFailure() {
        toastError(R.string.sb_text_error_connect_server);
        loadingDialogHandler.shouldDismissLoadingDialog();
    }

    @Override
    protected void onConfigure() {
    }

    @Override
    protected void onDrawPage() {
        Logger.i(">> ChannelFragment::onDrawPage() - %s", Logger.getCallerTraceInfo(ChannelFragment.class));
        params = this.params == null ? new MessageListParams() : this.params;
        params.setReverse(true);
        if (replyType == ReplyType.QUOTE_REPLY) {
            params.setReplyTypeFilter(com.sendbird.android.ReplyTypeFilter.ONLY_REPLY_TO_CHANNEL);
            params.setMessagePayloadFilter(new MessagePayloadFilter.Builder()
                    .setIncludeParentMessageInfo(true)
                    .setIncludeThreadInfo(true)
                    .setIncludeReactions(ReactionUtils.useReaction(channel))
                    .build());
        } else {
            params.setReplyTypeFilter(com.sendbird.android.ReplyTypeFilter.NONE);
            params.setMessagePayloadFilter(new MessagePayloadFilter.Builder()
                    .setIncludeThreadInfo(true)
                    .setIncludeReactions(ReactionUtils.useReaction(channel))
                    .build());
        }

        viewModel = createViewModel(channel);
        initHeaderOnReady(channel);
        initMessageList(channel);
        initMessageInput();
        drawChannel(channel);
    }

    private ChannelViewModel createViewModel(GroupChannel channel) {
        return new ViewModelProvider(getActivity(), new ViewModelFactory(channel)).get(channel.getUrl(), ChannelViewModel.class);
    }

    private void drawChannel(GroupChannel channel) {
        Logger.dev("++ drawChannel()");
        if (isActive()) {
            if (headerTitle == null) {
                binding.chvChannelHeader.getTitleTextView().setText(ChannelUtils.makeTitleText(getContext(), channel));
            }
            ChannelUtils.loadChannelCover(binding.chvChannelHeader.getProfileView(), channel);
            binding.tvInformation.setVisibility(channel.isFrozen() ? View.VISIBLE : View.GONE);
            binding.tvInformation.setText(R.string.sb_text_information_channel_frozen);
            drawMessageInput(channel);
        }
    }

    private void drawMessageInput(GroupChannel channel) {
        boolean isOperator = channel.getMyRole() == Member.Role.OPERATOR;
        boolean isBroadcastChannel = channel.isBroadcast();
        boolean isMuted = channel.getMyMutedState() == Member.MutedState.MUTED;
        boolean isFrozen = channel.isFrozen() && !isOperator;
        if (isBroadcastChannel) {
            binding.vgInputBox.setVisibility(isOperator ? View.VISIBLE : View.GONE);
        } else if (isMuted || isFrozen) {
            clearInput();
        }

        binding.vgInputBox.setEnabled(!isMuted && !isFrozen);
        // set hint
        setInputTextHint(isMuted, isFrozen);
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        int headerRightButtonIconResId = R.drawable.icon_info;
        ColorStateList headerLeftButtonIconTint = null;
        ColorStateList headerRightButtonIconTint = null;

        if (args != null) {
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerRightButtonIconResId = args.getInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, R.drawable.icon_info);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
            headerRightButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT);
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, null);
        }

        binding.chvChannelHeader.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        binding.chvChannelHeader.setUseLeftImageButton(useHeaderLeftButton);
        binding.chvChannelHeader.setUseRightButton(useHeaderRightButton);

        if (headerTitle != null) {
            binding.chvChannelHeader.getTitleTextView().setText(headerTitle);
        }

        binding.chvChannelHeader.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.chvChannelHeader.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.chvChannelHeader.setRightImageButtonResource(headerRightButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID)) {
            binding.chvChannelHeader.setRightImageButtonTint(headerRightButtonIconTint);
        }
        binding.chvChannelHeader.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady(GroupChannel channel) {
        Bundle args = getArguments();
        boolean useTypingIndicator = true;

        if (args != null) {
            useTypingIndicator = args.getBoolean(StringSet.KEY_USE_TYPING_INDICATOR, true);
        }

        if (headerLeftButtonListener != null) {
            binding.chvChannelHeader.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

        if (headerRightButtonListener != null) {
            binding.chvChannelHeader.setRightImageButtonClickListener(headerRightButtonListener);
        } else {
            binding.chvChannelHeader.setRightImageButtonClickListener(v -> {
                Intent intent = ChannelSettingsActivity.newIntent(getContext(), channel.getUrl());
                startActivityForResult(intent, GROUP_CHANNEL_SETTINGS_REQUEST_CODE);
            });
        }

        binding.chvChannelHeader.getProfileView().setVisibility(View.VISIBLE);
        viewModel.isChannelChanged().observe(this, this::drawChannel);

        if (useTypingIndicator) {
            viewModel.getTypingMembers().observe(this, typingMembers -> {
                if (typingMembers == null) {
                    binding.chvChannelHeader.getDescriptionTextView().setVisibility(View.GONE);
                } else {
                    binding.chvChannelHeader.getDescriptionTextView().setVisibility(View.VISIBLE);
                    binding.chvChannelHeader.getDescriptionTextView().setText(ChannelUtils.makeTypingText(getContext(), typingMembers));
                }
            });
        }
    }

    private void initMessageList(GroupChannel channel) {
        final Bundle args = getArguments();
        boolean useMessageGroupUI = args == null || args.getBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, true);
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendBirdUIKit.shouldUseDefaultUserProfile());
        final long startingPoint = args != null ? args.getLong(StringSet.KEY_STARTING_POINT, Long.MAX_VALUE) : Long.MAX_VALUE;
        final HighlightMessageInfo info = (args != null && args.containsKey(StringSet.KEY_HIGHLIGHT_MESSAGE_INFO)) ? args.getParcelable(StringSet.KEY_HIGHLIGHT_MESSAGE_INFO) : null;

        if (adapter == null) {
            adapter = new MessageListAdapter(channel, useMessageGroupUI);
        }
        adapter.setChannel(channel);
        adapter.setHighlightInfo(info);

        if (itemClickListener != null) {
            adapter.setOnItemClickListener(itemClickListener);
        }

        if (itemLongClickListener != null) {
            adapter.setOnItemLongClickListener(itemLongClickListener);
        }

        if (listItemClickListener == null) {
            listItemClickListener = this;
        }

        if (listItemLongClickListener == null) {
            listItemLongClickListener = this;
        }

        if (profileClickListener != null && useUserProfile) {
            adapter.setOnProfileClickListener(profileClickListener);
        }

        adapter.setOnListItemClickListener(listItemClickListener);
        adapter.setOnListItemLongClickListener(listItemLongClickListener);

        if (ReactionUtils.useReaction(channel)) {
            if (emojiReactionClickListener == null) {
                emojiReactionClickListener = (view, position, message, reactionKey) -> {
                    viewModel.toggleReaction(view, message, reactionKey);
                };
            }
            adapter.setEmojiReactionClickListener(emojiReactionClickListener);

            if (emojiReactionLongClickListener == null) {
                emojiReactionLongClickListener = (view, position, message, reactionKey) -> {
                    if (message == null || getContext() == null || getFragmentManager() == null) {
                        return;
                    }

                    EmojiReactionUserListView emojiReactionUserListView = new EmojiReactionUserListView(getContext());
                    emojiReactionUserListView.setEmojiReactionUserData(ChannelFragment.this,
                            position,
                            message.getReactions(),
                            getReactionUserInfo(message.getReactions()));
                    hideKeyboard();
                    DialogUtils.buildContentView(emojiReactionUserListView).showSingle(getFragmentManager());
                };
            }
            adapter.setEmojiReactionLongClickListener(emojiReactionLongClickListener);

            if (emojiReactionMoreButtonClickListener == null) {
                emojiReactionMoreButtonClickListener = (view, position, message) -> {
                    showEmojiListDialog(message);
                };
            }
            adapter.setEmojiReactionMoreButtonClickListener(emojiReactionMoreButtonClickListener);
        }

        final PagerRecyclerView recyclerView = binding.mrvMessageList.getRecyclerView();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPager(viewModel);
        recyclerView.setThreshold(5);
        recyclerView.setItemAnimator(new ItemAnimator());
        recyclerView.setOnScrollEndDetectListener(this::onScrollEndReaches);

        final LinearLayoutManager layoutManager = recyclerView.getLayoutManager();
        layoutManager.setReverseLayout(params == null || params.shouldReverse());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (needToShowScrollBottomButton()) {
                    binding.mrvMessageList.showScrollBottomButton();
                }
            }
        });

        binding.mrvMessageList.getTooltipView().setOnClickListener(v -> scrollToBottom());
        binding.mrvMessageList.getScrollBottomView().setOnClickListener(v -> {
            recyclerView.stopScroll();
            scrollToBottom();
        });

        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            int emptyIconResId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            binding.statusFrame.setEmptyIcon(emptyIconResId);
            ColorStateList emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            binding.statusFrame.setIconTint(emptyIconTint);
        }
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_TEXT_RES_ID)) {
            int emptyTextResId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_channel_message_empty);
            binding.statusFrame.setEmptyText(emptyTextResId);
        }
        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);

        viewModel.getMessageList().observe(this, receivedMessageData -> {
            final List<BaseMessage> messageList = receivedMessageData.getMessages();
            Logger.d("++ result messageList size : %s, source = %s", messageList.size(), receivedMessageData.getTraceName());

            if (messageList.isEmpty()) return;

            final String traceName = receivedMessageData.getTraceName();
            // The callback coming from setItems is worked asynchronously. So `isInitCallFinished` flag has to mark in advance.
            final boolean isInitCallEnded = isInitCallFinished.get();
            adapter.setItems(channel, messageList, messages -> {
                if (traceName != null && isActive()) {
                    Logger.d("++ Message action : %s", traceName);
                    switch (traceName) {
                        case StringSet.ACTION_FAILED_MESSAGE_ADDED:
                        case StringSet.ACTION_PENDING_MESSAGE_ADDED:
                            final BaseMessage message = adapter.getItem(0);
                            if (message.getSendingStatus() != BaseMessage.SendingStatus.SUCCEEDED) {
                                Logger.d("++ pending message added. message=%s", message);
                                scrollToBottom();
                            }
                            break;
                        case StringSet.EVENT_MESSAGE_SENT:
                            int firstVisibleItemPosition = recyclerView.findFirstVisibleItemPosition();
                            Logger.d("++ firstVisibleItemPosition=%s, message=%s", firstVisibleItemPosition, messageList.get(0).getMessage());
                            if (firstVisibleItemPosition <= 0) {
                                scrollToBottom();
                            }

                            final BaseMessage latestMessage = viewModel.getLatestMessage();
                            if (latestMessage instanceof FileMessage && getContext() != null) {
                                // Download from files already sent for quick image loading.
                                FileDownloader.downloadThumbnail(getContext(), (FileMessage) latestMessage);
                            }
                            break;
                        case StringSet.EVENT_MESSAGE_RECEIVED:
                            firstVisibleItemPosition = recyclerView.findFirstVisibleItemPosition();
                            Logger.d("++ firstVisibleItemPosition=%s, message=%s", firstVisibleItemPosition, messageList.get(0).getMessage());

                            if (anchorDialogShowing || firstVisibleItemPosition > 0) {
                                binding.mrvMessageList.showNewMessageTooltip(getTooltipMessage(tooltipMessageCount.incrementAndGet()));
                            } else if (!viewModel.hasNext()) {
                                if (firstVisibleItemPosition == 0) {
                                    scrollToBottom();
                                }
                            }
                            break;
                        case StringSet.MESSAGE_CHANGELOG:
                        case StringSet.MESSAGE_FILL:
                            if (recyclerView.findFirstVisibleItemPosition() == 0 && !anchorDialogShowing && !viewModel.hasNext()) {
                                scrollToBottom();
                            }
                            break;
                    }
                }
                if (!isInitCallEnded) {
                    int scrollPosition = scrollToStartingPointIfNeeded();
                    if (scrollPosition > 0) {
                        recyclerView.post(() -> {
                            if (needToShowScrollBottomButton()) {
                                binding.mrvMessageList.showScrollBottomButton();
                            } else {
                                binding.mrvMessageList.hideScrollBottomButton();
                            }
                        });
                    }
                }
            });
        });
        viewModel.getErrorToast().observe(this, this::toastError);

        viewModel.getMessageLoadState().observe(this, state -> {
            if (state == MessageLoadState.LOAD_ENDED) {
                if (!isInitCallFinished.getAndSet(true)) {
                    if (shouldAnimate.getAndSet(false)) {
                        final List<BaseMessage> founded = viewModel.getMessagesByCreatedAt(viewModel.getStartingPoint());
                        Logger.i("++ founded=%s, startingPoint=%s", founded, viewModel.getStartingPoint());
                        if (founded != null && founded.size() == 1) {
                            final long parentMessageId = founded.get(0).getMessageId();
                            Logger.i("++ founded parent message id = %s", parentMessageId);
                            recyclerView.postDelayed(() -> {
                                startAnimationForReplyMessage(parentMessageId);
                                if (needToShowScrollBottomButton()) {
                                    binding.mrvMessageList.showScrollBottomButton();
                                } else {
                                    binding.mrvMessageList.hideScrollBottomButton();
                                }
                            }, 200);
                        } else {
                            toastError(R.string.sb_text_error_original_message_not_found);
                        }
                    }
                }
            }
        });
        viewModel.setMessageCollectionHandler(new MessageCollectionHandler() {
            @Override
            public void onMessagesAdded(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
            }

            @Override
            public void onMessagesUpdated(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
            }

            @Override
            public void onMessagesDeleted(@NonNull MessageContext context, @NonNull GroupChannel channel, @NonNull List<BaseMessage> messages) {
                if (MessageInputView.Mode.EDIT == binding.vgInputBox.getInputMode() &&
                        targetMessage != null && messages.contains(targetMessage)) {
                    clearInput();
                } else if (MessageInputView.Mode.QUOTE_REPLY == binding.vgInputBox.getInputMode() &&
                        targetMessage != null && messages.contains(targetMessage)) {
                    binding.vgInputBox.setInputMode(MessageInputView.Mode.DEFAULT);
                }
            }

            @Override
            public void onChannelUpdated(@NonNull GroupChannelContext context, @NonNull GroupChannel channel) {
            }

            @Override
            public void onChannelDeleted(@NonNull GroupChannelContext context, @NonNull String channelUrl) {
                finish();
            }

            @Override
            public void onHugeGapDetected() {
                Logger.d(">> onHugeGapDetected()");
                if (viewModel.getStartingPoint() == 0 || viewModel.getStartingPoint() == Long.MAX_VALUE) {
                    loadInitial(viewModel.getStartingPoint());
                } else {
                    int position = layoutManager.findFirstVisibleItemPosition();
                    if (position >= 0) {
                        final BaseMessage message = adapter.getItem(position);
                        Logger.d("++ founded first visible message = %s", message);
                        loadInitial(message != null ? message.getCreatedAt() : startingPoint);
                    }
                }
            }
        });
        loadInitial(startingPoint);
    }

    private boolean needToShowScrollBottomButton() {
        final LinearLayoutManager layoutManager = binding.mrvMessageList.getRecyclerView().getLayoutManager();
        return layoutManager != null && layoutManager.findFirstVisibleItemPosition() > 0;
    }

    private synchronized void loadInitial(long startingPoint) {
        if (viewModel != null) {
            isInitCallFinished.set(false);
            viewModel.loadInitial(startingPoint, params);
        }
    }

    private int scrollToStartingPointIfNeeded() {
        int selectionPosition = 0;
        if (isActive()) {
            loadingDialogHandler.shouldDismissLoadingDialog();
            long startingPoint = viewModel.getStartingPoint();

            if (startingPoint >= 0) {
                int offset = binding.mrvMessageList.getRecyclerView().getHeight() / 2;
                selectionPosition = scrollToFoundPosition(startingPoint, offset);
            }
        }
        return selectionPosition;
    }

    public int scrollToFoundPosition(long createdAt, int offset) {
        Logger.d("_________ scrollToFoundPosition( %s )", createdAt);
        if (viewModel == null) return 0;
        final List<BaseMessage> messageList = adapter.getItems();
        if (messageList == null || messageList.isEmpty()) {
            Logger.d("_________ return scrollToFoundPosition");
            return 0;
        }

        final List<BaseMessage> list = new ArrayList<>(messageList);
        final int size = list.size();
        final LinearLayoutManager layoutManager = binding.mrvMessageList.getRecyclerView().getLayoutManager();
        if (layoutManager == null) {
            return 0;
        }

        final long firstMessageTs = list.get(size - 1).getCreatedAt();
        final long lastMessageTs = list.get(0).getCreatedAt();
        final long maxMessageTs = Math.max(lastMessageTs, firstMessageTs);
        final long minMessageTs = Math.min(lastMessageTs, firstMessageTs);
        int position = 0;
        if (createdAt >= minMessageTs && createdAt <= maxMessageTs) {
            for (int i = size - 1; i >= 0; i--) {
                BaseMessage message = list.get(i);
                if (message instanceof TimelineMessage) continue;

                long ct = message.getCreatedAt();
                //Logger.d("_________ [%s] : %s, [%s]", i, ct, message.getMessage());
                if (createdAt <= ct) {
                    Logger.d("_________ found message=%s, i=%s", message.getMessage(), i);
                    position = i;
                    break;
                }
            }
        } else if (createdAt >= maxMessageTs) {
            position = lastMessageTs == maxMessageTs ? 0 : (size - 1);
        } else {
            position = firstMessageTs == minMessageTs ? (size - 1) : 0;
        }

        layoutManager.scrollToPositionWithOffset(position, offset);
        return position;
    }

    private void initMessageInput() {
        Bundle args = getArguments();

        inputHint = getResources().getString(R.string.sb_text_channel_input_text_hint);
        if (args != null) {
            if (args.containsKey(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID)) {
                int inputLeftButtonIconResId = args.getInt(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_add);
                binding.vgInputBox.setAddImageResource(inputLeftButtonIconResId);
                binding.vgInputBox.setAddImageButtonTint(args.getParcelable(StringSet.KEY_INPUT_LEFT_BUTTON_ICON_TINT));
            }

            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID)) {
                int inputRightButtonIconResId = args.getInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID, R.drawable.icon_send);
                binding.vgInputBox.setSendImageResource(inputRightButtonIconResId);
                binding.vgInputBox.setSendImageButtonTint(args.getParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT));
            }

            if (args.containsKey(StringSet.KEY_INPUT_HINT)) {
                inputHint = args.getString(StringSet.KEY_INPUT_HINT, getString(R.string.sb_text_channel_input_text_hint));
                binding.vgInputBox.setInputTextHint(inputHint);
            }

            if (args.containsKey(StringSet.KEY_INPUT_TEXT)) {
                String inputText = args.getString(StringSet.KEY_INPUT_TEXT, "");
                binding.vgInputBox.setInputText(inputText);
            }

            if (args.containsKey(StringSet.KEY_KEYBOARD_DISPLAY_TYPE)) {
                KeyboardDisplayType displayType = (KeyboardDisplayType) args.getSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE);
                if (displayType != null && getFragmentManager() != null) {
                    binding.vgInputBox.setKeyboardDisplayType(getFragmentManager(), displayType);
                }
            }

            if (args.containsKey(StringSet.KEY_USE_INPUT_LEFT_BUTTON)) {
                boolean useInputLeftButton = args.getBoolean(StringSet.KEY_USE_INPUT_LEFT_BUTTON, true);
                if (useInputLeftButton) {
                    binding.vgInputBox.setAddButtonVisibility(View.VISIBLE);
                } else {
                    binding.vgInputBox.setAddButtonVisibility(View.GONE);
                }
            }

            if (args.containsKey(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS)) {
                boolean always = args.getBoolean(StringSet.KEY_INPUT_RIGHT_BUTTON_SHOW_ALWAYS, false);
                if (always) binding.vgInputBox.setSendButtonVisibility(View.VISIBLE);
                binding.vgInputBox.showSendButtonAlways(always);
            }
        }

        binding.vgInputBox.setOnSendClickListener(this::sendMessage);
        binding.vgInputBox.setOnAddClickListener(inputLeftButtonListener == null ? v -> showMediaSelectDialog() : inputLeftButtonListener);
        binding.vgInputBox.setOnEditCancelClickListener(v -> clearInput());
        binding.vgInputBox.setOnEditSaveClickListener(v -> {
            String text = getEditTextString();
            if (!TextUtils.isEmpty(text)) {
                UserMessageParams params = new UserMessageParams(text);
                if (null != targetMessage) {
                    updateUserMessage(targetMessage.getMessageId(), params);
                } else {
                    Logger.d("Target message for update is missing");
                }
            }
            clearInput();
        });
        binding.vgInputBox.setOnInputTextChangedListener((s, start, before, count) -> {
            if (inputTextChangedListener != null) {
                inputTextChangedListener.onInputTextChanged(s, start, before, count);
            }
            viewModel.setTyping(s.length() > 0);
        });
        binding.vgInputBox.setOnEditModeTextChangedListener((s, start, before, count) -> {
            if (editModeTextChangedListener != null) {
                editModeTextChangedListener.onInputTextChanged(s, start, before, count);
            }
            viewModel.setTyping(s.length() > 0);
        });
        binding.vgInputBox.setOnReplyCloseClickListener(v -> clearInput());
        binding.vgInputBox.setOnInputModeChangedListener((before, current) -> {
            boolean isOperator = channel.getMyRole() == Member.Role.OPERATOR;
            boolean isMuted = channel.getMyMutedState() == Member.MutedState.MUTED;
            boolean isFrozen = channel.isFrozen() && !isOperator;

            binding.vgInputBox.setEnabled(!isMuted && !isFrozen);
            // set hint
            setInputTextHint(isMuted, isFrozen);

            if (MessageInputView.Mode.EDIT == current) {
                if (targetMessage != null) binding.vgInputBox.setInputText(targetMessage.getMessage());
                binding.vgInputBox.showKeyboard();
            } else if (MessageInputView.Mode.QUOTE_REPLY == current) {
                if (targetMessage != null) binding.vgInputBox.drawMessageToReply(targetMessage);
                binding.vgInputBox.showKeyboard();
            } else {
                targetMessage = null;
            }
        });
    }

    private void onScrollEndReaches(PagerRecyclerView.ScrollDirection direction) {
        if (!viewModel.hasNext() && direction == PagerRecyclerView.ScrollDirection.Bottom) {
            binding.mrvMessageList.hideNewMessageTooltip();
            tooltipMessageCount.set(0);

            binding.mrvMessageList.hideScrollBottomButton();
        }
    }

    private void scrollToBottom() {
        if (binding == null || viewModel == null) return;
        if (viewModel.hasNext()) {
            loadInitial(Long.MAX_VALUE);
        } else {
            binding.mrvMessageList.getRecyclerView().scrollToPosition(0);
        }
        onScrollEndReaches(PagerRecyclerView.ScrollDirection.Bottom);
    }

    /**
     * It will be called when the input message's left button is clicked.
     * The default behavior is showing the menu, like, taking camera, gallery, and file.
     *
     * @since 2.0.1
     */
    protected void showMediaSelectDialog() {
        if (getContext() == null || getFragmentManager() == null) return;
        DialogListItem[] items = {
                new DialogListItem(R.string.sb_text_channel_input_camera, R.drawable.icon_camera),
                new DialogListItem(R.string.sb_text_channel_input_gallery, R.drawable.icon_photo),
                new DialogListItem(R.string.sb_text_channel_input_document, R.drawable.icon_document)
        };
        hideKeyboard();
        DialogUtils.buildItemsBottom(items, (view, position, key) -> {
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
        }).showSingle(getFragmentManager());
    }

    /**
     * Call taking camera application.
     *
     * @since 2.0.1
     */
    public void takeCamera() {
        SendBird.setAutoBackgroundDetection(false);
        checkPermission(PERMISSION_REQUEST_ALL, new IPermissionHandler() {
            @Override
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
                mediaUri = FileUtils.createPictureImageUri(getContext());
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
        checkPermission(PERMISSION_REQUEST_STORAGE, new IPermissionHandler() {
            @Override
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
        checkPermission(PERMISSION_REQUEST_STORAGE, new IPermissionHandler() {
            @Override
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

        if (requestCode == GROUP_CHANNEL_SETTINGS_REQUEST_CODE) {
            drawChannel(channel);
            return;
        }

        switch (requestCode) {
            case CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE:
                break;
            case PICK_IMAGE_PERMISSIONS_REQUEST_CODE:
            case PICK_FILE_PERMISSIONS_REQUEST_CODE:
                if (data != null) {
                    this.mediaUri = data.getData();
                }
                break;
        }

        if (this.mediaUri != null && isActive()) {
            sendFileMessage(mediaUri);
        }
    }

    private void sendMessage(View view) {
        Logger.dev("++ onClick()");
        if (view.getId() == binding.vgInputBox.getBinding().ibtnSend.getId()) {
            String text = getEditTextString();
            if (!TextUtils.isEmpty(text)) {
                UserMessageParams params = new UserMessageParams(text);
                sendUserMessage(params);
            }
        }
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
        if (viewModel != null) {
            CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
            if (cutsomHandler != null) {
                cutsomHandler.onBeforeSendUserMessage(params);
            }
            onBeforeSendUserMessage(params);
            if (targetMessage != null && replyType == ReplyType.QUOTE_REPLY) {
                params.setParentMessageId(targetMessage.getMessageId());
                params.setReplyToChannel(true);
            }
            viewModel.sendUserMessage(params);
            clearInput();
            scrollToBottom();
        }
    }

    /**
     * Sends a file with given file information.
     *
     * @param uri A file Uri
     * @since 1.0.4
     */
    protected void sendFileMessage(@NonNull Uri uri) {
        if (viewModel != null && getContext() != null) {
            FileInfo.fromUri(getContext(), uri, SendBirdUIKit.shouldUseImageCompression(), new OnResultHandler<FileInfo>() {
                @Override
                public void onResult(FileInfo info) {
                    FileMessageParams params = info.toFileParams();
                    CustomParamsHandler customHandler = SendBirdUIKit.getCustomParamsHandler();
                    if (customHandler != null) {
                        customHandler.onBeforeSendFileMessage(params);
                    }
                    onBeforeSendFileMessage(params);
                    if (targetMessage != null && replyType == ReplyType.QUOTE_REPLY) {
                        params.setParentMessageId(targetMessage.getMessageId());
                        params.setReplyToChannel(true);
                    }
                    viewModel.sendFileMessage(params, info);
                    clearInput();
                    scrollToBottom();
                }

                @Override
                public void onError(SendBirdException e) {
                    Logger.w(e);
                    toastError(R.string.sb_text_error_send_message);
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
        if (viewModel != null) {
            CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
            if (cutsomHandler != null) {
                cutsomHandler.onBeforeUpdateUserMessage(params);
            }
            onBeforeUpdateUserMessage(params);
            viewModel.updateUserMessage(messageId, params);
        }
    }

    /**
     * Delete a message
     *
     * @param message Message to delete.
     * @since 1.0.4
     */
    protected void deleteMessage(@NonNull BaseMessage message) {
        if (viewModel != null) {
            viewModel.deleteMessage(message);
        }
    }

    /**
     * Resends a failed message.
     *
     * @param message Failed message to resend.
     */
    protected void resendMessage(@NonNull BaseMessage message) {
        if (viewModel != null) {
            if (message.isResendable()) {
                viewModel.resendMessage(message);
            } else {
                toastError(R.string.sb_text_error_not_possible_resend_message);
            }
        }
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        // Do nothing on the channel.
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    @Override
    public void shouldDismissLoadingDialog() {
    }

    /**
     * It will be called when the tooltip retrieve a text to show the information of new messages.
     *
     * @param count Number of newly added messages.
     * @return Text to show in tooltip.
     * @since 2.1.8
     */
    protected String getTooltipMessage(int count) {
        String result = "";
        if (count > 1) {
            result = String.format(Locale.US, getString(R.string.sb_text_channel_tooltip_with_count), count);
        } else if (count == 1) {
            result = String.format(Locale.US, getString(R.string.sb_text_channel_tooltip), count);
        }
        return result;
    }

    private String getEditTextString() {
        return binding.vgInputBox.getInputText();
    }

    private void showFile(@NonNull File file, @NonNull String mimeType) {
        TaskQueue.addTask(new JobResultTask<Intent>() {
            @Override
            public Intent call() {
                Uri uri = FileUtils.fileToUri(getContext(), file);
                return IntentUtils.getFileViewerIntent(uri, mimeType);
            }

            @Override
            public void onResultForUiThread(Intent intent, SendBirdException e) {
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

    private void startAnimationForReplyMessage(long targetMessageId) {
        if (!isActive()) return;
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.shake_quoted_message);
        adapter.startAnimation(animation, targetMessageId);
    }

    @Override
    public void onIdentifiableItemClick(View view, String identifier, int position, BaseMessage message) {
        Logger.d("++ ChannelFragment::onItemClicked(), clickableType=%s", identifier);
        final BaseMessage.SendingStatus status = message.getSendingStatus();
        if (status == BaseMessage.SendingStatus.PENDING) return;

        switch (identifier) {
            case StringSet.Chat:
                // ClickableViewType.Chat
                if (status == BaseMessage.SendingStatus.SUCCEEDED) {
                    MessageType type = MessageViewHolderFactory.getMessageType(message);
                    switch (type) {
                        case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                        case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                            startActivity(PhotoViewActivity.newIntent(getContext(), BaseChannel.ChannelType.GROUP, (FileMessage) message));
                            break;
                        case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                        case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                        case VIEW_TYPE_FILE_MESSAGE_ME:
                        case VIEW_TYPE_FILE_MESSAGE_OTHER:
                            FileMessage fileMessage = (FileMessage) message;
                            FileDownloader.downloadFile(getContext(), fileMessage, new OnResultHandler<File>() {
                                @Override
                                public void onResult(File file) {
                                    showFile(file, fileMessage.getType());
                                }

                                @Override
                                public void onError(SendBirdException e) {
                                    toastError(R.string.sb_text_error_download_file);
                                }
                            });
                            break;
                        default:
                    }
                } else {
                    if (MessageUtils.isMine(message) &&
                            (message instanceof UserMessage || message instanceof FileMessage)) {
                        resendMessage(message);
                    }
                }
                break;
            case StringSet.Profile:
                // ClickableViewType.Profile
                final Bundle args = getArguments();
                final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendBirdUIKit.shouldUseDefaultUserProfile());
                if (getContext() == null || getFragmentManager() == null || !useUserProfile) return;
                hideKeyboard();
                DialogUtils.buildUserProfile(getContext(), message.getSender(), true, null, null).showSingle(getFragmentManager());
                break;
            case StringSet.QuoteReply:
                // ClickableViewType.Reply
                final long createdAt = message.getParentMessage() == null ? 0L :
                        message.getParentMessage().getCreatedAt();
                if (createdAt > 0) {
                    if (viewModel.hasMessageById(message.getParentMessageId())) {
                        final int offset = binding.mrvMessageList.getRecyclerView().getHeight() / 2;
                        scrollToFoundPosition(createdAt, offset);

                        binding.mrvMessageList.postDelayed(() -> {
                            startAnimationForReplyMessage(message.getParentMessageId());
                            if (needToShowScrollBottomButton()) {
                                binding.mrvMessageList.showScrollBottomButton();
                            } else {
                                binding.mrvMessageList.hideScrollBottomButton();
                            }
                        }, 100);
                    } else {
                        shouldAnimate.set(true);
                        loadInitial(createdAt);
                    }
                } else {
                    toastError(R.string.sb_text_error_original_message_not_found);
                }
                break;
        }
    }

    @Override
    public void onIdentifiableItemLongClick(View itemView, String identifier, int position, BaseMessage message) {
        Logger.d("++ ChannelFragment::onItemLongClick(), clickableType=%s", identifier);
        if (identifier.equals(ClickableViewIdentifier.Chat.name())) {
            final BaseMessage.SendingStatus status = message.getSendingStatus();
            if (status == BaseMessage.SendingStatus.PENDING) return;

            MessageType type = MessageViewHolderFactory.getMessageType(message);
            DialogListItem copy = new DialogListItem(R.string.sb_text_channel_anchor_copy, R.drawable.icon_copy);
            DialogListItem edit = new DialogListItem(R.string.sb_text_channel_anchor_edit, R.drawable.icon_edit);
            DialogListItem save = new DialogListItem(R.string.sb_text_channel_anchor_save, R.drawable.icon_download);
            DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete, false, !MessageUtils.isDeletableMessage(message));
            DialogListItem reply = new DialogListItem(R.string.sb_text_channel_anchor_reply, R.drawable.icon_reply, false, MessageUtils.hasParentMessage(message));
            DialogListItem retry = new DialogListItem(R.string.sb_text_channel_anchor_retry, 0);
            DialogListItem deleteFailed = new DialogListItem(R.string.sb_text_channel_anchor_delete, 0);

            DialogListItem[] actions = null;
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
                if (!ReactionUtils.canSendReaction(viewModel.getChannel())) {
                    if (getContext() != null) {
                        MessageAnchorDialog messageAnchorDialog = new MessageAnchorDialog.Builder(itemView, binding.mrvMessageList, actions)
                                .setOnItemClickListener(createMessageActionListener(message))
                                .setOnDismissListener(() -> anchorDialogShowing = false)
                                .build();
                        messageAnchorDialog.show();
                        anchorDialogShowing = true;
                    }
                } else if (MessageUtils.isUnknownType(message)) {
                    if (getContext() == null || getFragmentManager() == null) return;
                    DialogUtils
                            .buildItemsBottom(actions, createMessageActionListener(message))
                            .showSingle(getFragmentManager());
                } else {
                    showEmojiActionsDialog(message, actions);
                }
            }
        }
    }

    private void showEmojiActionsDialog(BaseMessage message, DialogListItem[] actions) {
        if (message == null || actions == null || getContext() == null || getFragmentManager() == null) {
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

        EmojiListView emojiListView = new EmojiListView.Builder(getContext())
                .setEmojiList(emojiList)
                .setReactionList(message.getReactions())
                .setShowMoreButton(showMoreButton)
                .create();

        hideKeyboard();
        SendBirdDialogFragment sendBirdDialogFragment = DialogUtils.buildContentViewAndItems(
                emojiListView, actions, createMessageActionListener(message));

        emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
            if (sendBirdDialogFragment != null) {
                sendBirdDialogFragment.dismiss();
            }
            viewModel.toggleReaction(view, message, emojiKey);
        });

        emojiListView.setMoreButtonClickListener(v -> {
            showEmojiListDialog(message);
        });

        sendBirdDialogFragment.showSingle(getFragmentManager());
    }

    private void showEmojiListDialog(BaseMessage message) {
        if (getContext() == null || getFragmentManager() == null) {
            return;
        }

        EmojiListView emojiListView = new EmojiListView.Builder(getContext())
                .setEmojiList(EmojiManager.getInstance().getAllEmojis())
                .setReactionList(message.getReactions())
                .setShowMoreButton(false)
                .create();
        hideKeyboard();
        SendBirdDialogFragment sendBirdDialogFragment = DialogUtils.buildContentView(emojiListView);

        emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
            if (sendBirdDialogFragment != null) {
                sendBirdDialogFragment.dismiss();
            }

            viewModel.toggleReaction(view, message, emojiKey);
        });

        emojiListView.setMoreButtonClickListener(v -> {
            showEmojiListDialog(message);
        });

        sendBirdDialogFragment.showSingle(getFragmentManager());
    }

    private void clearInput() {
        binding.vgInputBox.setInputMode(MessageInputView.Mode.DEFAULT);
        binding.vgInputBox.setInputText("");
    }

    private void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    private OnItemClickListener<Integer> createMessageActionListener(BaseMessage message) {
        return (view, position, key) -> {
            if (key == R.string.sb_text_channel_anchor_copy) {
                copyTextToClipboard(message.getMessage());
            } else if (key == R.string.sb_text_channel_anchor_edit) {
                targetMessage = message;
                binding.vgInputBox.setInputMode(MessageInputView.Mode.EDIT);
            } else if (key == R.string.sb_text_channel_anchor_delete) {
                if (MessageUtils.isFailed(message)) {
                    Logger.dev("delete");
                    deleteMessage(message);
                } else {
                    showWarningDialog(message);
                }
            } else if (key == R.string.sb_text_channel_anchor_save) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    download((FileMessage) message);
                } else {
                    checkPermission(PERMISSION_REQUEST_STORAGE, new IPermissionHandler() {
                        @Override
                        public String[] getPermissions(int requestCode) {
                            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE};
                        }

                        @Override
                        public void onPermissionGranted(int requestCode) {
                            download((FileMessage) message);
                        }
                    });
                }
            } else if (key == R.string.sb_text_channel_anchor_reply) {
                this.targetMessage = message;
                binding.vgInputBox.setInputMode(MessageInputView.Mode.QUOTE_REPLY);
            } else if (key == R.string.sb_text_channel_anchor_retry) {
                resendMessage(message);
            }
        };
    }

    private void download(@NonNull FileMessage fileMessage) {
        toastSuccess(R.string.sb_text_toast_success_start_download_file);
        TaskQueue.addTask(new JobResultTask<Boolean>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public Boolean call() throws Exception {
                FileDownloader.getInstance().saveFile(getContext(), fileMessage.getUrl(),
                        fileMessage.getType(), fileMessage.getName());
                return true;
            }

            @Override
            public void onResultForUiThread(Boolean result, SendBirdException e) {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_download_file);
                    return;
                }
                toastSuccess(R.string.sb_text_toast_success_download_file);
            }
        });
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(StringSet.LABEL_COPY_TEXT, text);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
            ContextUtils.toastSuccess(getContext(), R.string.sb_text_toast_success_copy);
        } else {
            toastError(R.string.sb_text_error_copy_message);
        }
    }

    private void showWarningDialog(BaseMessage message) {
        if (getContext() == null || getFragmentManager() == null) return;
        DialogUtils.buildWarning(
                getString(R.string.sb_text_dialog_delete_message),
                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                getString(R.string.sb_text_button_delete),
                delete -> {
                    Logger.dev("delete");
                    deleteMessage(message);
                },
                getString(R.string.sb_text_button_cancel),
                cancel -> {
                    Logger.dev("cancel");
                })
                .showSingle(getFragmentManager());
    }

    private Map<Reaction, List<User>> getReactionUserInfo(List<Reaction> reactionList) {
        Map<Reaction, List<User>> result = new HashMap<>();
        Map<String, User> userMap = new HashMap<>();

        for (Member member : channel.getMembers()) {
            userMap.put(member.getUserId(), member);
        }

        for (Reaction reaction : reactionList) {
            List<User> userList = new ArrayList<>();
            List<String> userIds = reaction.getUserIds();
            for (String userId : userIds) {
                User user = userMap.get(userId);
                userList.add(user);
            }
            result.put(reaction, userList);
        }

        return result;
    }

    private void setInputTextHint(final boolean isMuted, final boolean isFrozen) {
        // set hint
        String hintText = inputHint;
        if (MessageInputView.Mode.QUOTE_REPLY == binding.vgInputBox.getInputMode()) {
            hintText = getResources().getString(R.string.sb_text_channel_input_reply_text_hint);
        } else if (isMuted) {
            hintText = getResources().getString(R.string.sb_text_channel_input_text_hint_muted);
        } else if (isFrozen) {
            hintText = getResources().getString(R.string.sb_text_channel_input_text_hint_frozen);
        }
        Logger.dev("++ hint text : " + hintText);
        binding.vgInputBox.setInputTextHint(hintText);
    }

    private void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    private void setHeaderRightButtonListener(View.OnClickListener listener) {
        this.headerRightButtonListener = listener;
    }

    private void setMessageListAdapter(MessageListAdapter adapter) {
        this.adapter = adapter;
    }

    private void setItemClickListener(OnItemClickListener<BaseMessage> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private void setItemLongClickListener(OnItemLongClickListener<BaseMessage> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    private void setInputLeftButtonListener(View.OnClickListener listener) {
        this.inputLeftButtonListener = listener;
    }

    private void setEmojiReactionClickListener(OnEmojiReactionClickListener emojiReactionClickListener) {
        this.emojiReactionClickListener = emojiReactionClickListener;
    }

    private void setEmojiReactionLongClickListener(OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
        this.emojiReactionLongClickListener = emojiReactionLongClickListener;
    }

    private void setEmojiReactionMoreButtonClickListener(OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
        this.emojiReactionMoreButtonClickListener = emojiReactionMoreButtonClickListener;
    }

    private void setMessageListParams(MessageListParams params) {
        this.params = params;
    }

    private void setOnProfileClickListener(OnItemClickListener<BaseMessage> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    private void setOnInputTextChangedListener(OnInputTextChangedListener inputTextChangedListener) {
        this.inputTextChangedListener = inputTextChangedListener;
    }

    private void setOnEditModeTextChangedListener(OnInputTextChangedListener editModeTextChangedListener) {
        this.editModeTextChangedListener = editModeTextChangedListener;
    }

    private void setOnListItemClickListener(OnIdentifiableItemClickListener<BaseMessage> listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    private void setOnListItemLongClickListener(OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener) {
        this.listItemLongClickListener = listItemLongClickListener;
    }

    public static class Builder {
        private final Bundle bundle;
        private ChannelFragment customFragment;
        private MessageListAdapter adapter;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener headerRightButtonListener;
        private OnItemClickListener<BaseMessage> itemClickListener;
        private OnItemLongClickListener<BaseMessage> itemLongClickListener;
        private View.OnClickListener inputLeftButtonListener;
        private MessageListParams params;
        private OnEmojiReactionClickListener emojiReactionClickListener;
        private OnEmojiReactionLongClickListener emojiReactionLongClickListener;
        private OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener;
        private OnItemClickListener<BaseMessage> profileClickListener;
        private LoadingDialogHandler loadingDialogHandler;
        private OnInputTextChangedListener inputTextChangedListener;
        private OnInputTextChangedListener editModeTextChangedListener;

        private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
        private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         */
        public Builder(@NonNull String channelUrl) {
            this(channelUrl, SendBirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param themeMode  {@link SendBirdUIKit.ThemeMode}
         */
        public Builder(@NonNull String channelUrl, SendBirdUIKit.ThemeMode themeMode) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Constructor
         *
         * @param channelUrl       the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom channel fragment. It must inherit {@link ChannelFragment}.
         *
         * @param fragment custom channel fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.0.4
         */
        public <T extends ChannelFragment> Builder setCustomChannelFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets whether the header is used.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
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
        public Builder setUseHeaderLeftButton(boolean useHeaderLeftButton) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, useHeaderLeftButton);
            return this;
        }

        /**
         * Sets whether the marker of last seen at is used.
         *
         * @param useLastSeenAt <code>true</code> if the marker of last seen at is used,
         *                      <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @Deprecated Deprecate last seen at feature.
         */
        @Deprecated
        public Builder setUseLastSeenAt(boolean useLastSeenAt) {
            return this;
        }

        /**
         * Sets whether the typing indicator is used.
         *
         * @param useTypingIndicator <code>true</code> if the typing indicator is used,
         *                           <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
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
        public Builder setHeaderTitle(String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
            return this;
        }

        /**
         * Sets the icon on the left button of the header.
         *
         * @param resId the resource identifier of the drawable.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
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
        public Builder setInputHint(String hint) {
            bundle.putString(StringSet.KEY_INPUT_HINT, hint);
            return this;
        }

        /**
         * Sets the click listener on the left button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderLeftButtonListener(View.OnClickListener listener) {
            this.headerLeftButtonListener = listener;
            return this;
        }

        /**
         * Sets the click listener on the right button of the header.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderRightButtonListener(View.OnClickListener listener) {
            this.headerRightButtonListener = listener;
            return this;
        }

        /**
         * Sets the message list adapter.
         *
         * @param adapter the adapter for the message list.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMessageListAdapter(MessageListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @deprecated As of 2.2.0, replaced by {@link Builder#setOnListItemClickListener(OnIdentifiableItemClickListener)}
         */
        @Deprecated
        public Builder setItemClickListener(OnItemClickListener<BaseMessage> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @deprecated As of 2.2.0, replaced by {@link Builder#setOnListItemLongClickListener(OnIdentifiableItemLongClickListener)}
         */
        @Deprecated
        public Builder setItemLongClickListener(OnItemLongClickListener<BaseMessage> itemLongClickListener) {
            this.itemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Sets the click listener on the left button of the input.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setInputLeftButtonListener(View.OnClickListener listener) {
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
        public Builder setMessageListParams(MessageListParams params) {
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
        public Builder setEmojiReactionClickListener(OnEmojiReactionClickListener emojiReactionClickListener) {
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
        public Builder setEmojiReactionLongClickListener(OnEmojiReactionLongClickListener emojiReactionLongClickListener) {
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
        public Builder setEmojiReactionMoreButtonClickListener(OnItemClickListener<BaseMessage> emojiReactionMoreButtonClickListener) {
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
        public Builder setUseMessageGroupUI(boolean useMessageGroupUI) {
            bundle.putBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, useMessageGroupUI);
            return this;
        }

        /**
         * Sets the click listener on the profile of message.
         *
         * @param profileClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.2
         * @deprecated As of 2.2.0, replaced by {@link Builder#setOnListItemClickListener(OnIdentifiableItemClickListener)}
         */
        @Deprecated
        public Builder setOnProfileClickListener(OnItemClickListener<BaseMessage> profileClickListener) {
            this.profileClickListener = profileClickListener;
            return this;
        }

        /**
         * Sets whether the user profile uses.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown when the profile image clicked, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.2
         */
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
        public Builder setKeyboardDisplayType(KeyboardDisplayType type) {
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
        public Builder setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
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
        public Builder setOnEditModeTextChangedListener(OnInputTextChangedListener editModeTextChangedListener) {
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
        public Builder setInputText(String inputText) {
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
        public Builder setOnInputTextChangedListener(OnInputTextChangedListener inputTextChangedListener) {
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
        public Builder setHighlightMessageInfo(HighlightMessageInfo highlightMessageInfo) {
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
        public Builder setStartingPoint(long startTimemillis) {
            bundle.putLong(StringSet.KEY_STARTING_POINT, startTimemillis);
            return this;
        }

        /**
         * Sets the click listener on the item of message list.
         *
         * @param itemClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.2.0
         */
        public Builder setListItemClickListener(OnIdentifiableItemClickListener<BaseMessage> itemClickListener) {
            this.listItemClickListener = itemClickListener;
            return this;
        }

        /**
         * Sets the long click listener on the item of message list.
         *
         * @param itemLongClickListener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.2.0
         */
        public Builder setListItemLongClickListener(OnIdentifiableItemLongClickListener<BaseMessage> itemLongClickListener) {
            this.listItemLongClickListener = itemLongClickListener;
            return this;
        }

        /**
         * Creates an {@link ChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelFragment} applied to the {@link Bundle}.
         */
        public ChannelFragment build() {
            ChannelFragment fragment = customFragment != null ? customFragment : new ChannelFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setHeaderRightButtonListener(headerRightButtonListener);
            fragment.setMessageListAdapter(adapter);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setInputLeftButtonListener(inputLeftButtonListener);
            fragment.setMessageListParams(params);
            fragment.setEmojiReactionClickListener(emojiReactionClickListener);
            fragment.setEmojiReactionLongClickListener(emojiReactionLongClickListener);
            fragment.setEmojiReactionMoreButtonClickListener(emojiReactionMoreButtonClickListener);
            fragment.setOnProfileClickListener(profileClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            fragment.setOnInputTextChangedListener(inputTextChangedListener);
            fragment.setOnEditModeTextChangedListener(editModeTextChangedListener);
            fragment.setOnListItemClickListener(listItemClickListener);
            fragment.setOnListItemLongClickListener(listItemLongClickListener);
            return fragment;
        }
    }
}
