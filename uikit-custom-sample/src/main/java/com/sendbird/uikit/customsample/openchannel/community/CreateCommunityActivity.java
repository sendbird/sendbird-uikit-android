package com.sendbird.uikit.customsample.openchannel.community;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.databinding.ActivityCreateCommunityBinding;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.modules.components.StateHeaderComponent;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.DialogUtils;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.IntentUtils;
import com.sendbird.uikit.utils.PermissionUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.widgets.WaitingDialog;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Displays a create open channel screen used for community.
 */
public class CreateCommunityActivity extends AppCompatActivity {
    private final String[] REQUIRED_PERMISSIONS = PermissionUtils.CAMERA_PERMISSION;

    private ActivityCreateCommunityBinding binding;
    @NonNull
    private final StateHeaderComponent headerComponent = new StateHeaderComponent();
    private Uri mediaUri;
    private File mediaFile;

    private final ActivityResultLauncher<Intent> appSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), intent -> {
        final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
        if (hasPermission) {
            showMediaSelectDialog();
        }
    });
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionResults -> {
        if (PermissionUtils.getNotGrantedPermissions(permissionResults).isEmpty()) {
            showMediaSelectDialog();
        }
    });

    private final ActivityResultLauncher<Intent> takeCameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK) return;
        if (mediaUri != null) {
            mediaFile = FileUtils.uriToFile(getApplicationContext(), mediaUri);
            updateChannelCover();
        }
    });
    private final ActivityResultLauncher<Intent> getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        SendbirdChat.setAutoBackgroundDetection(true);
        final Intent intent = result.getData();
        int resultCode = result.getResultCode();

        if (resultCode != RESULT_OK || intent == null) return;
        this.mediaUri = intent.getData();
        if (mediaUri != null) {
            this.mediaFile = FileUtils.uriToFile(getApplicationContext(), mediaUri);
            updateChannelCover();
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = SendbirdUIKit.getDefaultThemeMode().getResId();
        setTheme(themeResId);
        this.binding = ActivityCreateCommunityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        headerComponent.getParams().setTitle(getString(R.string.text_create_community));
        headerComponent.getParams().setRightButtonText(getString(R.string.text_header_create_button));
        headerComponent.getParams().setLeftButtonIcon(AppCompatResources.getDrawable(this, R.drawable.icon_arrow_left));
        final int headerStyle = SendbirdUIKit.isDarkMode() ? R.style.Component_Dark_Header_State : R.style.Component_Header_State;
        final Context headerThemeContext = new ContextThemeWrapper(this, headerStyle);
        final View header = headerComponent.onCreateView(headerThemeContext, getLayoutInflater(), binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);

        headerComponent.setOnLeftButtonClickListener(v -> finish());
        headerComponent.setOnRightButtonClickListener(v -> createCommunityChannel());
        binding.ivChannelCover.setOnClickListener(v -> {
            Logger.dev("change channel cover");

            final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
            if (hasPermission) {
                showMediaSelectDialog();
                return;
            }

            requestPermission(REQUIRED_PERMISSIONS);
        });
        binding.etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.clearButton.setVisibility(!TextUtils.isEmpty(s) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.clearButton.setOnClickListener(v -> binding.etTitle.setText(""));
        SendbirdUIKit.connect(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendbirdChat.setAutoBackgroundDetection(true);
    }

    private void requestPermission(@NonNull String[] permissions) {
        // 1. check permission
        final boolean hasPermission = PermissionUtils.hasPermissions(this, permissions);
        if (hasPermission) {
            showMediaSelectDialog();
            return;
        }

        // 2. determine whether rationale popup should show
        final List<String> deniedList = PermissionUtils.getExplicitDeniedPermissionList(this, permissions);
        if (!deniedList.isEmpty()) {
            showPermissionRationalePopup(deniedList.get(0));
            return;
        }
        // 3. request permission
        this.permissionLauncher.launch(permissions);
    }

    private void showPermissionRationalePopup(@NonNull String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
        builder.setMessage(getPermissionGuideMessage(this, permission));
        builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            appSettingLauncher.launch(intent);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, com.sendbird.uikit.R.color.secondary_300));
    }

    private void createCommunityChannel() {
        if (TextUtils.isEmpty(binding.etTitle.getText()) || SendbirdChat.getCurrentUser() == null) {
            return;
        }

        OpenChannelCreateParams params = new OpenChannelCreateParams(SendbirdUIKit.getAdapter().getUserInfo().getUserId());
        params.setCustomType(StringSet.SB_COMMUNITY_TYPE);
        params.setName(binding.etTitle.getText().toString());
        if (mediaFile != null) {
            params.setCoverImage(mediaFile);
        }

        WaitingDialog.show(this);
        OpenChannel.createChannel(params, (openChannel, e) -> {
            WaitingDialog.dismiss();
            if (e != null) {
                ContextUtils.toastError(this, R.string.sb_text_error_create_channel);
                Logger.d(e);
                return;
            }

            startActivity(CommunityActivity.newIntent(this, openChannel.getUrl()));
            finish();
        });
    }

    private void updateChannelCover() {
        Glide.with(binding.getRoot().getContext())
                .load(mediaUri)
                .override(binding.ivChannelCover.getWidth(), binding.ivChannelCover.getHeight())
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.shape_image_view_background_light)
                .into(binding.ivChannelCover);
    }

    private void showMediaSelectDialog() {
        DialogListItem delete = new DialogListItem(R.string.text_remove_photo, 0, true);
        DialogListItem camera = new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera);
        DialogListItem gallery = new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery);
        DialogListItem[] items;
        if (mediaFile == null) {
            items = new DialogListItem[]{camera, gallery};
        } else {
            items = new DialogListItem[]{delete, camera, gallery};
        }

        DialogUtils.showListBottomDialog(this, items, (view, position, item) -> {
            try {
                final int key = item.getKey();
                SendbirdChat.setAutoBackgroundDetection(false);
                if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                    takeCamera();
                } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                    takePhoto();
                } else {
                    removeFile();
                }
            } catch (Exception e) {
                Logger.e(e);
            }
        });
    }

    private void takeCamera() {
        this.mediaUri = FileUtils.createPictureImageUri(this);
        if (mediaUri == null) return;
        Intent intent = IntentUtils.getCameraIntent(this, mediaUri);
        if (IntentUtils.hasIntent(this, intent)) {
            takeCameraLauncher.launch(intent);
        }
    }

    private void takePhoto() {
        Intent intent = IntentUtils.getImageGalleryIntent();
        getContentLauncher.launch(intent);
    }

    private void removeFile() {
        this.mediaFile = null;
        this.mediaUri = null;
        binding.ivChannelCover.setImageResource(0);
    }

    private static String getPermissionGuideMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = com.sendbird.uikit.R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }
}