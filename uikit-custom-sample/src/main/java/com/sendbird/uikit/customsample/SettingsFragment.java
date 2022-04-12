package com.sendbird.uikit.customsample;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.SendBird;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.utils.DrawableUtils;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
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
import java.util.TimeZone;

public class SettingsFragment extends Fragment {
    private String[] REQUIRED_PERMISSIONS;

    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 1001;
    private static final int PERMISSION_SETTINGS_REQUEST_ID = 2000;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2001;
    private static final int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 2002;

    private AppCompatImageView profileImageView;
    private TextView nicknameTextView;
    private SwitchCompat disturbSwitch;
    private Uri mediaUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPage(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.setAutoBackgroundDetection(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendBird.setAutoBackgroundDetection(true);
        if (resultCode != RESULT_OK) return;

        if (requestCode == PERMISSION_SETTINGS_REQUEST_ID) {
            if (getContext() == null) return;
            final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
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

        if (this.mediaUri != null && getContext() != null) {
            final File file = FileUtils.uriToFile(getContext().getApplicationContext(), mediaUri);
            updateUserProfileImage(file);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                if (getActivity() == null) return;
                String[] notGranted = PermissionUtils.getNotGrantedPermissions(getActivity(), permissions);
                List<String> deniedList = PermissionUtils.getShowRequestPermissionRationale(getActivity(), permissions);
                if (deniedList.size() == 0 && getActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getActivity().getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
                    builder.setMessage(getPermissionGuideMessage(getActivity(), notGranted[0]));
                    builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(intent, PERMISSION_SETTINGS_REQUEST_ID);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    if (getActivity() != null) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), com.sendbird.uikit.R.color.secondary_300));
                    }
                }
            }
        }
    }

    private void initPage(@NonNull View view) {
        if (getContext() == null) {
            return;
        }

        Toolbar toolbar = view.findViewById(R.id.tbSettings);
        toolbar.inflateMenu(R.menu.settings_menu);
        toolbar.getMenu().findItem(R.id.action_edit_profile)
                .getActionView().setOnClickListener(v -> {
            Logger.d("++ edit button clicked");
            showEditProfileDialog();
        });

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        boolean useDoNotDisturb = true;
        if (getArguments() != null) {
            useDoNotDisturb = getArguments().getBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, true);
        }

        profileImageView = view.findViewById(R.id.ivProfileView);
        loadUserProfileUrl(PreferenceUtils.getProfileUrl());
        TextView userIdTextView = view.findViewById(R.id.tvUserId);
        userIdTextView.setText(PreferenceUtils.getUserId());
        nicknameTextView = view.findViewById(R.id.tvNickname);
        nicknameTextView.setText(PreferenceUtils.getNickname());

        View disturbItem = view.findViewById(R.id.itemDisturb);
        disturbItem.setVisibility(useDoNotDisturb ? View.VISIBLE : View.GONE);
        if (useDoNotDisturb) {
            disturbItem.setOnClickListener(v -> {
                Logger.d("++ disturb clicked");
                updateDoNotDisturb();
            });

            disturbSwitch = view.findViewById(R.id.scDisturbSwitch);
            SendBird.getDoNotDisturb((b, i, i1, i2, i3, s, e) -> {
                PreferenceUtils.setDoNotDisturb(b);
                disturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
            });
            disturbSwitch.setOnClickListener(v -> {
                Logger.d("++ disturb clicked");
                updateDoNotDisturb();
            });
        }

        View signOutItem = view.findViewById(R.id.itemHome);
        signOutItem.setOnClickListener(v -> {
            Logger.d("++ home clicked");
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void showEditProfileDialog() {
        if (getContext() == null || getFragmentManager() == null) return;

        DialogListItem[] items = {
                new DialogListItem(R.string.text_settings_change_user_nickname),
                new DialogListItem(R.string.text_settings_change_user_profile_image)
        };

        DialogUtils.showListBottomDialog(requireContext(), items, (v, p, item) -> {
            final int key = item.getKey();
            if (key == R.string.text_settings_change_user_nickname) {
                Logger.dev("change user nickname");
                OnEditTextResultListener listener = result -> {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    updateUserNickname(result);
                };

                DialogEditTextParams params = new DialogEditTextParams(getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_name_hint));
                params.setEnableSingleLine(true);
                DialogUtils.showInputDialog(
                        requireContext(),
                        getString(R.string.text_settings_change_user_nickname),
                        params, listener,
                        getString(com.sendbird.uikit.R.string.sb_text_button_save), null,
                        getString(com.sendbird.uikit.R.string.sb_text_button_cancel), null);
            } else if (key == R.string.text_settings_change_user_profile_image) {
                Logger.dev("change user profile");

                if (getContext() == null) return;
                final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), REQUIRED_PERMISSIONS);
                if (hasPermission) {
                    showMediaSelectDialog();
                    return;
                }

                requestPermissions(REQUIRED_PERMISSIONS, STORAGE_PERMISSIONS_REQUEST_CODE);
            }
        });
    }

    private void updateUserNickname(@NonNull String nickname) {
        if (getContext() == null) return;
        WaitingDialog.show(getContext());
        SendbirdUIKit.updateUserInfo(nickname, null, e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                return;
            }

            PreferenceUtils.setNickname(nickname);
            nicknameTextView.setText(nickname);
        });
    }

    private void updateUserProfileImage(@NonNull File profileImage) {
        if (getContext() == null) return;
        WaitingDialog.show(getContext());
        SendBird.updateCurrentUserInfoWithProfileImage(null, profileImage, e -> {
            WaitingDialog.dismiss();
            if (e != null) {
                Logger.e(e);
                return;
            }

            String profileUrl = SendBird.getCurrentUser().getProfileUrl();
            PreferenceUtils.setProfileUrl(profileUrl);
            loadUserProfileUrl(profileUrl);
        });
    }

    private void updateDoNotDisturb() {
        disturbSwitch.setChecked(!PreferenceUtils.getDoNotDisturb());
        Logger.d("update do not disturb : " + !PreferenceUtils.getDoNotDisturb());
        SendBird.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb(), 0, 0, 23, 59, TimeZone.getDefault().getID(), e -> {
            if (e != null) {
                disturbSwitch.setChecked(PreferenceUtils.getDoNotDisturb());
                return;
            }
            Logger.d("update do not disturb on callback : " + !PreferenceUtils.getDoNotDisturb());
            PreferenceUtils.setDoNotDisturb(!PreferenceUtils.getDoNotDisturb());
        });
    }

    private void showMediaSelectDialog() {
        if (getContext() == null) return;
        DialogListItem[] items = {
                new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera),
                new DialogListItem(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery)};

        DialogUtils.showListDialog(requireContext(),
                getString(com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image),
                items, (v, p, item) -> {
                    try {
                        final int key = item.getKey();
                        SendBird.setAutoBackgroundDetection(false);
                        if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                            takeCamera();
                        } else if (key == com.sendbird.uikit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                            pickImage();
                        }
                    } catch (Exception e) {
                        Logger.e(e);
                    }
                });
    }

    private void takeCamera() {
        if (getContext() == null) {
            return;
        }

        this.mediaUri = FileUtils.createPictureImageUri(getContext());
        Intent intent = IntentUtils.getCameraIntent(getContext(), mediaUri);
        if (IntentUtils.hasIntent(getContext(), intent)) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void pickImage() {
        Intent intent = IntentUtils.getGalleryIntent();
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
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

    private void loadUserProfileUrl(final String url) {
        if (getContext() != null) {
            Glide.with(requireContext())
                    .load(url)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(DrawableUtils.setTintList(requireContext(), R.drawable.icon_user, SendbirdUIKit.getDefaultThemeMode().getMonoTintResId()))
                    .into(profileImageView);
        }
    }
}
