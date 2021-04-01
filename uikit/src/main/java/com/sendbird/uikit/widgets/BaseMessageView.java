package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import com.sendbird.uikit.model.HighlightMessageInfo;

public abstract class BaseMessageView extends FrameLayout {
    protected HighlightMessageInfo highlightMessageInfo;
    protected int highlightBackgroundColor;
    protected int highlightForegroundColor;

    public BaseMessageView(@NonNull Context context) {
        super(context);
    }

    public BaseMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    abstract public View getLayout();

    abstract public ViewDataBinding getBinding();

    public void setHighlightMessageInfo(HighlightMessageInfo highlightMessageInfo) {
        this.highlightMessageInfo = highlightMessageInfo;
    }
}
