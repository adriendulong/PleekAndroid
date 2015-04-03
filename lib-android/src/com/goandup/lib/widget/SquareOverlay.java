package com.goandup.lib.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by nicolas on 15/01/15.
 */
public class SquareOverlay extends LinearLayout
{
    private View overlay1;
    private View square;
    private View overlay2;

    private Listener listener;
    private int lastSizeSquare;

    public SquareOverlay(Context context)
    {
        this(context, null);
    }

    public SquareOverlay(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public SquareOverlay(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private void init()
    {
        int colorOverlay = Color.argb(230, 0, 0, 0);

        overlay1 = new View(getContext());
        overlay1.setLayoutParams(new LayoutParams(0, 0));
        overlay1.setBackgroundColor(colorOverlay);

        square = new View(getContext());
        square.setLayoutParams(new LayoutParams(0, 0));

        overlay2 = new View(getContext());
        overlay2.setLayoutParams(new LayoutParams(0, 0));
        overlay2.setBackgroundColor(colorOverlay);

        addView(overlay1);
        addView(square);
        addView(overlay2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int sizeSquare = Math.min(w, h);

        if(lastSizeSquare != sizeSquare)
        {
            setOrientation(w > h ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

            overlay1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
            square.setLayoutParams(new LayoutParams(sizeSquare, sizeSquare));
            overlay2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));

            if(listener != null) listener.sizeChange(sizeSquare);

            lastSizeSquare = sizeSquare;
        }
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public interface Listener
    {
        public void sizeChange(int sizeSquare);
    }
}
