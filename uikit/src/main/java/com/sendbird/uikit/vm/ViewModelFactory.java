package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageListParams;
import com.sendbird.android.MessageSearchQuery;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.CustomMemberListQueryHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;

import java.util.Objects;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Object[] params;

    public ViewModelFactory() {
        this.params = null;
    }

    public ViewModelFactory(Object... params) {
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChannelViewModel.class)) {
            return (T) new ChannelViewModel((GroupChannel) Objects.requireNonNull(params)[0]);
        } else if (modelClass.isAssignableFrom(ChannelListViewModel.class)) {
            return (T) new ChannelListViewModel();
        } else if (modelClass.isAssignableFrom(SelectableUserInfoListViewModel.class)) {
            return (T) new SelectableUserInfoListViewModel(params != null ? (CustomUserListQueryHandler) params[0] : null);
        } else if (modelClass.isAssignableFrom(UserTypeListViewModel.class)) {
            return (T) new UserTypeListViewModel((BaseChannel) Objects.requireNonNull(params)[0], (CustomMemberListQueryHandler<User>) params[1]);
        } else if (modelClass.isAssignableFrom(OpenChannelViewModel.class)) {
            return (T) new OpenChannelViewModel((OpenChannel) Objects.requireNonNull(params)[0], (MessageListParams) params[1]);
        } else if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel((GroupChannel) Objects.requireNonNull(params)[0], (MessageSearchQuery) Objects.requireNonNull(params)[1]);
        } else {
            return super.create(modelClass);
        }
    }
}
