package org.skywaves.mediavox.core.views.popupOptions;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import org.skywaves.mediavox.core.R;

/**
 * Created by Administrator on 2015/8/3.
 */
public class PopupWindowBottom {

    private View popupView;
    private PopupWindow mPopupWindow;
    private boolean arePopupsShown = false; // Track whether popups are currently shown

    private static final int TYPE_WRAP_CONTENT = 0, TYPE_MATCH_PARENT = 1;

    public PopupWindowBottom(View view) {
        popupView = view;
    }

    public void showMenu(View anchorView) {
        if (!arePopupsShown) {
            showFromBottom(anchorView);
            arePopupsShown = true; // Mark popups as shown after the first click
        }
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        initPopupWindow(TYPE_WRAP_CONTENT);
        mPopupWindow.showAtLocation(parent, gravity, x, y);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        arePopupsShown = false; // Mark popups as shown after the first click
    }

    private void showFromBottom(View anchor) {
        initPopupWindow(TYPE_MATCH_PARENT);
        mPopupWindow.setAnimationStyle(R.style.AnimationFromButtom);
        mPopupWindow.showAtLocation(anchor, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
    }

    public boolean isPopupShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void initPopupWindow(int type) {
        if (type == TYPE_WRAP_CONTENT) {
            mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }else if (type == TYPE_MATCH_PARENT) {
            mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
