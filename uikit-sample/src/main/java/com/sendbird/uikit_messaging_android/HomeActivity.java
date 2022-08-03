package com.sendbird.uikit_messaging_android;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.SendBirdPushHelper;
import com.sendbird.android.User;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.databinding.ActivityHomeBinding;
import com.sendbird.uikit_messaging_android.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelMainActivity;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis();

    @NonNull
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});
    @NonNull
    private final ActivityResultLauncher<Intent> appSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), intent -> {});

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendBird.getSDKVersion());
        binding.tvVersionInfo.setText(sdkVersion);

        binding.groupChannelButton.setOnClickListener(v -> clickGroupChannel());
        binding.openChannelButton.setOnClickListener(v -> clickOpenChannel());
        binding.btSignOut.setOnClickListener(v -> signOut());

        binding.tvUnreadCount.setTextAppearance(this, R.style.SendbirdCaption3OnDark01);
        binding.tvUnreadCount.setBackgroundResource(R.drawable.shape_badge_background);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            final String permission = Manifest.permission.POST_NOTIFICATIONS;
            if (ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED) {
                return;
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showPermissionRationalePopup();
                return;
            }
            requestPermissionLauncher.launch(permission);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SendBird.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
            if (e != null) {
                return;
            }

            if (totalCount > 0) {
                binding.tvUnreadCount.setVisibility(View.VISIBLE);
                binding.tvUnreadCount.setText(totalCount > 99 ?
                        getString(R.string.text_tab_badge_max_count) :
                        String.valueOf(totalCount));
            } else {
                binding.tvUnreadCount.setVisibility(View.GONE);
            }
        });

        SendBird.addUserEventHandler(USER_EVENT_HANDLER_KEY, new SendBird.UserEventHandler() {
            @Override
            public void onFriendsDiscovered(List<User> list) {}

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, Map<String, Integer> totalCountByCustomType) {
                if (totalCount > 0) {
                    binding.tvUnreadCount.setVisibility(View.VISIBLE);
                    binding.tvUnreadCount.setText(totalCount > 99 ?
                            getString(R.string.text_tab_badge_max_count) :
                            String.valueOf(totalCount));
                } else {
                    binding.tvUnreadCount.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
    }

    private void clickGroupChannel() {
        Intent intent = new Intent(this, GroupChannelMainActivity.class);
        startActivity(intent);
    }

    private void clickOpenChannel() {
        Intent intent = new Intent(this, OpenChannelMainActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        WaitingDialog.show(this);
        PushUtils.unregisterPushHandler(new SendBirdPushHelper.OnPushRequestCompleteListener() {
            @Override
            public void onComplete(boolean isActive, String token) {
                SendBirdUIKit.disconnect(() -> {
                    WaitingDialog.dismiss();
                    PreferenceUtils.clearAll();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(SendBirdException e) {
                WaitingDialog.dismiss();
            }
        });
    }

    private void showPermissionRationalePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title));
        builder.setMessage(String.format(Locale.US, getString(R.string.sb_text_need_to_allow_permission_notification), ContextUtils.getApplicationName(this)));
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
}