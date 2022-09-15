package com.sendbird.uikit.customsample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendbirdChat;
import com.sendbird.uikit.BuildConfig;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.customsample.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.customsample.widgets.WaitingDialog;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Displays a login screen.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        EditText etUserId = findViewById(R.id.etUserId);
        EditText etNickname = findViewById(R.id.etNickname);
        TextView tvVersion = findViewById(R.id.tvVersionInfo);

        etUserId.setSelectAllOnFocus(true);
        etNickname.setSelectAllOnFocus(true);

        String sdkVersion = String.format(getResources().getString(R.string.text_version_info), BuildConfig.VERSION_NAME, SendbirdChat.getSdkVersion());
        tvVersion.setText(sdkVersion);

        findViewById(R.id.btSignIn).setOnClickListener(v -> {
            String userId = etUserId.getText().toString();
            // Remove all spaces from userID
            String userIdString = userId.replaceAll("\\s", "");

            String userNickname = etNickname.getText().toString();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userNickname)) {
                return;
            }

            PreferenceUtils.setUserId(userIdString);
            PreferenceUtils.setNickname(userNickname);

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
                PreferenceUtils.setNickname(userNickname);
                PushUtils.registerPushHandler(new MyFirebaseMessagingService());
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
}
