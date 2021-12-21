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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class ImageWaffleView extends ViewGroup {
    protected final int DIVIDER_WIDTH = 1;
    private final int ROUNDING_RADIUS = 1;
    private static final int ROUND_BORDER = 1;

    private final Paint roundingPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final RectF canvasBounds = new RectF();
    private final Canvas tempCanvas = new Canvas();
    private Bitmap tempBitmap;

    private static class KillerWaffleChildImageView extends AppCompatImageView {

        final ImageWaffleView imageWaffleView;

        KillerWaffleChildImageView(ImageWaffleView imageWaffleView) {
            super(imageWaffleView.getContext());

            this.imageWaffleView = imageWaffleView;

            setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        @Override
        public void requestLayout() {
            // suppress layout for performance
            super.requestLayout();
            imageWaffleView.forceLayout();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            imageWaffleView.invalidate();
        }
    }

    public ImageWaffleView(Context context) {
        this(context, null);
    }

    public ImageWaffleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageWaffleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        roundingPaint.setAntiAlias(true);
        roundingPaint.setFilterBitmap(true);
        borderPaint.setAntiAlias(true);
    }

    protected ImageView prepareSingleImageView() {
        return prepareImageViews(1).get(0);
    }

    protected List<ImageView> prepareImageViews(int length) {
        if (length > 4 || length < 0) {
            throw new IllegalArgumentException("Invalid length : " + length);
        }

        Queue<ImageView> prevImageViews = new LinkedList<>();
        if (getChildCount() == length) {
            for (int i = 0; i < getChildCount(); i++) {
                prevImageViews.add((ImageView) getChildAt(i));
            }
        } else {
            removeAllViews();
        }

        List<ImageView> prepared = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            prepared.add(pollOrNewImageView(prevImageViews));
        }

        ImageView toBeRemoved;
        while ((toBeRemoved = prevImageViews.poll()) != null) {
            removeView(toBeRemoved);
        }

        return Collections.unmodifiableList(prepared);
    }

    private ImageView pollOrNewImageView(Queue<ImageView> prevImageViews) {
        ImageView polled = prevImageViews.poll();
        if (polled != null) {
            return polled;
        }

        ImageView imageView = new KillerWaffleChildImageView(this);
        addView(imageView);
        return imageView;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (isInEditMode()) {
            Paint debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setColor(getResources().getColor(R.color.background_400));
            canvas.drawRoundRect(new RectF(0, 0, width, height), ROUNDING_RADIUS, ROUNDING_RADIUS, debugPaint);
            return;
        }

        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas);
            return;
        }

        if (tempBitmap == null || tempBitmap.isRecycled() || (tempBitmap.getWidth() != width || tempBitmap.getHeight() != height)) {
            if (tempBitmap != null) {
                tempBitmap.recycle();
            }

            try {
                tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e) {
                tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            }
        } else {
            tempBitmap.eraseColor(Color.TRANSPARENT);
        }
        tempCanvas.setBitmap(tempBitmap);

        //drawBackground(tempCanvas); // draw below children
        super.dispatchDraw(tempCanvas); // draw children
        //drawForeground(tempCanvas); // draw above children
        //drawGlass(tempCanvas); // draw top-most layer

        roundingPaint.setShader(new BitmapShader(tempBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        canvasBounds.set(0, 0, width, height);
        borderPaint.setColor(getResources().getColor(SendBirdUIKit.isDarkMode() ? R.color.ondark_04 : R.color.onlight_04));

        canvas.drawRoundRect(canvasBounds, (float)(width / 2), (float)(height / 2), borderPaint);
        canvasBounds.set(ROUND_BORDER + paddingLeft, ROUND_BORDER + paddingTop, width - ROUND_BORDER - paddingRight, height - ROUND_BORDER - paddingBottom);
        canvas.drawRoundRect(canvasBounds, (float)(width / 2), (float)(height / 2), roundingPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        int halfWidth = (specWidth - DIVIDER_WIDTH + ROUND_BORDER) / 2;
        int halfHeight = (specHeight - DIVIDER_WIDTH + ROUND_BORDER) / 2;

        switch (getChildCount()) {
            case 1:
                measureInGrid(getChildAt(0), specWidth, specHeight);
                break;
            case 2:
                measureInGrid(getChildAt(0), halfWidth, specHeight);
                measureInGrid(getChildAt(1), halfWidth, specHeight);
                break;
            case 3:
                measureInGrid(getChildAt(0), specWidth, halfHeight);
                measureInGrid(getChildAt(1), halfWidth, halfHeight);
                measureInGrid(getChildAt(2), halfWidth, halfHeight);
                break;
            case 4:
                measureInGrid(getChildAt(0), halfWidth, halfHeight);
                measureInGrid(getChildAt(1), halfWidth, halfHeight);
                measureInGrid(getChildAt(2), halfWidth, halfHeight);
                measureInGrid(getChildAt(3), halfWidth, halfHeight);
                break;
        }

        setMeasuredDimension(specWidth, specHeight);
    }

    private void measureInGrid(View view, int width, int height) {
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int halfWidth = (getMeasuredWidth() - DIVIDER_WIDTH - ROUND_BORDER) / 2;
        int halfHeight = (getMeasuredHeight() - DIVIDER_WIDTH - ROUND_BORDER) / 2;

        switch (getChildCount()) {
            case 1:
                getChildAt(0).layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
                break;
            case 2:
                getChildAt(0).layout(0, 0, halfWidth, getMeasuredHeight());
                getChildAt(1).layout(getMeasuredWidth() - halfWidth, 0, getMeasuredWidth(), getMeasuredHeight());
                break;
            case 3:
                getChildAt(0).layout(0, 0, getMeasuredWidth(), halfHeight);
                getChildAt(1).layout(0, getMeasuredHeight() - halfHeight, halfWidth, getMeasuredHeight());
                getChildAt(2).layout(getMeasuredWidth() - halfWidth, getMeasuredHeight() - halfHeight, getMeasuredWidth(), getMeasuredHeight());
                break;
            case 4:
                getChildAt(0).layout(0, 0, halfWidth, halfHeight);
                getChildAt(1).layout(getMeasuredWidth() - halfWidth, 0, getMeasuredWidth(), halfHeight);
                getChildAt(2).layout(0, getMeasuredHeight() - halfHeight, halfWidth, getMeasuredHeight());
                getChildAt(3).layout(getMeasuredWidth() - halfWidth, getMeasuredHeight() - halfHeight, getMeasuredWidth(), getMeasuredHeight());
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (tempBitmap != null) {
            tempBitmap.recycle();
        }
    }
}
