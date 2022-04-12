package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.modules.components.HeaderComponent;

public class CustomUserTypedHeaderComponent extends HeaderComponent {

    public CustomUserTypedHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = new Toolbar(context);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.sb_size_56)));
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.primary_300));
        toolbar.setTitle(getParams().getTitle());
        toolbar.setTitleTextAppearance(context, R.style.SendbirdH1OnDark01);
        toolbar.inflateMenu(R.menu.user_typed_menu);
        toolbar.setNavigationIcon(getParams().getLeftButtonIcon());
        toolbar.setNavigationOnClickListener(this::onLeftButtonClicked);
            ((AppCompatImageView) toolbar.getMenu().findItem(R.id.action_right)
                    .getActionView().findViewById(R.id.rightIcon)).setImageDrawable(getParams().getRightButtonIcon());
            toolbar.getMenu().findItem(R.id.action_right)
                    .getActionView().setOnClickListener(v -> {
                Logger.d("++ right button clicked");
                onRightButtonClicked(v);
        });
        return toolbar;
    }
}
