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
    private final OnItemClickListener<Integer> listener;
    private int nameMarginLeft = R.dimen.sb_size_24;
    private final boolean isIconLeft;

    DialogListAdapter(DialogListItem[] items, OnItemClickListener<Integer> listener, boolean isIconLeft) {
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
            holder.bind(items[position]);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.length;
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        private final SbViewDialogListItemBinding binding;
        private final Context context;
        private final int listItemAppearance;
        private final ColorStateList buttonTint;

        private final OnItemClickListener<Integer> listener;
        private final boolean isIconLeft;

        private ListViewHolder(SbViewDialogListItemBinding binding,
                              OnItemClickListener<Integer> listener,
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
                listItemAppearance = a.getResourceId(R.styleable.DialogView_sb_dialog_view_list_item_appearance, R.style.SendbirdSubtitle2OnLight01);
                int listItemBackground = a.getResourceId(R.styleable.DialogView_sb_dialog_view_list_item_background, R.drawable.selector_rectangle_light);
                buttonTint = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_icon_tint);
                this.binding.clItem.setBackgroundResource(listItemBackground);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.name.getLayoutParams();
                params.setMargins((int) context.getResources().getDimension(nameMarginLeft), 0, 0, 0);
            } finally {
                a.recycle();
            }
        }

        private void bind(DialogListItem item) {
            if (item != null && item.getKey() != 0) {
                binding.name.setText(item.getKey());
                binding.name.setTextAppearance(context, listItemAppearance);
            }

            if (item != null && item.getIcon() != 0) {
                Drawable icon = DrawableUtils.setTintList(itemView.getContext(), item.getIcon(), buttonTint);
                if (isIconLeft) {
                    binding.iconLeft.setVisibility(View.VISIBLE);
                    binding.iconLeft.setImageDrawable(icon);
                } else {
                    binding.iconRight.setVisibility(View.VISIBLE);
                    binding.iconRight.setImageDrawable(icon);
                }
            }

            binding.getRoot().setOnClickListener((v) -> {
                if (listener != null && item != null && item.getKey() != 0) {
                    listener.onItemClick(binding.getRoot(), getAdapterPosition(), item.getKey());
                }
            });

            if (item != null && item.isAlert()) {
                int alertColor = SendBirdUIKit.isDarkMode() ? R.color.error_200 : R.color.error_300;
                binding.name.setTextColor(context.getResources().getColor(alertColor));
            }
        }
    }
}
