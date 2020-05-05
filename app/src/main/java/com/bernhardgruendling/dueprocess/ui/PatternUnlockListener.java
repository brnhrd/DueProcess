package com.bernhardgruendling.dueprocess.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bernhardgruendling.dueprocess.util.CornerHelper;

import java.util.ArrayList;

public class PatternUnlockListener implements View.OnTouchListener {
    private static final String TAG = "PatternUnlockListener";

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private int windowWidth;
    private int windowHeight;
    private WindowManager wm;
    private Context context;
    private final Integer firstCornerIdx;
    private ArrayList<Integer> codeInput = new ArrayList<>();
    private MyCodeListener listener;


    public PatternUnlockListener(WindowManager.LayoutParams params, WindowManager wm, int windowWidth, int windowHeight, Context context, MyCodeListener listener, Integer firstCodePosition) {
        this.listener = listener;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.params = params;
        this.wm = wm;
        this.context = context;
        this.firstCornerIdx = firstCodePosition;
    }

    public interface MyCodeListener {
        void onCodeInputFinished(ArrayList<Integer> inputCode);

        void onCodeInputProgress(ArrayList<Integer> inputCode);

        void onCodeInputStarted();
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                onTouchUp(view);
                return true;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(view, event);
                return true;
            default:
                return false;
        }
    }

    private void onTouchMove(View view, MotionEvent event) {
        //TODO bad code
        int cornerIdx = CornerHelper.getCornerIdx(windowWidth, windowHeight, event.getRawX(), event.getRawY());
        if (cornerIdx >= 0 && codeInput.size() > 0 && codeInput.get(codeInput.size() - 1) != cornerIdx) {
            codeInput.add(cornerIdx);
            listener.onCodeInputProgress(codeInput);
        } else if (cornerIdx >= 0 && codeInput.size() == 0) {
            codeInput.add(cornerIdx);
            listener.onCodeInputProgress(codeInput);
        }


        params.x = initialX + (int) (event.getRawX() - initialTouchX);
        params.y = initialY + (int) (event.getRawY() - initialTouchY);
        wm.updateViewLayout(view, params);
    }

    private void onTouchUp(View view) {
        params.x = CornerHelper.getPointFromCorner(windowWidth, windowHeight, firstCornerIdx).x;
        params.y = CornerHelper.getPointFromCorner(windowWidth, windowHeight, firstCornerIdx).y;
        wm.updateViewLayout(view, params);
        listener.onCodeInputFinished(codeInput);

        codeInput.clear();
    }

    private void onTouchDown(MotionEvent event) {
        initialX = params.x;
        initialY = params.y;
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();
        listener.onCodeInputStarted();
    }


}