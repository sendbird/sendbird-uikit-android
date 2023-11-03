# Changelog
### v3.10.0 (Nov 3, 2023) with Chat SDK `v4.13.0`
* Added the `Suggested Replies` feature to enable quick and effective question asking to the bot.
  * Added `ChannelConfig.enableSuggestedReplies` configuration to enable/disable `Suggested Replies` feature.
* Added the `Form type message` feature to enable the user to submit a form type message received by the bot.
  * Added `ChannelConfig.enableFormTypeMessage` configuration to enable/disable `Form type message` feature.
### v3.9.3 (Oct 26, 2023) with Chat SDK `v4.13.0`
* Improve stability.

### v3.9.2 (Oct 12, 2023) with Chat SDK `v4.12.3`
* Added `setVoiceRecorderConfig(VoiceRecorderConfig)` and `getVoiceRecorderConfig()` in `SendbirdUIKit`.
* Added `setOnEmojiReactionUserListProfileClickListener(OnItemClickListener<User>)` in `ChannelFragment.Builder` and `MessageThreadFragment.Builder`.
* Improved stability.
### v3.9.1 (Sep 26, 2023) with Chat SDK `v4.12.1`
* Fixed a problem with the user mentions feature in ThreadFragment
* Filter deactivated users from the user mentions list.
### v3.9.0 (Sep 22, 2023) with Chat SDK `v4.12.1`
* Support Multiple files message
  * Added `setEnableMultipleFilesMessage(boolean)`, `getEnableMultipleFilesMessage()` in `ChannelConfig`.
  * Added `isMultipleMediaEnabled()`, `onMultipleMediaResult(List<Uri>)`, and `onSingleMediaResult(Uri)` in `ChannelFragment` and `MessageThreadFragment`.
  * Added `sendMultipleFilesMessage(List<FileInfo>, MultipleFilesMessageCreateParams)` in `ChannelViewModel` and `MessageThreadViewModel`.
  * Added `onBeforeSendMultipleFilesMessage(MultipleFilesMessageCreateParams)` in `ChannelFragment`, `MessageThreadFragment`, and `CustomParamsHandler`.
* Added `clone()` in `ChannelConfig`, `ChannelListConfig`, `ChannelSettingConfig`, and `OpenChannelConfig`.

Custom Providers are supported to create and customize various components used in UIKit. Each Provider plays a role in generating key components used in UIKit. You can customize each Provider to easily use and customize UIKit's main components.
* Support custom providers
  * ModuleProviders
  * AdapterProviders
  * FragmentProviders
  * ViewModelProviders
* Simple example of using each Provider to work with custom data.

**ModuleProviders**
```kotlin
ModuleProviders.channel = ChannelModuleProvider { context, args ->
    ChannelModule(context).apply {
        setHeaderComponent(CustomHeaderComponent())
    }
}
```

**AdapterProviders**
```kotlin
AdapterProviders.channelList = ChannelListAdapterProvider { uiParams ->
    CustomChannelListAdapter()
}
```

**FragmentProviders**
```kotlin
FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
    ChannelFragment.Builder(channelUrl)
        .setUseHeader(true)
        .setCustomFragment(CustomChannelFragment())
        .withArguments(args)
        .build()
}
```

**ViewModelProviders**
```kotlin
ViewModelProviders.channel = ChannelViewModelProvider { owner, channelUrl, params, config ->
    ViewModelProvider(
        owner,
        CustomViewModelFactory(channelUrl, params, config)
    )[channelUrl, CustomChannelViewModel::class.java]
}
```
> All Providers must be configured before use, and it's recommended to configure them in the Application class.

### v3.8.0 (Sep 4 2023) with Chat SDK `v4.12.0`
* Support category filtering in feed notification channel. Categories by which messages can be filtered can be created and edited in the dashboard.
* Added `startChatWithAiBot(Context, String, Boolean, CompletionHandler)` that initiates a group channel with AI Bot which is created in Sendbird dashboard and launches `ChannelActivity`. `SendbirdUIKit.init()` and `SendbirdUIKit.connect()` must precede.

### v3.7.0 (Jul 17 2023) with Chat SDK `v4.11.0`
* Change the default authentication method for FeedChannel from WebSocket connection to API.
* Added `authenticatedFeed(AuthenticationHandler)` in `SendbirdUIKit`
* Added `moveToMessage(long, boolean)` in `ChannelFragment`

### v3.6.1 (Jul 12, 2023) with Chat SDK `v4.9.4`
* Improved stability

### v3.6.0 (Jun 23, 2023) with Chat SDK `v4.9.1`
* Support feature configuration 
  * Added `UIKitConfig` object
  * Added `setChannelConfig(ChannelConfig)` in `ChannelFragment.Builder`, `MessageThreadFragment.Builder`
  * Added `setChanneListConfig(ChannelListConfig)` in `ChannelListFragment.Builder`
  * Added `setChannelSettingConfig(ChannelSettingConfig)` in `ChannelSettingsFragment.Builder`
  * Added `setOpenChannelConfig(OpenChannelConfig)` in `OpenChannelFragment.Builder`
  * Deprecated `setUseDefaultUserProfile(boolean)`, `shouldUseDefaultUserProfile()` in `SendbirdUIKit`
  * Deprecated `setUseChannelListTypingIndicators(boolean)`, `isUsingChannelListTypingIndicators()` in `SendbirdUIKit`
  * Deprecated `setUseChannelListMessageReceiptStatus(boolean)`, `isUsingChannelListMessageReceiptStatus` in `SendbirdUIKit`
  * Deprecated `setUseUserMention(boolean)`, `isUsingUserMention()` in `SendbirdUIKit`
  * Deprecated `setUseVoiceMessage(boolean)`, `isUsingVoiceMessage()` in `SendbirdUIKit`
  * Deprecated `setReplyType(ReplyType)`, `getReplyType()` in `SendbirdUIKit`
  * Deprecated `setThreadReplySelectType(ThreadReplySelectType)`, `getThreadReplySelectType()` in `SendbirdUIKit`
  * Deprecated `setUseTypingIndicator(boolean)` in `ChannelFragment.Builder`
  * Deprecated `ReactionUtils`
* Improved voice recorder
* Improved stability

### v3.5.7 (Jun 9, 2023) with Chat SDK `v4.9.0`
* An interface has been added to modify the data used for the Views displayed in the RecyclerView. Through each Provider, it is possible to change the data of the Views displayed in the RecyclerView.
  * Added `setMessageListAdapter(MessageListAdapter, MessageDisplayDataProvider)` in `ChannelFragment.Builder`
  * Added `setThreadListAdapter(ThreadListAdapter, MessageDisplayDataProvider)` in `MessageThreadFragment.Builder`
  * Added `setMessageSearchAdapter(MessageSearchAdapter, MessageDisplayDataProvider)` in `MessageSearchFragment.Builder`
  * Added `setChannelListAdapter(ChannelListAdapter, MessageDisplayDataProvider)` in `ChannelListFragment.Builder`
  * Added `setMessageDisplayDataProvider(MessageDisplayDataProvider)` in `MessageListAdapter`, `ThreadListAdapter`, `MessageSearchAdapter` and `ChannelListAdapter`

### v3.5.6 (May 26, 2023) with Chat SDK `v4.8.3`
* UIKit common
  * Improved voice recognition
* Channel Notification
  * Added interfaces to set custom theme resource on all Activities

### v3.5.5 (May 19, 2023) with Chat SDK `v4.8.1`
* Improved stability

### v3.5.4 (May 16, 2023) with Chat SDK `v4.8.1`
* UIKit common
  * Updated Android Gradle Plugin to version `7.4.2`
  * Improved FileProvider compatibility
  * Optimized Proguard rules
  * Improved stability
* Channel Notification
  * Added function for collecting click events
  * Added loading view while downloading template data
  * Improved template view stability

### v3.5.3 (Apr 12, 2023) with Chat SDK `v4.6.1`
* Added `setOnMessageMentionClickListener(OnItemClickListener<User>)` in `ChannelFragment.Builder`
* Added `setOnMessageMentionClickListener(OnItemClickListener<User>)` in `MessageThreadFragment.Builder`
* Improved stability

### v3.5.2 (Apr 6, 2023) with Chat SDK `v4.6.1`
* Added `setUseHeaderLeftButton(boolean)` in `FeedNotificationChannelFragment.Builder`
* Added `setUseHeaderLeftButton(boolean)` in `ChatNotificationChannelFragment.Builder`

### v3.5.1 (Mar 30, 2023) with Chat SDK `v4.6.0`
* Extended the maximum recording time of voice message to 10 minutes.
* Notifications
  * Fixed bug that the theme is not applying when the notifications are empty
* Improved stability

### v3.5.0 (Mar 14, 2023) with Chat SDK `v4.6.0`
We’re excited to announce the launch of Sendbird Notifications v1.0! It’s a powerful solutions that makes it easier for brands to send marketing, transactional, and operational messages to their users. We’ve introduced a new type of channel called the notification channel that’s specifically designed for these kinds of messages. Just a heads up, you’ll need to use notification channels with Sendbird Notifications, otherwise things might not work properly.
* Support Notification Channel
  * Added `FeedNotificationChannelActivity` and `FeedNotificationChannelFragment`
  * Added `ChatNotificationChannelActivity` and `ChatNotificationChannelFragment`

### v3.4.0 (Feb 23, 2023) with Chat SDK `v4.4.0`
* Support voice message in GroupChannel
  * Added `setUseVoiceMessage(boolean)` in `SendbirdUIKit`
  * Added `isUsingVoiceMessage()` in `SendbirdUIKit`
  * Added `VIEW_TYPE_VOICE_MESSAGE_ME`, `VIEW_TYPE_VOICE_MESSAGE_OTHER` in `MessageType`
  * Added `takeVoiceRecorder(View, int, BaseMessage)` in `ChannelFragment`, `MessageThreadFragment`
  * Added `sendVoiceFileMessage(VoiceMessageInfo)` in `ChannelFragment`, `MessageThreadFragment`
  * Added `setOnVoiceRecorderButtonClickListener(OnClickListener)` in `ChannelFragment.Builder`, `MessageThreadFragment.Builder`

### v3.3.3 (Jan 19, 2023) with Chat SDK `v4.2.1`
* Improved stability

### v3.3.2 (Dec 09, 2022) with Chat SDK `v4.1.3`
* Support authenticated file caching
* Change the default value of `SendbirdUIKit.shouldUseImageCompression()` to `true`
* Change the default value of `SendbirdUIKit.getCompressQuality()` to `70`
* Improved message input dialog mode
* Improved stability

### v3.3.1 (Nov 21, 2022) with Chat SDK `v4.1.3`
* Fixed message update issue when an app is built with Proguard on
* Improved stability

### v3.3.0 (Nov 10, 2022) with Chat SDK `v4.1.1`
* Support thread type in GroupChannel
  * Added `THREAD` in `ReplyType`
  * Added `enum ThreadReplySelectType { PARENT, THREAD }`
  * Added `setThreadReplySelectType(threadReplySelectType)` in `SendbirdUIKit`
  * Added `getThreadReplySelectType()` in `SendbirdUIKit`
  * Added `MessageThreadActivity`, `MessageThreadFragment`, `MessageThreadModule`, `MessageThreadViewModel`, `MessageThreadHeaderComponent`, `ThreadListComponent`, `MessageThreadInputComponent`, and `ThreadListAdapter`
  * Added `newRedirectToMessageThreadIntent(Context, String, long)` in `ChannelActivity`
  * Added `VIEW_TYPE_PARENT_MESSAGE_INFO` in `MessageType`
  * Added `ThreadInfo`, `ParentMessageMenu` in `ClickableViewIdentifier`
  * Added `onThreadInfoClicked(View, int, BaseMessage)` in `ChannelFragment`
  * Added `setOnThreadInfoClickListener(OnItemClickListener<BaseMessage>)` in `ChannelFragment.Builder`
* Added `MessageListUIParams` class
* Added `bind(BaseChannel, BaseMessage, MessageListUIParams)` in `MessageViewHolder`
* Added `createViewHolder(LayoutInflater, ViewGroup, MessageType, MessageListUIParams)` in `MessageViewHolderFactory`
* Added `createOpenChannelViewHolder(LayoutInflater, ViewGroup, MessageType, MessageListUIParams)` in `MessageViewHolderFactory`
* Deprecated `bind(BaseChannel, BaseMessage, MessageGroupType)` in `MessageViewHolder`
* Deprecated `createViewHolder(LayoutInflater, ViewGroup, MessageType, boolean)` in `MessageViewHolderFactory`
* Deprecated `createOpenChannelViewHolder(LayoutInflater, ViewGroup, MessageType, boolean)` in `MessageViewHolderFactory`
* Added `setUseMessageListBanner(boolean)` in `ChannelFragment.Builder`
* Added `setUseBanner(boolean)` in `MessageListComponent.Params`
* Added `setUseUserIdForNickname(boolean)` and `isUsingUserIdForNickname()` in `SendbirdUIKit`

### v3.2.2 (Oct 27, 2022) with Chat SDK `v4.1.1` 
* Added `setOnScrollFirstButtonClickListener(OnConsumableClickListener)` in `ChannelFragment.Builder` and `OpenChannelFragment.Builder`
* Added `scrollToFirst()`, `setOnScrollFirstButtonClickListener(OnConsumableClickListener)`, and `onScrollFirstButtonClicked(View)` in `MessageListComponent` and `OpemChannelMessageListComponent`
* Deprecated `setOnScrollBottomButtonClickListener(View.OnClickListener)` in `ChannelFragment.Builder` and `OpenChannelFragment.Builder`
* Deprecated `scrollToBottom()`, `setOnScrollBottomButtonClickListener(View.OnClickListener)`, and `onScrollBottomButtonClicked(View)` in `MessageListComponent` and `OpemChannelMessageListComponent`
* Improved stability

### v3.2.1 (Sep 29, 2022) with Chat SDK `v4.0.9`
* Added `takeVideo()` in `ChannelFragment` and `OpenChannelFragment`
* Support custom font in message bubble and input filed.
  * Added `setRepliedMessageTextUIConfig(TextUIConfig)`, and `setMessageInputTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` in `OpenChannelFragment.Builder`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` and `getMessageInputTextUIConfig()` in `MessageInputComponent.Params`
  * Added `setMessageInputTextUIConfig(TextUIConfig)` and `getMessageInputTextUIConfig()` in `OpenChannelMessageInputComponent.Params`
  * Added `setRepliedMessageTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
  * Added `setCustomFontRes(int)` in `TextUIConfig.Builder`

### v3.2.0 (Sep 15, 2022) with Chat SDK `v4.0.8`
* Support OpenChannel list
  * Added `OpenChannelListActivity`, `OpenChannelListFragment`, `OpenChannelListModule`, `OpenChannelListViewModel`, `OpenChannelListComponent`, and `OpenChannelListAdapter`
  * Added `CreateOpenChannelActivity`, `CreateOpenChannelFragment`, `CreateOpenChannelModule`, `CreateOpenChannelViewModel`, and `ChannelProfileInputComponent`
* Moved widgets class into internal package.
* `setCustomFragment()` functions have been added in the all Fragment.Builder class
* Improved stability

### v3.1.1 (Aug 17, 2022) with Chat SDK `v4.0.5`
* Added `setMessageTextUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setSentAtTextUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`, `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setNicknameTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`, `MessageListComponent.Params`
* Added `setNicknameTextUIConfig(TextUIConfig, TextUIConfig, TextUIConfig)` in `OpenChannelFragment.Builder`, `OpenChannelMessageListComponent.Params`
* Added `setMessageBackground(int, int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setMessageBackground(Drawable, Drawable)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setReactionListBackground(int, int)` in `ChannelFragment.Builder`
* Added `setReactionListBackground(Drawable, Drawable)` in `MessageListComponent.Params`
* Added `setOgtagBackground(int, int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setOgtagBackground(Drawable, Drawable)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`
* Added `setLinkedTextColor(int)` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
* Added `setLinkedTextColor(ColorStateList)` in `MessageListComponent.Params`, `OpenChannelMessageListComponent.Params`

### v3.1.0 (Aug 3, 2022) with Chat SDK `v4.0.5`
* Support Android 13
  * Set the `maxSdkVersion` of `android.permission.READ_EXTERNAL_STORAGE` to `32`
* Removed `android.permission.REQUEST_INSTALL_PACKAGES` permission
* Support moderation in OpenChannel
  * Added `MODERATIONS` in `OpenChannelSettingsMenuComponent.Menu`
  * Added `OpenChannelModerationActivity`, `OpenChannelModerationFragment`, `OpenChannelModerationModule`, `OpenChannelModerationViewModel`
  * Added `OpenChannelOperatorListActivity`, `OpenChannelOperatorListFragment`, `OpenChannelOperatorListModule`, `OpenChannelOperatorListViewModel`, `OpenChannelOperatorListAdapter`
  * Added `OpenChannelRegisterOperatorActivity`, `OpenChannelRegisterOperatorFragment`, `OpenChannelRegisterOperatorModule`, `OpenChannelRegisterOperatorViewModel`, `OpenChannelRegisterOperatorAdapter`
  * Added `OpenChannelBannedUserListActivity`, `OpenChannelBannedUserListFragment`, `OpenChannelBannedUserListModule`, `OpenChannelBannedUserListViewModel`, `OpenChannelBannedUserListAdapter`
  * Added `OpenChannelMutedParticipantListActivity`, `OpenChannelMutedParticipantListFragment`, `OpenChannelMutedParticipantListModule`, `OpenChannelMutedParticipantListViewModel`, `OpenChannelMutedParticipantListAdapter`
  * Added `newOpenChannelModerationFragment()`, `newOpenChannelOperatorListFragment()`, `newOpenChannelRegisterOperatorFragment()`, `newOpenChannelMutedParticipantListFragment()`, `newOpenChannelBannedUserListFragment()` in `UIKitFragmentFactory`
* Improved stability

### v3.0.0 (Jul 12, 2022) with Chat SDK `v4.0.4`
* Support `modules` and `components` in the UIKit
* Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `OpenChannelFragment.Builder`
* Rename `PromoteOperator` to `RegisterOperator`
  * Rename `PromoteOperatorActivity` to `RegisterOperatorActivity`
  * Rename `PromoteOperatorFragment` to `RegisterOperatorFragment`
  * Rename `PromoteOperatorModule` to `RegisterOperatorModule`
  * Rename `PromoteOperatorListComponent` to `RegisterOperatorListComponent`
  * Rename `PromoteOperatorListAdapter` to `RegisterOperatorListAdapter`
  * Rename `PromoteOperatorViewModel` to `RegisterOperatorViewModel`
  * Rename `newPromoteOperatorFragment()` to `newRegisterOperatorFragment()` in `UIKitFragmentFactory`
  * Rename `onBindPromoteOperatorListComponent()` to `onBindRegisterOperatorListComponent()` in `RegisterOperatorFragment`
  * Rename `setPromoteOperatorListAdapter()` to `setRegisterOperatorListAdapter()` in `RegisterOperatorFragment.Builder`
  * Rename `setMemberListComponent()` to `setRegisterOperatorListComponent()` in `RegisterOperatorModule`
  * Rename `getPromoteOperatorListComponent()` to `getRegisterOperatorListComponent()` in `RegisterOperatorModule`
  * Rename `getOperatorDismissed()` to `getOperatorUnregistered()` in `PromoteOperatorViewModel`
* See more details and breaking changes. [[details](/changelogs/BREAKINGCHANGES_V3.md)]
* See the Migration Guide for Converting V2 to V3. [[details](/changelogs/MIGRATIONGUIDE_V3.md)]

### v3.0.0-beta.3 (Jun 02, 2022) with Chat SDK `v3.1.14`
* Synchronized Builder methods and Params methods
  * Added `setErrorText()` in `BannedUserListFragment.Builder`, `ChannelListFragment.Builder`, `MemberListFragment.Builder`, `MutedMemberListFragment.Builder`, `OperatorListFragment.Builder`
  * Added `setOnInputRightButtonClickListener()`, `setOnEditModeCancelButtonClickListener()`, `setOnEditModeSaveButtonClickListener()`, `setOnQuoteReplyModeCloseButtonClickListener()`, `setOnInputModeChangedListener()`, `setUseSuggestedMentionListDivider()`, `setOnTooltipClickListener()`, `setOnScrollBottomButtonClickListener()`, `setErrorText()` in `ChannelFragment.Builder`, `OpenChannelFragment.Builder`
  * Added `setRightButtonText()`, `setOnRightButtonClickListener()` in `ChannelSettings.Builder`
  * Added `setOnRightButtonClickListener()`, `setOnUserSelectChangedListener()`, `setOnUserSelectionCompleteListener()`, `setErrorText()` in `CreateChannelFragment.Builder`, `PromoteOperatorFragment.Builder`
  * Added `setOnInputTextChangedListener()`, `setOnClearButtonClickListener()`, `setErrorText()` in `MessageSearchFragment.Builder`
  * Added `setHeaderRightButtonIconResId()`, `setHeaderRightButtonIcon()`, `setUseHeaderRightButton()`, `setOnHeaderRightButtonClickListener()` in `ModerationFragment.Builder`
  * Added `setOnInputRightButtonClickListener()`, `setOnEditModeCancelButtonClickListener()`, `setOnEditModeSaveButtonClickListener()`, `setOnInputModeChangedListener()`, `setOnScrollBottomButtonClickListener()`, `setOnMessageProfileLongClickListener()`, `setOnMessageInsertedListener()`, `setErrorText()` in `OpenChannelFragment.Builder`
  * Added `setHeaderRightButtonIconResId()`, `setHeaderRightButtonIcon()`, `setUseHeaderRightButton()`, `setOnHeaderRightButtonClickListener()`, `setOnActionItemClickListener()`, `setErrorText()` in `ParticipantListFragment.Builder`

* Mention improvement
  * Added `setStartingPoint(long, boolean)` in `ChannelFragment.Builder`
  * Removed `HighlightMessageInfo` class
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)` in `ChannelFragment.Builder`
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)` and `getHighlightMessageInfo()` in `MessageListAdapter`
  * Removed `setHighlightMessageInfo(HighlightMessageInfo)`, `getHighlightMessageInfo()`, `setSearchedTextUIConfig(TextUIConfig)`, and `setSearchedTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
  * Removed `setSearchedTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`

* Added channel push setting option for sent from mentioned only
* Added `ChannelPushSettingFragment` and `ChannelPushSettingActivity`
* Added `ChannelPushSettingViewModel`, `ChannelPushSettingModule`, and `ChannelPushSettingComponent`

### v3.0.0-beta.2 (Apr 29, 2022) with Chat SDK `v3.1.12`
* Supported user mention in `GroupChannel`
* Now you can send mentioning text to the other users in `GroupChannel`. These following functions are available
    * Added `setUseMention(boolean)`, `setUserMentionConfig(UserMentionConfig)`, `getUserMentionConfig()` and `isUsingMention()` in `SendbirdUIKit`
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `ChannelFragment.Builder`
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`
    * Added `setMessageUIConfig(MessageUIConfig)` and `getMessageUIConfig()` in `MessageListAdapter`
    * Added `setMessageUIConfig(MessageUIConfig)` in `MessageViewHolder`
    * Added `bindUserMention(UserMentionConfig, OnMentionEventListener)` in `MessageInputComponent`
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `MessageInputComponent`
    * Added `setUseSuggestedMentionListDivider(boolean)` in `MessageInputComponent`
    * Added `notifySuggestedMentionDataChanged(List<User>)` in `MessageInputComponent`
    * Added `getMentionSuggestion()` in `ChannelViewModel`
    * Added `loadMemberList(String)` in `ChannelViewModel`

### v3.0.0-beta (Apr 12, 2022) with Chat SDK `v3.1.10`
* Support `modules` and `components` in the UIKit
* See more details and breaking changes. [[details](/changelogs/BREAKINGCHANGES_V3.md)]
* See the Migration Guide for Converting V2 to V3. [[details](/changelogs/MIGRATIONGUIDE_V3.md)]

### Up to v2.x
[Change log](/changelogs/CHANGELOG_V2.md)
