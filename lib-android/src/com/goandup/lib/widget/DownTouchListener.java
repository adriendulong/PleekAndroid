package com.goandup.lib.widget;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;

public class DownTouchListener implements OnTouchListener
{
	private int colorDown;
	private int colorUp;
	private Drawable drawableDown;
	private Drawable drawableUp;
	
	public DownTouchListener(int colorDown) {
		this(colorDown, Color.TRANSPARENT);
	}
	
	public DownTouchListener(int colorDown, int colorUp) {
		super();
		this.colorDown = colorDown;
		this.colorUp = colorUp;
	}
	
	public DownTouchListener(Drawable drawableDown, Drawable drawableUp) {
		super();
		this.drawableDown = drawableDown;
		this.drawableUp = drawableUp;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		int action = event.getAction();

		if(action == MotionEvent.ACTION_DOWN)
		{
			if(view instanceof TextView)
			{
				if(colorUp == -1) colorUp = ((TextView)view).getCurrentTextColor();
				((TextView)view).setTextColor(colorDown);
			}
			else
			{
				if(drawableDown != null)
				{
					view.setBackgroundDrawable(drawableDown);
				}
				else
				{
					if(colorUp == -1) colorUp = Utile.getBgColor(view);
					view.setBackgroundColor(colorDown);
				}
			}
		}
		else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
		{
			if(view instanceof TextView)
			{
				((TextView)view).setTextColor(colorUp);
			}
			else
			{
				if(drawableUp != null)
				{
					view.setBackgroundDrawable(drawableUp);
				}
				else
				{
					view.setBackgroundColor(colorUp);
				}
			}
		}

		return false;
	}
}