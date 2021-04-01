package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.sendbird.uikit.R;

public class RoundCornerView extends ViewGroup {
    private final Paint roundingPaint = new Paint();
    private final RectF canvasBounds = new RectF();
    private final Canvas tempCanvas = new Canvas();
    private Bitmap tempBitmap;
    private float radius;
    private final AppCompatImageView child;

    public RoundCornerView(Context context) {
        this(context, null);
    }

    public RoundCornerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundCornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        roundingPaint.setAntiAlias(true);
        roundingPaint.setFilterBitmap(true);

        this.radius = getContext().getResources().getDimension(R.dimen.sb_size_16);
        this.child = new AppCompatImageView(getContext());
        this.child.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.child.setMinimumWidth((int) getContext().getResources().getDimension(R.dimen.sb_size_100));
        this.child.setMinimumHeight((int) getContext().getResources().getDimension(R.dimen.sb_size_100));
        this.child.setMaxWidth((int) getContext().getResources().getDimension(R.dimen.sb_message_max_width));
        this.child.setMaxHeight((int) getContext().getResources().getDimension(R.dimen.sb_message_max_height));
        addView(child);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        child.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (isInEditMode()) {
            Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setColor(getResources().getColor(R.color.background_400));
            canvas.drawRoundRect(new RectF(0, 0, width, height), 0, 0, debugPaint);
            return;
        }

        if (width <= 0 || height <=0) {
            super.dispatchDraw(canvas);
            return;
        }

        if (tempBitmap == null || tempBitmap.isRecycled() || (tempBitmap.getWidth() != getWidth() || tempBitmap.getHeight() != getHeight())) {
            if (tempBitmap != null) {
                tempBitmap.recycle();
            }
            try {
                tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
            }
        } else {
            tempBitmap.eraseColor(Color.TRANSPARENT);
        }

        tempCanvas.setBitmap(tempBitmap);
        super.dispatchDraw(tempCanvas); // draw children
        roundingPaint.setShader(new BitmapShader(tempBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvasBounds.set(0, 0, width, height);
        canvas.drawRoundRect(canvasBounds, radius, radius, roundingPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);
        child.measure(MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (tempBitmap != null) {
            tempBitmap.recycle();
        }
    }

    public ImageView getContent() {
        return child;
    }
}
