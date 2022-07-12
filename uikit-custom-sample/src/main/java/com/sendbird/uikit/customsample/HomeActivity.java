package com.sendbird.uikit.customsample;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.sendbird.uikit.widgets.WaitingDialog;

import java.util.List;
import java.util.Map;

/**
 * Displays a channel select screen.
 */
public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis();

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
}