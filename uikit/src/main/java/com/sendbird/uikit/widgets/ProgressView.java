package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.utils.DrawableUtils;

public class ProgressView extends ProgressBar {
    public ProgressView(Context context) {
        super(context);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int loadingTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        Drawable loading = DrawableUtils.setTintList(context, R.drawable.sb_progress, loadingTint);
        this.setIndeterminateDrawable(loading);
    }
}
