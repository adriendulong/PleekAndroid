package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.goandup.lib.R;
import com.goandup.lib.utile.L;

public class EditTextFont extends EditText
{
    private Listener listener;

    public EditTextFont(Context context) {
        super(context);
    }

    public EditTextFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public EditTextFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFont);
        String customFont = a.getString(R.styleable.CustomFont_customFont);
        boolean fromHTML = a.getBoolean(R.styleable.CustomFont_fromHTML, false);
        setCustomFont(ctx, customFont);
        a.recycle();
        
        if(fromHTML) setText(Html.fromHtml(getText().toString()));
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try
        {
        	tf = Typeface.createFromAsset(ctx.getAssets(), asset);  
        } 
        catch (Exception e)
        {
            L.e("EditTextFont : Impossible de charger la CustomFont e=["+e.getMessage()+"] - asset=["+asset+"]");
            return false;
        }

        setTypeface(tf);  
        return true;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if(listener != null) return listener.keyPress(keyCode);
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if(listener != null) listener.sizeChange(w, h, oldw, oldh);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener
    {
        void sizeChange(int w, int h, int oldw, int oldh);
        boolean keyPress(int keyCode);
    }

    public Bitmap getBitmapOfView()
    {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        draw(c);
        return bitmap;
    }
}