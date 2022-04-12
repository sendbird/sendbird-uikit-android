package com.sendbird.uikit.customsample.groupchannel.components;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.modules.components.MessageSearchHeaderComponent;
import com.sendbird.uikit.utils.TextUtils;

public class CustomMessageSearchHeaderComponent extends MessageSearchHeaderComponent {
    private EditText input;
    @Nullable
    private View.OnClickListener cancelButtonClickListener;

    public CustomMessageSearchHeaderComponent() {
        super();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_custom_message_search_header, parent, false);
        input = view.findViewById(R.id.etSearch);
        TextView cancel = view.findViewById(R.id.cancel);

        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                final String text = input.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    onSearchRequested(input.getText().toString());
                }
                return true;
            }
            return false;
        });
        cancel.setOnClickListener(v -> {
            if (cancelButtonClickListener != null) cancelButtonClickListener.onClick(v);
        });
        return view;
    }

    public void setCancelButtonClickListener(@Nullable View.OnClickListener cancelButtonClickListener) {
        this.cancelButtonClickListener = cancelButtonClickListener;
    }
}
