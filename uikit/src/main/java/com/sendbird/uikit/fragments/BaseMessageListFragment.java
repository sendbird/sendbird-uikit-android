package com.sendbird.uikit.fragments;

import static android.app.Activity.RESULT_OK;

import static com.sendbird.uikit.utils.FileUtils.generateCacheKey;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.BaseFileMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Emoji;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MultipleFilesMessage;
import com.sendbird.android.message.Reaction;
import com.sendbird.android.message.SendingStatus;
import com.sendbird.android.message.UploadableFileInfo;
import com.sendbird.android.message.UserMessage;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.MultipleFilesMessageCreateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.MutedState;
import com.sendbird.android.user.Sender;
import com.sendbird.android.user.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.PhotoViewActivity;
import com.sendbird.uikit.activities.adapter.BaseMessageListAdapter;
import com.sendbird.uikit.activities.adapter.SuggestedMentionListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.internal.extensions.EmojiExtensionsKt;
import com.sendbird.uikit.internal.extensions.MessageExtensionsKt;
import com.sendbird.uikit.internal.tasks.JobResultTask;
import com.sendbird.uikit.internal.tasks.TaskQueue;
import com.sendbird.uikit.internal.ui.messages.VoiceMessageView;
import com.sendbird.uikit.internal.ui.reactions.EmojiListView;
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionUserListView;
import com.sendbird.uikit.internal.ui.widgets.VoiceMessageInputView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.model.VoiceMessageInfo;
import com.sendbird.uikit.model.configurations.ChannelConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit.modules.BaseMessageListModule;
import com.sendbird.uikit.modules.components.BaseMessageListComponent;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.PermissionUtils;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.BaseMessageListViewModel;
import com.sendbird.uikit.vm.FileDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class BaseMessageListFragment<
    LA extends BaseMessageListAdapter,
    LC extends BaseMessageListComponent<LA>,
    MT extends BaseMessageListModule<LC>,
    VM extends BaseMessageListViewModel> extends BaseModuleFragment<MT, VM> {
    private static final int MULTIPLE_FILES_COUNT_LIMIT = 10;
    @Nullable
    private OnItemClickListener<BaseMessage> messageClickListener;
    @Nullable
    private OnItemClickListener<BaseMessage> messageProfileClickListener;
    @Nullable
    private OnItemClickListener<User> emojiReactionUserListProfileClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageLongClickListener;
    @Nullable
    private OnItemLongClickListener<BaseMessage> messageProfileLongClickListener;
    @Nullable
    private OnItemClickListener<User> messageMentionClickListener;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;
    /**
     * This is custom adapter set from {@link #setAdapter(BaseMessageListAdapter)}.
     * The actual adapter is from {@link BaseMessageListModule#getMessageListComponent()} -> {@link BaseMessageListComponent#getAdapter()}.
     */
    @Nullable
    private LA customAdapter;
    @Nullable
    private SuggestedMentionListAdapter suggestedMentionListAdapter;
    @NonNull
    protected ChannelConfig channelConfig = UIKitConfig.getGroupChannelConfig();

    @Nullable
    BaseMessage targetMessage;
    @Nullable
    private Uri mediaUri;

    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null) return;
        final Uri mediaUri = intent.getData();
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || getContext() == null) return;
        final Uri mediaUri = this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });
    private final ActivityResultLauncher<Intent> takeVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        final Uri mediaUri = this.mediaUri;
        if (mediaUri != null && isFragmentAlive()) {
            sendFileMessage(mediaUri);
        }
    });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
        registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(
            getMultipleFilesMessageFileCountLimit()), this::onMultipleMediaResult);

    private final ActivityResultLauncher<PickVisualMediaRequest> pickSingleMedia =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onSingleMediaResult);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null && args.containsKey(StringSet.KEY_CHANNEL_CONFIG)) {
            channelConfig = args.getParcelable(StringSet.KEY_CHANNEL_CONFIG);
        }
    }

    @Override
    protected void onConfigureParams(@NonNull MT module, @NonNull Bundle args) {
        if (loadingDialogHandler != null) module.setOnLoadingDialogHandler(loadingDialogHandler);
    }

    @Override
    public void onDestroy() {
        Logger.i(">> BaseMessageListFragment::onDestroy()");
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);
    }

    @Override
    protected void onBeforeReady(@NonNull ReadyStatus status, @NonNull MT module, @NonNull VM viewModel) {
        Logger.d(">> BaseMessageListFragment::onBeforeReady()");
        module.getMessageListComponent().setPagedDataLoader(viewModel);
        if (this.customAdapter != null) {
            module.getMessageListComponent().setAdapter(customAdapter);
        }
        module.getMessageInputComponent().setSuggestedMentionListAdapter(suggestedMentionListAdapter == null ? new SuggestedMentionListAdapter() : suggestedMentionListAdapter);
    }

    /**
     * Make context menu items that are shown when the message is long clicked.
     *
     * @param message A clicked message.
     * @return Collection of {@link DialogListItem}
     * since 2.2.3
     */
    @NonNull
    protected List<DialogListItem> makeMessageContextMenu(@NonNull BaseMessage message) {
        return new ArrayList<>();
    }

    void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {}

    /**
     * It will be called when the message context menu was clicked.
     *
     * @param message  A clicked message.
     * @param view     The view that was clicked.
     * @param position The position that was clicked.
     * @param item     {@link DialogListItem} that was clicked.
     * @return <code>true</code> if long click event was handled, <code>false</code> otherwise.
     * since 2.2.3
     */
    protected boolean onMessageContextMenuItemClicked(@NonNull BaseMessage message, @NonNull View view, int position, @NonNull DialogListItem item) {
        return false;
    }

    /**
     * Called when the item of the message list is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param message  The message that the clicked item displays
     * since 3.0.0
     */
    protected void onMessageClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageClickListener != null) {
            messageClickListener.onItemClick(view, position, message);
            return;
        }
        if (message.getSendingStatus() == SendingStatus.SUCCEEDED) {
            MessageType type = MessageViewHolderFactory.getMessageType(message);
            switch (type) {
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                    startActivity(PhotoViewActivity.newIntent(requireContext(), ChannelType.GROUP, (FileMessage) message));
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
                        public void onError(@Nullable SendbirdException e) {
                            toastError(R.string.sb_text_error_download_file);
                        }
                    });
                    break;
                case VIEW_TYPE_VOICE_MESSAGE_ME:
                case VIEW_TYPE_VOICE_MESSAGE_OTHER:
                    if (view instanceof VoiceMessageView) {
                        ((VoiceMessageView) view).callOnPlayerButtonClick();
                    }
                    break;
                default:
            }
        } else {
            if (MessageUtils.isMine(message) &&
                (message instanceof UserMessage ||
                    message instanceof BaseFileMessage)) {
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
     * since 3.0.0
     */
    protected void onMessageProfileClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileClickListener != null) {
            messageProfileClickListener.onItemClick(view, position, message);
            return;
        }

        final Sender sender = message.getSender();
        if (sender != null) {
            showUserProfile(sender);
        }
    }

    /**
     * Called when the emoji reaction user list profile view is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param user  The user that the clicked item displays
     * since 3.9.2
     */
    protected void onEmojiReactionUserListProfileClicked(@NonNull View view, int position, @NonNull User user) {
        if (emojiReactionUserListProfileClickListener != null) {
            emojiReactionUserListProfileClickListener.onItemClick(view, position, user);
            return;
        }

        showUserProfile(user);
    }

    /**
     * Called when the item of the message list is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onMessageLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageLongClickListener != null) {
            messageLongClickListener.onItemLongClick(view, position, message);
            return;
        }
        final SendingStatus status = message.getSendingStatus();
        if (status == SendingStatus.PENDING) return;
        showMessageContextMenu(view, message, makeMessageContextMenu(message));
    }

    /**
     * Called when the profile view of the message is long-clicked.
     *
     * @param view     The View long-clicked
     * @param position The position long-clicked
     * @param message  The message that the long-clicked item displays
     * since 3.0.0
     */
    protected void onMessageProfileLongClicked(@NonNull View view, int position, @NonNull BaseMessage message) {
        if (messageProfileLongClickListener != null) {
            messageProfileLongClickListener.onItemLongClick(view, position, message);
        }
    }

    /**
     * Called when the mentioned user of the message is clicked.
     *
     * @param view     The View clicked
     * @param position The position clicked
     * @param user  The user that the clicked item displays
     * since 3.5.3
     */
    protected void onMessageMentionClicked(@NonNull View view, int position, @NonNull User user) {
        if (messageMentionClickListener != null) {
            messageMentionClickListener.onItemClick(view, position, user);
            return;
        }

        showUserProfile(user);
    }

    @NonNull
    OnItemClickListener<DialogListItem> createMessageActionListener(@NonNull BaseMessage message) {
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
                String cacheKey = generateCacheKey(fileMessage.getUrl(), fileMessage.getRequestId());
                FileDownloader.getInstance().saveFile(getContext(), fileMessage.getUrl(),
                    fileMessage.getType(), fileMessage.getName(), cacheKey);
                return true;
            }

            @Override
            public void onResultForUiThread(@Nullable Boolean result, @Nullable SendbirdException e) {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_download_file);
                    return;
                }
                toastSuccess(R.string.sb_text_toast_success_download_file);
            }
        });
    }

    void copyTextToClipboard(@NonNull String text) {
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

    void showWarningDialog(@NonNull BaseMessage message) {
        if (getContext() == null) return;
        String title;
        if (message instanceof MultipleFilesMessage) {
            title = String.format(
                getString(R.string.sb_text_dialog_delete_multiple_files_message),
                ((MultipleFilesMessage) message).getFiles().size()
            );
        } else {
            title = getString(R.string.sb_text_dialog_delete_message);
        }
        DialogUtils.showWarningDialog(
            requireContext(),
            title,
            getString(R.string.sb_text_button_delete),
            delete -> {
                Logger.dev("delete");
                deleteMessage(message);
            },
            getString(R.string.sb_text_button_cancel),
            cancel -> Logger.dev("cancel"));
    }

    void showConfirmDialog(@NonNull String message) {
        if (getContext() == null) return;
        DialogUtils.showConfirmDialog(
            requireContext(),
            message,
            getString(R.string.sb_text_button_ok),
            null,
            false);
    }

    void showEmojiActionsDialog(@NonNull BaseMessage message, @NonNull DialogListItem[] actions) {
        boolean showMoreButton = false;

        final BaseMessageListAdapter adapter = getModule().getMessageListComponent().getAdapter();
        if (adapter == null) {
            return;
        }

        MessageExtensionsKt.setEmojiCategories(message, adapter.getEmojiCategories(message));
        final List<Emoji> emojiList = MessageExtensionsKt.allowedEmojiList(message);
        int shownEmojiSize = emojiList.size();
        if (emojiList.size() > 6) {
            showMoreButton = true;
            shownEmojiSize = 5;
        }
        List<Emoji> shownEmojiList = emojiList.subList(0, shownEmojiSize);

        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(requireContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiListView emojiListView = EmojiListView.create(contextThemeWrapper, shownEmojiList, message.getReactions(), showMoreButton);
        hideKeyboard();
        if (actions.length > 0 || shownEmojiList.size() > 0) {
            final AlertDialog dialog = DialogUtils.showContentViewAndListDialog(requireContext(), emojiListView, actions, createMessageActionListener(message));

            emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
                dialog.dismiss();

                if (!view.isSelected()) {
                    // when adding emoji, check if it's allowed
                    if (!EmojiExtensionsKt.containsEmoji(emojiList, emojiKey)) {
                        toastError(R.string.sb_text_error_add_reaction);
                        return;
                    }
                }

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
    }

    private void showUserProfile(@NonNull User sender) {
        final Bundle args = getArguments();
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, UIKitConfig.getCommon().getEnableUsingDefaultUserProfile());
        if (getContext() == null || SendbirdUIKit.getAdapter() == null || !useUserProfile) return;
        hideKeyboard();
        boolean useChannelCreateButton = !sender.getUserId().equals(SendbirdUIKit.getAdapter().getUserInfo().getUserId());
        DialogUtils.showUserProfileDialog(getContext(), sender, useChannelCreateButton, null, null);
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

    void hideKeyboard() {
        if (getView() != null) {
            SoftInputUtils.hideSoftKeyboard(getView());
        }
    }

    void toggleReaction(@NonNull View view, @NonNull BaseMessage message, @NonNull String reactionKey) {
        getViewModel().toggleReaction(view, message, reactionKey, e -> {
            if (e != null && isFragmentAlive()) {
                toastError(view.isSelected() ? R.string.sb_text_error_delete_reaction : R.string.sb_text_error_add_reaction);
            }
        });
    }

    void showEmojiReactionDialog(@NonNull BaseMessage message, int position) {
        if (getContext() == null) {
            return;
        }

        final GroupChannel channel = getViewModel().getChannel();
        if (channel == null || channel.isSuper()) return;
        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiReactionUserListView emojiReactionUserListView = new EmojiReactionUserListView(contextThemeWrapper);
        emojiReactionUserListView.setOnProfileClickListener(this::onEmojiReactionUserListProfileClicked);
        emojiReactionUserListView.setEmojiReactionUserData(this,
            position,
            message.getReactions(),
            getReactionUserInfo(channel, message.getReactions()));
        hideKeyboard();
        DialogUtils.showContentDialog(requireContext(), emojiReactionUserListView);
    }

    void showEmojiListDialog(@NonNull BaseMessage message) {
        if (getContext() == null) {
            return;
        }

        final BaseMessageListAdapter adapter = getModule().getMessageListComponent().getAdapter();
        if (adapter == null) {
            return;
        }

        MessageExtensionsKt.setEmojiCategories(message, adapter.getEmojiCategories(message));
        final List<Emoji> emojiList = MessageExtensionsKt.allowedEmojiList(message);
        final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_list);
        final EmojiListView emojiListView = EmojiListView.create(contextThemeWrapper, emojiList, message.getReactions(), false);
        hideKeyboard();
        final AlertDialog dialog = DialogUtils.showContentDialog(requireContext(), emojiListView);

        emojiListView.setEmojiClickListener((view, position, emojiKey) -> {
            dialog.dismiss();

            if (!view.isSelected()) {
                // when adding emoji, check if it's allowed
                if (!EmojiExtensionsKt.containsEmoji(emojiList, emojiKey)) {
                    toastError(R.string.sb_text_error_add_reaction);
                    return;
                }
            }
            getViewModel().toggleReaction(view, message, emojiKey, e -> {
                if (e != null)
                    toastError(view.isSelected() ? R.string.sb_text_error_delete_reaction : R.string.sb_text_error_add_reaction);
            });
        });
    }

    void sendFileMessageInternal(@NonNull FileInfo fileInfo, @NonNull FileMessageCreateParams params) {
        if (targetMessage != null && channelConfig.getReplyType() != ReplyType.NONE) {
            params.setParentMessageId(targetMessage.getMessageId());
            params.setReplyToChannel(true);
        }
        getViewModel().sendFileMessage(params, fileInfo);
    }

    void sendMultipleFilesMessageInternal(@NonNull List<FileInfo> fileInfos, @NonNull MultipleFilesMessageCreateParams params) {
        if (targetMessage != null && channelConfig.getReplyType() != ReplyType.NONE) {
            params.setParentMessageId(targetMessage.getMessageId());
            params.setReplyToChannel(true);
        }
        getViewModel().sendMultipleFilesMessage(fileInfos, params);
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
            public void onResultForUiThread(@Nullable Intent intent, @Nullable SendbirdException e) {
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

    /**
     * It will be called when the input message's left button is clicked.
     * The default behavior is showing the menu, like, taking camera, gallery, and file.
     *
     * since 2.0.1
     */
    protected void showMediaSelectDialog() {
        if (getContext() == null) return;
        final List<DialogListItem> items = new ArrayList<>();
        if (channelConfig.getInput().getCamera().getEnablePhoto()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_camera, R.drawable.icon_camera));
        }
        if (channelConfig.getInput().getCamera().getEnableVideo()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_take_video, R.drawable.icon_camera));
        }
        if (channelConfig.getInput().getGallery().getEnablePhoto() || channelConfig.getInput().getGallery().getEnableVideo()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_gallery, R.drawable.icon_photo));
        }
        if (channelConfig.getInput().getEnableDocument()) {
            items.add(new DialogListItem(R.string.sb_text_channel_input_document, R.drawable.icon_document));
        }
        if (items.isEmpty()) return;
        hideKeyboard();
        DialogUtils.showListBottomDialog(requireContext(), items.toArray(new DialogListItem[0]), (view, position, item) -> {
            final int key = item.getKey();
            try {
                if (key == R.string.sb_text_channel_input_camera) {
                    takeCamera();
                } else if (key == R.string.sb_text_channel_input_take_video) {
                    takeVideo();
                } else if (key == R.string.sb_text_channel_input_gallery) {
                    takePhoto();
                } else {
                    takeFile();
                }
            } catch (Exception e) {
                Logger.e(e);
                if (key == R.string.sb_text_channel_input_camera) {
                    toastError(R.string.sb_text_error_open_camera);
                } else if (key == R.string.sb_text_channel_input_take_video) {
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
     * since 2.0.1
     */
    public void takeCamera() {
        SendbirdChat.setAutoBackgroundDetection(false);
        requestPermission(PermissionUtils.CAMERA_PERMISSION, () -> {
            if (getContext() == null) return;
            this.mediaUri = FileUtils.createImageFileUri(getContext());
            if (mediaUri == null) return;
            Intent intent = IntentUtils.getCameraIntent(requireContext(), mediaUri);
            if (IntentUtils.hasIntent(requireContext(), intent)) {
                takeCameraLauncher.launch(intent);
            }
        });
    }

    /**
     * Call taking camera application for video capture.
     *
     * since 3.2.1
     */
    public void takeVideo() {
        SendbirdChat.setAutoBackgroundDetection(false);
        requestPermission(PermissionUtils.CAMERA_PERMISSION, () -> {
            if (getContext() == null) return;
            this.mediaUri = FileUtils.createVideoFileUri(getContext());
            if (mediaUri == null) return;

            Intent intent = IntentUtils.getVideoCaptureIntent(getContext(), mediaUri);
            if (IntentUtils.hasIntent(getContext(), intent)) {
                takeVideoLauncher.launch(intent);
            }
        });
    }

    /**
     * Call taking gallery application.
     *
     * since 2.0.1
     */
    public void takePhoto() {
        takeMedia(isMultipleMediaEnabled() ? pickMultipleMedia : pickSingleMedia);
    }

    @VisibleForTesting
    void takeMedia(@NonNull ActivityResultLauncher<PickVisualMediaRequest> picker) {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType =
            channelConfig.getInput().getGallery().getPickVisualMediaType();
        if (mediaType != null) {
            SendbirdChat.setAutoBackgroundDetection(false);
            picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(mediaType)
                .build());
        }
    }

    /**
     * Returns whether the multiple media is enabled or not.
     *
     * @return true if the multiple media is enabled, false otherwise.
     * since 3.9.0
     */
    public boolean isMultipleMediaEnabled() {
        GroupChannel channel = getViewModel().getChannel();
        if (channel == null) return false;
        return channelConfig.getEnableMultipleFilesMessage() &&
            !channel.isBroadcast() && !channel.isSuper();
    }

    /**
     * This method will be invoked when PickMultipleVisualMediaRequest is finished.
     * @param uris the list of uris of the picked media.
     * since 3.9.0
     */
    @VisibleForTesting
    void onMultipleMediaResult(@NonNull List<Uri> uris) {
        SendbirdChat.setAutoBackgroundDetection(true);
        if (uris.isEmpty()) return;
        if (uris.size() > getMultipleFilesMessageFileCountLimit()) {
            showConfirmDialog(getString(R.string.sb_text_error_multiple_files_count_limit, getMultipleFilesMessageFileCountLimit()));
            return;
        }

        if (uris.size() == 1) {
            sendSingleMedia(uris.get(0));
        } else {
            sendMultipleMedia(uris);
        }
    }

    /**
     * This method will be invoked when PickVisualMediaRequest is finished.
     * @param uri the uri of the picked media.
     * since 3.9.0
     */
    @VisibleForTesting
    void onSingleMediaResult(@Nullable Uri uri) {
        SendbirdChat.setAutoBackgroundDetection(true);
        if (uri != null && isFragmentAlive()) {
            sendSingleMedia(uri);
        }
    }

    @VisibleForTesting
    void sendSingleMedia(@NonNull Uri uri) {
        sendFileMessage(uri);
    }

    @VisibleForTesting
    void sendMultipleMedia(@NonNull List<Uri> uris) {
        if (getContext() != null) {
            FileInfo.fromUris(getContext(), uris, SendbirdUIKit.shouldUseImageCompression(), new OnResultHandler<List<FileInfo>>() {
                @Override
                public void onResult(@NonNull List<FileInfo> result) {
                    sendMultipleMediaFileInfo(result);
                }

                @Override
                public void onError(@Nullable SendbirdException e) {
                    Logger.w(e);
                    toastError(R.string.sb_text_error_send_message);
                }
            });
        }
    }

    @VisibleForTesting
    void sendMultipleMediaFileInfo(@NonNull List<FileInfo> fileInfos) {
        List<FileInfo> images = new ArrayList<>();
        List<FileInfo> videos = new ArrayList<>();
        for (FileInfo fileInfo : fileInfos) {
            String mimeType = fileInfo.getMimeType();
            if (mimeType == null) continue;
            if (mimeType.startsWith(StringSet.image)) {
                images.add(fileInfo);
            } else if (mimeType.startsWith(StringSet.video)) {
                videos.add(fileInfo);
            }
        }

        final List<Integer> fileSizes = new ArrayList<>();
        FileMessageCreateParams imageParams = null;
        MultipleFilesMessageCreateParams multipleFilesParams = null;
        final List<FileMessageCreateParams> videosParams = new ArrayList<>();

        if (images.size() == 1) {
            final FileInfo image = images.get(0);
            imageParams = getFileMessageCreateParams(image);
            fileSizes.add(imageParams.getFileSize());
        } else if (images.size() > 1) {
            multipleFilesParams = getMultipleFilesMessageCreateParams(images);
            for (UploadableFileInfo uploadableFileInfo : multipleFilesParams.getUploadableFileInfoList()) {
                fileSizes.add(uploadableFileInfo.getFileSize());
            }
        }

        for (FileInfo video : videos) {
            final FileMessageCreateParams videoParams = getFileMessageCreateParams(video);
            videosParams.add(videoParams);
            fileSizes.add(videoParams.getFileSize());
        }

        if (isUploadFileSizeLimitExceeded(fileSizes)) {
            showConfirmDialog(getString(
                    R.string.sb_text_error_file_upload_size_limit,
                    FileUtils.getReadableFileSize(
                            SendbirdChat.getAppInfo() == null ?
                                    0 :
                                    SendbirdChat.getAppInfo().getUploadSizeLimit()
                    )
            ));
            return;
        }

        if (imageParams != null) {
            sendFileMessageInternal(images.get(0), imageParams);
        } else if (multipleFilesParams != null) {
            sendMultipleFilesMessageInternal(images, multipleFilesParams);
        }

        for (int i = 0 ; i < videos.size() ; i++) {
            sendFileMessageInternal(videos.get(i), videosParams.get(i));
        }
    }

    @NonNull
    private MultipleFilesMessageCreateParams getMultipleFilesMessageCreateParams(List<FileInfo> images) {
        MultipleFilesMessageCreateParams multipleFilesParams;
        multipleFilesParams = FileInfo.toMultipleFilesParams(images);
        final CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeSendMultipleFilesMessage(multipleFilesParams);
        }
        onBeforeSendMultipleFilesMessage(multipleFilesParams);
        return multipleFilesParams;
    }

    @NonNull
    private FileMessageCreateParams getFileMessageCreateParams(@NonNull FileInfo fileInfo) {
        final FileMessageCreateParams imageParams = fileInfo.toFileParams();
        final CustomParamsHandler customHandler = SendbirdUIKit.getCustomParamsHandler();
        if (customHandler != null) {
            customHandler.onBeforeSendFileMessage(imageParams);
        }
        onBeforeSendFileMessage(imageParams);
        return imageParams;
    }

    private boolean isUploadFileSizeLimitExceeded(@NonNull List<Integer> fileSizes) {
        if (getContext() == null || SendbirdChat.getAppInfo() == null) return false;
        final long uploadSizeLimit = SendbirdChat.getAppInfo().getUploadSizeLimit();
        for (int fileSize : fileSizes) {
            if (fileSize > uploadSizeLimit) {
                return true;
            }
        }
        return false;
    }

    private static int getMultipleFilesMessageFileCountLimit() {
        return Math.min(
            MULTIPLE_FILES_COUNT_LIMIT,
            SendbirdChat.isInitialized() && SendbirdChat.getAppInfo() != null ?
                SendbirdChat.getAppInfo().getMultipleFilesMessageFileCountLimit()
                : MULTIPLE_FILES_COUNT_LIMIT
        );
    }

    /**
     * Call taking file chooser application.
     *
     * since 2.0.1
     */
    public void takeFile() {
        SendbirdChat.setAutoBackgroundDetection(false);
        final String[] permissions = PermissionUtils.GET_CONTENT_PERMISSION;
        if (permissions.length > 0) {
            requestPermission(permissions, () -> {
                Intent intent = IntentUtils.getFileChooserIntent();
                getContentLauncher.launch(intent);
            });
        } else {
            Intent intent = IntentUtils.getFileChooserIntent();
            getContentLauncher.launch(intent);
        }
    }

    /**
     * Call taking voice recorder.
     *
     * since 3.4.0
     */
    public void takeVoiceRecorder() {
        requestPermission(PermissionUtils.RECORD_AUDIO_PERMISSION, () -> {
            if (getContext() == null) return;
            final Context contextThemeWrapper = ContextUtils.extractModuleThemeContext(getContext(), getModule().getParams().getTheme(), R.attr.sb_component_channel_message_input);
            final VoiceMessageInputView recorderView = new VoiceMessageInputView(contextThemeWrapper);
            hideKeyboard();
            final AlertDialog dialog = DialogUtils.showContentDialog(contextThemeWrapper, recorderView);
            dialog.setCanceledOnTouchOutside(false);
            recorderView.setOnSendButtonClickListener((sendButton, position, voiceMessageInfo) -> {
                sendVoiceFileMessage(voiceMessageInfo);
                dialog.dismiss();
            });
            recorderView.setOnCancelButtonClickListener(cancelButton -> {
                dialog.dismiss();
            });
        });
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    protected boolean shouldShowLoadingDialog() {
        return getModule().shouldShowLoadingDialog();
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 1.2.5
     */
    protected void shouldDismissLoadingDialog() {
        getModule().shouldDismissLoadingDialog();
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageCreateParams}.
     * since 1.0.4
     */
    protected void onBeforeSendUserMessage(@NonNull UserMessageCreateParams params) {
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageCreateParams}.
     * since 1.0.4
     */
    protected void onBeforeSendFileMessage(@NonNull FileMessageCreateParams params) {
    }

    /**
     * It will be called before sending multiple files message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of multiple files message. Refer to {@link MultipleFilesMessageCreateParams}.
     * since 3.9.0
     */
    protected void onBeforeSendMultipleFilesMessage(@NonNull MultipleFilesMessageCreateParams params) {
    }

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageUpdateParams}.
     * since 1.0.4
     */
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams params) {
    }

    /**
     * Sends a user message.
     *
     * @param params Params of user message. Refer to {@link UserMessageCreateParams}.
     * since 1.0.4
     */
    protected void sendUserMessage(@NonNull UserMessageCreateParams params) {
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
     * since 1.0.4
     */
    protected void sendFileMessage(@NonNull Uri uri) {
        if (getContext() != null) {
            FileInfo.fromUri(getContext(), uri, SendbirdUIKit.shouldUseImageCompression(), new OnResultHandler<FileInfo>() {
                @Override
                public void onResult(@NonNull FileInfo info) {
                    BaseMessageListFragment.this.mediaUri = null;
                    sendFileMessage(info);
                }

                @Override
                public void onError(@Nullable SendbirdException e) {
                    Logger.w(e);
                    toastError(R.string.sb_text_error_send_message);
                    BaseMessageListFragment.this.mediaUri = null;
                }
            });
        }
    }

    /**
     * Sends a voice message with given file information.
     *
     * @param info A voice file information
     * since 3.4.0
     */
    protected void sendVoiceFileMessage(@NonNull VoiceMessageInfo info) {
        final GroupChannel channel = getViewModel().getChannel();
        if (channel == null) return;
        boolean isOperator = channel.getMyRole() == Role.OPERATOR;
        boolean isMuted = channel.getMyMutedState() == MutedState.MUTED;
        boolean isFrozen = channel.isFrozen() && !isOperator;
        if (isMuted || isFrozen) {
            if (isMuted) {
                toastError(R.string.sb_text_error_user_muted);
            } else {
                toastError(R.string.sb_text_error_channel_frozen);
            }
            final File voiceFile = new File(info.getPath());
            voiceFile.delete();
            return;
        }

        if (getContext() != null) {
            final FileInfo fileInfo = FileInfo.fromVoiceFileInfo(info,
                FileUtils.getChannelFileCacheDir(getContext(), getViewModel().getChannelUrl()));
            sendFileMessage(fileInfo);
        }
    }

    @VisibleForTesting
    void sendFileMessage(@NonNull FileInfo info) {
        final FileMessageCreateParams params = getFileMessageCreateParams(info);
        if (isUploadFileSizeLimitExceeded(Collections.singletonList(params.getFileSize()))) {
            showConfirmDialog(getString(
                R.string.sb_text_error_file_upload_size_limit,
                FileUtils.getReadableFileSize(
                    SendbirdChat.getAppInfo() == null ?
                        0 :
                        SendbirdChat.getAppInfo().getUploadSizeLimit()
                )
            ));
            return;
        }
        sendFileMessageInternal(info, params);
    }

    /**
     * Updates a <code>UserMessage</code> that was previously sent in the channel.
     *
     * @param messageId The ID of the message. This must be a message that exists in the channel's history,
     *                  or an error will be returned.
     * @param params    Params of a message. Refer to {@link UserMessageUpdateParams}.
     * since 1.0.4
     */
    protected void updateUserMessage(long messageId, @NonNull UserMessageUpdateParams params) {
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
     * since 1.0.4
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
     * since 2.2.3
     */
    protected void saveFileMessage(@NonNull FileMessage message) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            download(message);
        } else {
            requestPermission(PermissionUtils.GET_CONTENT_PERMISSION, () -> download(message));
        }
    }

    /**
     * Enables or disables the input view with hint text.
     * If the user's status is not allowed to send messages, the enable value cannot be changed.
     *
     * @param enabled <code>true</code> if the input view is enabled, <code>false</code> otherwise
     * @param hintText The hint text to be displayed
     * @return <code>true</code> if the enable setting is reflected, <code>false</code> otherwise
     * since 3.17.0
     */
    public boolean tryToChangeEnableInputView(boolean enabled, @NonNull String hintText) {
        Logger.d(">> BaseMessageListFragment::tryToChangeEnableInputView(%s, %s)", enabled, hintText);
        final GroupChannel channel = getViewModel().getChannel();
        if (channel == null) return false;
        return getModule().getMessageInputComponent().tryToChangeEnableStatus(channel, enabled, hintText);
    }

    /**
     * Returns the URL of the channel with the required data to use this fragment.
     *
     * @return The URL of a channel this fragment is currently associated with
     * since 3.0.0
     */
    @NonNull
    protected String getChannelUrl() {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        return args.getString(StringSet.KEY_CHANNEL_URL, "");
    }

    /**
     * Sets the click listener on the item of message list.
     *
     * @param messageClickListener The callback that will run.
     * since 3.3.0
     */
    void setOnMessageClickListener(@Nullable OnItemClickListener<BaseMessage> messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    /**
     * Sets the click listener on the profile of message.
     *
     * @param messageProfileClickListener The callback that will run.
     * since 3.3.0
     */
    void setOnMessageProfileClickListener(@Nullable OnItemClickListener<BaseMessage> messageProfileClickListener) {
        this.messageProfileClickListener = messageProfileClickListener;
    }

    /**
     * Sets the click listener on the profile of emoji reaction user list.
     *
     * @param emojiReactionUserListProfileClickListener The callback that will run.
     * since 3.9.2
     */
    void setOnEmojiReactionUserListProfileClickListener(@Nullable OnItemClickListener<User> emojiReactionUserListProfileClickListener) {
        this.emojiReactionUserListProfileClickListener = emojiReactionUserListProfileClickListener;
    }

    /**
     * Sets the click listener on the mentioned user of message.
     *
     * @param messageMentionClickListener The callback that will run.
     * since 3.5.3
     */
    void setOnMessageMentionClickListener(@Nullable OnItemClickListener<User> messageMentionClickListener) {
        this.messageMentionClickListener = messageMentionClickListener;
    }

    /**
     * Sets the long click listener on the item of message list.
     *
     * @param messageLongClickListener The callback that will run.
     * since 3.3.0
     */
    void setOnMessageLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    /**
     * Sets the long click listener on the item of message list.
     *
     * @param messageProfileLongClickListener The callback that will run.
     * since 3.3.0
     */
    void setOnMessageProfileLongClickListener(@Nullable OnItemLongClickListener<BaseMessage> messageProfileLongClickListener) {
        this.messageProfileLongClickListener = messageProfileLongClickListener;
    }

    /**
     * Sets the custom loading dialog handler
     *
     * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
     * since 3.3.0
     */
    void setOnLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Sets the message list adapter.
     *
     * @param adapter the adapter for the message list.
     * since 3.3.0
     */
    void setAdapter(@Nullable LA adapter) {
        this.customAdapter = adapter;
    }

    /**
     * Sets the suggested mention list adapter.
     *
     * @param suggestedMentionListAdapter the adapter for the mentionable user list.
     * since 3.3.0
     */
    void setSuggestedMentionListAdapter(@Nullable SuggestedMentionListAdapter suggestedMentionListAdapter) {
        this.suggestedMentionListAdapter = suggestedMentionListAdapter;
    }

    @NonNull
    @VisibleForTesting
    ActivityResultLauncher<PickVisualMediaRequest> getPickMultipleMedia() {
        return pickMultipleMedia;
    }

    @NonNull
    @VisibleForTesting
    ActivityResultLauncher<PickVisualMediaRequest> getPickSingleMedia() {
        return pickSingleMedia;
    }
}
