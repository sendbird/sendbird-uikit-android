package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.R;

public class ThemeableRecyclerView extends RecyclerView {
    private final DividerItemDecoration dividerDecoration;
    private final float dividerMarginRight;
    private final float dividerMarginLeft;
    private int dividerColor;
    private float dividerHeight;

    public ThemeableRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public ThemeableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThemeableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ListComponent, defStyle, 0);
        try {
            int backgroundResId = a.getResourceId(R.styleable.ListComponent_sb_recycler_view_background, R.color.background_50);
            dividerColor = a.getColor(R.styleable.ListComponent_sb_recycler_view_divide_line_color, context.getResources().getColor(R.color.onlight_04));
            dividerHeight = a.getDimension(R.styleable.ListComponent_sb_recycler_view_divide_line_height, context.getResources().getDimensionPixelSize(R.dimen.sb_size_1));
            dividerMarginLeft = a.getDimension(R.styleable.ListComponent_sb_recycler_view_divide_margin_left, 0);
            dividerMarginRight = a.getDimension(R.styleable.ListComponent_sb_recycler_view_divide_margin_right, 0);

            setBackgroundResource(backgroundResId);
            final Drawable divider = createDividerDrawable((int) dividerHeight, dividerColor, (int) dividerMarginLeft, (int) dividerMarginRight);
            dividerDecoration = new DividerItemDecoration(context, LinearLayout.VERTICAL) {
                @Override
                public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                    int dividerLeft = parent.getPaddingLeft();
                    int dividerRight = parent.getWidth() - parent.getPaddingRight();

                    int childCount = parent.getChildCount();
                    for (int i = 0; i <= childCount - 2; i++) {
                        View child = parent.getChildAt(i);

                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                        int dividerTop = child.getBottom() + params.bottomMargin;
                        int dividerBottom = dividerTop + divider.getIntrinsicHeight();

                        divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                        divider.draw(canvas);
                    }
                }
            };
            dividerDecoration.setDrawable(divider);
            setUseDivider(true);

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
        Drawable divider = createDividerDrawable((int) dividerHeight, dividerColor, (int) dividerMarginLeft, (int) dividerMarginRight);
        dividerDecoration.setDrawable(divider);
    }

    public void setDividerHeight(float dividerHeight) {
        this.dividerHeight = dividerHeight;
        Drawable divider = createDividerDrawable((int) dividerHeight, dividerColor, (int) dividerMarginLeft, (int) dividerMarginRight);
        dividerDecoration.setDrawable(divider);
    }
}
