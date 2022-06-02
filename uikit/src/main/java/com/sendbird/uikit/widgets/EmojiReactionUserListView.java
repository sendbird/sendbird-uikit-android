package com.sendbird.uikit.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sendbird.android.Reaction;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.EmojiReactionUserListAdapter;
import com.sendbird.uikit.databinding.SbFragmentUserListBinding;
import com.sendbird.uikit.databinding.SbViewEmojiReactionUserListBinding;
import com.sendbird.uikit.model.EmojiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmojiReactionUserListView extends FrameLayout {
    private SbViewEmojiReactionUserListBinding binding;

    public EmojiReactionUserListView(@NonNull Context context) {
        this(context, null);
    }

    public EmojiReactionUserListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.sb_widget_emoji_message);
    }

    public EmojiReactionUserListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiReactionCountList, defStyleAttr, R.style.Widget_Sendbird_Emoji);

        try {
            binding = SbViewEmojiReactionUserListBinding.inflate(LayoutInflater.from(context), this, true);
            int tabLayoutBackgroundResId = a.getResourceId(R.styleable.EmojiReactionCountList_sb_emoji_reaction_count_tab_layout_background, R.drawable.sb_tab_layout_border_background_light);
            int indicatorColor = a.getColor(R.styleable.EmojiReactionCountList_sb_emoji_reaction_count_tab_indicator_color, context.getResources().getColor(R.color.primary_300));
            binding.tabLayoutPanel.setBackgroundResource(tabLayoutBackgroundResId);
            binding.tabLayout.setSelectedTabIndicatorColor(indicatorColor);
        } finally {
            a.recycle();
        }
    }

    public void setEmojiReactionUserData(@NonNull Fragment fragment, int currentPosition, @NonNull List<Reaction> reactionList, @NonNull Map<Reaction, List<User>> reactionUserInfo) {
        UserListPagerAdapter pagerAdapter = new UserListPagerAdapter(getContext(), fragment, reactionList, reactionUserInfo);
        binding.vpEmojiReactionUserList.setAdapter(pagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.vpEmojiReactionUserList,
                (tab, position) -> {
                    EmojiReactionCountView view = new EmojiReactionCountView(getContext());
                    Reaction reaction = reactionList.get(position);
                    if (reaction != null && reaction.getUserIds() != null) {
                        view.setCount(reaction.getUserIds().size());
                        view.setEmojiUrl(EmojiManager.getInstance().getEmojiUrl(reaction.getKey()));
                    }
                    tab.setCustomView(view);
                }
        ).attach();

        TabLayout.Tab defaultTab = binding.tabLayout.getTabAt(currentPosition);
        if (defaultTab != null) {
            defaultTab.select();
        }
    }

    private static class UserListPagerAdapter extends FragmentStateAdapter {
        private final int itemCount;
        private final List<Fragment> fragmentList = new ArrayList<>();

        UserListPagerAdapter(@NonNull Context context,
                             @NonNull Fragment fragment,
                             @NonNull List<Reaction> reactionList,
                             @NonNull Map<Reaction, List<User>> reactionUserInfo) {
            super(fragment);
            this.itemCount = reactionUserInfo.size();
            for (Reaction reaction: reactionList) {
                final List<User> userList = reactionUserInfo.get(reaction);
                fragmentList.add(new UserListFragment(context, userList == null ? Collections.emptyList(): userList));
            }
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    public static class UserListFragment extends Fragment {
        private final Context context;
        private SbFragmentUserListBinding binding;
        private final List<User> userList;

        UserListFragment(@NonNull Context context, @NonNull List<User> userList) {
            this.context = context;
            this.userList = new ArrayList<>(userList);
        }

        @Override
        @NonNull
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            LayoutInflater themeInflater = inflater.cloneInContext(context);
            binding = SbFragmentUserListBinding.inflate(themeInflater);
            binding.rvUserList.setUseDivider(false);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            EmojiReactionUserListAdapter userListAdapter = new EmojiReactionUserListAdapter(userList);
            binding.rvUserList.setAdapter(userListAdapter);
            binding.rvUserList.setHasFixedSize(true);
        }
    }
}
