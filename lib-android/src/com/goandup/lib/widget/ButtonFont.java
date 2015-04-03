package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.goandup.lib.R;
import com.goandup.lib.utile.L;

public class ButtonFont extends Button
{
    public ButtonFont(Context context) {
        super(context);
    }

    public ButtonFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public ButtonFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFont);
        String customFont = a.getString(R.styleable.CustomFont_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try
        {
        	tf = Typeface.createFromAsset(ctx.getAssets(), asset);  
        } 
        catch (Exception e)
        {
            L.e("TextViewFont : Impossible de charger la CustomFont e=["+e.getMessage()+"]");
            return false;
        }

        setTypeface(tf);  
        return true;
    }

}