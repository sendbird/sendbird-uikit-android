package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.sendbird.uikit.model.MessageUIConfig;

public abstract class BaseMessageView extends FrameLayout {
    @Nullable
    protected MessageUIConfig messageUIConfig;

    public BaseMessageView(@NonNull Context context) {
        super(context);
    }

    public BaseMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    abstract public View getLayout();

    @NonNull
    abstract public ViewBinding getBinding();

    public void setMessageUIConfig(@Nullable MessageUIConfig messageUIConfig) {
        this.messageUIConfig = messageUIConfig;
    }
}
