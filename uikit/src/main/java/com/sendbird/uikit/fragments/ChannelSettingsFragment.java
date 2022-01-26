package com.sendbird.uikit.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.MemberListActivity;
import com.sendbird.uikit.activities.MessageSearchActivity;
import com.sendbird.uikit.activities.ModerationActivity;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentChannelSettingsBinding;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.ChannelSettingsView;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment displaying the informations of the channel.
 */
public class ChannelSettingsFragment extends BaseFragment implements PermissionFragment.IPermissionHandler, LoadingDialogHandler {
    private static final int CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 2002;
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_SETTINGS" + System.currentTimeMillis();;

    private SbFragmentChannelSettingsBinding binding;
    private Uri mediaUri;
    private GroupChannel channel;

    private View.OnClickListener memberSettingClickListener;
    protected View.OnClickListener headerLeftButtonListener;
    protected OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel> menuItemClickListener;
    private LoadingDialogHandler loadingDialogHandler;

    public ChannelSettingsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> ChannelSettingsFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID);
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {}

            @Override
            public void onUserJoined(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsFragment::onUserJoined()");
                    Logger.d("++ joind user : " + user);
                    ChannelSettingsFragment.this.channel = channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onUserLeft(GroupChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsFragment::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    if (channel.getMyMemberState() == Member.MemberState.NONE) {
                        finish();
                        return;
                    }
                    ChannelSettingsFragment.this.channel = channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsFragment::onChannelChanged()");
                    ChannelSettingsFragment.this.channel = (GroupChannel) channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> ChannelSettingsFragment::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    finish();
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> ChannelSettingsFragment::onOperatorUpdated()");
                    ChannelSettingsFragment.this.channel = (GroupChannel) channel;
                    Logger.i("++ my role : " + ((GroupChannel) channel).getMyRole());
                    drawSettingsView();
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> ChannelSettingsFragment::onUserBanned()");
                    finish();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_channel_settings, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeaderOnCreated();
    }

    @Override
    public void onReady(User user, ReadyStatus status) {
        Logger.i(">> ChannelSettingsFragment::onReady( status : %s)", status);
        if (status == ReadyStatus.ERROR || !containsExtra(StringSet.KEY_CHANNEL_URL)) {
            toastError(R.string.sb_text_error_get_channel);
            return;
        }
        initPage();
    }

    private void initPage() {
        String channelUrl = getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(channelUrl)) {
            toastError(R.string.sb_text_error_get_channel);
            finish();
            return;
        }
        GroupChannel.getChannel(channelUrl, (channel, e) -> {
            this.channel = channel;
            onConfigure();
            onDrawPage();
        });
    }

    protected void onConfigure() {
        Logger.i(">> ChannelSettingsFragment::doConfigure()");
    }

    protected void onDrawPage() {
        Logger.i(">> ChannelSettingsFragment::onDrawPage()");
        initHeaderOnReady(channel);
        initChannelSetting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
    }

    private boolean isCurrentChannel(@NonNull String channelUrl) {
        return channelUrl.equals(channel.getUrl());
    }

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
        showMediaSelectDialog();
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        String headerTitle = getString(R.string.sb_text_header_channel_settings);
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        ColorStateList headerLeftButtonIconTint = null;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, getString(R.string.sb_text_header_channel_settings));
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
        }

        binding.abSettingsHeader.setVisibility(useHeader ? View.VISIBLE : View.GONE);
        binding.abSettingsHeader.getTitleTextView().setText(headerTitle);
        binding.abSettingsHeader.setUseLeftImageButton(useHeaderLeftButton);
        binding.abSettingsHeader.setUseRightButton(useHeaderRightButton);

        binding.abSettingsHeader.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abSettingsHeader.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abSettingsHeader.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady(GroupChannel channel) {
        if (headerLeftButtonListener != null) {
            binding.abSettingsHeader.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

        if (channel.isBroadcast() && channel.getMyRole() != Member.Role.OPERATOR) {
            binding.abSettingsHeader.setRightTextButtonString("");
        } else {
            binding.abSettingsHeader.setRightTextButtonString(getString(R.string.sb_text_button_edit));
            binding.abSettingsHeader.setRightTextButtonClickListener((v) -> {
                DialogListItem[] items = {
                        new DialogListItem(R.string.sb_text_channel_settings_change_channel_name),
                        new DialogListItem(R.string.sb_text_channel_settings_change_channel_image)
                };

                if (getContext() == null || getFragmentManager() == null) return;
                DialogUtils.buildItemsBottom(items, (view, p, item) -> {
                    final int key = item.getKey();
                    if (key == R.string.sb_text_channel_settings_change_channel_name) {
                        if (getContext() == null || getFragmentManager() == null) return;

                        Logger.dev("change channel name");
                        OnEditTextResultListener listener = res -> {
                            GroupChannelParams params = new GroupChannelParams().setName(res);
                            updateGroupChannel(params);
                        };

                        DialogEditTextParams params = new DialogEditTextParams(getString(R.string.sb_text_channel_settings_change_channel_name_hint));
                        params.setEnableSingleLine(true);
                        DialogUtils.buildEditText(
                                getString(R.string.sb_text_channel_settings_change_channel_name),
                                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                                params, listener,
                                getString(R.string.sb_text_button_save), null,
                                getString(R.string.sb_text_button_cancel), null).showSingle(getFragmentManager());
                    } else if (key == R.string.sb_text_channel_settings_change_channel_image) {
                        Logger.dev("change channel image");
                        checkPermission(PICK_IMAGE_PERMISSIONS_REQUEST_CODE, this);
                    }
                }).showSingle(getFragmentManager());
            });
        }
    }

    private void initChannelSetting() {
        if (this.loadingDialogHandler == null) {
            this.loadingDialogHandler = this;
        }
        binding.csvSettings.setOnItemClickListener((v, position, menu) -> {
            Logger.d("OnSettingsItem clicked menu : " + menu);
            if (menu == ChannelSettingsView.ChannelSettingMenu.MEMBERS && memberSettingClickListener != null) {
                memberSettingClickListener.onClick(v);
                if (menuItemClickListener != null) {
                    menuItemClickListener.onMenuItemClicked(v, menu, channel);
                }

                /* memberSettingClickListener added ealier than menuItemClickListener.
                 * For backward compatibility, if memberSettingClickListener exists, the event always will be consumed.
                 */
                return;
            }
            if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, menu, channel)) {
                return;
            }

            switch (menu) {
                case MODERATIONS:
                    startActivity(ModerationActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case NOTIFICATIONS:
                    loadingDialogHandler.shouldShowLoadingDialog();
                    GroupChannel.PushTriggerOption option = channel.getMyPushTriggerOption() == GroupChannel.PushTriggerOption.OFF ?
                            GroupChannel.PushTriggerOption.ALL :
                            GroupChannel.PushTriggerOption.OFF;

                    channel.setMyPushTriggerOption(option, e -> {
                        loadingDialogHandler.shouldDismissLoadingDialog();
                        if (e != null) {
                            Logger.e(e);
                            if (option == GroupChannel.PushTriggerOption.ALL) {
                                toastError(R.string.sb_text_error_push_notification_on);
                            } else {
                                toastError(R.string.sb_text_error_push_notification_off);
                            }
                            return;
                        }
                        Logger.i("++ push notifications : %s", option);
                    });
                    break;
                case MEMBERS:
                    Logger.dev("members");
                    Logger.i("++ members");
                    startActivity(MemberListActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case LEAVE_CHANNEL:
                    leaveChannel();
                    break;
                case SEARCH_IN_CHANNEL:
                    Logger.dev("call message search");
                    Logger.i("++ call message search");
                    startActivity(MessageSearchActivity.newIntent(getContext(), channel.getUrl()));
                    break;
            }
        });
        drawSettingsView();
    }

    private void showMediaSelectDialog() {
        if (getContext() == null || getFragmentManager() == null) return;

        DialogListItem[] items = {
                new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_camera),
                new DialogListItem(R.string.sb_text_channel_settings_change_channel_image_gallery)};

        DialogUtils.buildItems(getString(R.string.sb_text_channel_settings_change_channel_image),
                (int) getResources().getDimension(R.dimen.sb_dialog_width_280),
                items, (v, p, item) -> {
                    try {
                        final int key = item.getKey();
                        SendBird.setAutoBackgroundDetection(false);
                        if (key == R.string.sb_text_channel_settings_change_channel_image_camera) {
                            takeCamera();
                        } else if (key == R.string.sb_text_channel_settings_change_channel_image_gallery) {
                            pickImage();
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                        toastError(R.string.sb_text_error_open_camera);
                    }
                }).showSingle(getFragmentManager());
    }

    private void takeCamera() {
        this.mediaUri = FileUtils.createPictureImageUri(getContext());
        Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
        if (IntentUtils.hasIntent(getContext(), intent)) {
            startActivityForResult(intent, CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void pickImage() {
        Intent intent = IntentUtils.getGalleryIntent();
        startActivityForResult(intent, PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendBird.setAutoBackgroundDetection(true);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE:
                    break;
                case PICK_IMAGE_PERMISSIONS_REQUEST_CODE:
                    if (data != null) {
                        this.mediaUri = data.getData();
                    }
                    break;
            }

            if (this.mediaUri != null && channel != null) {
                TaskQueue.addTask(new JobResultTask<File>() {
                    @Override
                    public File call() {
                        return FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);
                    }

                    @Override
                    public void onResultForUiThread(File file, SendBirdException e) {
                        if (e != null) {
                            Logger.w(e);
                            return;
                        }
                        GroupChannelParams params = new GroupChannelParams().setCoverImage(file);
                        toastSuccess(R.string.sb_text_toast_success_start_upload_file);
                        updateGroupChannel(params);
                    }
                });
            }
        }
    }

    /**
     * It will be called before updating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.0.4
     */
    protected void onBeforeUpdateGroupChannel(@NonNull GroupChannelParams params) {
    }

    /**
     * Update this channel with GroupChannelParams.
     *
     * @param params Params of channel. Refer to {@link GroupChannelParams}.
     * @since 1.0.4
     */
    protected void updateGroupChannel(@NonNull GroupChannelParams params) {
        if (channel != null) {
            CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
            if (cutsomHandler != null) {
                cutsomHandler.onBeforeUpdateGroupChannel(params);
            }
            onBeforeUpdateGroupChannel(params);
            channel.updateChannel(params, (updatedChannel, e) -> {
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_update_channel);
                    return;
                }
                Logger.i("++ updated channel name : %s", updatedChannel.getName());
            });
        }
    }

    private void drawSettingsView() {
        if (channel != null && binding != null && isActive()) {
            binding.csvSettings.drawSettingsView(channel);
        }
    }

    /**
     * Leaves this channel.
     *
     * @since 1.0.4
     */
    protected void leaveChannel() {
        if (channel != null) {
            loadingDialogHandler.shouldShowLoadingDialog();
            channel.leave(e -> {
                loadingDialogHandler.shouldDismissLoadingDialog();
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_leave_channel);
                    return;
                }
                Logger.i("++ leave channel");
                finish();
            });
        }
    }

    /**
     * Sets the click listener on the left button of the header.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    @Deprecated
    private void setMemberSettingClickListener(View.OnClickListener listener) {
        this.memberSettingClickListener = listener;
    }

    /**
     * Sets the channel setting menu click listener.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel> listener) {
        this.menuItemClickListener = listener;
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * @since 1.2.5
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * @since 1.2.5
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    public static class Builder {
        private final Bundle bundle;
        private ChannelSettingsFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private View.OnClickListener memberSettingClickListener;
        private OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel> menuItemClickListener;
        private LoadingDialogHandler loadingDialogHandler;

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
         * @param themeMode {@link SendBirdUIKit.ThemeMode}
         */
        public Builder(@NonNull String channelUrl, SendBirdUIKit.ThemeMode themeMode) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, themeMode.getResId());
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Constructor
         *
         * @param channelUrl the url of the channel will be implemented.
         * @param customThemeResId the resource identifier for custom theme.
         */
        public Builder(@NonNull String channelUrl, @StyleRes int customThemeResId) {
            bundle = new Bundle();
            bundle.putInt(StringSet.KEY_THEME_RES_ID, customThemeResId);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
        }

        /**
         * Sets the custom channel settings fragment. It must inherit {@link ChannelSettingsFragment}.
         * @param fragment custom channel setting fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         *
         * @since 1.0.4
         */
        public <T extends ChannelSettingsFragment> Builder setCustomChannelSettingsFragment(T fragment) {
            this.customFragment = fragment;
            return this;
        }

        /**
         * Sets the title of the header.
         *
         * @param title text to be displayed.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setHeaderTitle(String title) {
            bundle.putString(StringSet.KEY_HEADER_TITLE, title);
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
         * @since 1.2.3
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
         * Sets the click listener on the member setting.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.1.1
         * @deprecated As of 1.2.0, replaced by {@link ChannelSettingsFragment#setOnMenuItemClickListener(OnMenuItemClickListener)} ()}.
         */
        @Deprecated
        public Builder setMemberSettingClickListener(View.OnClickListener listener) {
            this.memberSettingClickListener = listener;
            return this;
        }

        /**
         * Sets the channel setting menu click listener.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 1.2.0
         */
        public Builder setOnSettingMenuClickListener(OnMenuItemClickListener<ChannelSettingsView.ChannelSettingMenu, GroupChannel> listener) {
            this.menuItemClickListener = listener;
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
         * Creates an {@link ChannelSettingsFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link ChannelSettingsFragment} applied to the {@link Bundle}.
         */
        public ChannelSettingsFragment build() {
            ChannelSettingsFragment fragment = customFragment != null ? customFragment : new ChannelSettingsFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setMemberSettingClickListener(memberSettingClickListener);
            fragment.setOnMenuItemClickListener(menuItemClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
