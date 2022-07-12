package com.sendbird.uikit.customsample.groupchannel.components.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.adapter.SuggestedMentionListAdapter;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.interfaces.OnItemClickListener;

/**
 * Implements the customized <code>SuggestedMentionListAdapter</code> to adapt the suggested mention user list items.
 */
public class CustomSuggestedMentionListAdapter extends SuggestedMentionListAdapter {
   @Nullable
   private OnItemClickListener<User> itemClickListener;

   @NonNull
   @Override
   public BaseViewHolder<User> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_custom_suggested_mention_holder, parent, false);
      return new SuggestedMentionViewHolder(view);
   }

   @Override
   public void setOnItemClickListener(@Nullable OnItemClickListener<User> listener) {
      this.itemClickListener = listener;
   }

   private class SuggestedMentionViewHolder extends BaseViewHolder<User> {
      private final TextView nickname;

      /**
       * Constructor
       *
       * @param itemView View to be displayed.
       */
      public SuggestedMentionViewHolder(@NonNull View itemView) {
         super(itemView);
         nickname = itemView.findViewById(R.id.tvNickname);

         itemView.setOnClickListener(v -> {
            int userPosition = getAdapterPosition();

            if (userPosition != NO_POSITION) {
               final User user = getItem(userPosition);
               if (itemClickListener != null) {
                  itemClickListener.onItemClick(v, userPosition, user);
               }
            }
         });
      }

      @Override
      public void bind(@NonNull User user) {
         nickname.setText(user.getNickname());
      }
   }
}
