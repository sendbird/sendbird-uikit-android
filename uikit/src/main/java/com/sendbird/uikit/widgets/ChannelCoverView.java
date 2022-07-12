package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.TextUtils;

import java.util.List;

public class ChannelCoverView extends ImageWaffleView {
    private int defaultImageResId;

    public ChannelCoverView(@NonNull Context context) {
        super(context);
    }

    public ChannelCoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelCoverView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDefaultImageResId(int defaultImageResId) {
        this.defaultImageResId = defaultImageResId;
    }

    public void loadImage(@NonNull String url) {
        ImageView imageView = prepareSingleImageView();
        drawImageFromUrl(imageView, url);
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

    @Nullable
    private Drawable getDefaultDrawable() {
        if (defaultImageResId > 0) {
            return AppCompatResources.getDrawable(getContext(), defaultImageResId);
        } else {
            int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
            int backgroundTint = R.color.background_300;
            return DrawableUtils.createOvalIcon(getContext(),
                    backgroundTint, R.drawable.icon_user, iconTint);
        }
    }

    private void drawImage(@NonNull ImageView imageView, @Nullable Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public void drawBroadcastChannelCover() {
        ImageView imageView = prepareSingleImageView();
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = SendbirdUIKit.getDefaultThemeMode().getSecondaryTintResId();
        imageView.setImageDrawable(DrawableUtils.createOvalIcon(getContext(), backgroundTint, R.drawable.icon_broadcast, iconTint));
    }
}
