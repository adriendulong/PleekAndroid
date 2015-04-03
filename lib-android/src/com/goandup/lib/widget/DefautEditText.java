package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

import com.goandup.lib.R;

/**
 * @author nico dto
 */

public class DefautEditText extends EditText implements OnFocusChangeListener, OnTouchListener
{
//	public final static int COLOR_TEXT_ENABLE = Color.WHITE;
	public final static int COLOR_TEXT_DISABLE = 0xff777777;

	private String defautText;
	private int type;
	private Context context;
	private int enableTextColor;

	private OnFocusChangeListener onFocusChangeListener;
	private OnTouchListener onTouchListener;

	public DefautEditText(Context context)
	{
		this(context, null);
	}
	public DefautEditText(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	public DefautEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.context = context;

		if(attrs != null)
		{
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DefautEditText);

			for (int i = 0; i < a.getIndexCount(); ++i)
			{
				int attr = a.getIndex(i);
				switch (attr)
				{
				case R.styleable.DefautEditText_defautText:
					defautText = a.getString(attr);
					break;
				}
			}
			a.recycle();
		}

		init();
	}

	private void init()
	{
		super.setOnFocusChangeListener(this);
		super.setOnTouchListener(this);
		enableTextColor = getCurrentTextColor();
		
		setTextColor(COLOR_TEXT_DISABLE);
		type = getInputType();
		super.setInputType(InputType.TYPE_CLASS_TEXT);
		
		if(defautText != null) setValue(getValue());
	}

	public String getDefautText()
	{
		return defautText;
	}

	public void setDefautText(String defautText)
	{
		this.defautText = defautText;
		setValue(getValue());
	}

	public void setDefautText(int idDefautText)
	{
		this.defautText = context.getResources().getString(idDefautText);
		setValue(getValue());
	}

	public String getValue()
	{
		String value = getText().toString();
		if(value == null || value.equals(defautText) || value.trim().length() == 0) return null;
		else return value;
	}

	public void setValue(String value)
	{
		if(value == null)
		{
			setText(defautText);
			setTextColor(COLOR_TEXT_DISABLE);
			super.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		else
		{
			setText(value);
			setTextColor(enableTextColor);
			super.setInputType(type);
		}
	}

	@Override
	public void setInputType(int type)
	{
		this.type = type;
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener l)
	{
		onFocusChangeListener = l;
	}

	@Override
	public void onFocusChange(View view, boolean isFocus)
	{
		if(onFocusChangeListener != null) onFocusChangeListener.onFocusChange(view, isFocus);
		if(isFocus)
		{
			if(DefautEditText.this.getText().toString().equals(defautText))
			{
				DefautEditText.this.setText("");
				super.setInputType(type);
				DefautEditText.this.setTextColor(enableTextColor);
			}
		}
		else
		{
			if(DefautEditText.this.getText().toString().trim().length() == 0)
			{
				DefautEditText.this.setText(defautText);
				super.setInputType(InputType.TYPE_CLASS_TEXT);
				DefautEditText.this.setTextColor(COLOR_TEXT_DISABLE);
			}
		}
	}

	@Override
	public void setOnTouchListener(OnTouchListener l)
	{
		onTouchListener = l;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		if(onTouchListener != null) onTouchListener.onTouch(view, event);

		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			if(getText().toString().equals(defautText))
			{
				setText("");
				super.setInputType(type);
				setTextColor(enableTextColor);
			}
		}
		return false;
	}
}
