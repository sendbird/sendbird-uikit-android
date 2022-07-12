package com.sendbird.uikit_messaging_android.openchannel.community;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.uikit.SendbirdUIKit;
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
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.consts.StringSet;
import com.sendbird.uikit_messaging_android.databinding.ActivityCreateCommunityBinding;
import com.sendbird.uikit_messaging_android.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Displays a create open channel screen used for community.
 */
public class CreateCommunityActivity extends AppCompatActivity {
    private String[] REQUIRED_PERMISSIONS;

    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1001;
    private static final int PERMISSION_SETTINGS_REQUEST_ID = 2000;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 2002;

    private ActivityCreateCommunityBinding binding;
    @NonNull
    private final StateHeaderComponent headerComponent = new StateHeaderComponent();
    private Uri mediaUri;
    private File mediaFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = SendbirdUIKit.getDefaultThemeMode().getResId();
        setTheme(themeResId);
        binding = ActivityCreateCommunityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        final boolean isDark = PreferenceUtils.isUsingDarkTheme();
        binding.background.setBackgroundResource(isDark ? R.color.background_600 : R.color.background_50);
        binding.ivChannelCover.setBackgroundResource(isDark ? R.drawable.shape_image_view_background_dark : R.drawable.shape_image_view_background_light);
        binding.titleLayout.setBackgroundResource(isDark ? R.drawable.shape_edit_text_background_dark : R.drawable.shape_edit_text_background_light);
        binding.etTitle.setTextColor(getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
        binding.etTitle.setHintTextColor(getResources().getColor(isDark ? R.color.ondark_03 : R.color.onlight_03));
        binding.clearButton.setBackgroundResource(isDark ? R.drawable.selector_edit_text_clear_button_dark : R.drawable.selector_edit_text_clear_button_light);

        headerComponent.getParams().setTitle(getString(R.string.text_create_community));
        headerComponent.getParams().setRightButtonText(getString(R.string.text_header_create_button));
        headerComponent.getParams().setLeftButtonIcon(AppCompatResources.getDrawable(this, R.drawable.icon_arrow_left));
        final int headerStyle = SendbirdUIKit.isDarkMode() ? R.style.Component_Dark_Header_State : R.style.Component_Header_State;
        final Context headerThemeContext = new ContextThemeWrapper(this, headerStyle);
        final View header = headerComponent.onCreateView(headerThemeContext, getLayoutInflater(), binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);

        headerComponent.setOnLeftButtonClickListener(v -> finish());
        headerComponent.setOnRightButtonClickListener(v -> createCommunityChannel());
        int iconTint = PreferenceUtils.isUsingDarkTheme() ? R.color.onlight_01 : R.color.ondark_01;
        binding.ivCameraIcon.setImageDrawable(DrawableUtils.setTintList(this, R.drawable.icon_camera, iconTint));
        binding.ivChannelCover.setOnClickListener(v -> {
            Logger.dev("change channel cover");

            final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
            if (hasPermission) {
                showMediaSelectDialog();
                return;
            }

            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private void createCommunityChannel() {
        if (TextUtils.isEmpty(binding.etTitle.getText())) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendbirdChat.setAutoBackgroundDetection(true);

        if (resultCode != RESULT_OK) return;

        if (requestCode == PERMISSION_SETTINGS_REQUEST_ID) {
            final boolean hasPermission = PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
            if (hasPermission) {
                showMediaSelectDialog();
            }
            return;
        }

        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                break;
            case PICK_IMAGE_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    this.mediaUri = data.getData();
                }
                break;
        }

        if (this.mediaUri != null) {
            mediaFile = FileUtils.uriToFile(getApplicationContext(), mediaUri);
            updateChannelCover();
        }
    }

    private void updateChannelCover() {
        Glide.with(binding.background.getContext())
                .load(mediaUri)
                .override(binding.ivChannelCover.getWidth(), binding.ivChannelCover.getHeight())
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(PreferenceUtils.isUsingDarkTheme() ? R.drawable.shape_image_view_background_dark : R.drawable.shape_image_view_background_light)
                .into(binding.ivChannelCover);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean isAllGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                showMediaSelectDialog();
            } else {
                String[] notGranted = PermissionUtils.getNotGrantedPermissions(this, permissions);
                List<String> deniedList = PermissionUtils.getShowRequestPermissionRationale(this, permissions);
                if (deniedList.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
                    builder.setMessage(getPermissionGuideMessage(this, notGranted[0]));
                    builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, com.sendbird.uikit.R.color.secondary_300));
                }
            }
        }
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
                    pickImage();
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
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void pickImage() {
        Intent intent = IntentUtils.getImageGalleryIntent();
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
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