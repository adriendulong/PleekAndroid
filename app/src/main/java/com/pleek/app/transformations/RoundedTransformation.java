package com.pleek.app.transformations;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

public class RoundedTransformation implements com.squareup.picasso.Transformation {

    private final int pixels;
    private int w;
    private int h;
    private final boolean squareTL;
    private final boolean squareTR;
    private final boolean squareBL;
    private final boolean squareBR;

    public RoundedTransformation(int pixels, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {
        this.pixels = pixels;
        this.squareTL = squareTL;
        this.squareTR = squareTR;
        this.squareBL = squareBL;
        this.squareBR = squareBR;
    }

    @Override
    public Bitmap transform(final Bitmap bitmap) {
        w = bitmap.getWidth();
        h = bitmap.getWidth();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        //make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);


        //draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, 0, w/2, h/2, paint);
        }

        if (squareTR) {
            canvas.drawRect(w/2, 0, w, h/2, paint);
        }

        if (squareBL) {
            canvas.drawRect(0, h/2, w/2, h, paint);
        }

        if (squareBR) {
            canvas.drawRect(w/2, h/2, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0,0, paint);

        bitmap.recycle();
        return output;
    }

    @Override
    public String key() {
        return "rounded(radius=" + pixels + ")";
    }
}