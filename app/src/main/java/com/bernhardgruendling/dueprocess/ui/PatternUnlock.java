package com.bernhardgruendling.dueprocess.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bernhardgruendling.dueprocess.R;
import com.bernhardgruendling.dueprocess.util.CornerHelper;

import java.util.ArrayList;
import java.util.List;

public class PatternUnlock {
    public static final String TAG = "PatternUnlock";
    private Context context;
    private List<Integer> code;
    private List<View> parentViews;
    private List<View> childViews;
    private WindowManager wm;

    public PatternUnlock(Context context, List<Integer> code) {
        this.context = context;
        this.code = code;
        this.parentViews = new ArrayList<>();
        this.childViews = new ArrayList<>();
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setCode(List<Integer> code) {
        this.code = code;
    }

    public void initOverlay(boolean opaque, PatternUnlockListener.MyCodeListener listener) {
        initOverlay(opaque, listener, code.get(0));
    }

    public void initOverlay(boolean opaque, PatternUnlockListener.MyCodeListener listener, int firstCorner) {
        int pixelformat = PixelFormat.TRANSLUCENT;
        if (opaque) {
            pixelformat = PixelFormat.OPAQUE; //only for debugging
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                200,
                200,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_SPLIT_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                pixelformat);
        params.gravity = Gravity.TOP | Gravity.LEFT;

        params.windowAnimations = android.R.style.Animation_Toast;

        Point displaySize = new Point();
        wm.getDefaultDisplay().getSize(displaySize);
        params.x = CornerHelper.getPointFromCorner(displaySize.x, displaySize.y, firstCorner).x;
        params.y = CornerHelper.getPointFromCorner(displaySize.x, displaySize.y, firstCorner).y;

        ImageView mChildLayout = new ImageView(context);
        ViewGroup mParentView = new FrameLayout(context);
        mParentView.addView(mChildLayout);

        mParentView.setOnTouchListener(new PatternUnlockListener(params, wm, displaySize.x, displaySize.y, context, listener, firstCorner));
        mChildLayout.setImageDrawable(context.getDrawable(R.drawable.ic_circle));

        wm.addView(mParentView, params);
        parentViews.add(mParentView);
        childViews.add(mChildLayout);
        mChildLayout.setVisibility(View.INVISIBLE);

    }

    public void clearOverlays() {
        for (View v : parentViews) {
            wm.removeViewImmediate(v);
        }
        parentViews.clear();
    }

    public void initSetupOverlay(boolean visible, PatternUnlockListener.MyCodeListener listener) {
        for (int i = 0; i < 4; i++) {
            initOverlay(visible, listener, i);
        }
    }

    public void startPulseAnimation() {
        for (View v : childViews) {
            v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pulse));
        }
    }


}


