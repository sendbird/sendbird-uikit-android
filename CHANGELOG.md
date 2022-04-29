# Change Log

### v3.0.0-beta.2 (Apr 29, 2022) with Core SDK `v3.1.12`
* Supported user mention in `GroupChannel`.
* Now you can send mentioning text to the other users in `GroupChannel`. These following functions are available.
    * Added `setUseMention(boolean)`, `setUserMentionConfig(UserMentionConfig)`, `getUserMentionConfig()` and `isUsingMention()` in `SendbirdUIKit`.
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `ChannelFragment.Builder`.
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`.
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `ChannelFragment.Builder`.
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `ChannelFragment.Builder`.
    * Added `setMentionUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`.
    * Added `setEditedTextMarkUIConfig(TextUIConfig, TextUIConfig)` in `MessageListComponent.Params`.
    * Added `setSearchedTextUIConfig(TextUIConfig)` in `MessageListComponent.Params`.
    * Added `setMessageUIConfig(MessageUIConfig)` and `getMessageUIConfig()` in `MessageListAdapter`.
    * Added `setMessageUIConfig(MessageUIConfig)` in `MessageViewHolder`.
    * Added `bindUserMention(UserMentionConfig, OnMentionEventListener)` in `MessageInputComponent`.
    * Added `setSuggestedMentionListAdapter(SuggestedMentionListAdapter)` in `MessageInputComponent`.
    * Added `setUseSuggestedMentionListDivider(boolean)` in `MessageInputComponent`.
    * Added `notifySuggestedMentionDataChanged(List<User>)` in `MessageInputComponent`.
    * Added `getMentionSuggestion()` in `ChannelViewModel`.
    * Added `loadMemberList(String)` in `ChannelViewModel`.

### v3.0.0-beta (Apr 12, 2022) with Core SDK `v3.1.10`
* Support `modules` and `components` in the UIKit
* See more details and breaking changes. [[details](/changelogs/BREAKINGCHANGES_V3.md)]
* See the Migration Guide for Converting V2 to V3. [[details](/changelogs/MIGRATIONGUIDE_V3.md)]

### Up to v2.x
[Change log](/changelogs/CHANGELOG_V2.md)