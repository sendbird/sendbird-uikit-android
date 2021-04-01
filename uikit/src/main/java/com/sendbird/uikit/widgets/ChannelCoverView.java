package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.util.List;

public class ChannelCoverView extends ImageWaffleView {
    private int defaultImageResId;

    public ChannelCoverView(Context context) {
        super(context);
    }

    public ChannelCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDefaultImageResId(int defaultImageResId) {
        this.defaultImageResId = defaultImageResId;
    }

    public void loadImage(String url) {
        ImageView imageView = prepareSingleImageView();
        if (imageView != null) {
            drawImageFromUrl(imageView, url);
        }
    }

    public void loadImages(@NonNull List<String> imageUrlList) {
        if (imageUrlList.size() <= 0) {
            drawImage(prepareSingleImageView(), getDefaultDrawable());
            return;
        }
        List<ImageView> profileIamges = prepareImageViews(imageUrlList.size());

        int size = Math.min(4, imageUrlList.size());
        for(int i = 0; i < size; i++) {
            ImageView imageView = profileIamges.get(i);
            String url = imageUrlList.get(i);
            drawImageFromUrl(imageView, url);
        }
    }

    private void drawImageFromUrl(@NonNull ImageView imageView, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            drawImage(imageView, getDefaultDrawable());
            return;
        }
        int overrideSize = getResources()
                .getDimensionPixelSize(R.dimen.sb_size_64);

        Glide.with(imageView.getContext())
                .load(url)
                .override(overrideSize, overrideSize)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(getDefaultDrawable())
                .into(imageView);
    }

    private Drawable getDefaultDrawable() {
        if (defaultImageResId > 0) {
            return AppCompatResources.getDrawable(getContext(), defaultImageResId);
        } else {
            int iconTint = SendBirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
            int backgroundTint = R.color.background_300;
            return DrawableUtils.createOvalIcon(getContext(),
                    backgroundTint, R.drawable.icon_user, iconTint);
        }
    }

    private void drawImage(@NonNull ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void drawBroadcastChannelCover() {
        ImageView imageView = prepareSingleImageView();
        int iconTint = SendBirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = SendBirdUIKit.getDefaultThemeMode().getSecondaryTintResId();
        imageView.setImageDrawable(DrawableUtils.createOvalIcon(getContext(), backgroundTint, R.drawable.icon_broadcast, iconTint));
    }
}
