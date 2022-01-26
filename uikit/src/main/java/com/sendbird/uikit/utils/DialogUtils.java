package com.sendbird.uikit.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.fragments.SendBirdDialogFragment;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.LoadingDialogHandler;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.widgets.UserProfile;
import com.sendbird.uikit.widgets.WaitingDialog;

import java.util.Collections;

public final class DialogUtils {

    private DialogUtils(){
        throw new UnsupportedOperationException();
    }
    
    public static SendBirdDialogFragment buildItems(String title,
                                                    int dialogWidth,
                                                    DialogListItem[] items,
                                                    OnItemClickListener<DialogListItem> itemClickListener) {
        return new SendBirdDialogFragment.Builder()
                .setDialogWidth(dialogWidth)
                .setTitle(title)
                .setItems(items, itemClickListener, SendBirdDialogFragment.ItemIconGravity.END)
                .create();
    }

    public static SendBirdDialogFragment buildItemsBottom(DialogListItem[] items,
                                                          OnItemClickListener<DialogListItem> itemClickListener) {
        return new SendBirdDialogFragment.Builder()
                .setDialogGravity(SendBirdDialogFragment.DialogGravity.BOTTOM)
                .setItems(items, itemClickListener, SendBirdDialogFragment.ItemIconGravity.START)
                .create();
    }

    public static SendBirdDialogFragment buildEditText(String title,
                                                       int dialogWidth,
                                                       DialogEditTextParams editTextParams,
                                                       OnEditTextResultListener editTextResultListener,
                                                       String positiveButtonText,
                                                       View.OnClickListener positiveButtonListener,
                                                       String negativeButtonText,
                                                       View.OnClickListener negativeButtonListener) {
         return new SendBirdDialogFragment.Builder()
                 .setDialogWidth(dialogWidth)
                 .setTitle(title)
                 .setEditText(editTextParams, editTextResultListener)
                 .setPositiveButton(positiveButtonText, positiveButtonListener)
                 .setNegativeButton(negativeButtonText, negativeButtonListener)
                 .create();

    }

    public static SendBirdDialogFragment buildWarning(String title,
                                                      int dialogWidth,
                                                      String warningButtonText,
                                                      View.OnClickListener warningButtonListener,
                                                      String negativeButtonText,
                                                      View.OnClickListener negativeButtonListener) {
        int negativeButtonTextColor = SendBirdUIKit.isDarkMode() ?
                R.color.sb_button_uncontained_text_color_cancel_dark :
                R.color.sb_button_uncontained_text_color_cancel_light;
        int positiveButtonTextColor = SendBirdUIKit.isDarkMode() ?
                R.color.sb_button_uncontained_text_color_alert_dark :
                R.color.sb_button_uncontained_text_color_alert_light;
        return new SendBirdDialogFragment.Builder()
                .setDialogWidth(dialogWidth)
                .setTitle(title)
                .setNegativeButton(negativeButtonText, negativeButtonTextColor, negativeButtonListener)
                .setPositiveButton(warningButtonText, positiveButtonTextColor,  warningButtonListener)
                .create();
    }

    public static SendBirdDialogFragment buildContentView(View contentView) {
        return new SendBirdDialogFragment.Builder()
                .setDialogGravity(SendBirdDialogFragment.DialogGravity.BOTTOM)
                .setContentView(contentView)
                .create();
    }

    public static SendBirdDialogFragment buildContentViewTop(View contentView) {
        return new SendBirdDialogFragment.Builder()
                .setDialogGravity(SendBirdDialogFragment.DialogGravity.TOP)
                .setContentView(contentView)
                .create();
    }

    public static SendBirdDialogFragment buildContentViewAndItems(View contentView,
                                                                  DialogListItem[] items,
                                                                  OnItemClickListener<DialogListItem> itemClickListener) {
        return new SendBirdDialogFragment.Builder()
                .setDialogGravity(SendBirdDialogFragment.DialogGravity.BOTTOM)
                .setContentView(contentView)
                .setItems(items, itemClickListener, SendBirdDialogFragment.ItemIconGravity.START)
                .create();
    }

    public static SendBirdDialogFragment buildUserProfile(@NonNull Context context, @NonNull User user,
                                                          boolean useChannelCreatable,
                                                          OnItemClickListener<User> userProfileItemClickListener,
                                                          LoadingDialogHandler handler) {
        UserProfile userProfile = new UserProfile(context);
        userProfile.drawUserProfile(user);
        userProfile.setUseChannelCreateButton(useChannelCreatable);

        SendBirdDialogFragment dialogFragment = new SendBirdDialogFragment.Builder()
                .setDialogGravity(SendBirdDialogFragment.DialogGravity.BOTTOM)
                .setContentView(userProfile)
                .create();

        userProfile.setOnItemClickListener((view, position, menuItem) -> {
            dialogFragment.dismiss();
            if (userProfileItemClickListener != null) {
                userProfileItemClickListener.onItemClick(view, position, menuItem);
            } else {
                createDirectChannel(context, user, handler);
            }
        });

        return dialogFragment;
    }

    private static void createDirectChannel(Context context, User user, LoadingDialogHandler handler) {
        GroupChannelParams params = new GroupChannelParams();
        params.addUserId(user.getUserId());
        params.setName("");
        params.setCoverUrl("");
        params.setOperators(Collections.singletonList(SendBird.getCurrentUser()));

        CustomParamsHandler cutsomHandler = SendBirdUIKit.getCustomParamsHandler();
        if (cutsomHandler != null) {
            cutsomHandler.onBeforeCreateGroupChannel(params);
        }

        if (handler == null) {
            WaitingDialog.show(context);
        } else {
            handler.shouldShowLoadingDialog();
        }
        GroupChannel.createChannel(params, (channel, e) -> {
            if (handler == null) {
                WaitingDialog.dismiss();
            } else {
                handler.shouldDismissLoadingDialog();
            }
            if (e != null) {
                ContextUtils.toastError(context, R.string.sb_text_error_create_channel);
                Logger.e(e);
                return;
            }

            Intent intent = ChannelActivity.newIntent(context, channel.getUrl());
            context.startActivity(intent);
        });
    }
}
