package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

class WrapHeightImageView extends AppCompatImageView {
    private int targetWidth = 0;
    private int targetHeight = 0;

    public WrapHeightImageView(Context context) {
        this(context, null);
    }

    public WrapHeightImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrapHeightImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSize(int width, int height) {
        this.targetWidth = width;
        this.targetHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        if (targetWidth > 0 && targetHeight > 0) {
            applyMeasuredDimension(targetWidth, targetHeight);
        } else {
            Drawable d = getDrawable();
            if (d != null) {
                int dWidth = d.getIntrinsicWidth();
                int dHeight = d.getIntrinsicHeight();
                applyMeasuredDimension(dWidth, dHeight);
            } else {
                if (targetWidth > 0 && targetHeight > 0) {
                    applyMeasuredDimension(targetWidth, targetHeight);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }

    private void applyMeasuredDimension(int width, int height) {
        float minWidth = getMinimumWidth();
        float minHeight = getMinimumHeight();
        float maxWidth = getMaxWidth();
        float maxHeight = getMaxHeight();

        float newWidth;
        float newHeight;
        if (width < minWidth) {
            newHeight = minWidth / width * height;
            if (newHeight > minHeight) {
                newWidth = minWidth;
                newHeight = Math.min(newHeight, maxHeight);
            } else {
                newWidth = Math.min(minHeight / height * width, maxWidth);
                newHeight = minHeight;
            }
        } else {
            if (width > maxWidth) {
                newHeight = maxWidth * height / width;
                if (newHeight > minHeight) {
                    newWidth = maxWidth;
                    newHeight = Math.min(newHeight, maxHeight);
                } else {
                    newWidth = Math.min(minHeight / height * width, maxWidth);
                    newHeight = minHeight;
                }
            } else {
                if (height > minHeight) {
                    newWidth = width;
                    newHeight = Math.min(height, maxHeight);
                } else {
                    newWidth = Math.min(minHeight / height * width, maxWidth);
                    newHeight = minHeight;
                }
            }
        }
        setMeasuredDimension((int)Math.ceil(newWidth), (int)Math.ceil(newHeight));
    }
}
