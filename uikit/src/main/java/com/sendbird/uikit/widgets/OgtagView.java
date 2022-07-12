package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.message.OGMetaData;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.databinding.SbViewOgtagBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ImageUtils;
import com.sendbird.uikit.utils.TextUtils;

public class OgtagView extends FrameLayout {

    private final SbViewOgtagBinding binding;

    public OgtagView(@NonNull Context context) {
        this(context, null);
    }

    public OgtagView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_ogtag);
    }

    public OgtagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, null);
    }

    @NonNull
    public static OgtagView inflate(@NonNull Context context, @Nullable ViewGroup parent) {
        return new OgtagView(context, null, R.attr.sb_widget_ogtag, parent);
    }

    private OgtagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, @Nullable ViewGroup parent) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyleAttr, 0);
        try {
            if (parent == null) {
                parent = this;
            }
            this.binding = SbViewOgtagBinding.inflate(LayoutInflater.from(getContext()), parent, true);
            int ogtagTitleAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_ogtag_title_appearance, R.style.SendbirdBody2OnLight01);
            int ogtagDescriptionAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_ogtag_description_appearance, R.style.SendbirdCaption2OnLight01);
            int ogtagUrlAppearance = a.getResourceId(R.styleable.MessageView_User_sb_message_ogtag_url_appearance, R.style.SendbirdCaption2OnLight02);

            binding.tvOgTitle.setTextAppearance(context, ogtagTitleAppearance);
            binding.tvOgDescription.setTextAppearance(context, ogtagDescriptionAppearance);
            binding.tvOgUrl.setTextAppearance(context, ogtagUrlAppearance);
        } finally {
            a.recycle();
        }
    }

    public void drawOgtag(@Nullable OGMetaData ogMetaData) {
        if (ogMetaData == null || binding == null) {
            return;
        }

        if (ogMetaData.getOgImage() != null &&
                (ogMetaData.getOgImage().getSecureUrl() != null || ogMetaData.getOgImage().getUrl() != null)) {
            binding.ivOgImage.setVisibility(VISIBLE);
            String ogImageUrl;
            if (ogMetaData.getOgImage().getSecureUrl() != null) {
                ogImageUrl = ogMetaData.getOgImage().getSecureUrl();
            } else {
                ogImageUrl = ogMetaData.getOgImage().getUrl();
            }

            int thumbnailIconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
            RequestBuilder<Drawable> builder = Glide.with(getContext())
                    .asDrawable()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(DrawableUtils.setTintList(
                            ImageUtils.resize(getContext().getResources(), AppCompatResources.getDrawable(getContext(), R.drawable.icon_photo), R.dimen.sb_size_48, R.dimen.sb_size_48),
                            AppCompatResources.getColorStateList(getContext(), thumbnailIconTint)))
                    .error(DrawableUtils.setTintList(
                            ImageUtils.resize(getContext().getResources(), AppCompatResources.getDrawable(getContext(), R.drawable.icon_thumbnail_none), R.dimen.sb_size_48, R.dimen.sb_size_48),
                            AppCompatResources.getColorStateList(getContext(), thumbnailIconTint)));

            binding.ivOgImage.setScaleType(ImageView.ScaleType.CENTER);
            builder.load(ogImageUrl).centerCrop().thumbnail(0.3f).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    binding.ivOgImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return false;
                }
            }).into(binding.ivOgImage);
        } else {
            binding.ivOgImage.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(ogMetaData.getTitle())) {
            binding.tvOgTitle.setVisibility(VISIBLE);
            binding.tvOgTitle.setText(ogMetaData.getTitle());
        } else {
            binding.tvOgTitle.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(ogMetaData.getDescription())) {
            binding.tvOgDescription.setVisibility(VISIBLE);
            binding.tvOgDescription.setText(ogMetaData.getDescription());
        } else {
            binding.tvOgDescription.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(ogMetaData.getUrl())) {
            binding.tvOgUrl.setVisibility(VISIBLE);
            binding.tvOgUrl.setText(ogMetaData.getUrl());
        } else {
            binding.tvOgUrl.setVisibility(GONE);
        }
    }
}
