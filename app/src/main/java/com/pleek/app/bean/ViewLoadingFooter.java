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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicolas on 10/03/15.
 */
public class ViewLoadingFooter extends LinearLayout
{
    private ImageView logoLoad;
    private boolean isShow;

    public ViewLoadingFooter(Context context)
    {
        this(context, null);
    }

    public ViewLoadingFooter(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public ViewLoadingFooter(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private void init()
    {
        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        logoLoad = new ImageView(getContext());
        LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        p.topMargin = p.bottomMargin = Screen.getInstance(getContext()).dpToPx(10);
        logoLoad.setLayoutParams(p);
        logoLoad.setImageResource(R.drawable.logo_load);
        addView(logoLoad);
        isShow = true;
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(isShow)
                        {
                            AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
                            animation1.setDuration(500);
                            animation1.setFillAfter(true);
                            logoLoad.startAnimation(animation1);

                            isShow = false;
                        }
                        else
                        {
                            AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
                            animation1.setDuration(500);
                            animation1.setFillAfter(true);
                            logoLoad.startAnimation(animation1);

                            isShow = true;
                        }
                    }
                });
            }
        }, 0, 500);
    }
}
