package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.modules.components.SelectUserHeaderComponent;

/**
 * Implements the customized <code>SelectUserHeaderComponent</code>.
 */
public class CustomSelectUserHeaderComponent extends SelectUserHeaderComponent {
    private Toolbar toolbar;

    public CustomSelectUserHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        toolbar = new Toolbar(context);
        toolbar.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.sb_size_56)));
        toolbar.setBackgroundColor(context.getResources().getColor(R.color.primary_300));
        toolbar.setTitleTextAppearance(context, R.style.SendbirdH1OnDark01);
        toolbar.setSubtitleTextAppearance(context, R.style.SendbirdCaption1OnDark02);
        toolbar.setTitle(getParams().getTitle());
        toolbar.inflateMenu(R.menu.select_user_menu);
        toolbar.setNavigationIcon(R.drawable.icon_arrow_left);
        toolbar.setNavigationOnClickListener(this::onLeftButtonClicked);

        toolbar.getMenu().findItem(R.id.action_select)
                .getActionView().setOnClickListener(v -> {
            Logger.d("++ select button clicked");
            onRightButtonClicked(v);
        });
        return toolbar;
    }

    @Override
    public void notifySelectedUserChanged(int count) {
        if (count > 0) {
            ((TextView) toolbar.getMenu().findItem(R.id.action_select)
                    .getActionView().findViewById(R.id.tvSelect))
                    .setText(String.format(toolbar.getContext().getString(R.string.text_select_count), count));
        } else {
            ((TextView) toolbar.getMenu().findItem(R.id.action_select)
                    .getActionView().findViewById(R.id.tvSelect))
                    .setText(R.string.sb_text_button_selected);
        }
    }
}
