package com.sendbird.uikit.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.adapter.UserListAdapter;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.databinding.SbFragmentSelectUsersBinding;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.vm.SelectableUserInfoListViewModel;
import com.sendbird.uikit.vm.ViewModelFactory;
import com.sendbird.uikit.widgets.StatusFrameView;

import java.util.Collections;
import java.util.List;

/**
 * Fragment displaying the user list.
 *
 * @since 1.2.0
 */
abstract public class SelectUserFragment extends BaseGroupChannelFragment {
    private SbFragmentSelectUsersBinding binding;
    private SelectableUserInfoListViewModel viewModel;
    protected UserListAdapter adapter;
    protected View.OnClickListener headerLeftButtonListener;
    protected CustomUserListQueryHandler customUserListQueryHandler;
    protected String headerRightButtonText = "";

    public SelectUserFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(">> SelectUserFragment::onCreate()");
        Bundle args = getArguments();
        int themeResId = SendBirdUIKit.getDefaultThemeMode().getResId();
        if (args != null) {
            themeResId = args.getInt(StringSet.KEY_THEME_RES_ID);
        }

        if (getActivity() != null) {
            getActivity().setTheme(themeResId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sb_fragment_select_users, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
        initHeaderOnCreated();
        initStatusFrame();
    }

    @Override
    protected void onReadyFailure() {
        Logger.i(">> SelectUserFragment::onReadyFailure()");
        setErrorFrame();
    }

    /**
     * It will be called before {@link #onDrawPage} called.
     * If there is anything to set in the Fragment directly without going through the Builder, set it here.
     */
    protected void onConfigure() {
    }

    /**
     * Draw page with set data.
     */
    protected void onDrawPage() {
        this.viewModel = new ViewModelProvider(getViewModelStore(), new ViewModelFactory(customUserListQueryHandler)).get(SelectableUserInfoListViewModel.class);
        initHeaderOnReady();
        initUserList();
    }

    protected void setErrorFrame() {
        binding.statusFrame.setStatus(StatusFrameView.Status.CONNECTION_ERROR);
        binding.statusFrame.setOnActionEventListener(v -> {
            binding.statusFrame.setStatus(StatusFrameView.Status.LOADING);
            connect();
        });
    }

    private void initStatusFrame() {
        Bundle args = getArguments();

        int emptyIconId = R.drawable.icon_members;
        ColorStateList emptyIconTint = null;
        int emptyTextId = R.string.sb_text_user_list_empty;

        if (args != null) {
            emptyIconId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID, R.drawable.icon_members);
            emptyIconTint = args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT);
            emptyTextId = args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID, R.string.sb_text_user_list_empty);
        }

        binding.statusFrame.setEmptyIcon(emptyIconId);
        binding.statusFrame.setEmptyText(emptyTextId);
        if (args != null && args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
            binding.statusFrame.setIconTint(emptyIconTint);
        }
    }

    private void initHeaderOnCreated() {
        Bundle args = getArguments();
        String headerTitle = getString(R.string.sb_text_header_select_members);
        headerRightButtonText = getString(R.string.sb_text_button_selected);
        boolean useHeader = false;
        boolean useHeaderLeftButton = true;
        boolean useHeaderRightButton = true;
        int headerLeftButtonIconResId = R.drawable.icon_arrow_left;
        ColorStateList headerLeftButtonIconTint = null;

        if (args != null) {
            headerTitle = args.getString(StringSet.KEY_HEADER_TITLE, headerTitle);
            headerRightButtonText = args.getString(StringSet.KEY_HEADER_RIGHT_BUTTON_TEXT, headerRightButtonText);
            useHeader = args.getBoolean(StringSet.KEY_USE_HEADER, false);
            useHeaderLeftButton = args.getBoolean(StringSet.KEY_USE_HEADER_LEFT_BUTTON, true);
            useHeaderRightButton = args.getBoolean(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, true);
            headerLeftButtonIconResId = args.getInt(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID, R.drawable.icon_arrow_left);
            headerLeftButtonIconTint = args.getParcelable(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_TINT);
        }

        setRightButtonText(headerRightButtonText);
        binding.abvSelectUsers.setVisibility(useHeader ? View.VISIBLE : View.GONE);
        binding.abvSelectUsers.getTitleTextView().setText(headerTitle);
        binding.abvSelectUsers.setUseRightButton(useHeaderRightButton);
        binding.abvSelectUsers.setUseLeftImageButton(useHeaderLeftButton);
        binding.abvSelectUsers.setLeftImageButtonResource(headerLeftButtonIconResId);
        if (args != null && args.containsKey(StringSet.KEY_HEADER_LEFT_BUTTON_ICON_RES_ID)) {
            binding.abvSelectUsers.setLeftImageButtonTint(headerLeftButtonIconTint);
        }
        binding.abvSelectUsers.setLeftImageButtonClickListener(v -> finish());
    }

    private void initHeaderOnReady() {
        if (headerLeftButtonListener != null) {
            binding.abvSelectUsers.setLeftImageButtonClickListener(headerLeftButtonListener);
        }

        setRightButtonEnabled(false);
        binding.abvSelectUsers.getRightTextButton().setOnClickListener((v) -> {
            if (adapter != null) {
                onUserSelectComplete(adapter.getSelectedUserList());
            }
        });
    }

    /**
     * Called when the user selection completed.
     *
     * @param selectedUsers selected user's ids.
     * @since 1.2.0
     */
    protected void onUserSelectComplete(List<String> selectedUsers) {
    }

    /**
     * Sets the right button text.
     *
     * @param text {@link CharSequence} set on the invite button
     * @since 1.2.0
     */
    protected void setRightButtonText(CharSequence text) {
        if (binding != null && !TextUtils.isEmpty(text)) {
            binding.abvSelectUsers.setRightTextButtonString(text.toString());
        }
    }

    /**
     * Sets the right button enabled.
     *
     * @param enabled whether the invite button is enabled or not
     * @since 1.2.0
     */
    protected void setRightButtonEnabled(boolean enabled) {
        if (binding != null) {
            binding.abvSelectUsers.setRightTextButtonEnabled(enabled);
        }
    }

    /**
     * Sets the lists of user ids that you want to disable.
     * @return The user id list.
     *
     * @since 1.2.0
     */
    protected List<String> getDisabledUserIds() {
        return Collections.emptyList();
    }

    private void initUserList() {
        if (adapter == null) {
            adapter = new UserListAdapter();
        }

        adapter.setDisabledUserList(getDisabledUserIds());
        adapter.setOnUserSelectChangedListener((selectedUsers, isChecked) -> {
            int count = selectedUsers == null ? 0 : selectedUsers.size();
            setRightButtonEnabled(selectedUsers != null && count > 0);
            setRightButtonText(count > 0 ? count + " " + headerRightButtonText : headerRightButtonText);
        });
        binding.rvSelectableUserList.setAdapter(adapter);
        binding.rvSelectableUserList.setHasFixedSize(true);
        binding.rvSelectableUserList.setPager(viewModel);
        binding.rvSelectableUserList.setThreshold(5);

        viewModel.getStatusFrame().observe(this, binding.statusFrame::setStatus);

        viewModel.getUserList().observe(this, users -> {
            Logger.dev("++ users size : %s", users.size());
            adapter.setItems(users);
        });
    }

    /**
     * Sets the user list adapter.
     *
     * @param adapter the adapter for the user list.
     * @since 1.2.0
     */
    protected <T extends UserListAdapter> void setUserListAdapter(T adapter) {
        this.adapter = adapter;
    }

    /**
     * Sets the click listener on the left button of the header.
     *
     * @param listener The callback that will run.
     * @since 1.2.0
     */
    protected void setHeaderLeftButtonListener(View.OnClickListener listener) {
        this.headerLeftButtonListener = listener;
    }

    /**
     * Sets the handler that loads the list of user.
     *
     * @param handler The callback that will run.
     * @since 1.2.0
     */
    protected void setCustomUserListQueryHandler(CustomUserListQueryHandler handler) {
        this.customUserListQueryHandler = handler;
    }
}
