package com.sendbird.uikit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.databinding.SbViewDialogListItemBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.model.DialogListItem;
import com.sendbird.uikit.utils.DrawableUtils;

class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.ListViewHolder> {

    private final DialogListItem[] items;
    private final OnItemClickListener<DialogListItem> listener;
    private int nameMarginLeft = R.dimen.sb_size_24;
    private final boolean isIconLeft;

    DialogListAdapter(DialogListItem[] items, OnItemClickListener<DialogListItem> listener, boolean isIconLeft) {
        this.items = items;
        this.listener = listener;
        this.isIconLeft = isIconLeft;
    }

    void setNameMarginLeft(int resId) {
        this.nameMarginLeft = resId;
    }

    @NonNull
    @Override
    public DialogListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DialogListAdapter.ListViewHolder(SbViewDialogListItemBinding.
                inflate(LayoutInflater.from(parent.getContext()), parent, false),
                listener, nameMarginLeft, isIconLeft);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogListAdapter.ListViewHolder holder, int position) {
        if (items != null && position >= 0 && position < items.length) {
            DialogListItem item = items[position];
            if (item != null) holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.length;
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        private final SbViewDialogListItemBinding binding;
        private final Context context;
        private final ColorStateList buttonTint;

        private final OnItemClickListener<DialogListItem> listener;
        private final boolean isIconLeft;

        private ListViewHolder(SbViewDialogListItemBinding binding,
                              OnItemClickListener<DialogListItem> listener,
                              int nameMarginLeft, boolean isIconLeft) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.isIconLeft = isIconLeft;
            context = binding.getRoot().getContext();
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    null,
                    R.styleable.DialogView,
                    R.attr.sb_dialog_view_style, 0);
            try {
                final int listItemAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_list_item_appearance, R.style.SendbirdSubtitle2OnLight01);
                final ColorStateList listItemTextColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_list_item_text_color);
                int listItemBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_list_item_background, R.drawable.selector_rectangle_light);
                buttonTint = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_icon_tint);
                this.binding.clItem.setBackgroundResource(listItemBackground);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.name.getLayoutParams();
                params.setMargins((int) context.getResources().getDimension(nameMarginLeft), 0, 0, 0);

                binding.name.setTextAppearance(context, listItemAppearance);
                if (listItemTextColor != null) binding.name.setTextColor(listItemTextColor);
            } finally {
                a.recycle();
            }
        }

        private void bind(@NonNull DialogListItem item) {
            binding.getRoot().setEnabled(!item.isDisabled());

            if (item.getKey() != 0) {
                binding.name.setText(item.getKey());
                binding.name.setEnabled(!item.isDisabled());
            }

            if (item.getIcon() != 0) {
                Drawable icon = DrawableUtils.setTintList(itemView.getContext(), item.getIcon(), buttonTint);
                if (isIconLeft) {
                    binding.iconLeft.setEnabled(!item.isDisabled());
                    binding.iconLeft.setVisibility(View.VISIBLE);
                    binding.iconLeft.setImageDrawable(icon);
                } else {
                    binding.iconRight.setEnabled(!item.isDisabled());
                    binding.iconRight.setVisibility(View.VISIBLE);
                    binding.iconRight.setImageDrawable(icon);
                }
            }

            binding.getRoot().setOnClickListener((v) -> {
                if (listener != null && item.getKey() != 0) {
                    listener.onItemClick(binding.getRoot(), getAdapterPosition(), item);
                }
            });

            if (item.isAlert()) {
                int alertColor = SendBirdUIKit.isDarkMode() ? R.color.error_200 : R.color.error_300;
                binding.name.setTextColor(context.getResources().getColor(alertColor));
            }
        }
    }
}
