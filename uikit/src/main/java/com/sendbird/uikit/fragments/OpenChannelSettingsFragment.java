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
import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ParticipantsListActivity;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentOpenChannelSettingsBinding;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnMenuItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.OpenChannelSettingsView;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment displaying the informations of the channel.
 * When the channel is created this fragment will connect to the Sendbird and also will enter the channel that is taken from the server through set channel url automatically.
 *
 * @since 2.0.0
 */
public class OpenChannelSettingsFragment extends BaseOpenChannelFragment implements PermissionFragment.IPermissionHandler, LoadingDialogHandler {
    private static final int CAPTURE_IMAGE_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 2002;
    private final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHANNEL_SETTINGS" + System.currentTimeMillis();;

    private SbFragmentOpenChannelSettingsBinding binding;
    private Uri mediaUri;

    protected View.OnClickListener headerLeftButtonListener;
    protected OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel> menuItemClickListener;
    private LoadingDialogHandler loadingDialogHandler;

    public OpenChannelSettingsFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> OpenChannelSettingsFragment::onCreate()");
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
            public void onUserEntered(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserEntered()");
                    Logger.d("++ joind user : " + user);
                    OpenChannelSettingsFragment.this.channel = channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onUserExited(OpenChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserLeft()");
                    Logger.d("++ left user : " + user);
                    OpenChannelSettingsFragment.this.channel = channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onChannelChanged()");
                    OpenChannelSettingsFragment.this.channel = (OpenChannel) channel;
                    drawSettingsView();
                }
            }

            @Override
            public void onChannelDeleted(String channelUrl, BaseChannel.ChannelType channelType) {
                if (isCurrentChannel(channelUrl)) {
                    Logger.i(">> OpenChannelSettingsFragment::onChannelDeleted()");
                    Logger.d("++ deleted channel url : " + channelUrl);
                    // will have to finish activity
                    finish();
                }
            }

            @Override
            public void onOperatorUpdated(BaseChannel channel) {
                if (isCurrentChannel(channel.getUrl())) {
                    Logger.i(">> OpenChannelSettingsFragment::onOperatorUpdated()");
                    OpenChannelSettingsFragment.this.channel = (OpenChannel) channel;
                    Logger.i("++ Am I an operator : " + ((OpenChannel) channel).isOperator(SendBird.getCurrentUser()));
                    if (!((OpenChannel) channel).isOperator(SendBird.getCurrentUser())) {
                        finish();
                    }
                }
            }

            @Override
            public void onUserBanned(BaseChannel channel, User user) {
                if (isCurrentChannel(channel.getUrl()) &&
                        user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    Logger.i(">> OpenChannelSettingsFragment::onUserBanned()");
                    finish();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_open_channel_settings, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeaderOnCreated();
    }

    @Override
    protected void onReadyFailure() {
        Logger.i(">> OpenChannelSettingsFragment::onReadyFailure()");
        toastError(R.string.sb_text_error_get_channel);
    }

    protected void onConfigure() {
        Logger.i(">> OpenChannelSettingsFragment::doConfigure()");
    }

    protected void onDrawPage() {
        Logger.i(">> OpenChannelSettingsFragment::onDrawPage()");
        String channelUrl = getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(channelUrl)) {
            toastError(R.string.sb_text_error_get_channel);
            finish();
            return;
        }
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

    private void initHeaderOnReady(OpenChannel channel) {
        if (this.loadingDialogHandler == null) {
            this.loadingDialogHandler = this;
        }

        if (headerLeftButtonListener != null) {
            binding.abSettingsHeader.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

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
                        OpenChannelParams params = new OpenChannelParams().setName(res);
                        updateOpenChannel(params);
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

    private void initChannelSetting() {
        binding.csvSettings.setOnItemClickListener((v, position, menu) -> {
            Logger.d("OnSettingsItem clicked menu : " + menu);
            if (menuItemClickListener != null && menuItemClickListener.onMenuItemClicked(v, menu, channel)) {
                return;
            }

            switch (menu) {
                case PARTICIPANTS:
                    Logger.dev("show participants");
                    Logger.i("++ show participants");
                    startActivity(ParticipantsListActivity.newIntent(getContext(), channel.getUrl()));
                    break;
                case DELETE_CHANNEL:
                    deleteChannel();
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
                final File file = FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);

                OpenChannelParams params = new OpenChannelParams().setCoverImage(file);
                toastSuccess(R.string.sb_text_toast_success_start_upload_file);
                updateOpenChannel(params);
            }
        }
    }

    /**
     * It will be called before updating group channel.
     * If you want add more data, you can override this and set the data.
     *
     * @param params Params of channel. Refer to {@link OpenChannelParams}.
     */
    protected void onBeforeUpdateOpenChannel(@NonNull OpenChannelParams params) {
    }

    /**
     * Update this channel with OpenChannelParams.
     *
     * @param params Params of channel. Refer to {@link OpenChannelParams}.
     */
    protected void updateOpenChannel(@NonNull OpenChannelParams params) {
        if (channel != null) {
            CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
            if (cutsomHandler != null) {
                cutsomHandler.onBeforeUpdateOpenChannel(params);
            }
            onBeforeUpdateOpenChannel(params);
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
        if (isActive() && binding != null && channel != null) {
            binding.csvSettings.drawSettingsView(channel);
        }
    }

    /**
     * Leaves this channel.
     */
    protected void deleteChannel() {
        if (channel != null) {
            loadingDialogHandler.shouldShowLoadingDialog();
            channel.delete(e -> {
                loadingDialogHandler.shouldDismissLoadingDialog();
                if (e != null) {
                    Logger.e(e);
                    toastError(R.string.sb_text_error_delete_channel);
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
     */
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    /**
     * Sets the channel setting menu click listener.
     *
     * @param listener The callback that will run.
     */
    protected void setOnMenuItemClickListener(OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel> listener) {
        this.menuItemClickListener = listener;
    }

    private void setLoadingDialogHandler(LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    /**
     * This is a Builder that is able to create the OpenChannelSettings fragment.
     * The builder provides options how the channel is showing and working. Also you can set the event handler what you want to override.
     */
    public static class Builder {
        private final Bundle bundle;
        private OpenChannelSettingsFragment customFragment;
        private View.OnClickListener headerLeftButtonListener;
        private OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel> menuItemClickListener;
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
         * Sets the custom channel settings fragment. It must inherit {@link OpenChannelSettingsFragment}.
         * @param fragment custom channel setting fragment.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public <T extends OpenChannelSettingsFragment> Builder setCustomOpenChannelSettingsFragment(T fragment) {
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
         * Sets the channel setting menu click listener.
         *
         * @param listener The callback that will run.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setOnSettingMenuClickListener(OnMenuItemClickListener<OpenChannelSettingsView.OpenChannelSettingMenu, OpenChannel> listener) {
            this.menuItemClickListener = listener;
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
         * Creates an {@link OpenChannelSettingsFragment} with the arguments supplied to this
         * builder.
         *
         * @return The {@link OpenChannelSettingsFragment} applied to the {@link Bundle}.
         */
        public OpenChannelSettingsFragment build() {
            OpenChannelSettingsFragment fragment = customFragment != null ? customFragment : new OpenChannelSettingsFragment();
            fragment.setArguments(bundle);
            fragment.setHeaderLeftButtonListener(headerLeftButtonListener);
            fragment.setOnMenuItemClickListener(menuItemClickListener);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
