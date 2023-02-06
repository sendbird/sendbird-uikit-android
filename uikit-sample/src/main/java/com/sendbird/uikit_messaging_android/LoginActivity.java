package com.sendbird.uikit_messaging_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.handler.GroupChannelCallbackHandler;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit_messaging_android.databinding.ActivityLoginBinding;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog;

import java.util.Collections;

/**
 * Displays a login screen.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.etUserId.setSelectAllOnFocus(true);
        binding.etNickname.setSelectAllOnFocus(true);

        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendbirdChat.getSdkVersion());
        binding.tvVersionInfo.setText(sdkVersion);

        binding.btSignIn.setOnClickListener(v -> {
            Editable userId = binding.etUserId.getText();
            Editable userNickname = binding.etNickname.getText();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
                return;
            }

            // Remove all spaces from userID
            String userIdString = userId.toString().replaceAll("\\s", "");

            PreferenceUtils.setUserId(userIdString);
            PreferenceUtils.setNickname(userNickname.toString());

            WaitingDialog.show(this);
            SendbirdUIKit.connect((user, e) -> {
                if (e != null || user == null) {
                    Logger.e(e);
                    WaitingDialog.dismiss();
                    PreferenceUtils.clearAll();
                    return;
                }

                createNotificationChannel(user, (groupChannel, e1) -> {
                    if (e1 != null) {
                        Logger.e(e1);
                        WaitingDialog.dismiss();
                        PreferenceUtils.clearAll();
                        return;
                    }
                    WaitingDialog.dismiss();
                    PreferenceUtils.setUserId(userIdString);
                    PreferenceUtils.setNickname(userNickname.toString());

                    PushUtils.registerPushHandler(new MyFirebaseMessagingService());
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
            });
        });
    }

    private void createNotificationChannel(@NonNull User user, @NonNull GroupChannelCallbackHandler callback) {
        final GroupChannelCreateParams params = new GroupChannelCreateParams();
        final String CUSTOM_TYPE = "SENDBIRD_NOTIFICATION_CHANNEL_NOTIFICATION";
        final String CHANNEL_NAME = "Notifications";

        /*
          The name of the channel.
          It will appear on the Group channels menu of the Sendbird Dashboard.
          It's meant to be a channel's display name on a user's channel list.
         */
        params.setName(CHANNEL_NAME);

        /*
          A custom channel type which is used for channel grouping.
          The custom type of a notification channel must start with SENDBIRD_NOTIFICATION_CHANNEL_.
          - The same custom type must be used for all users' group channel.
          - It must not be assigned to other user-to-user group channels.
         */
        params.setCustomType(CUSTOM_TYPE);

        /*
          Use a combination of the custom type as a prefix and a user ID
          (e.g. SENDBIRD_NOTIFICATION_CHANNEL_NOTIFICATION_user123)
          so that a channel URL can be inferred from a user ID.
          Only alphanumeric characters, hyphens, and underscores are allowed in a channel URL.
         */
        params.setChannelUrl(CUSTOM_TYPE + "_" + user.getUserId());

        /*
          To allow a user to delete a notification from their own Notification Center,
          add the notification receiver's user ID to operator_ids.
          Channel operators can delete a message from the channel.
         */
        params.setOperators(Collections.singletonList(SendbirdChat.getCurrentUser()));

        /*
          Must be true to ensure that only one notification channel per a user exists and the channel can be properly targeted when Sendbird delivers a message to the user.
         */
        params.setDistinct(true);

        /*
          By setting strict to true,
          you will get an error response when you try to create a channel with an unexisting user ID.
         */
        params.setStrict(true);

        GroupChannel.createChannel(params, callback);
    }
}
