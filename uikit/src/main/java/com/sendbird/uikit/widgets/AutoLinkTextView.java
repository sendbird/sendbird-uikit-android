package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.util.Linkify;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.sendbird.uikit.R;
import com.sendbird.uikit.log.Logger;

public class AutoLinkTextView extends AppCompatTextView {
    private SBLinkMovementMethod.OnLinkClickListener onLinkClickListener;
    private SBLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener;
    private int clickedLinkBackgroundColor;
    private int clickedLinkTextColor;
    private int linkifyMask = Linkify.ALL;

    public AutoLinkTextView(Context context) {
        this(context, null);
    }

    public AutoLinkTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoLinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AutoLinkTextView, defStyleAttr, 0);
        try {
            linkifyMask = a.getInt(R.styleable.AutoLinkTextView_sb_auto_link_text_view_linkify_mask, Linkify.ALL);
            clickedLinkBackgroundColor = a.getResourceId(R.styleable.AutoLinkTextView_sb_auto_link_text_view_clicked_background_color, 0);
            clickedLinkTextColor = a.getResourceId(R.styleable.AutoLinkTextView_sb_auto_link_text_view_clicked_text_color, 0);
        } catch (Exception e) {
            Logger.e(e);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        try {
            Linkify.addLinks(this, linkifyMask);
            setMovementMethod(new SBLinkMovementMethod.Builder()
                    .setOnLinkClickListener(onLinkClickListener)
                    .setOnLinkLongClickListener(onLinkLongClickListener)
                    .setClickedLinkBackgroundColor(clickedLinkBackgroundColor)
                    .setClickedLinkTextColor(clickedLinkTextColor)
                    .create());
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    public void setOnLinkClickListener(SBLinkMovementMethod.OnLinkClickListener onLinkClickListener) {
        this.onLinkClickListener = onLinkClickListener;
    }

    public void setOnLinkLongClickListener(SBLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener) {
        this.onLinkLongClickListener = onLinkLongClickListener;
    }

    void setClickedLinkBackgroundColor(int clickedLinkBackgroundColor) {
        this.clickedLinkBackgroundColor = clickedLinkBackgroundColor;
    }

    void setClickedLinkTextColor(int clickedLinkTextColor) {
        this.clickedLinkTextColor = clickedLinkTextColor;
    }

    public int getLinkifyMask() {
        return linkifyMask;
    }
}
