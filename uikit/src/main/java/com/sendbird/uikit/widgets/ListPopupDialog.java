package com.sendbird.uikit.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.activities.adapter.MutableBaseAdapter;
import com.sendbird.uikit.databinding.SbViewListPopupBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.utils.ContextUtils;

import java.lang.ref.WeakReference;
import java.util.List;

class ListPopupDialog<T> {
   @NonNull
   private final PopupWindow popupWindow;
   @Nullable
   private WeakReference<View> anchor;
   @Nullable
   private WeakReference<View> anchorRoot;
   @NonNull
   private final SbViewListPopupBinding binding;
   @Nullable
   private MutableBaseAdapter<T> adapter;
   @Nullable
   private OnItemClickListener<T> itemClickListener;

   @NonNull
   private final View.OnAttachStateChangeListener mOnAnchorDetachedListener =
           new View.OnAttachStateChangeListener() {
              @Override
              public void onViewAttachedToWindow(View v) {
                 // Anchor might have been reattached in a different position.
                 alignToAnchor();
              }

              @Override
              public void onViewDetachedFromWindow(View v) {
                 // Leave the popup in its current position.
                 // The anchor might become attached again.
              }
           };
   @NonNull
   private final ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener = this::alignToAnchor;
   @NonNull
   private final View.OnLayoutChangeListener mOnLayoutChangeListener =
           (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> alignToAnchor();


   public ListPopupDialog(@NonNull Context context) {
      this.binding = SbViewListPopupBinding.inflate(LayoutInflater.from(context));
      this.binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
      this.binding.recyclerView.setItemAnimator(null);
      this.popupWindow = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      this.popupWindow.setAnimationStyle(R.style.Animation_Sendbird_Popup);
      setCanceledOnTouchOutside(true);
   }

   public void setContentView(@NonNull View contentView) {
      popupWindow.setContentView(contentView);
   }

   public void setAdapter(@NonNull MutableBaseAdapter<T> adapter) {
      this.adapter = adapter;
      this.binding.recyclerView.setAdapter(adapter);
      this.adapter.setOnItemClickListener((view, position, data) -> {
         dismiss();
         if (itemClickListener != null) itemClickListener.onItemClick(view, position, data);
      });
   }

   @Nullable
   public MutableBaseAdapter<T> getAdapter() {
      return adapter;
   }

   public void setScrollPosition(int position) {
      final LinearLayoutManager layoutManager = (LinearLayoutManager) this.binding.recyclerView.getLayoutManager();
      if (layoutManager != null) {
         layoutManager.scrollToPosition(position);
      }
   }

   public void setOnItemClickListener(@Nullable OnItemClickListener<T> onItemClickListener) {
      this.itemClickListener = onItemClickListener;
   }

   public void setCanceledOnTouchOutside(boolean outsideTouchable) {
      this.binding.getRoot().setOnClickListener(v -> {
         if (outsideTouchable) {
            dismiss();
         }
      });
   }

   public void update(@NonNull View anchorView, @NonNull List<T> items) {
      if (this.adapter == null) return;
      this.adapter.setItems(items);

      if (items.isEmpty()) {
         dismiss();
      } else {
         if (!isShowing()) {
            showAsDropUp(anchorView);
         }
      }
   }

   public void dismiss() {
      detachFromAnchor();
      this.popupWindow.dismiss();
   }

   public void showAsDropUp(@NonNull View anchorView) {
      attachToAnchor(anchorView);
      LayoutParams p = new LayoutParams();
      findDropUpPosition(anchorView, p);
      this.popupWindow.setHeight(getHeightAbove(anchorView));
      this.popupWindow.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.END, p.x, p.y);
   }

   public boolean isShowing() {
      return this.popupWindow.isShowing();
   }

   public void setUseDivider(boolean useDivider) {
      binding.recyclerView.setUseDivider(useDivider);
   }

   private void alignToAnchor() {
      if (getAnchor() == null) return;

      LayoutParams p = new LayoutParams();
      findDropUpPosition(getAnchor(), p);
      this.popupWindow.update(p.x, p.y, -1, getHeightAbove(getAnchor()), true);
   }

   private int getHeightAbove(@NonNull View anchor) {
      final int[] appScreenLocation = new int[2];
      final View appRootView = anchor.getRootView();
      appRootView.getLocationOnScreen(appScreenLocation);

      final int[] screenLocation = new int[2];
      anchor.getLocationOnScreen(screenLocation);

      final int lengthAboveAnchor = screenLocation[1] - appScreenLocation[1];

      int statusBarHeight = 0;

      // calculated Android top status bar.
      Window window = ContextUtils.getWindow(anchor.getContext());
      if (window != null) {
         Rect rectangle = new Rect();
         window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
         statusBarHeight = rectangle.top;
      }

      return lengthAboveAnchor - statusBarHeight;
   }

   private void findDropUpPosition(@NonNull View anchor, @NonNull LayoutParams outParams) {
      final int[] appScreenLocation = new int[2];
      final View appRootView = anchor.getRootView();
      appRootView.getLocationOnScreen(appScreenLocation);

      final int[] screenLocation = new int[2];
      anchor.getLocationOnScreen(screenLocation);

      final int[] drawingLocation = new int[2];
      drawingLocation[0] = screenLocation[0] - appScreenLocation[0];
      drawingLocation[1] = screenLocation[1] - appScreenLocation[1];
      outParams.x = drawingLocation[0];
      // calculated the y value assuming Gravity.BOTTOM.
      outParams.y = appRootView.getHeight() - drawingLocation[1];
   }

   private void attachToAnchor(@NonNull View anchor) {
      detachFromAnchor();

      final ViewTreeObserver vto = anchor.getViewTreeObserver();
      if (vto != null) {
         vto.addOnScrollChangedListener(mOnScrollChangedListener);
      }
      anchor.addOnAttachStateChangeListener(mOnAnchorDetachedListener);

      final View anchorRoot = anchor.getRootView();
      anchorRoot.addOnLayoutChangeListener(mOnLayoutChangeListener);

      this.anchor = new WeakReference<>(anchor);
      this.anchorRoot = new WeakReference<>(anchorRoot);
   }

   private void detachFromAnchor() {
      final View anchor = getAnchor();
      if (anchor != null) {
         final ViewTreeObserver vto = anchor.getViewTreeObserver();
         vto.removeOnScrollChangedListener(mOnScrollChangedListener);
         anchor.removeOnAttachStateChangeListener(mOnAnchorDetachedListener);
      }

      final View anchorRoot = this.anchorRoot != null ? this.anchorRoot.get() : null;
      if (anchorRoot != null) {
         anchorRoot.removeOnLayoutChangeListener(mOnLayoutChangeListener);
      }
   }

   @Nullable
   private View getAnchor() {
      return this.anchor != null ? this.anchor.get() : null;
   }

   private static class LayoutParams {
      int x;
      int y;
   }
}
