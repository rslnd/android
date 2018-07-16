package com.rslnd.rosalindandroid;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

// aka AndroidBug5497Workaround
class SoftKeyboardPanWorkaround {
    private final String TAG = "SoftKeyboardPanWorkaround";

    public static void assistActivity (Activity activity) {
        new SoftKeyboardPanWorkaround(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private SoftKeyboardPanWorkaround(Activity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        Log.i(TAG, "possiblyResizeChildOfContent");
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                Log.i(TAG, "keyboard probably just became visible");
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                Log.i(TAG, "keyboard probably just became hidden");
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        int usableHeight = r.bottom - r.top;

        Log.i(TAG, "Usable height: " + usableHeight);

        return usableHeight;
    }
}