package com.sendbird.uikit_messaging_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendbirdChat;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit_messaging_android.databinding.ActivityLoginBinding;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog;

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

        if (com.sendbird.uikit_messaging_android.BuildConfig.DEBUG) {
            binding.applicationId.setVisibility(View.VISIBLE);
            binding.btSave.setVisibility(View.VISIBLE);
            if (SendbirdUIKit.getAdapter() != null) {
                binding.applicationId.setText(SendbirdUIKit.getAdapter().getAppId());
            }
            binding.btSave.setOnClickListener(v -> {
                final Editable appId = binding.applicationId.getText();
                if (!TextUtils.isEmpty(appId)) {
                    PreferenceUtils.setAppId(appId.toString());
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        } else {
            binding.applicationId.setVisibility(View.GONE);
            binding.btSave.setVisibility(View.GONE);
        }

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
                if (e != null) {
                    Logger.e(e);
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
    }
}
