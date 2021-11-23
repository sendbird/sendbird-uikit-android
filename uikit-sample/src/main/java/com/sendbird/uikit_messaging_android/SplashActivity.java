package com.sendbird.uikit_messaging_android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.widgets.WaitingDialog;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        BaseApplication.initStateChanges().observe(this, initState -> {
            Logger.i("++ init state : %s", initState);
            WaitingDialog.dismiss();
            switch (initState) {
                case MIGRATING:
                    WaitingDialog.show(SplashActivity.this);
                    break;
                case FAILED:
                case SUCCEED:
                    WaitingDialog.dismiss();
                    String userId = PreferenceUtils.getUserId();
                    if (!TextUtils.isEmpty(userId)) {
                        SendBirdUIKit.connect((user, e) -> {
                            startActivity(getNextIntent());
                            finish();
                        });
                    } else {
                        startActivity(getNextIntent());
                        finish();
                    }
                    break;
            }
        });
    }

    private Intent getNextIntent() {
        String userId = PreferenceUtils.getUserId();
        if (!TextUtils.isEmpty(userId)) {
            return new Intent(SplashActivity.this, HomeActivity.class);
        }

        return new Intent(SplashActivity.this, LoginActivity.class);
    }
}
