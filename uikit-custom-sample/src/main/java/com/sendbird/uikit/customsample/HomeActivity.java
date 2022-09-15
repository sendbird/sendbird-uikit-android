package com.sendbird.uikit.customsample;

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

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.PushRequestCompleteHandler;
import com.sendbird.android.handler.UserEventHandler;
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.customsample.databinding.ActivityHomeBinding;
import com.sendbird.uikit.customsample.groupchannel.GroupChannelMainActivity;
import com.sendbird.uikit.customsample.openchannel.OpenChannelMainActivity;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.customsample.widgets.WaitingDialog;
import com.sendbird.uikit.utils.ContextUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Displays a channel select screen.
 */
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
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendbirdChat.getSdkVersion());
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
                showPermissionRationalePopup(permission);
                return;
            }
            requestPermissionLauncher.launch(permission);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // initialize total unread count
        SendbirdChat.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e) -> {
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

        // register total unread count event
        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, new UserEventHandler() {
            @Override
            public void onFriendsDiscovered(@NonNull List<User> list) {}

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, @NonNull Map<String, Integer> totalCountByCustomType) {
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
        SendbirdChat.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
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
        PushUtils.unregisterPushHandler(new PushRequestCompleteHandler() {
            @Override
            public void onComplete(boolean isActive, String token) {
                SendbirdUIKit.disconnect(() -> {
                    WaitingDialog.dismiss();
                    PreferenceUtils.clearAll();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(@NonNull SendbirdException e) {
                WaitingDialog.dismiss();
            }
        });
    }

    private void showPermissionRationalePopup(@NonNull String permission) {
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
