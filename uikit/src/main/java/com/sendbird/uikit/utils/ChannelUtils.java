package com.sendbird.uikit.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.Sender;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.widgets.ChannelCoverView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChannelUtils {

    public static String makeTitleText(@NonNull Context context, GroupChannel channel) {
        String channelName = channel.getName();
        if (!TextUtils.isEmpty(channelName) && !channelName.equals(StringSet.GROUP_CHANNEL)) {
            return channelName;
        }

        List<Member> members = channel.getMembers();

        String result;
        if (members.size() < 2 || SendBird.getCurrentUser() == null) {
            result = context.getString(R.string.sb_text_channel_list_title_no_members);
        } else if (members.size() == 2) {
            StringBuilder names = new StringBuilder();
            for (Member member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                String nickName = member.getNickname();
                names.append(", ")
                        .append(TextUtils.isEmpty(nickName) ?
                                context.getString(R.string.sb_text_channel_list_title_unknown) : nickName);
            }
            result = names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuilder names = new StringBuilder();
            for (Member member : members) {
                if (member.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    continue;
                }

                count++;
                String nickName = member.getNickname();
                names.append(", ")
                        .append(TextUtils.isEmpty(nickName) ?
                                context.getString(R.string.sb_text_channel_list_title_unknown) : nickName);

                if(count >= 10) {
                    break;
                }
            }
            result = names.delete(0, 2).toString();
        }

        return result;
    }

    public static String getLastMessage(Context context, GroupChannel channel) {
        // Bind last message text according to the type of message. Specifically, if
        // the last message is a File Message, there must be special formatting.

        final BaseMessage lastMessage = channel.getLastMessage();
        if (lastMessage == null) {
            return "";
        }

        if (lastMessage instanceof FileMessage) {
            Sender sender = lastMessage.getSender();
            return String.format(context.getString(R.string.sb_text_channel_list_last_message_file),
                    sender != null ? sender.getNickname() : context.getString(R.string.sb_text_channel_list_last_message_file_unknown));
        }
        return lastMessage.getMessage();
    }

    public static void loadImage(ChannelCoverView coverView, String url) {
        List<String> urls = new ArrayList<>();
        urls.add(url);
        coverView.loadImages(urls);
    }

    public static void loadChannelCover(ChannelCoverView coverView, BaseChannel channel) {
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            if (groupChannel.isBroadcast() && isDefaultChannelCover(channel)) {
                coverView.drawBroadcastChannelCover();
                return;
            }
            List<String> urls = makeProfileUrlsFromChannel(groupChannel);
            coverView.loadImages(urls);
        } else {
            coverView.loadImage(channel.getCoverUrl());
        }
    }

    public static List<String> makeProfileUrlsFromChannel(GroupChannel channel) {
        List<String> urls = new ArrayList<>();
        if (!isDefaultChannelCover(channel)) {
            urls.add(channel.getCoverUrl());
        } else {
            String myUserId = "";
            if (SendBird.getCurrentUser() != null) {
                myUserId = SendBird.getCurrentUser().getUserId();
            }
            List<Member> memberList = channel.getMembers();
            int index = 0;
            while (index < memberList.size() && urls.size() < 4) {
                Member member = memberList.get(index);
                ++index;
                if (member.getUserId().equals(myUserId)) {
                    continue;
                }
                urls.add(member.getProfileUrl());
            }
        }
        return urls;
    }

    private static boolean isDefaultChannelCover(BaseChannel channel) {
        return TextUtils.isEmpty(channel.getCoverUrl()) || channel.getCoverUrl().contains(StringSet.DEFAULT_CHANNEL_COVER_URL);
    }

    public static String makeTypingText(Context context, List<? extends User> typingUsers) {
        if (typingUsers.size() == 1) {
            return String.format(context.getString(R.string.sb_text_channel_typing_indicator_single),
                    typingUsers.get(0).getNickname());
        } else if (typingUsers.size() == 2) {
            return String.format(context.getString(R.string.sb_text_channel_typing_indicator_double),
                    typingUsers.get(0).getNickname(), typingUsers.get(1).getNickname());
        } else {
            return context.getString(R.string.sb_text_channel_typing_indicator_multiple);
        }
    }

    public static boolean isChannelPushOff(GroupChannel channel) {
        return channel.getMyPushTriggerOption() == GroupChannel.PushTriggerOption.OFF;
    }

    public static CharSequence makeMemberCountText(int memberCount) {
        String text = String.valueOf(memberCount);
        if (memberCount > 1000) {
            text = String.format(Locale.US, "%.1fK", memberCount / 1000F);
        }
        return text;
    }
}
