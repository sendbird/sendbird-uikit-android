package com.sendbird.uikit.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sendbird.uikit.R;
import com.sendbird.uikit.consts.DialogEditTextParams;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.OnEditTextResultListener;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.model.DialogListItem;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * The SendBirdUIKit dialog fragment.
 *
 * @since 2.0.0
 */
public class SendBirdDialogFragment extends DialogFragment {
    private Params params;

    public SendBirdDialogFragment() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final DialogView dialogView = new DialogView(getActivity());
        int style = R.style.SendBird_Dialog;
        if (params != null) {
            dialogView.setContentView(params.contentView);
            dialogView.setTitle(params.title);
            dialogView.setPositiveButton(params.positiveButtonText, params.positiveButtonTextColor, v -> {
                dismiss();
                if (params.positiveButtonClickListener != null)
                    params.positiveButtonClickListener.onClick(v);
            });
            dialogView.setNegativeButton(params.negativeButtonText, params.negativeButtonTextColor, v -> {
                dismiss();
                if (params.negativeButtonClickListener != null)
                    params.negativeButtonClickListener.onClick(v);
            });
            dialogView.setNeutralButton(params.neutralButtonText, params.negativeButtonTextColor, v -> {
                dismiss();
                if (params.neutralButtonClickListener != null)
                    params.neutralButtonClickListener.onClick(v);
            });
            dialogView.setEditText(params.editTextParams, params.editTextResultListener);
            dialogView.setItems(params.items, (view, position, data) -> {
                dismiss();
                if (params.itemClickListener != null)
                    params.itemClickListener.onItemClick(view, position, data);
            }, params.itemIconGravity == ItemIconGravity.START);
            if (params.dialogGravity == DialogGravity.BOTTOM) dialogView.setBackgroundBottom();
            style = params.dialogGravity == DialogGravity.BOTTOM ? R.style.SendBird_Dialog_Bottom : R.style.SendBird_Dialog;
        } else {
            setShowsDialog(false);
            dismiss();
        }

        final AlertDialog.Builder builder = createDialogBuilder(getActivity(), style);
        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        int res = super.show(transaction, tag);
        if (this.getFragmentManager() != null) this.getFragmentManager().executePendingTransactions();
        applyDialogParams();
        return res;
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        super.show(manager, tag);
        manager.executePendingTransactions();
        applyDialogParams();
    }

    @Override
    public void showNow(@NonNull FragmentManager manager, @Nullable String tag) {
        super.showNow(manager, tag);
        applyDialogParams();
    }

    /**
     * Display the dialog, removing existing dialog which has tag {@link StringSet#TAG_SENDBIRD_DIALOG_FRAGMENT }
     * and adding the fragment to the given FragmentManager.
     *
     * @param fragmentManager The FragmentManager this fragment will be added to.
     */
    public void showSingle(@NonNull FragmentManager fragmentManager) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(StringSet.TAG_SENDBIRD_DIALOG_FRAGMENT);
        if (prev instanceof DialogFragment) {
            ((DialogFragment) prev).dismiss();
            ft.remove(prev);
            ft.commitNow();
        }
        this.showNow(fragmentManager, StringSet.TAG_SENDBIRD_DIALOG_FRAGMENT);
    }

    private void applyDialogParams() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            if (params.dialogGravity == DialogGravity.BOTTOM) {
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            } else if (params.dialogGravity == DialogGravity.TOP) {
                dialog.getWindow().setGravity(Gravity.TOP);
            }
            dialog.getWindow().setLayout(params.dialogWidth, params.dialogHeight);
        }
    }

    private static AlertDialog.Builder createDialogBuilder(final Context context, final int defStyleRes) {
        return new AlertDialog.Builder(context, defStyleRes);
    }

    private void setParams(Params params) {
        this.params = params;
    }

    public enum DialogGravity {
        CENTER, BOTTOM, TOP
    }

    public enum ItemIconGravity {
        START, END
    }

    public static class Builder {
        private final Params params;

        /**
         * Constructor
         */
        public Builder() {
            params = new Params();
        }

        /**
         * Sets the dialog width.
         *
         * @param width Dialog width.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDialogWidth(int width) {
            this.params.dialogWidth = width;
            return this;
        }

        /**
         * Sets the dialog height.
         *
         * @param height Dialog height.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDialogHeight(int height) {
            this.params.dialogHeight = height;
            return this;
        }

        /**
         * Sets the dialog title.
         *
         * @param title Dialog title.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setTitle(String title) {
            this.params.title = title;
            return this;
        }

        /**
         * Sets the dialog gravity.
         *
         * @param dialogGravity {@link DialogGravity}.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDialogGravity(DialogGravity dialogGravity) {
            this.params.dialogGravity = dialogGravity;
            return this;
        }

        /**
         * Set a list of items to be displayed in the dialog as the content.
         *
         * @param items Items to be displayed in the dialog as the content.
         * @param itemClickListener The listener that will be called when an item is clicked.
         * @param iconGravity {@link ItemIconGravity} that will locate an icon for each items.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setItems(DialogListItem[] items, OnItemClickListener<DialogListItem> itemClickListener, ItemIconGravity iconGravity) {
            this.params.items = items;
            this.params.itemClickListener = itemClickListener;
            this.params.itemIconGravity = iconGravity;
            return this;
        }

        /**
         * Sets {@link android.widget.EditText} content in the dialog.
         *
         * @param editTextParams The params of {@link android.widget.EditText}.
         * @param editTextResultListener The listener that return the input result when positive button is pressed.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @see DialogEditTextParams
         */
        public Builder setEditText(DialogEditTextParams editTextParams, OnEditTextResultListener editTextResultListener) {
            this.params.editTextParams = editTextParams;
            this.params.editTextResultListener = editTextResultListener;
            return this;
        }

        /**
         * Set a listener to be invoked when the positive button of the dialog is pressed.
         *
         * @param text The text to display in the positive button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setPositiveButton(String text, View.OnClickListener clickListener) {
            return setPositiveButton(text, 0, clickListener);
        }

        /**
         * Set a listener to be invoked when the positive button of the dialog is pressed.
         *
         * @param text The text to display in the positive button
         * @param textColor The text color to display in the positive button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setPositiveButton(String text, @ColorRes int textColor, View.OnClickListener clickListener) {
            this.params.positiveButtonText = text;
            this.params.positiveButtonTextColor = textColor;
            this.params.positiveButtonClickListener = clickListener;
            return this;
        }

        /**
         * Set a listener to be invoked when the negative button of the dialog is pressed.
         *
         * @param text The text to display in the negative button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setNegativeButton(String text, View.OnClickListener clickListener) {
            return setNegativeButton(text, 0, clickListener);
        }

        /**
         * Set a listener to be invoked when the negative button of the dialog is pressed.
         *
         * @param text The text to display in the negative button
         * @param textColor The text color to display in the negative button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setNegativeButton(String text, @ColorRes int textColor, View.OnClickListener clickListener) {
            this.params.negativeButtonText = text;
            this.params.negativeButtonTextColor = textColor;
            this.params.negativeButtonClickListener = clickListener;
            return this;
        }

        /**
         * Set a listener to be invoked when the neutral button of the dialog is pressed.
         *
         * @param text The text to display in the neutral button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setNeutralButton(String text, View.OnClickListener clickListener) {
            return setNeutralButton(text, 0, clickListener);
        }

        /**
         * Set a listener to be invoked when the neutral button of the dialog is pressed.
         *
         * @param text The text to display in the neutral button
         * @param textColor The text color to display in the neutral button
         * @param clickListener The {@link View.OnClickListener} to use.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setNeutralButton(String text, @ColorRes int textColor, View.OnClickListener clickListener) {
            this.params.neutralButtonText = text;
            this.params.neutralButtonTextColor = textColor;
            this.params.neutralButtonClickListener = clickListener;
            return this;
        }

        /**
         * Sets a custom view to be the contents of the alert dialog.
         *
         * @param view The view to use as the contents of the alert dialog
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setContentView(View view) {
            this.params.contentView = view;
            return this;
        }

        /**
         * Creates an {@link SendBirdDialogFragment} with the arguments supplied to this builder.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public SendBirdDialogFragment create() {
            SendBirdDialogFragment sendBirdDialogFragment = new SendBirdDialogFragment();
            sendBirdDialogFragment.setParams(params);
            return sendBirdDialogFragment;
        }
    }

    private static class Params {
        private int dialogHeight = WRAP_CONTENT;
        private int dialogWidth = MATCH_PARENT;
        private String title;
        private DialogGravity dialogGravity = DialogGravity.CENTER;
        private DialogListItem[] items;
        private OnItemClickListener<DialogListItem> itemClickListener;
        private ItemIconGravity itemIconGravity;
        private DialogEditTextParams editTextParams;
        private OnEditTextResultListener editTextResultListener;
        private String positiveButtonText;
        @ColorRes private int positiveButtonTextColor;
        private View.OnClickListener positiveButtonClickListener;
        private String negativeButtonText;
        @ColorRes private int negativeButtonTextColor;
        private View.OnClickListener negativeButtonClickListener;
        private String neutralButtonText;
        @ColorRes private int neutralButtonTextColor;
        private View.OnClickListener neutralButtonClickListener;
        private View contentView;
    }
}
