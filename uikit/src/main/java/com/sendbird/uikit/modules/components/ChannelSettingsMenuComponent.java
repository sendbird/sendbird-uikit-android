package com.sendbird.uikit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.SingleMenuType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.MenuViewProvider;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.internal.ui.widgets.SingleMenuItemView;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.configurations.ChannelSettingConfig;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit.utils.ChannelUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class creates and performs a view corresponding the channel settings menu area in Sendbird UIKit.
 * since 3.0.0
 */
public class ChannelSettingsMenuComponent {
    public enum Menu {
        /**
         * A menu of Moderations to users or members control.
         */
        MODERATIONS,

        /**
         * A menu of notification settings to switch tern on and off.
         */
        NOTIFICATIONS,

        /**
         * A menu for viewing the members of the current channel.
         */
        MEMBERS,

        /**
         * A menu to leave the current channel.
         */
        LEAVE_CHANNEL,

        /**
         * A menu to search messages in the current channel.
         */
        SEARCH_IN_CHANNEL,

        /**
         * A custom menu.
         */
        CUSTOM,
    }

    /**
     * A collection of default menus.
     * since 3.16.0
     */
    public final static List<Menu> defaultMenuSet = Collections.unmodifiableList(
        Arrays.asList(
            Menu.MODERATIONS,
            Menu.NOTIFICATIONS,
            Menu.MEMBERS,
            Menu.SEARCH_IN_CHANNEL,
            Menu.LEAVE_CHANNEL
        )
    );

    @NonNull
    private final Params params;
    @Nullable
    private View menuView;

    @Nullable
    protected OnItemClickListener<Menu> menuClickListener;

    @NonNull
    private final Map<Menu, SingleMenuItemView> defaultMenuViews = new HashMap<>();

    /**
     * Constructor
     * since 3.0.0
     */
    public ChannelSettingsMenuComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.menuView;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final TypedValue values = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.sb_component_channel_settings_menu, values, true);
        final Context menuThemeContext = new ContextThemeWrapper(context, values.resourceId);
        final LinearLayout layout = new LinearLayout(menuThemeContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < params.getMenuList().size(); i++) {
            final Menu menu = params.getMenuList().get(i);
            final View menuView = createMenuView(menuThemeContext, menu, i);
            if (menu != Menu.CUSTOM) {
                menuView.setOnClickListener(v -> onMenuClicked(v, menu));
                defaultMenuViews.put(menu, (SingleMenuItemView) menuView);
            }
            layout.addView(menuView);
        }
        this.menuView = layout;
        return layout;
    }

    /**
     * Creates a custom menu view.
     *
     * @param context The {@code Context} this component is currently associated with
     * @param title The title of the menu
     * @param type The type of the menu
     * @param iconResId The icon resource id of the menu
     * @return The custom menu view
     * since 3.16.0
     */
    @NonNull
    public View createMenuView(
        @NonNull Context context,
        @NonNull String title,
        @Nullable String description,
        @NonNull SingleMenuType type,
        @DrawableRes int iconResId,
        @ColorRes int iconTintResId
    ) {
        return SingleMenuItemView.createMenuView(context, title, description, type, iconResId, iconTintResId);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (this.menuView == null) return;

        final SingleMenuItemView membersItemView = defaultMenuViews.get(Menu.MEMBERS);
        if (membersItemView != null) {
            membersItemView.setDescription(ChannelUtils.makeMemberCountText(channel.getMemberCount()).toString());
        }

        final SingleMenuItemView notificationItemView = defaultMenuViews.get(Menu.NOTIFICATIONS);
        if (notificationItemView != null) {
            final GroupChannel.PushTriggerOption pushTriggerOption = channel.getMyPushTriggerOption();
            notificationItemView.setDescription(ChannelUtils.makePushSettingStatusText(menuView.getContext(), pushTriggerOption));
        }

        final SingleMenuItemView moderationsItemView = defaultMenuViews.get(Menu.MODERATIONS);
        if (moderationsItemView != null) {
            moderationsItemView.setVisibility(channel.getMyRole() == Role.OPERATOR ? View.VISIBLE : View.GONE);
        }
        final SingleMenuItemView messageSearchItemView = defaultMenuViews.get(Menu.SEARCH_IN_CHANNEL);
        if (messageSearchItemView != null) {
            messageSearchItemView.setVisibility(
                ChannelSettingConfig.getEnableMessageSearch(params.channelSettingConfig) ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     * The click event about the {@link Menu#CUSTOM} menu won’t be called.
     * If you want to handle the {@link Menu#CUSTOM} menu, you should handle it yourself after creating a custom menu view.
     *
     * @param menuClickListener The callback that will run
     * @see Menu
     * since 3.0.0
     */
    public void setOnMenuClickListener(@NonNull OnItemClickListener<Menu> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * @param menu The menu that the clicked item displays
     * @see Menu
     * since 3.0.0
     */
    protected void onMenuClicked(@NonNull View view, @NonNull Menu menu) {
        if (this.menuClickListener != null) this.menuClickListener.onItemClick(view, 0, menu);
    }

    @NonNull
    private View createMenuView(@NonNull Context context, @NonNull Menu menu, int position) {
        if (menu == Menu.CUSTOM) {
            final MenuViewProvider provider = params.getCustomMenuViewProvider();
            if (provider != null) {
                return provider.provideMenuView(context, position);
            }
            Logger.d("MenuViewProvider is not set. Creating a default View.");
            return new View(context);
        }

        return createDefaultMenuView(context, menu);
    }

    @NonNull
    private View createDefaultMenuView(@NonNull Context context, @NonNull Menu menu) {
        final SingleMenuItemView menuView = new SingleMenuItemView(context);
        switch (menu) {
            case MODERATIONS:
                menuView.setName(context.getString(R.string.sb_text_channel_settings_moderations));
                menuView.setMenuType(SingleMenuType.NEXT);
                menuView.setIcon(R.drawable.icon_moderations);
                menuView.setVisibility(View.GONE);
                break;
            case NOTIFICATIONS:
                menuView.setName(context.getString(R.string.sb_text_channel_settings_notification));
                menuView.setMenuType(SingleMenuType.NEXT);
                menuView.setIcon(R.drawable.icon_notifications);
                break;
            case MEMBERS:
                menuView.setName(context.getString(R.string.sb_text_channel_settings_members));
                menuView.setMenuType(SingleMenuType.NEXT);
                menuView.setIcon(R.drawable.icon_members);
                break;
            case LEAVE_CHANNEL:
                menuView.setName(context.getString(R.string.sb_text_channel_settings_leave_channel));
                menuView.setMenuType(SingleMenuType.NONE);
                menuView.setIcon(R.drawable.icon_leave);
                menuView.setIconTint(SendbirdUIKit.getDefaultThemeMode().getErrorTintColorStateList(context));
                break;
            case SEARCH_IN_CHANNEL:
                menuView.setName(context.getString(R.string.sb_text_channel_settings_message_search));
                menuView.setMenuType(SingleMenuType.NONE);
                menuView.setIcon(R.drawable.icon_search);
                menuView.setVisibility(
                    ChannelSettingConfig.getEnableMessageSearch(params.channelSettingConfig) ? View.VISIBLE : View.GONE);
                break;
        }
        return menuView;
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        @NonNull
        private ChannelSettingConfig channelSettingConfig = UIKitConfig.getGroupChannelSettingConfig();

        @NonNull
        private List<Menu> menuList = defaultMenuSet;

        @Nullable
        private MenuViewProvider customMenuViewProvider;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the {@link ChannelSettingConfig} for the channel settings menu.
         * Use {@code UIKitConfig.groupChannelSettingConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val channelSettingsMenuComponent = ChannelSettingsMenuComponent()
         * channelSettingsMenuComponent.params.channelSettingConfig = UIKitConfig.groupChannelSettingConfig.clone().apply {
         *     this.enableMessageSearch = true
         * }
         * </pre>
         *
         * @param channelSettingConfig The ChannelSettingConfig for the channel settings menu.
         * @see ChannelSettingConfig
         * since 3.6.0
         */
        public void setChannelSettingConfig(@NonNull ChannelSettingConfig channelSettingConfig) {
            this.channelSettingConfig = channelSettingConfig;
        }

        /**
         * Returns the {@link ChannelSettingConfig} for the channel settings menu.
         *
         * @return The ChannelSettingConfig for the channel settings menu.
         * @see ChannelSettingConfig
         * since 3.6.0
         */
        @NonNull
        public ChannelSettingConfig getChannelSettingConfig() {
            return channelSettingConfig;
        }

        /**
         * Sets the list of menus to be displayed in the channel settings menu.
         * If the CUSTOM menu is included in the list, you must set the {@link MenuViewProvider} to create a custom menu view.
         *
         * @param menuList A list of settings menus in guaranteed with order.
         * @param provider The provider to create custom menu view.
         * since 3.16.0
         */
        public void setMenuList(@NonNull List<Menu> menuList, @Nullable MenuViewProvider provider) {
            this.menuList = menuList;
            this.customMenuViewProvider = provider;
        }

        /**
         * Returns the list of menus to be displayed in the channel settings menu.
         *
         * @return A list of settings menus in guaranteed with order.
         * since 3.16.0
         */
        @NonNull
        public List<Menu> getMenuList() {
            return menuList;
        }

        /**
         * Returns the {@link MenuViewProvider} to create a custom menu view.
         *
         * @return The provider to create custom menu view.
         * since 3.16.0
         */
        @Nullable
        public MenuViewProvider getCustomMenuViewProvider() {
            return customMenuViewProvider;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_CHANNEL_SETTING_CONFIG} is mapped to {@link #setChannelSettingConfig(ChannelSettingConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_CHANNEL_SETTING_CONFIG)) {
                setChannelSettingConfig(args.getParcelable(StringSet.KEY_CHANNEL_SETTING_CONFIG));
            }
            return this;
        }
    }
}
