package com.pleek.app.bean;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.goandup.lib.utile.Screen;
import com.pleek.app.R;
import com.pleek.app.views.CircleProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicolas on 10/03/15.
 */
public class ViewLoadingFooter extends LinearLayout
{
    private CircleProgressBar circleProgressBar;
    private boolean isShow;

    public ViewLoadingFooter(Context context)
    {
        this(context, null);
    }

    public ViewLoadingFooter(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public ViewLoadingFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        circleProgressBar = new CircleProgressBar(getContext());
        LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        p.topMargin = p.bottomMargin = Screen.getInstance(getContext()).dpToPx(10);
        circleProgressBar.setLayoutParams(p);
        circleProgressBar.setShowArrow(false);
        addView(circleProgressBar);
        isShow = true;
    }
}
