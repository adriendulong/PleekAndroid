package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.goandup.lib.R;
import com.goandup.lib.utile.L;

import java.util.HashMap;
import java.util.Map;

public class TextViewFont extends TextView {

    private static Map<String, Typeface> fonts = new HashMap<String, Typeface>();

    public TextViewFont(Context context) {
        super(context);
    }

    public TextViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public TextViewFont(Context context, AttributeSet attrs, int defStyle) {
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
        if (!fonts.containsKey(asset)) {
            Typeface tf = null;
            try {
                tf = Typeface.createFromAsset(ctx.getAssets(), asset);
            } catch (Exception e) {
                L.e("TextViewFont : Impossible de charger la CustomFont e=[" + e.getMessage() + "] - asset=[" + asset + "]");
                return false;
            }

            setTypeface(tf);
            fonts.put(asset, tf);
        } else {
            setTypeface(fonts.get(asset));
        }

        return true;
    }

    public Bitmap getBitmapOfView()
    {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        draw(c);
        return bitmap;
    }

}