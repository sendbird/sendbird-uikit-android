package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.modules.components.HeaderComponent;

public class CustomModerationHeaderComponent extends HeaderComponent {
    public CustomModerationHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = new Toolbar(context);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.sb_size_56)));
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.primary_300));
        toolbar.setTitle(R.string.sb_text_channel_settings_moderations);
        toolbar.setTitleTextAppearance(context, R.style.SendbirdH1OnDark01);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_left);
        toolbar.setNavigationOnClickListener(this::onLeftButtonClicked);
        return toolbar;
    }
}
