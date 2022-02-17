package com.sendbird.uikit.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdError;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.OpenChannelSettingsActivity;
import com.sendbird.uikit.activities.ParticipantsListActivity;
import com.sendbird.uikit.activities.PhotoViewActivity;
import com.sendbird.uikit.activities.adapter.OpenChannelMessageListAdapter;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.KeyboardDisplayType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentOpenChannelBinding;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnIdentifiableItemClickListener;
import com.sendbird.uikit.interfaces.OnIdentifiableItemLongClickListener;
import com.sendbird.uikit.interfaces.OnInputTextChangedListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.interfaces.OnResultHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.FileInfo;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.MessageUtils;
import com.sendbird.uikit.utils.SoftInputUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.FileDownloader;
import com.sendbird.uikit.vm.OpenChannelViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.MessageInputView;
import com.sendbird.uikit.widgets.PagerRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fragment displaying the list of messages in the channel.
 * Now you are able to see the history of messages on OpenChannel.
 * You can call this via {@link Builder}.
 *
 * @since 2.0.0
 */
public class OpenChannelFragment extends BaseOpenChannelFragment implements OnIdentifiableItemClickListener<BaseMessage>,
        OnIdentifiableItemLongClickListener<BaseMessage>, LoadingDialogHandler {

    private static final int CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 2002;
    private static final int PICK_FILE_PERMISSIONS_REQUEST_CODE = 2003;
    private static final int GROUP_CHANNEL_SETTINGS_REQUEST_CODE = 2004;
    private static final int PERMISSION_REQUEST_ALL = 2005;
    private static final int PERMISSION_REQUEST_STORAGE = 2006;

    private SbFragmentOpenChannelBinding binding;
    private OpenChannelViewModel viewModel;
    private OpenChannelMessageListAdapter adapter;
    private String inputHint;
    private MessageAnchorDialog messageAnchorDialog;

    private Uri mediaUri;
    @Nullable private BaseMessage targetMessage;

    private boolean hasHeaderDescription;
    private View.OnClickListener headerLeftButtonListener;
    private View.OnClickListener headerRightButtonListener;
    private OnItemClickListener<BaseMessage> profileClickListener;
    private OnItemClickListener<BaseMessage> itemClickListener;
    private OnItemLongClickListener<BaseMessage> itemLongClickListener;
    private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
    private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;
    private View.OnClickListener inputLeftButtonListener;
    private MessageListParams params;
    private LoadingDialogHandler loadingDialogHandler;
    private OnInputTextChangedListener inputTextChangedListener;
    private OnInputTextChangedListener editModeTextChangedListener;
    final AtomicBoolean isInitialCall = new AtomicBoolean(true);
    private String headerTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> OpenChannelFragment::onCreate()");
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
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
        if (channel != null) {
            channel.exit(null);
        }

        if (isInitialCall.get()) {
            loadingDialogHandler.shouldDismissLoadingDialog();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.i(">> OpenChannelFragment::onConfigurationChanged(%s)", newConfig.orientation);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.i(">> OpenChannelFragment::onCreateView()");
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_open_channel, container, false);
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

    protected void onConfigure() {
        Logger.i(">> OpenChannelFragment::onConfigure() - %s", Logger.getCallerTraceInfo(OpenChannelFragment.class));
    }

    protected void onDrawPage() {
        Logger.i(">> OpenChannelFragment::onDrawPage() - %s", Logger.getCallerTraceInfo(OpenChannelFragment.class));
        channel.enter(e -> {
            if (!isActive()) return;
            if (e != null) {
                toastError(e.getCode() == SendBirdError.ERR_INVALID_AUTHORITY ? R.string.sb_text_error_enter_channel_from_authority : R.string.sb_text_error_enter_channel);
                finish();
                return;
            }
            viewModel = createViewModel(channel);
            getLifecycle().addObserver(viewModel);

            initHeaderOnReady(channel);
            initMessageList(channel);
            initMessageInput();
            drawChannel(channel);
        });
    }

    private OpenChannelViewModel createViewModel(OpenChannel channel) {
        return new ViewModelProvider(getViewModelStore(), new ViewModelFactory(channel, params)).get(channel.getUrl(), OpenChannelViewModel.class);
    }

    private void drawChannel(OpenChannel channel) {
        Logger.dev("++ drawChannel()");
        if (isActive()) {
            if (headerTitle == null) {
                binding.chvChannelHeader.getTitleTextView().setText(channel.getName());
            }
            ChannelUtils.loadChannelCover(binding.chvChannelHeader.getProfileView(), channel);
            binding.tvInformation.setVisibility(channel.isFrozen() ? View.VISIBLE : View.GONE);
            binding.tvInformation.setText(R.string.sb_text_information_channel_frozen);
            boolean isOperator = channel.isOperator(SendBird.getCurrentUser());
            boolean isFrozen = channel.isFrozen() && !isOperator;
            if (isFrozen) clearInput();
            binding.vgInputBox.setEnabled(!isFrozen);
            // set hint
            setInputTextHint(isFrozen);
            updateHeaderParticipantCount();
        }
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        boolean useHeader = false;
        boolean useHeaderLeftButton = false;
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        ColorStateList headerLeftButtonIconTint = null;
        String headerDescription = null;

        if (args != null) {
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, false);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
            headerDescription = args.getString(StringSet.KEY_HEADER_DESCRIPTION, null);
            hasHeaderDescription = args.containsKey(StringSet.KEY_HEADER_DESCRIPTION);
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, null);
        }

        binding.chvChannelHeader.setVisibility(useHeader ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(headerDescription)) {
            binding.chvChannelHeader.getDescriptionTextView().setVisibility(View.VISIBLE);
            binding.chvChannelHeader.getDescriptionTextView().setText(headerDescription);
        }
        binding.chvChannelHeader.setUseLeftImageButton(useHeaderLeftButton);
        binding.chvChannelHeader.setUseRightButton(useHeaderRightButton);

        if (headerTitle != null) {
            binding.chvChannelHeader.getTitleTextView().setText(headerTitle);
        }

        binding.chvChannelHeader.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.chvChannelHeader.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.chvChannelHeader.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady(OpenChannel channel) {
        Bundle args = getArguments();
        boolean useHeaderProfileImage = true;
        if (args != null) {
            useHeaderProfileImage = args.getBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE, true);
        }
        final boolean isOperator = channel.isOperator(SendBird.getCurrentUser());
        int headerRightButtonIconResId = channel.isOperator(SendBird.getCurrentUser()) ? R.drawable.icon_info : R.drawable.icon_members;
        headerRightButtonIconResId = args != null ? args.getInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, headerRightButtonIconResId) : headerRightButtonIconResId;
        ColorStateList headerRightButtonIconTint = args != null ? args.getParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT) : null;

        binding.chvChannelHeader.setRightImageButtonResource(headerRightButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID)) {
            binding.chvChannelHeader.setRightImageButtonTint(headerRightButtonIconTint);
        }
        if (headerLeftButtonListener != null) {
            binding.chvChannelHeader.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

        if (headerRightButtonListener != null) {
            binding.chvChannelHeader.setRightImageButtonClickListener(headerRightButtonListener);
        } else {
            binding.chvChannelHeader.setRightImageButtonClickListener(v -> {
                if (isOperator) {
                    Intent intent = OpenChannelSettingsActivity.newIntent(getContext(), channel.getUrl());
                    startActivityForResult(intent, GROUP_CHANNEL_SETTINGS_REQUEST_CODE);
                } else {
                    startActivity(ParticipantsListActivity.newIntent(getContext(), channel.getUrl()));
                }
            });
        }

        binding.chvChannelHeader.getProfileView().setVisibility(useHeaderProfileImage ? View.VISIBLE : View.GONE);
        viewModel.isChannelChanged().observe(this, this::drawChannel);
        viewModel.getChannelDeleted().observe(this, deleted -> finish());
        viewModel.getMessageDeleted().observe(this, deletedMessageId -> {
            if (MessageInputView.Mode.EDIT == binding.vgInputBox.getInputMode() &&
                    null != targetMessage &&
                    targetMessage.getMessageId() == deletedMessageId) {
                clearInput();
            }
        });
    }

    private void updateHeaderParticipantCount() {
        if (hasHeaderDescription) {
            return;
        }

        final int count = channel.getParticipantCount();
        binding.chvChannelHeader.getDescriptionTextView().setVisibility(View.VISIBLE);
        binding.chvChannelHeader.getDescriptionTextView()
                .setText(String.format(getString(R.string.sb_text_header_participants_count), ChannelUtils.makeMemberCountText(count)));
    }

    private void initMessageList(OpenChannel channel) {
        Bundle args = getArguments();
        boolean useMessageGroupUI = args == null || args.getBoolean(StringSet.KEY_USE_MESSAGE_GROUP_UI, true);
        final boolean useUserProfile = args == null || args.getBoolean(StringSet.KEY_USE_USER_PROFILE, SendBirdUIKit.shouldUseDefaultUserProfile());
        final boolean overlayMode = args == null || args.getBoolean(StringSet.KEY_USE_OVERLAY_MODE, false);

        if (adapter == null) {
            adapter = new OpenChannelMessageListAdapter(channel, useMessageGroupUI);
        }

        if (overlayMode) {
            binding.bg.setBackgroundResource(R.color.onlight_02);
        }
        adapter.setChannel(channel);

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

        final PagerRecyclerView recyclerView = binding.mrvMessageList.getRecyclerView();
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setPager(viewModel);
        recyclerView.setThreshold(5);
        recyclerView.setItemAnimator(new ItemAnimator());
        recyclerView.setOnScrollEndDetectListener(this::hideScrollBottomButton);

        binding.mrvMessageList.getScrollBottomView().setOnClickListener(v -> {
            recyclerView.stopScroll();
            scrollToBottom();
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0 && adapter != null) {
                    BaseMessage message = adapter.getItem(positionStart);
                    LinearLayoutManager layoutManager = binding.mrvMessageList.getRecyclerView().getLayoutManager();
                    if ((BaseMessage.belongsTo(message, SendBird.getCurrentUser().getUserId()) || (layoutManager != null && layoutManager.findFirstVisibleItemPosition() == 0)) && !isAnchorShowing()) {
                        scrollToBottom();
                    }
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                LinearLayoutManager layoutManager = binding.mrvMessageList.getRecyclerView().getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() > 0) {
                    binding.mrvMessageList.showScrollBottomButton();
                }
            }
        });

        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            int emptyIconResId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_chat);
            binding.statusFrame.setEmptyIcon(emptyIconResId);
            binding.statusFrame.setIconTint(args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT));
        }
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_TEXT_RES_ID)) {
            int emptyTextResId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_channel_message_empty);
            binding.statusFrame.setEmptyText(emptyTextResId);
        }
        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);

        viewModel.getMessageLoadState().observe(this, state -> {
            switch (state) {
                case LOAD_STARTED:
                    break;
                case LOAD_ENDED:
                    if (isActive() && isInitialCall.getAndSet(false)) {
                        loadingDialogHandler.shouldDismissLoadingDialog();
                    }
                    break;
            }
        });

        viewModel.getMessageList().observe(this, messageList -> {
            Logger.dev("++ result messageList size : %s", messageList.size());
            adapter.setItems(channel, messageList);
        });
        viewModel.getErrorToast().observe(this, this::toastError);
        viewModel.load();
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
        binding.vgInputBox.setOnInputTextChangedListener(inputTextChangedListener);
        binding.vgInputBox.setOnEditModeTextChangedListener(editModeTextChangedListener);
        binding.vgInputBox.setOnInputModeChangedListener((before, current) -> {
            boolean isOperator = channel.isOperator(SendBird.getCurrentUser());
            boolean isFrozen = channel.isFrozen() && !isOperator;

            binding.vgInputBox.setEnabled(!isFrozen);
            // set hint
            setInputTextHint(isFrozen);

            if (MessageInputView.Mode.EDIT == current) {
                if (targetMessage != null) binding.vgInputBox.setInputText(targetMessage.getMessage());
                binding.vgInputBox.showKeyboard();
            } else {
                targetMessage = null;
            }
        });
    }

    private void hideScrollBottomButton(PagerRecyclerView.ScrollDirection direction) {
        if (direction == PagerRecyclerView.ScrollDirection.Bottom) {
            binding.mrvMessageList.hideScrollBottomButton();
        }
    }

    private void scrollToBottom() {
        if (binding == null) return;
        RecyclerView.LayoutManager layoutMgr = binding.mrvMessageList.getRecyclerView().getLayoutManager();
        if (layoutMgr == null) return;
        layoutMgr.scrollToPosition(0);
    }

    private void setInputTextHint(final boolean isFrozen) {
        String hintText = inputHint;
        if (isFrozen) {
            hintText = getResources().getString(R.string.sb_text_channel_input_text_hint_frozen);
        }
        Logger.dev("++ hint text : " + hintText);
        binding.vgInputBox.setInputTextHint(hintText);
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
        DialogUtils.buildItemsBottom(items, (view, position, item) -> {
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
            drawChannel(viewModel.getChannel());
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
     */
    protected void onBeforeSendUserMessage(@NonNull UserMessageParams params) {
    }

    /**
     * It will be called before sending message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of file message. Refer to {@link FileMessageParams}.
     */
    protected void onBeforeSendFileMessage(@NonNull FileMessageParams params) {
    }

    /**
     * It will be called before updating message.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     */
    protected void onBeforeUpdateUserMessage(@NonNull UserMessageParams params) {
    }

    /**
     * Sends a user message.
     *
     * @param params Params of user message. Refer to {@link UserMessageParams}.
     */
    protected void sendUserMessage(@NonNull UserMessageParams params) {
        if (viewModel != null) {
            CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
            if (cutsomHandler != null) {
                cutsomHandler.onBeforeSendUserMessage(params);
            }
            onBeforeSendUserMessage(params);
            viewModel.sendUserMessage(params);
            clearInput();
        }
    }

    /**
     * Sends a file with given file information.
     *
     * @param uri A file Uri
     */
    protected void sendFileMessage(@NonNull Uri uri) {
        if (viewModel != null && getContext() != null) {
            FileInfo.fromUri(getContext(), uri, SendBirdUIKit.shouldUseImageCompression(), new OnResultHandler<FileInfo>() {
                @Override
                public void onResult(FileInfo info) {
                    FileMessageParams params = info.toFileParams();
                    CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
                    if (cutsomHandler != null) {
                        cutsomHandler.onBeforeSendFileMessage(params);
                    }
                    onBeforeSendFileMessage(params);
                    viewModel.sendFileMessage(params, info);
                    clearInput();
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
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        // Do nothing on the channel.
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     */
    @Override
    public void shouldDismissLoadingDialog() {
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

    @Override
    public void onIdentifiableItemClick(View view, String identifier, int position, BaseMessage message) {
        Logger.d("++ OpenChannelFragment::onItemClicked()");
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
                            startActivity(PhotoViewActivity.newIntent(getContext(), BaseChannel.ChannelType.OPEN, (FileMessage) message));
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
                DialogUtils.buildUserProfile(getContext(), message.getSender(), false, null, null).showSingle(getFragmentManager());
                break;
        }
    }

    @Override
    public void onIdentifiableItemLongClick(View itemView, String clickableType, int position, BaseMessage message) {
        Logger.d("++ OpenChannelFragment::onItemLongClick()");
        if (clickableType.equals(ClickableViewIdentifier.Chat.name())) {
            final BaseMessage.SendingStatus status = message.getSendingStatus();
            if (status == BaseMessage.SendingStatus.PENDING) return;

            final List<DialogListItem> items = makeMessageContextMenu(message);
            showMessageContextMenu(itemView, message, items);
        }
    }

    private void showMessageContextMenu(@NonNull View anchorView, @NonNull BaseMessage message, @NonNull List<DialogListItem> items) {
        int size = items.size();
        final DialogListItem[] actions = items.toArray(new DialogListItem[size]);

        if (MessageUtils.isUnknownType(message)) {
            if (getContext() == null || getFragmentManager() == null) return;
            DialogUtils
                    .buildItemsBottom(actions, createMessageActionListener(message))
                    .showSingle(getFragmentManager());
        } else {
            if (getContext() == null) return;
            messageAnchorDialog = new MessageAnchorDialog.Builder(anchorView, binding.mrvMessageList, actions)
                    .setOnItemClickListener(createMessageActionListener(message))
                    .build();
            messageAnchorDialog.show();
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
        DialogListItem delete = new DialogListItem(R.string.sb_text_channel_anchor_delete, R.drawable.icon_delete);
        DialogListItem retry = new DialogListItem(R.string.sb_text_channel_anchor_retry, 0);
        DialogListItem deleteFailed = new DialogListItem(R.string.sb_text_channel_anchor_delete, 0);

        DialogListItem[] actions = null;
        switch (type) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                if (status == BaseMessage.SendingStatus.SUCCEEDED) {
                    actions = new DialogListItem[]{copy, edit, delete};
                } else if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                }
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                actions = new DialogListItem[]{copy};
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_ME:
                if (MessageUtils.isFailed(message)) {
                    actions = new DialogListItem[]{retry, deleteFailed};
                } else {
                    actions = new DialogListItem[]{delete, save};
                }
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                actions = new DialogListItem[]{save};
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

    /**
     * It will be called when the message context menu was clicked.
     *
     * @param message A clicked message.
     * @param view The view that was clicked.
     * @param position The position that was clicked.
     * @param item {@link DialogListItem} that was clicked.
     * @return <code>true</code> if long click event was handled, <code>false</code> otherwise.
     * @since 2.2.3
     */
    protected boolean onMessageContextMenuItemClicked(@NonNull BaseMessage message, @NonNull View view, int position, @NonNull DialogListItem item) {
        final int key = item.getKey();
        if (key == R.string.sb_text_channel_anchor_copy) {
            copyTextToClipboard(message.getMessage());
            return true;
        } else if (key == R.string.sb_text_channel_anchor_edit) {
            editMessage(message);
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
        } else if (key == R.string.sb_text_channel_anchor_retry) {
            resendMessage(message);
            return true;
        }

        return false;
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

    @NonNull
    private OnItemClickListener<DialogListItem> createMessageActionListener(@NonNull BaseMessage message) {
        return (view, position, item) -> onMessageContextMenuItemClicked(message, view, position, item);
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

    private void editMessage(BaseMessage message) {
        targetMessage = message;
        binding.vgInputBox.setInputMode(MessageInputView.Mode.EDIT);
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
                cancel -> Logger.dev("cancel"))
                .showSingle(getFragmentManager());
    }

    private boolean isAnchorShowing() {
        return (messageAnchorDialog != null && messageAnchorDialog.isShowing());
    }

    private void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    private void setHeaderRightButtonListener(View.OnClickListener listener) {
        this.headerRightButtonListener = listener;
    }

    private void setMessageListAdapter(OpenChannelMessageListAdapter adapter) {
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

    /**
     * This is a Builder that is able to create the OpenChannel fragment.
     * The builder provides options how the channel is showing and working. Also you can set the event handler what you want to override.
     *
     * @since 2.0.0
     */
    public static class Builder {
        private final Bundle bundle;
        private OpenChannelFragment customFragment;
        private OpenChannelMessageListAdapter adapter;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener headerRightButtonListener;
        private OnItemClickListener<BaseMessage> itemClickListener;
        private OnItemLongClickListener<BaseMessage> itemLongClickListener;
        private View.OnClickListener inputLeftButtonListener;
        private MessageListParams params;
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
         * Sets the custom channel fragment. It must inherit {@link OpenChannelFragment}.
         *
         * @param fragment custom channel fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public <T extends OpenChannelFragment> Builder setCustomOpenChannelFragment(T fragment) {
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
         * Sets the description of the header.
         *
         * @param description text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderDescription(String description) {
            bundle.putString(StringSet.KEY_HEADER_DESCRIPTION, description);
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
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
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
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setHeaderRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_HEADER_RIGHT_BUTTON_ICON_TINT, tint);
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
         * Sets whether the left button of the input is used.
         *
         * @param useInputLeftButton <code>true</code> if the left button of the input is used,
         *                            <code>false</code> otherwise.
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
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
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
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        public Builder setInputRightButtonIcon(@DrawableRes int resId, @Nullable ColorStateList tint) {
            bundle.putInt(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_RES_ID, resId);
            bundle.putParcelable(StringSet.KEY_INPUT_RIGHT_BUTTON_ICON_TINT, tint);
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
        public Builder setOpenChannelMessageListAdapter(OpenChannelMessageListAdapter adapter) {
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
         */
        public Builder setMessageListParams(MessageListParams params) {
            this.params = params;
            return this;
        }

        /**
         * Sets whether the message group UI is used.
         *
         * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
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
         */
        public Builder setUseUserProfile(boolean useUserProfile) {
            bundle.putBoolean(StringSet.KEY_USE_USER_PROFILE, useUserProfile);
            return this;
        }

        /**
         * The channel displays as a overlay mode.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder useOverlayMode() {
            bundle.putBoolean(StringSet.KEY_USE_OVERLAY_MODE, true);
            bundle.putInt(StringSet.KEY_THEME_RES_ID, R.style.SendBird_Overlay);
            return this;
        }

        /**
         * The message input displays as a dialog type. (Refer to {@link KeyboardDisplayType})
         *
         * @return This Builder object to allow for chaining of calls to set methods.
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
         * @param tint Color state list to use for tinting this resource, or null to clear the tint.
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
         * Sets whether the profile image of the header is used.
         *
         * @param useHeaderProfileImage <code>true</code> if the profile image of the header is used, <code>false</code> otherwise.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.2.4
         */
        public Builder setUseHeaderProfileImage(boolean useHeaderProfileImage) {
            bundle.putBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE, useHeaderProfileImage);
            return this;
        }

        /**
         * Creates an {@link OpenChannelFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelFragment} applied to the {@link Bundle}.
         */
        public OpenChannelFragment build() {
            OpenChannelFragment fragment = customFragment != null ? customFragment : new OpenChannelFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setHeaderRightButtonListener(headerRightButtonListener);
            fragment.setMessageListAdapter(adapter);
            fragment.setItemClickListener(itemClickListener);
            fragment.setItemLongClickListener(itemLongClickListener);
            fragment.setInputLeftButtonListener(inputLeftButtonListener);
            fragment.setMessageListParams(params);
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
