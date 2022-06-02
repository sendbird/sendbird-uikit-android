package com.sendbird.uikit.customsample.groupchannel.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.MessageListParams;

import java.util.Objects;

/**
 * Provides to create the customized <code>ViewModel</code>s.
 */
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Object[] params;

    public ViewModelFactory(@NonNull Object... params) {
        this.params = params;
    }   

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CustomChannelViewModel.class)) {
            return (T) new CustomChannelViewModel((String) Objects.requireNonNull(params)[0], params.length > 1 ? (MessageListParams) params[1] : null);
        } else {
            return super.create(modelClass);
        }
    }
}
