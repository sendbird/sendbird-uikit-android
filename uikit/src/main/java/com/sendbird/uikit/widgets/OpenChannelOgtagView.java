package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sendbird.android.OGMetaData;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewOpenChannelOgtagBinding;
import com.sendbird.uikit.utils.DrawableUtils;
import com.sendbird.uikit.utils.ImageUtils;
import com.sendbird.uikit.utils.TextUtils;

public class OpenChannelOgtagView extends FrameLayout {

    private SbViewOpenChannelOgtagBinding binding;

    public OpenChannelOgtagView(@NonNull Context context) {
        this(context, null);
    }

    public OpenChannelOgtagView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_open_channel_message_user_style);
    }

    public OpenChannelOgtagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, null);
    }

    public static OpenChannelOgtagView inflate(@NonNull Context context, @Nullable ViewGroup parent) {
        return new OpenChannelOgtagView(context, null, R.attr.sb_open_channel_message_user_style, parent);
    }

    private OpenChannelOgtagView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, ViewGroup parent) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, parent);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, ViewGroup parent) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageView, defStyleAttr, 0);
        try {
            if (parent == null) {
                parent = this;
            }
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sb_view_open_channel_ogtag, parent, true);
            int ogtagTitleAppearence = a.getResourceId(R.styleable.MessageView_sb_message_ogtag_title_appearance, R.style.SendbirdBody3OnLight01);
            int ogtagDescAppearence = a.getResourceId(R.styleable.MessageView_sb_message_ogtag_description_appearance, R.style.SendbirdCaption2OnLight01);
            int ogtagUrlAppearence = a.getResourceId(R.styleable.MessageView_sb_message_ogtag_url_appearance, R.style.SendbirdCaption2OnLight02);

            binding.tvOgTitle.setTextAppearance(context, ogtagTitleAppearence);
            binding.tvOgDescription.setTextAppearance(context, ogtagDescAppearence);
            binding.tvOgUrl.setTextAppearance(context, ogtagUrlAppearence);
        } finally {
            a.recycle();
        }
    }

    public void drawOgtag(@Nullable OGMetaData metaData) {
        setVisibility(metaData != null ? View.VISIBLE : View.GONE);
        if (metaData == null || binding == null) {
            return;
        }

        if (metaData.getOGImage() != null &&
                (metaData.getOGImage().getSecureUrl() != null || metaData.getOGImage().getUrl() != null)) {
            binding.ivOgImage.setVisibility(VISIBLE);
            String ogImageUrl;
            if (metaData.getOGImage().getSecureUrl() != null) {
                ogImageUrl = metaData.getOGImage().getSecureUrl();
            } else {
                ogImageUrl = metaData.getOGImage().getUrl();
            }

            int thumbnailIconTint = SendBirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;

            RequestBuilder<Drawable> builder = Glide.with(getContext())
                    .asDrawable()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(DrawableUtils.setTintList(
                            ImageUtils.resize(getContext().getResources(), AppCompatResources.getDrawable(getContext(), R.drawable.icon_photo), R.dimen.sb_size_48, R.dimen.sb_size_48),
                            AppCompatResources.getColorStateList(getContext(), thumbnailIconTint)))
                    .error(DrawableUtils.setTintList(
                            ImageUtils.resize(getContext().getResources(), AppCompatResources.getDrawable(getContext(), R.drawable.icon_thumbnail_none), R.dimen.sb_size_48, R.dimen.sb_size_48),
                            AppCompatResources.getColorStateList(getContext(), thumbnailIconTint)));;

            binding.ivOgImage.setRadius(getResources().getDimensionPixelSize(R.dimen.sb_size_8));
            binding.ivOgImage.getContent().setScaleType(ImageView.ScaleType.CENTER);
            builder.load(ogImageUrl).centerCrop().thumbnail(0.3f).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    binding.ivOgImage.getContent().setScaleType(ImageView.ScaleType.CENTER_CROP);
                    return false;
                }
            }).into(binding.ivOgImage.getContent());
        } else {
            binding.ivOgImage.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(metaData.getTitle())) {
            binding.tvOgTitle.setVisibility(VISIBLE);
            binding.tvOgTitle.setText(metaData.getTitle());
        } else {
            binding.tvOgTitle.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(metaData.getDescription())) {
            binding.tvOgDescription.setVisibility(VISIBLE);
            binding.tvOgDescription.setText(metaData.getDescription());
        } else {
            binding.tvOgDescription.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(metaData.getUrl())) {
            binding.tvOgUrl.setVisibility(VISIBLE);
            binding.tvOgUrl.setText(metaData.getUrl());
        } else {
            binding.tvOgUrl.setVisibility(GONE);
        }
    }
}
