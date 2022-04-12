# V3 Breaking changes

- #### Prerequisites

  - minSDKVersion is increased from **16** to **21**.
  - changed `databinding` of buildFeatures to use `viewbinding`.

- #### Breaking changes

  ##### Common changes

  - All `useHeader` property's default value in `Builder` class has been changed from `false` to `true`.

  - All fragments inherites `BaseModuleFragment` and it has abstract methods related to the module. So below functions are added all fragments. 

    > For more information on this changes of `BaseModuleFragment`, refer to [this](https://sendbird.com/docs/uikit/v1/android/guides/group-channel).
  
    - `onCreateModule(Bundle)`
    - `onConfigureParams(BaseModule, Bundle)`
    - `onCreateViewModel()`
    - `onBeforeReady(ReadyStatus, BaseModule, BaseViewModel)`
    - `onReady(ReadyStatus, BaseModule, BaseViewModel)`

  - Added `Modules` and `Components` classes. Each fragment has it's corresponding module and components.
  
    | fragment                        | module                    | component                                                    | viewmodel                    | style name                   | recyclerview adapter          |
    | ------------------------------- | ------------------------- | ------------------------------------------------------------ | ---------------------------- | ---------------------------- | ----------------------------- |
    | **ChannelListFragment**         | ChannelListModule         | HeaderComponent<br />ChannelListComponent<br />StatusComponent | ChannelListViewModel         | `Module.ChannelList`         | ChanelListAdapter             |
    | **ChannelFragment**             | ChannelModule             | ChannelHeaderComponent<br />MessageListComponent<br />MessageInputComponent<br />StatusComponent | ChannelViewModel             | `Module.Channel`             | MessageListAdapter            |
    | **OpenChannelFragment**         | OpenChannelModule         | OpenChannelHeaderComponent<br />OpenChannelMessageListComponent<br />OpenChannelMessageInputComponent<br />StatusComponent | OpenChannelViewModel         | `Module.OpenChannel`         | OpenChannelMessageListAdapter |
    | **CreateChannelFragment**       | CreateChannelModule       | SelectUserHeaderComponent<br />CreateChannelUserListComponent<br />StatusComponent | CreateChannelViewModel       | `Module.CreateChannel`       | CreateChannelUserListAdapter  |
    | **ChannelSettingsFragment**     | ChannelSettingsModule     | ChannelSettingsHeaderComponent<br />ChannelSettingsInfoComponent<br />ChannelSettingsMenuComponent | ChannelSettingsViewModel     | `Module.ChannelSettings`     |                               |
    | **OpenChannelSettingsFragment** | OpenChannelSettingsModule | OpenChannelSettingsHeaderComponent<br />OpenChannelSettingsInfoComponent<br />OpenChannelSettingsMenuComponent | OpenChannelSettingsViewModel | `Module.OpenChannelSettings` |                               |
    | **InviteUserFragment**          | InviteUserModule          | SelectUserHeaderComponent<br />InviteUserListComponent<br />StatusComponent | InviteUserViewModel          | `Module.InviteUser`          | InviteUserListAdapter         |
    | **PromoteOperatorFragment**     | PromoteOperatorModule     | SelectUserHeaderComponent<br />PromoteOperatorListComponent<br />StatusComponent | PromoteOperatorViewModel     | `Module.PromoteOperators`    | PromoteOperatorListAdapter    |
    | **ModerationFragment**          | ModerationModule          | HeaderComponent<br />ModerationListComponent                 | ModerationViewModel          | `Module.Moderation`          |                               |
    | **MemberListFragment**          | MemberListModule          | HeaderComponent<br />MemberListComponent<br />StatusComponent | MemberListViewModel          | `Module.MemberList`          | MemberListAdapter             |
    | **BannedUserListFragment**      | BannedUserListModule      | HeaderComponent<br />BannedUserListComponent<br />StatusComponent | BannedUserListViewModel      | `Module.BannedUserList`      | BannedUserListAdater          |
    | **MutedMemberListFragment**     | MutedMemberListModule     | HeaderComponent<br />MutedMemberListComponent<br />StatusComponent | MutedMemberListViewModel     | `Module.MutedMemberList`     | MutedMemberListAdapter        |
    | **OperatorListFragment**        | OperatorListModule        | HeaderComponent<br />OperatorListComponent<br />StatusComponent | OperatorListViewModel        | `Module.OperatorList`        | OperatorListAdapter           |
    | **MessageSearchFragment**       | MessageSearchModule       | MessageSearchHeaderComponent<br />MessageSearchListComponent<br />StatusComponent | MessageSearchViewModel       | `Module.MessageSearch`       | MessageSearchAdapter          |
    | **ParticipantListFragment**     | ParticipantListModule     | HeaderComponent<br />ParticipantListComponent<br />StatusComponent | ParticipantViewModel         | `Module.ParticipantList`     | ParticipantListAdapter        |

  - Deleted below functions in all fragments.
  
    - `onConfigure()`
    - `onDrawPage()`
    - `onReadyFailure()`
    
  - Deleted **deprecated** functions in `MessageListAdapter`.
    - MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>)
    - MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>)
    - MessageListAdapter(GroupChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>, boolean)
    - setItems(GroupChannel,  List<BaseMessage>)
    - setOnItemClickListener(OnItemClickListener<BaseMessage>)
    - setOnItemLongClickListener(OnItemLongClickListener<BaseMessage>)
    - setOnProfileClickListener(OnItemClickListener<BaseMessage>)
  
  - Deleted **deprecated** functions in `OpenChannelMessageListAdapter`.
    - OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>)
    - OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>)
    - OpenChannelMessageListAdapter(OpenChannel, OnItemClickListener<BaseMessage>, OnItemLongClickListener<BaseMessage>, boolean)
    - setItems(OpenChannel,  List<BaseMessage>)
    - setOnItemClickListener(OnItemClickListener<BaseMessage>)
    - setOnItemLongClickListener(OnItemLongClickListener<BaseMessage>)
    - setOnProfileClickListener(OnItemClickListener<BaseMessage>)
    
  - Deleted `CustomMemberListQueryHandler` class.
  
  - Replaced interface of  `CreateableChannelType` to `CreatableChannelType`.


------

##### Group channel list

- ###### ChannelListActivity

  - Replaced `ChannelListFragment createChannelListFragment()` to `Fragment createFragment()`.

- ###### ChannelListFragment

  - Deleted `setErrorFrame()`
  - Added `onBindHeaderComponent(HeaderComponent, ChannelListViewModel)`
  - Added `onBindChannelListComponent(ChannelListComponent, ChannelListViewModel)`
  - Added `onBindStatusComponent(StatusComponent, ChannelListViewModel)`
  
- ###### ChannelListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftbuttonClickListener(OnClickListener`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightbuttonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemClickListener)` to `setOnItemLongClickListener(OnItemClickListener)`
  - Deleted `setCustomChannelListFragment(ChannelListFragment)`
  - Deleted `setIncludeEmpty(boolean)`
  - Deleted `setCustomChannelListFragment(ChannelListFragment)`
  - Added `withArguments(Bundle)`

------

  ##### Chat in group channel

- ###### ChannelActivity

    - Replaced `ChannelFragment createChannelFragment(String)` to `Fragment createFragment()`.

- ###### ChannelFragment 

    - Moved `getTooltipMessage(int)` into `MessageListComponent`
    - Replaced `onIdentifiableItemClick(View, String, int, BaseMessage)` to use `onMessageClicked(View, int, BaseMessage)`, `onMessageProfileClicked(View, int, BaseMessage)`, and `onQuoteReplyMessageClicked(View, int, BaseMessage)`.
    - Replaced `onIdentifiableItemLongClick(View, String, int, BaseMessage)` to use `onMessageLongClicked(View, int, BaseMessage)`, `onMessageProfileLongClicked(View, int, BaseMessage)`, and `onQuoteReplyMessageLongClicked(View, int, BaseMessage)`.
    - Added `onBindChannelHeaderComponent(ChannelHeaderComponent, ChannelViewMode, GroupChannel)`
    - Added `onBindMessageListComponent(MessageListComponent, ChannelViewModel, GroupChannel)`
    - Added `onBindMessageInputComponent(MessageInputComponent, ChannelViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, ChannelViewModel, GroupChannel)`

- ###### ChannelFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftbuttonClickListener(OnClickListener`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightbuttonClickListener(OnClickListener)`
  - Replaced `setInputLeftButtonListener(OnClickListener)` to `setOnInputLeftbuttonClickListener(OnClickListener)`
  - Replaced `setOnProfileClickListener(OnItemClickListener<>)` to `setOnMessageProfileClickListener(OnItemClickListener<>)`
  - Deleted `setCustomChannelFragment(ChannelFragment)`
  - Deleted `setItemClickListener(OnItemClickListener)`
  - Deleted `setItemLongClickListener(OnItemClickListener)`
  - Deleted `setListItemClickListener(OnIdentificableItemClickListener<>)`
  - Deleted `setListItemLongClickListener(OnIdentificableItemClickListener<>)`
  - Deleted `setLastSeenAt(boolean)`
  - Deleted `setCustomChannelFragment(ChannelFragment)`
  - Added `withArguments(Bundle)`
  - Added `setOnMessageClickListener(OnItemClickListener)`
  - Added `setOnMessageLongClickListener(OnItemLongClickListener)`
  - Added `setOnQuoteReplyMessageClickListener(OnItemClickListener)`
  - Added `setOnQuoteReplyMessageLongClickListener(OnItemLongClickListener)`


------

  ##### Chat in open channel

- ###### OpenChannelFragment.Builder

  - Replaced `onIdentifiableItemClick(View, String, int, BaseMessage)` to use `onMessageClicked(View, int, BaseMessage)` and  `onMessageProfileClicked(View, int, BaseMessage)`.
  - Replaced `onIdentifiableItemLongClick(View, String, int, BaseMessage)` to use `onMessageLongClicked(View, int, BaseMessage)` and `onMessageProfileLongClicked(View, int, BaseMessage)`.
  - Added `onBindChannelHeaderComponent(OpenChannelChannelHeaderComponent, OpenChannelViewModel, OpenChannel)`
  - Added `onBindMessageListComponent(OpenChannelMessageListComponent, OpenChannelViewModel, OpenChannel)`
  - Added `onBindMessageInputComponent(OpenChannelMessageInputComponent, OpenChannelViewModel, OpenChannel)`
  - Added `onBindStatusComponent(StatusComponent, OpenChannelViewModel, OpenChannel)`

  - ###### OpenChannelFragment.Builder

    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftbuttonClickListener(OnClickListener`
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightbuttonClickListener(OnClickListener)`
    - Replaced `setInputLeftButtonListener(OnClickListener)` to `setOnInputLeftbuttonClickListener(OnClickListener)`
    - Replaced `setOnProfileClickListener(OnItemClickListener<>)` to `setOnMessageProfileClickListener(OnItemClickListener<>)`
    - Deleted `setCustomOpenChannelFragment(OpenChannelFragment)`
    - Deleted `setItemClickListener(OnItemClickListener)`
    - Deleted `setItemLongClickListener(OnItemClickListener)`
    - Deleted `setListItemClickListener(OnIdentificableItemClickListener<>)`
    - Deleted `setListItemLongClickListener(OnIdentificableItemClickListener<>)`
    - Added `withArguments(Bundle)`
    - Added `setOnProfileLongClickListener(OnItemClickListener<>)` to `setOnMessageProfileClickListener(OnItemClickListener<>)`
    - Added `setOnMessageClickListener(OnItemClickListener)`
    - Added `setOnMessageLongClickListener(OnItemLongClickListener)`

------

  ##### Create group channel

- ###### CreateChannelActivity

    - Replaced `CreateChannelFragment createChannelFragment()` and `CreateChannelFragment createChannelFragment(CreatableChannelType)` to `Fragment createFragment()`.

- ###### CreateChannelFragment

    - Deleted `setErrorFrame()` .
    - Replaced `setRightButtonText(CharSequence)` and `setCreateButtonText(CharSequence)`  to use `setRightButtonText(String)` in `StateHeaderComponent.Params`.
    - Replaced `setRightButtonEnabled(boolean)` and `setCreateButtonEnabled(boolean)`  to use `setUseRightButton(boolean)` in `StateHeaderComponent.Params`.
    - Replaced `setUserListAdapter(UserListAdapter)` to use `setAdapter(UserInfoListAdapter)` in `CreateChannelUserListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `StateHeaderComponent`
    - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to use `PagedQueryHandler<UserInfo> createQueryHandler()` in `CreateChannelViewModel`.
    - Added `onBindHeaderComponent(SelectUserHeaderComponent, CreateChannelViewModel)`
    - Added `onBindUserListComponent(CreateChannelUserListComponent, CreateChannelViewModel)`
    - Added `onBindStatusComponent(StatusComponent, CreateChannelViewModel)`

- ###### CreateChannelFragment.Builder

  - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to `setCustomPagedQueryHandler(PagedQueryHandler<UserInfo>)`
  - Replaced `setUserListAdapter(UserListAdapter)` to `setCreateChannelUserListAdapter(CreateChannelUserListAdapter)`
  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Deleted `setCustomCreateChannelFragment(CreateChannelFragment)`
  - Added `withArguments(Bundle)`


------

  ##### Configure group channel settings

  - ###### ChannelSettingsActivity

    - Replaced `ChannelSettingsFragment createChannelSettingsFragment(String)` to `Fragment createFragment()`.

  - ###### ChannelSettingsFragment

    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `ChannelSettingsHeaderComponent`.
    - Replaced `setOnMenuItemClickListener(OnMenuItemClickListener)` to use `setOnMenuClickListener(OnItemClickListener<Menu>)` in `ChannelSettingsMenuComponent`.
    - Added `onBindHeaderComponent(ChannelSettingsHeaderComponent, ChannelSettingsViewModel, GroupChannel)`
    - Added `onBindSettingsInfoComponent(ChannelSettingsInfoComponent, ChannelSettingsViewModel, GroupChannel)`
    - Added `onBindSettingsMenuComponent(ChannelSettingsMenuComponent, ChannelSettingsViewModel, GroupChannel)`
    
- ###### ChannelSettingsFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setOnSettingMenuClickListener(OnMenuItemClickListener)` to `setOnMenuClickListener(OnItemClickListener)`
  - Deleted `setMemberSettingClickListener(OnClickListener)`
  - Deleted `setCustomChannelSettingsFragment(ChannelSettingsFragment)`
  - Added `withArguments(Bundle)`

------

  ##### Configure open channel settings

- ###### OpenChannelSettingsActivity

    - Replaced `OpenChannelSettingsFragment createOpenChannelSettingsFragment(String)` to `Fragment createFragment()`.

- ###### OpenChannelSettingsFragment

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `ChannelSettingsHeaderComponent`.
  - Replaced `setOnMenuItemClickListener(OnMenuItemClickListener)` to use `setOnMenuClickListener(OnItemClickListener<Menu>)` in `ChannelSettingsMenuComponent`.
  - Added `onBindHeaderComponent(OpenChannelSettingsHeaderComponent, OpenChannelSettingsViewModel, OpenChannel)`
  - Added `onBindSettingsInfoComponent(OpenChannelSettingsInfoComponent, OpenChannelSettingsViewModel, OpenChannel)`
  - Added `onBindSettingsMenuComponent(OpenChannelSettingsMenuComponent, OpenChannelSettingsViewModel, OpenChannel)`

- ###### OpenChannelSettingsFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setOnSettingMenuClickListener(OnMenuItemClickListener)` to `setOnMenuClickListener(OnItemClickListener)`
  - Deleted `setCustomOpenChannelSettingsFragment(OpenChannelSettingsFragment)`
  - Deleted `setMemberSettingClickListener(OnClickListener)`
  - Added `withArguments(Bundle)`

------

  ##### Invite users

- ###### InviteChannelActivity

    - Replaced `InviteChannelActivity` to `InviteUserActivity`.
    - Replaced `InviteChannelFragment createInviteChannelFragment(String)` to `Fragment createFragment()`.

- ###### InviteChannelFragment

    - Replaced `InviteChannelFragment` to `InviteUserFragment`.
    - Deleted `setErrorFrame()`.
    - Replaced `onUserSelectComplete(List)` to `onUserSelectionCompleted(List)`.
    - Replaced `setInviteButtonText(CharSequence)`, `setInviteButtonEnabled(boolean)`, `setRightButtonText(CharSequence)`, and `setRightButtonEnabled(boolean)` to use `notifySelectedUserChanged(int)` in `SelectUserHeaderComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `SelectUserHeaderComponent`.
    - Replaced `setUserListAdapter(UserListAdapter)` to use `setAdapter(InviteUserListAdapter)` in `InviteUserListComponent`.
    - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to use `PagedQueryHandler<UserInfo> createQueryHandler(String)` in `InviteUserViewModel`.
    - Added `onBindHeaderComponent(SelectUserHeaderComponent, InviteUserViewModel, GroupChannel)`
    - Added `onBindInviteUserListComponent(InviteUserListComponent, InviteUserViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, InviteUserViewModel, GroupChannel)`

- ###### InviteChannelFragment.Builder

  - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to `setCustomPagedQueryHandler(PagedQueryHandler<UserInfo>)`
  - Replaced `setUserListAdapter(UserListAdapter)` to `setInviteUserListAdapter(InviteUserListAdapter)`
  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Deleted `setCustomInviteChannelFragment(InviteChannelFragment)`
  - Added `withArguments(Bundle)`


------

  ##### Promote operators

- ###### PromoteOperatorsActivity

    - Renamed `PromoteOperatorsActivity` to `PromoteOperatorListActivity`
    - Replaced `Fragment createPromoteOperatorFragment(GroupChannel)` to `Fragment createFragment()`.

- ###### PromoteOperatorsFragment

    - Renamed `PromoteOperatorsFragment` to `PromoteOperatorListFragment`
    - Deleted `setErrorFrame()`.
    - Replaced `getDisabledUserIds()` to use `setDisabledUserIdList(List<String>)` in `PromoteOperatorListAdapter`.
    - Replaced `onUserSelectComplete(List)` to `onUserSelectionCompleted(List)`
    - Replaced `setRightButtonText(CharSequence)` and `setRightButtonEnabled(boolean)` to use `notifySelectedUserChanged(int)` in `SelectUserHeaderComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `SelectUserHeaderComponent`.
    - Replaced `setUserListAdapter(UserListAdapter)` to use `setAdapter(PromoteOperatorListAdapter)` in `PromoteOperatorListComponent`.
    - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to use `PagedQueryHandler<Member> createQueryHandler(String)` in `PromoteOperatorsViewModel`.
    - Added `onBindHeaderComponent(SelectUserHeaderComponent, PromoteOperatorViewModel, GroupChannel)`
    - Added `onBindPromoteOperatorListComponent(PromoteOperatorListComponent, PromoteOperatorViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, PromoteOperatorViewModel, GroupChannel)`

- ###### PromoteOperatorListFragment.Builder

  - Replaced `setCustomUserListQueryHandler(CustomUserListQueryHandler)` to `setCustomPagedQueryHandler(PagedQueryHandler<Member>)`
  - Replaced `setUserListAdapter(UserListAdapter)` to `setPromoteOperatorListAdapter(PromoteOperatorListAdapter)`
  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Deleted `setCustomPromoteOperatorFragment(PromoteOperatorsFragment)`
  - Added `withArguments(Bundle)`


------

  ##### Moderate channels and members

- ###### ModerationActivity

    - Replaced `ModerationFragment createModerationsFragment(String)` to `Fragment createFragment()`.

- ###### ModerationFragment

    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setOnMenuItemClickListener(OnMenuItemClickListener)` to use `setOnMenuClickListener(OnItemClickListener<Menu>)` in `ModerationListComponent`.
    - Added `onBindHeaderComponent(SelectUserHeaderComponent, ModerationViewModel, GroupChannel)`
    - Added `onBindModerationListComponent(ModerationListComponent, ModerationViewModel, GroupChannel)`

- ###### ModerationFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Deleted `setCustomModerationFragment(ModerationFragment)`
  - Added `withArguments(Bundle)`


------

  ##### List channel members

- ###### MemberListActivity

    - Replaced `MemberListFragment createMemberListFragment(String)` to `Fragment createFragment()`.

- ###### MemberListFragment

    - Deleted `setErrorFrame()`.
    - Replaced `onOperatorDismissed()` and `onChannelDeleted()`to use observe LiveData in `MemberListViewModel`. See `getOperatorDismissed()` and `getChannelDeleted()` methods.
    - Replaced `setCustomQueryHandler(CustomMemberListQuery)` to use `PagedQueryHandler<Member> createQueryHandler(String)` in `MemberListViewModel`.
    - Replaced `onItemClicked(View, int, Member)` to `onItemClicked(View, int, Member)` in `MemberListComponent`.
    - Replaced `onItemLongClicked(View, int, Member)` to `onItemLongClicked(View, int, Member)` in `MemberListComponent`.
    - Replaced `onActionItemClicked(View, int, Member)` to `onActionItemClicked(View, int, Member)` in `MemberListComponent`.
    - Replaced `onProfileClicked(View, int, Member)` to `onUserProfileClicked(View, int, Member)` in `MemberListComponent`.
    - Replaced `setMemberListAdapter(MemberListAdapter)` to `setAdapter(MemberListAdapter)` in `MemberListComponent`.
    - Replaced `setItemClickListener(OnItemClickListener<Member>)` to `setOnItemClickListener(OnItemClickListener<Member>)` in `MemberListComponent`.
    - Replaced `setItemLongClickListener(OnItemLongClickListener<Member>)` to `setOnItemLongClickListener(OnItemLongClickListener<Member>)` in `MemberListComponent`.
    - Replaced `setActionItemClickListener(0nItemClickListener<Member>)` to `setOnActionItemClickListener(OnItemClickListener<Member>)` in `MemberListComponent`.
    - Replaced `setOnProfileClickListener(0nItemClickListener<Member>)` to `setOnProfileClickListener(OnItemClickListener<Member>)` in `MemberListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to use `setOnRightButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setLoadingDialogHandler(LoadingDialogHandler)` to `setOnLoadingDialogHandler(LoadingDialogHandler)  ` in `MemberListModule`.
    - Added `onBindHeaderComponent(HeaderComponent, MemberListViewModel, GroupChannel)`
    - Added `onBindMemberListComponent(MemberListComponent, MemberListViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, MemberListViewModel, GroupChannel)`

- ###### MemberListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightButtonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemLongClickListener)` to `setOnItemLongClickListener(OnItemLongClickListener)`
  - Replaced `setActionItemClickListener(OnItemClickListener)` to `setOnActionItemClickListener(OnItemClickListener)`
  - Deleted `setCustomMemberListFragment(MemberListFragment)`
  - Added `withArguments(Bundle)`


------

  ##### List banned uses in group channel

- ###### BannedListActivity

    - Renamed `BannedListActivity` to `BannedUserListActivity`
    - Replaced `Fragment createBannedListFragment(String)` to `Fragment createFragment()`.

- ###### BannedListFragment

    - Renamed `BannedListFragment` to `BannedUserListFragment`
    - Deleted `setErrorFrame()`.
    - Replaced `onOperatorDismissed()` and `onChannelDeleted()`to use observe LiveData in `BannedListViewModel`. See `getOperatorDismissed()` and `getChannelDeleted()` methods.
    - Replaced `setCustomQueryHandler(CustomMemberListQuery<User>)` to use `PagedQueryHandler<User> createQueryHandler(String)` in `BannedListViewModel`.
    - Replaced `setUserListAdapter(UserTypeListAdapter)` to `setAdapter(BannedUserListAdapter)` in `BannedUserListComponent`.
    - Replaced `onItemClicked(View, int, User)` to `onItemClicked(View, int, User)` in `BannedUserListComponent`.
    - Replaced `onItemLongClicked(View, int, User)` to `onItemLongClicked(View, int, User)` in `BannedUserListComponent`.
    - Replaced `onActionItemClicked(View, int, User)` to `onActionItemClicked(View, int, User)` in `BannedUserListComponent`.
    - Replaced `onProfileClicked(View, int, User)` to `onUserProfileClicked(View, int, User)` in `BannedUserListComponent`.
    - Replaced `setItemClickListener(OnItemClickListener<User>)` to `setOnItemClickListener(OnItemClickListener<User>)` in `BannedUserListComponent`.
    - Replaced `setItemLongClickListener(OnItemLongClickListener<User>)` to `setOnItemLongClickListener(OnItemLongClickListener<User>)` in `BannedUserListComponent`.
    - Replaced `setActionItemClickListener(0nItemClickListener<User>)` to `setOnActionItemClickListener(OnItemClickListener<User>)` in `BannedUserListComponent`.
    - Replaced `setOnProfileClickListener(0nItemClickListener<User>)` to `setOnProfileClickListener(OnItemClickListener<User>)` in `BannedUserListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to use `setOnRightButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setLoadingDialogHandler(LoadingDialogHandler)` to `setOnLoadingDialogHandler(LoadingDialogHandler)  ` in `BannedUserListModule`.
    - Added `onBindHeaderComponent(HeaderComponent, BannedUserListViewModel, GroupChannel)`
    - Added `onBindBannedUserListComponent(BannedUserListComponent, BannedUserListViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, BannedUserListViewModel, GroupChannel)`

- ###### BannedListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightButtonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemLongClickListener)` to `setOnItemLongClickListener(OnItemLongClickListener)`
  - Replaced `setActionItemClickListener(OnItemClickListener)` to `setOnActionItemClickListener(OnItemClickListener)`
  - Replaced `setUserListAdapter(UserTypeListAdapter)` to `setBannedUserListAdapter(BannedUserListAdapter)`
  - Deleted `setCustomBannedMemberFragment(BannedListFragment)`
  - Added `withArguments(Bundle)`


------

  ##### List muted users in group channel

- ###### MutedMemberListActivity

    - Replaced `Fragment createMutedMemberListFragment(String)` to `Fragment createFragment()`.

- ###### MutedMemberListFragment

    - Deleted `setErrorFrame()`.
    - Replaced `onOperatorDismissed()` and `onChannelDeleted()`to use observe LiveData in `MutedMemberListViewModel`. See `getOperatorDismissed()` and `getChannelDeleted()` methods.
    - Replaced `setCustomQueryHandler(CustomMemberListQuery<Member>)` to use `PagedQueryHandler<Member> createQueryHandler(String)` in `BannedListViewModel`.
    - Replaced `setMemberListAdapter(MemberListAdapter)` to `setAdapter(MutedMemberListAdapter)` in `MutedMemberListComponent`.
    - Replaced `onItemClicked(View, int, Member)` to `onItemClicked(View, int, Member)` in `MutedMemberListComponent`.
    - Replaced `onItemLongClicked(View, int, Member)` to `onItemLongClicked(View, int, Member)` in `MutedMemberListComponent`.
    - Replaced `onActionItemClicked(View, int, Member)` to `onActionItemClicked(View, int, Member)` in `MutedMemberListComponent`.
    - Replaced `onProfileClicked(View, int, Member)` to `onUserProfileClicked(View, int, Member)` in `MutedMemberListComponent`.
    - Replaced `setItemClickListener(OnItemClickListener<Member>)` to `setOnItemClickListener(OnItemClickListener<Member>)` in `MutedMemberListComponent`.
    - Replaced `setItemLongClickListener(OnItemLongClickListener<Member>)` to `setOnItemLongClickListener(OnItemLongClickListener<Member>)` in `MutedMemberListComponent`.
    - Replaced `setActionItemClickListener(0nItemClickListener<Member>)` to `setOnActionItemClickListener(OnItemClickListener<User>)` in `MutedMemberListComponent`.
    - Replaced `setOnProfileClickListener(0nItemClickListener<Member>)` to `setOnProfileClickListener(OnItemClickListener<User>)` in `MutedMemberListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to use `setOnRightButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setLoadingDialogHandler(LoadingDialogHandler)` to `setOnLoadingDialogHandler(LoadingDialogHandler)  ` in `MutedMemberListModule`.
    - Added `onBindHeaderComponent(HeaderComponent, MutedMemberListViewModel, GroupChannel)`
    - Added `onBindMutedMemberListComponent(MutedMemberListComponent, MutedMemberListViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, MutedMemberListViewModel, GroupChannel)`

- ###### MutedMemberListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightButtonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemLongClickListener)` to `setOnItemLongClickListener(OnItemLongClickListener)`
  - Replaced `setActionItemClickListener(OnItemClickListener)` to `setOnActionItemClickListener(OnItemClickListener)`
  - Replaced `setMemberListAdpater(MemberListAdapter)` to `setMutedMemberListAdapter(MutedMemberListAdapter)`
  - Deleted `setCustomMutedMemberFragment(MutedMemberListFragment)`
  - Added `withArguments(Bundle)`


------

  ##### List operators of group channel

- ###### OperatorListActivity

    - Replaced `Fragment createOperatorListFragment(String)` to `Fragment createFragment()`.

- ###### OperatorListFragment

    - Deleted `setErrorFrame()`.
    - Replaced `onOperatorDismissed()` and `onChannelDeleted()`to use observe LiveData in `OperatorListViewModel`. See `getOperatorDismissed()` and `getChannelDeleted()` methods.
    - Replaced `setCustomQueryHandler(CustomMemberListQuery<User>)` to use `PagedQueryHandler<User> createQueryHandler(String)` in `OperatorListViewModel`.
    - Replaced `onItemClicked(View, int, User)` to `onItemClicked(View, int, User)` in `OperatorListComponent`.
    - Replaced `onItemLongClicked(View, int, User)` to `onItemLongClicked(View, int, User)` in `OperatorListComponent`.
    - Replaced `onActionItemClicked(View, int, User)` to `onActionItemClicked(View, int, User)` in `OperatorListComponent`.
    - Replaced `onProfileClicked(View, int, User)` to `onUserProfileClicked(View, int, User)` in `OperatorListComponent`.
    - Replaced `setItemClickListener(OnItemClickListener<User>)` to `setOnItemClickListener(OnItemClickListener<User>)` in `OperatorListComponent`.
    - Replaced `setItemLongClickListener(OnItemLongClickListener<User>)` to `setOnItemLongClickListener(OnItemLongClickListener<User>)` in `OperatorListComponent`.
    - Replaced `setActionItemClickListener(0nItemClickListener<User>)` to `setOnActionItemClickListener(OnItemClickListener<User>)` in `OperatorListComponent`.
    - Replaced `setOnProfileClickListener(0nItemClickListener<User>)` to `setOnProfileClickListener(OnItemClickListener<User>)` in `OperatorListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to use `setOnRightButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setLoadingDialogHandler(LoadingDialogHandler)` to `setOnLoadingDialogHandler(LoadingDialogHandler)  ` in `OperatorListModule`.
    - Added `onBindHeaderComponent(HeaderComponent, OperatorListViewModel, GroupChannel)`
    - Added `onBindOperatorListComponent(OperatorListComponent, OperatorListViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, OperatorListViewModel, GroupChannel)`

- ###### OperatorListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightButtonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemLongClickListener)` to `setOnItemLongClickListener(OnItemLongClickListener)`
  - Replaced `setActionItemClickListener(OnItemClickListener)` to `setOnActionItemClickListener(OnItemClickListener)`
  - Replaced `setUserListAdapter(UserTypeListAdapter)` to `setOperatorListAdapter(OperatorListAdapter)`
  - Deleted `setCustomOperatorListFragment(OperatorListFragment)`
  - Added `withArguments(Bundle)`


------

  ##### Search messages

  - ###### MessageSearchActivity

    - Replaced `MessageSearchFragment createMessageSearchFragment(String)` to `Fragment createFragment()`.

  - ###### MessageSearchFragment

    - Deleted `setErrorFrame()`.
    - Added `onBindHeaderComponent(MessageSearchHeaderComponent, MessageSearchViewModel, GroupChannel)`
    - Added `onBindMessageSearchListComponent(MessageSearchListComponent, MessageSearchViewModel, GroupChannel)`
    - Added `onBindStatusComponent(StatusComponent, MessageSearchViewModel, GroupChannel)`
    
- ###### MessageSearchFragment.Builder

  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Deleted `setCustomMessageSearchFragment(MessageSearchFragment)`
  - Added `withArguments(Bundle)`

------

  ##### List participants of open channel

- ###### ParticipantsListActivity

    - Renamed `ParticipantsListActivity` to `ParticipantListActivity`
    - Replaced `ParticipantsListFragment createParticipantsListFragment(String)` to `Fragment createFragment()`.

- ###### ParticipantsListFragment

    - Renamed `ParticipantsListFragment` to `ParticipantListFragment`
    - Deleted `setErrorFrame()`.
    - Added `getChannelDeleted()` to use observe LiveData in `ParticipantsViewModel`.
    - Added `PagedQueryHandler<User> createQueryHandler(String)` in `ParticipantsViewModel`.
    - Replaced `setUserListAdapter(UserTypeListAdapter)` to `setAdapter(ParticipantsListAdapter)` in `ParticipantsListComponent`.
    - Replaced `onItemClicked(View, int, User)` to `onItemClicked(View, int, User)` in `ParticipantsListComponent`.
    - Replaced `onItemLongClicked(View, int, User)` to `onItemLongClicked(View, int, User)` in `ParticipantsListComponent`.
    - Added `onActionItemClicked(View, int, User)` in `ParticipantsListComponent`.
    - Replaced `onProfileClicked(View, int, User)` to `onUserProfileClicked(View, int, User)` in `ParticipantsListComponent`.
    - Replaced `setItemClickListener(OnItemClickListener<User>)` to `setOnItemClickListener(OnItemClickListener<User>)` in `ParticipantsListComponent`.
    - Replaced `setItemLongClickListener(OnItemLongClickListener<User>)` to `setOnItemLongClickListener(OnItemLongClickListener<User>)` in `ParticipantsListComponent`.
    - Added `setOnActionItemClickListener(OnItemClickListener<User>)` in `ParticipantsListComponent`.
    - Replaced `setOnProfileClickListener(0nItemClickListener<User>)` to `setOnProfileClickListener(OnItemClickListener<User>)` in `ParticipantsListComponent`.
    - Replaced `setHeaderLeftButtonListener(OnClickListener)` to use `setOnLeftButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setHeaderRightButtonListener(OnClickListener)` to use `setOnRightButtonClickListener(OnClickListener)` in `HeaderComponent`.
    - Replaced `setLoadingDialogHandler(LoadingDialogHandler)` to `setOnLoadingDialogHandler(LoadingDialogHandler)  ` in `ParticipantsListModule`.
    - Added `onBindHeaderComponent(HeaderComponent, ParticipantViewModel, OpenChannel)`
    - Added `onBindParticipantsListComponent(ParticipantListComponent, ParticipantViewModel, OpenChannel)`
    - Added `onBindStatusComponent(StatusComponent, ParticipantViewModel, OpenChannel)`

- ###### ParticipantsListFragment.Builder

  - Replaced `setHeaderLeftButtonListener(OnClickListener)` to `setOnLeftButtonClickListener(OnClickListener)`
  - Replaced `setHeaderRightButtonListener(OnClickListener)` to `setOnRightButtonClickListener(OnClickListener)`
  - Replaced `setItemClickListener(OnItemClickListener)` to `setOnItemClickListener(OnItemClickListener)`
  - Replaced `setItemLongClickListener(OnItemLongClickListener)` to `setOnItemLongClickListener(OnItemLongClickListener)`
  - Replaced `setActionItemClickListener(OnItemClickListener)` to `setOnActionItemClickListener(OnItemClickListener)`
  - Replaced `setUserListAdapter(UserTypeListAdapter)` to `setParticipantListAdapter(ParticipantListAdapter)`
  - Deleted `setCustomParticipantsListFragment(ParticipantsListFragment)`
  - Added `withArguments(Bundle)`


------

  ##### Adapters of recycler view 

  These adapters are binded to `RecyclerView` by using in each screen. See the follow table.

  | feature name        | v2.0                   | v3.0                     |
  | ------------------- | ---------------------- | ------------------------ |
  | **MemberList**      | `MemberListAdapter`    | `MemberListAdapter`      |
  | **OperatorList**    | `UserTypeListAdapter`  | `OperatorListAdapter`    |
  | **MutedMemberList** | `MemberListAdapter`    | `MutedMemberListAdapter` |
  | **BannedUserList**  | `UserTypeListAdapter`  | `BannedUserListAdapter`  |
  | **Participantlist** | ` UserTypeListAdapter` | `ParticipantListAdapter` |

  
