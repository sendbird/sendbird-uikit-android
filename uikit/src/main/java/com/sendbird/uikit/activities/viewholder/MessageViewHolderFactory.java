package com.sendbird.uikit.activities.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.UserMessage;
import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.model.TimelineMessage;
import com.sendbird.uikit.utils.MessageUtils;

/**
 * A Factory manages a type of messages.
 */
public class MessageViewHolderFactory {
    public static MessageViewHolder createOpenChannelViewHolder(@NonNull LayoutInflater inflater,
                                                                @NonNull ViewGroup parent,
                                                                MessageType viewType,
                                                                boolean useMessageGroupUI) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                holder = new OpenChannelUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_user_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OpenChannelFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_file_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OpenChannelImageFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_file_image_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OpenChannelVideoFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_file_video_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_time_line_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new OpenChannelAdminMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_admin_message, parent, false), useMessageGroupUI);
                break;
            default:
                // unknown message type
                holder = new OpenChannelUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_open_channel_user_message, parent, false), useMessageGroupUI);
        }
        return holder;
    }

    public static MessageViewHolder createViewHolder(@NonNull LayoutInflater inflater,
                                                     @NonNull ViewGroup parent,
                                                     MessageType viewType,
                                                     boolean useMessageGroupUI) {
        MessageViewHolder holder;
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                holder = new MyUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_my_user_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                holder = new OtherUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_other_user_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_ME:
                holder = new MyFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_my_file_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                holder = new OtherFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_other_file_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                holder = new MyImageFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_my_file_image_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                holder = new OtherImageFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_other_file_image_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                holder = new MyVideoFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_my_file_video_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                holder = new OtherVideoFileMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_other_file_video_message, parent, false), useMessageGroupUI);
                break;
            case VIEW_TYPE_TIME_LINE:
                holder = new TimelineViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_time_line_message, parent, false), false);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                holder = new AdminMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_admin_message, parent, false), false);
                break;
            default:
                // unknown message type
                if (viewType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME) {
                    holder = new MyUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_my_user_message, parent, false), useMessageGroupUI);
                } else {
                    holder = new OtherUserMessageViewHolder(DataBindingUtil.inflate(inflater, R.layout.sb_view_other_user_message, parent, false), useMessageGroupUI);
                }
        }
        return holder;
    }

    /**
     * Return the type of message as an integer.
     *
     * @param message Message to know the type.
     * @return Type of message as an integer.
     */
    public static int getViewType(@NonNull BaseMessage message) {
        return getMessageType(message).getValue();
    }

    /**
     * Return the type of message as {@link MessageType}.
     *
     * @param message Message to know the type.
     * @return Type of message as {@link MessageType}.
     */
    public static MessageType getMessageType(@NonNull BaseMessage message) {
        MessageType type;

        if (message instanceof UserMessage) {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        } else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            String mimeType = fileMessage.getType().toLowerCase();
            if (mimeType.startsWith(StringSet.image)) {
                if (mimeType.contains(StringSet.svg)) {
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                    }
                } else {
                    // If the sender is current user
                    if (MessageUtils.isMine(message)) {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                    } else {
                        type = MessageType.VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                    }
                }
            } else if (mimeType.startsWith(StringSet.video)) {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (MessageUtils.isMine(message)) {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    type = MessageType.VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
            }
        } else if (message instanceof TimelineMessage) {
            type = MessageType.VIEW_TYPE_TIME_LINE;
        } else if (message instanceof AdminMessage) {
            type = MessageType.VIEW_TYPE_ADMIN_MESSAGE;
        } else {
            if (MessageUtils.isMine(message)) {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME;
            } else {
                type = MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
            }
        }

        return type;
    }
}
