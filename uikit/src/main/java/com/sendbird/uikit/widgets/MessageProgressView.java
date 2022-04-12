package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.utils.DrawableUtils;

public class MessageProgressView extends ProgressBar {
    public MessageProgressView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MessageProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        int loadingTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        Drawable loading = DrawableUtils.setTintList(context, R.drawable.sb_message_progress, loadingTint);
        this.setIndeterminateDrawable(loading);
    }
}
