package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.R;

public class ThemeableRecyclerView extends RecyclerView {
    private boolean useDividerLine = true;
    private DividerItemDecoration dividerDecoration;
    private int dividerColor;
    private float dividerHeight;
    private float dividerMarginLeft;
    private float dividerMarginRight;

    public ThemeableRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ThemeableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_recycler_view_style);
    }

    public ThemeableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ThemeableRecyclerView, defStyle, 0);
        try {
            useDividerLine = a.getBoolean(R.styleable.ThemeableRecyclerView_sb_pager_recycler_view_use_divide_line, true);
            dividerColor = a.getColor(R.styleable.ThemeableRecyclerView_sb_pager_recycler_view_divide_line_color, context.getResources().getColor(R.color.onlight_04));
            dividerHeight = a.getDimension(R.styleable.ThemeableRecyclerView_sb_pager_recycler_view_divide_line_height, context.getResources().getDimensionPixelSize(R.dimen.sb_size_1));
            dividerMarginLeft = a.getDimension(R.styleable.ThemeableRecyclerView_sb_pager_recycler_view_divide_margin_left, 0);
            dividerMarginRight = a.getDimension(R.styleable.ThemeableRecyclerView_sb_pager_recycler_view_divide_margin_right, 0);

            dividerDecoration = new DividerItemDecoration(context, LinearLayout.VERTICAL);
            Drawable divider = createDividerDrawable((int) dividerHeight, dividerColor, (int) dividerMarginLeft, (int) dividerMarginRight);
            dividerDecoration.setDrawable(divider);
            setUseDivider(useDividerLine);

        } finally {
            a.recycle();
        }
    }

    private static Drawable createDividerDrawable(int height, int color, int marginLeft, int marginRight) {
        GradientDrawable divider = new GradientDrawable();
        divider.setShape(GradientDrawable.RECTANGLE);
        divider.setSize(0, height);
        divider.setColor(color);
        return new InsetDrawable(divider, marginLeft, 0, marginRight, 0);
    }

    public void setUseDivider(boolean useDividerLine) {
        if (useDividerLine) {
            addItemDecoration(dividerDecoration);
        } else {
            removeItemDecoration(dividerDecoration);
        }
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }

    public void setDividerHeight(float dividerHeight) {
        this.dividerHeight = dividerHeight;
    }
}
