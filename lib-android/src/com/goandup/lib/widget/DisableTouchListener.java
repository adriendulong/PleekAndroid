package com.goandup.lib.widget;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by nicolas on 03/09/2014.
 */
public class DisableTouchListener implements View.OnTouchListener
{
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        return true;
    }
}
