package com.pleek.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by tiago on 07/04/2015.
 */
public class CustomGridView extends GridViewWithHeaderAndFooter {

    private boolean scrollingHorizontalRight;// indicate if scroll horizontal to -> (for item)
    private boolean scrollingHorizontalLeft;// indicate if scroll horizontal to <- (for item)
    private boolean scrollable = true;// true if we can scroll (not locked), false if we cannot scroll (locked)

    public CustomGridView(Context context) {
        super(context);
    }

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (!scrollable) return false; // disable scrolling
        }

        return super.onTouchEvent(ev);
    }

    private int initialX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //determine if is horizontal scroll
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            initialX = (int) ev.getRawX();
            scrollingHorizontalRight = false;
            scrollingHorizontalLeft = false;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            int x = (int) ev.getRawX();
            int moveX = x - initialX;

            if (Math.abs(moveX) > 30) {
                scrollingHorizontalRight = moveX > 0;
                scrollingHorizontalLeft = moveX < 0;
                return true;
            }
        }

        if (!scrollable) return false; //disable scrolling

        return super.onInterceptTouchEvent(ev);
    }

    public boolean isScrollingHorizontal() {
        return scrollingHorizontalRight || scrollingHorizontalLeft ;
    }

    public boolean isScrollingHorizontalRight() {
        return scrollingHorizontalRight;
    }

    public boolean isScrollingHorizontalLeft() {
        return scrollingHorizontalLeft;
    }

    public void setScrollingEnabled(boolean enabled) {
        scrollable = enabled;
    }

    public boolean isScrollable() {
        return scrollable;
    }
}
