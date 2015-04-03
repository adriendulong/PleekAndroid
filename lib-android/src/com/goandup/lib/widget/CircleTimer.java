package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.goandup.lib.R;

public class CircleTimer extends ImageView
{
	private int width;
	private int height;
	private float angle;
	private int color;
	private int colorBorder;
	private int widthBorder;

	public CircleTimer(Context context)
	{
		this(context, null);
	}
	public CircleTimer(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	public CircleTimer(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		if(attrs != null)
		{
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleTimer, defStyle, 0);

			angle = a.getInt(R.styleable.CircleTimer_startAngle, 0);
			color = a.getColor(R.styleable.CircleTimer_color, Color.WHITE);
			colorBorder = a.getColor(R.styleable.CircleTimer_colorBorder, Color.BLACK);
			widthBorder = a.getDimensionPixelSize(R.styleable.CircleTimer_widthBorder, 0);
			a.recycle();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);

		if(bmp == null) bmp = Bitmap.createBitmap(width, height,  Bitmap.Config.ARGB_8888);
		if(border == null) border = new Paint();
		if(rectF == null) rectF = new RectF(0, 0, width, height);
		if(paint == null) paint = new Paint();
		
		setAngle(0f);
	}

	public void startTimer(final int time)
	{
		startTimer(time, false);
	}

	private MyThread thread;
	
	public void startTimer(final int time, final boolean repeat)
	{
		reset();
		
		final float angleByFrame = 360f / ((float)time / 20f);
		
		thread = new MyThread(angleByFrame, repeat);
		thread.start();
	}
	
	public void reset()
	{
		if(thread != null) thread.goodStop();
		
		if(width > 0 && height > 0)
		{
			bmp = Bitmap.createBitmap(width, height,  Bitmap.Config.ARGB_8888);
			border = new Paint();
			rectF = new RectF(0, 0, width, height);
			paint = new Paint();
			setAngle(0f);
		}
	}

	public void addAngle(float angle)
	{
		setAngle((getAngle() + angle) % 360);
	}

	public float getAngle()
	{
		return angle;
	}

	public void setAngle(float angle)
	{
		this.angle = angle;
		postInvalidate();
	}
	
	public int getColorBorder() {
		return colorBorder;
	}
	
	public void setColorBorder(int colorBorder) {
		this.colorBorder = colorBorder;
	}
	
	public int getWidthBorder() {
		return widthBorder;
	}
	
	public void setWidthBorder(int widthBorder) {
		this.widthBorder = widthBorder;
	}

	Bitmap bmp;
	Paint border;
	RectF rectF;
	Paint paint;
	@Override
    protected void onDraw(Canvas canvas)
    {
		border.setColor(colorBorder);
		border.setAntiAlias(true);
		border.setStrokeWidth(1);
		border.setStyle(Style.STROKE);
		canvas.drawArc (rectF, 270, angle, true, border);

		paint.setColor(color);
		paint.setAntiAlias(true);
		canvas.drawArc (rectF, 270, angle, true, paint);

		if(widthBorder > 0)
		{
			border.setStrokeWidth(widthBorder);
			canvas.drawCircle(width >> 1, height >> 1, (width >> 1) - (widthBorder >> 1), border);
		}
		
        super.onDraw(canvas);
    }
	
	private class MyThread extends Thread
	{
		private boolean running = true;
	
		private float angleByFrame;
		private boolean repeat;
		
		public MyThread(float angleByFrame, boolean repeat) {
			super();
			this.angleByFrame = angleByFrame;
			this.repeat = repeat;
		}

		public void goodStop()
		{
			running = false;
		}
		
		@Override
		public void run() {

			while(running)
			{
				try {
					Thread.sleep(20);
				} catch (Exception e) { }

				addAngle(angleByFrame);
				
				if(angle > 360f && !repeat) running = false;
			}
		}
	}
}
