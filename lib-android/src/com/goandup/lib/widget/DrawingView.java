package com.goandup.lib.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by nicolas on 13/08/2014.
 */
public class DrawingView extends View
{
    private Path path;
    private Paint paint;
    private LinkedList<Line> historyTask;
    private Listener listener;
    private boolean active;
    private Bitmap backgroundImage;
    private int backgroundColor;
    private Rect rectCanvas;

    public DrawingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        historyTask = new LinkedList<Line>();

        path = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(16);

        active = true;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (backgroundImage != null && backgroundImage.getWidth() > 0 && backgroundImage.getHeight() > 0)
        {
            Rect bgRect = new Rect(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());
            if(rectCanvas == null) rectCanvas = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(backgroundImage, bgRect, rectCanvas, null);
        }
        else{
            canvas.drawColor(backgroundColor);
        }
        int initialColor = paint.getColor();
        float initialSize = paint.getStrokeWidth();
        for (Line line : historyTask)
        {
            paint.setColor(line.color);
            paint.setStrokeWidth(line.size);
            canvas.drawPath(line.path, paint);
        }
        paint.setColor(initialColor);
        paint.setStrokeWidth(initialSize);
        canvas.drawPath(path, paint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!active) return false;

        float x = event.getX();
        float y = event.getY();

        int action = event.getAction();

        if(action == MotionEvent.ACTION_DOWN)
        {
            path.reset();
            path.moveTo(x, y);
            mX = x;
            mY = y;

            invalidate();
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
            {
                path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }

            invalidate();
        }
        else if(action == MotionEvent.ACTION_UP)
        {
            path.lineTo(mX, mY);
            historyTask.add(new Line(paint.getStrokeWidth(), paint.getColor(), path));
            path = new Path();

            if(listener != null) listener.historyStackChange(historyTask.size());

            invalidate();
        }

        return true;
    }

    public void undo()
    {
        historyTask.removeLast();
        invalidate();

        if(listener != null) listener.historyStackChange(historyTask.size());
    }

    public void captureImage(ListenerCapture listenerCapture)
    {
        captureImage(0, 0, listenerCapture);
    }

    public void captureImage(int captureWidth, int captureHeight, ListenerCapture listenerCapture)
    {
        if(captureWidth == 0 || captureHeight == 0){
            captureWidth = getWidth();
            captureHeight = getWidth();
        }

        Bitmap globalBitmap = null;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if(captureWidth > 0 && captureHeight > 0 && viewWidth > 0 && viewHeight > 0)
        {
            //final canvas
            globalBitmap = Bitmap.createBitmap(captureWidth, captureHeight, Bitmap.Config.RGB_565);
            Canvas globalCanvas = new Canvas(globalBitmap);
            globalCanvas.drawColor(isWithBackground() ? Color.BLACK : backgroundColor);

            //DrawView to bitmap
            Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            draw(c);

            //resize bitmap in fianl canvas
            float ratioView = (float)viewWidth / (float)viewHeight;
            float ratioCapture = (float)captureWidth / (float)captureHeight;
            int left = 0;
            int top = 0;
            int scaleWidth;
            int scaleHeight;
            if(ratioView > ratioCapture)
            {
                scaleWidth = captureWidth;
                scaleHeight = (int) (((float)scaleWidth/(float)viewWidth) * viewHeight);
                top = (captureHeight - scaleHeight) >> 1;
            }
            else
            {
                scaleHeight = captureHeight;
                scaleWidth = (int) (((float)scaleHeight/(float)viewHeight) * viewWidth);
                left = (captureWidth - scaleWidth) >> 1;
            }

            //draw bitmap in final canvas with good size and position
            Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            Rect dstRect = new Rect(left, top, left+scaleWidth, top+scaleHeight);
            globalCanvas.drawBitmap(bitmap, srcRect, dstRect, null);
        }

        if(listenerCapture != null) listenerCapture.repCapture(globalBitmap);
    }

    /**
     * getter / setter
     */
    public void setPaintColor(int color) {
        paint.setColor(color);
    }

    public void setPaintSize(int size) {
        paint.setStrokeWidth(size);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public int getSizeUndo() {
        return historyTask.size();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getColor() {
        return paint.getColor();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    public void setBackgroundImage(Bitmap backgroundImage)
    {
        this.backgroundImage = backgroundImage;
        invalidate();
    }

    public void setBackgroundImage(Drawable backgroundImage)
    {
        Bitmap b = null;
        if(backgroundImage instanceof BitmapDrawable)
        {
            b = ((BitmapDrawable)backgroundImage).getBitmap();
        }
        setBackgroundImage(b);
    }

    public boolean isWithBackground() {
        return backgroundImage != null;
    }

    /**
     * class
     */
    private class Line
    {
        public Path path;
        public int color;
        public float size;

        private Line(float size, int color, Path path)
        {
            this.size = size;
            this.color = color;
            this.path = path;
        }
    }

    /**
     * interface
     */
    public interface Listener
    {
        public void historyStackChange(int nbUndo);
    }
    public interface ListenerCapture
    {
        public void repCapture(Bitmap bitmap);
    }
}