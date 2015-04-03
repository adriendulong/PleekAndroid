package com.goandup.lib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by nicolas on 06/01/15.
 */
public class ObservableVideoView extends VideoView
{
    private Listener listener;
    private boolean paused;

    public ObservableVideoView(Context context) {
        this(context, null);
    }

    public ObservableVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //init
    }


    @Override
    public void start()
    {
        super.start();

        if(paused && listener != null) listener.onResume();
        paused = false;
    }

    @Override
    public void pause()
    {
        super.pause();

        if(listener != null) listener.onPause();
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener
    {
        public void onPause();
        public void onResume();
    }
}
