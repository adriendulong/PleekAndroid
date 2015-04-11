package com.pleek.app.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.bean.Reaction;
import com.pleek.app.utils.PicassoUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nicolas on 19/12/14.
 */
public class ReactAdapter extends BaseAdapter implements View.OnTouchListener {
    private final int NB_COLUMN = 3;
    private final int maxSizeReactPx = 225;

    private List<Reaction> listReact;
    private Listener listener;
    private Context context;
    private List<View> listTextViewUsername;

    private int heightListView;
    private MediaPlayer mediaPlayer;
    private ViewGroup currentItemPlay;
    private Reaction currentReactPlay;
    private boolean isUsernameShow;

    private Screen screen;
    private DoubletapRunnable doubletapRunnable;

    int oneDp, heightPiki, widthPiki;

    public ReactAdapter(List<Reaction> listReact, Listener listener) {
        this(listReact, listener, listener instanceof Context ? (Context) listener : null);
    }

    public ReactAdapter(List<Reaction> listReact, Listener listener, Context context) {
        this.listReact = listReact;
        this.listener = listener;
        this.context = context;

        screen = Screen.getInstance(context);
        listTextViewUsername = new ArrayList<View>();
        doubletapRunnable = new DoubletapRunnable();

        oneDp = screen.dpToPx(1);
        heightPiki = (screen.getWidth() - screen.dpToPx(NB_COLUMN - 1)) / NB_COLUMN; //(screen widt - NB_COLUMN-1 divider) / NB_COLUMN
        widthPiki = heightPiki + oneDp;
    }

    private int nbRow = -1;
    private int minNbRow = -1;

    @Override
    public int getCount() {
        if (listReact == null) {
            return 0;
        }

        return listReact.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return getCount() > 0 ? listReact.get(i % getCount()) : null;
    }

    @Override
    public long getItemId(int i) {
        return getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        clearView(view);

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_react, parent, false);
            view.getLayoutParams().height = heightPiki;
            view.getLayoutParams().width = widthPiki;

            ReactViewHolder vh = new ReactViewHolder(view);
            view.setTag(R.id.vh, vh);
        }

        ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);

        if (vh != null) {
            // All other itm is item_react or placeholder
            listTextViewUsername.remove(vh.txtUserName);
            listTextViewUsername.add(vh.txtUserName);
            boolean isPlaceHolder = false;

            if (vh.imgReact != null) {
                vh.imgReact.setVisibility(View.VISIBLE);
                vh.progressBar.setVisibility(View.GONE);
                vh.imgError.setVisibility(View.GONE);
                vh.imgMute.setVisibility(View.GONE);
                vh.txtUserName.setVisibility(isUsernameShow ? View.VISIBLE : View.GONE);

                if (i >= 1 && i <= listReact.size()) {
                    Reaction react = listReact.get(i - 1);
                    vh.txtUserName.setText("@" + react.getNameUser());

                    if (react.getUrlPhoto() != null) { //is Parse React
                        //Drawable drawablePlaceHolder = new BitmapDrawable(context.getResources(), react.getTmpPhoto());

                        if (heightPiki < maxSizeReactPx) {
                            PicassoUtils.with(context)
                                    .load(react.getUrlPhoto())
                                    .resize((int) (widthPiki * 0.80), (int) (heightPiki * 0.80))
                                    .into(vh.imgReact);
                        } else {
                            PicassoUtils.with(context)
                                    .load(react.getUrlPhoto())
                                    .resize((int) (widthPiki * 0.80), (int) (heightPiki * 0.80))
                                    .into(vh.imgReact);
                        }

                        vh.imgPlay.setVisibility(react.isVideo() ? View.VISIBLE : View.GONE);
                        react.loadVideoToTempFile(context);
                    } else {
                        vh.imgReact.setImageBitmap(react.getTmpPhoto());
                        vh.imgPlay.setVisibility(View.GONE);
                        vh.progressBar.setVisibility(react.isLoadError() ? View.GONE : View.VISIBLE);
                        if (react.isLoadError()) {
                            vh.imgError.setVisibility(View.VISIBLE);
                            vh.imgError.setImageResource(R.drawable.picto_reload);
                        }
                    }
                } else {
                    isPlaceHolder = true;
                    vh.imgReact.setImageDrawable(null);
                    vh.imgReact.setBackgroundColor(context.getResources().getColor(i % 2 != 0 ? R.color.grisFondPikiPlaceholder1 : R.color.grisFondPikiPlaceholder2));
                    vh.itemView.setOnTouchListener(null);
                    vh.imgPlay.setVisibility(View.GONE);
                    listTextViewUsername.remove(vh.txtUserName);
                    vh.txtUserName.setText("");
                }
            }

            if (!isPlaceHolder && i > 0) {
                vh.itemView.setTag(new Integer(i - 1));
                vh.itemView.setTag(R.string.tag_downpresse, new DownRunnable(vh.itemView));
                vh.itemView.setTag(R.string.tag_longpresse, new LongpressRunnable());
                vh.itemView.setOnTouchListener(this);
            }
        }

        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        if (view.getTag() instanceof Integer) {
            int position = (Integer)view.getTag();

            DownRunnable down = (DownRunnable) view.getTag(R.string.tag_downpresse);
            LongpressRunnable longpress = (LongpressRunnable) view.getTag(R.string.tag_longpresse);

            if (action == MotionEvent.ACTION_DOWN) {
                //highlight with 90ms delay. Because I dont want highlight if user scroll
                handler.postDelayed(down, DOWN_TIME);
                handler.postDelayed(longpress, LOGNPRESS_TIME);

                if(isLastTapForDoubleTap && listener != null) {
                    listener.doubleTapReaction(listReact.get(position));
                } else {
                    isLastTapForDoubleTap = true;
                    handler.postDelayed(doubletapRunnable, DOUBLETAP_TIME); //this runnable just pass "isLastTapForDoubleTap" to false in 300ms
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                //finger move, cancel highlight
                handler.removeCallbacks(down);
                handler.removeCallbacks(longpress);
                if(isUsernameShow) showUsername(false);

                itemMarkDown(view, false);
            }

            if (action == MotionEvent.ACTION_UP) {
                // highlight just after clicked during 90ms
                itemMarkDown(view, true);

                handler.postDelayed(down, DOWN_TIME);

                //send to listener
                if (position >= 0) {
                    Reaction react = listReact.get(position);

                    if (react.isVideo()) {
                        if (react.equals(currentReactPlay)) tggleMuteVideo(view);
                        else playVideo(react, view);
                    }

                    else if (listener != null) listener.clickOnReaction(react);
                }
            }
        }

        return true;
    }

    //DOWN EFFECT
    private final int DOWN_TIME = 90;//ms
    final Handler handler = new Handler();

    public Reaction removeReaction(int position) {
        Reaction removed = listReact.remove(position);
        notifyDataSetChanged();
        return removed;
    }

    public Reaction getReaction(int position) {
        return listReact.get(position);
    }

    class DownRunnable implements Runnable {
        private View view;

        public DownRunnable(View view) {
            this.view = view;
        }

        @Override
        public void run()  {
            View layoutOverlay = view.findViewById(R.id.layoutOverlay);
            boolean isVisible = layoutOverlay != null && layoutOverlay.getVisibility() == View.VISIBLE;
            itemMarkDown(view, !isVisible);
        }
    }

    private final int LOGNPRESS_TIME = 700;//ms
    class LongpressRunnable implements Runnable {
        @Override
        public void run() {
            showUsername(true);
        }
    }

    private final int DOUBLETAP_TIME = 300;//ms
    private boolean isLastTapForDoubleTap;
    class DoubletapRunnable implements Runnable {
        @Override
        public void run()
        {
            isLastTapForDoubleTap = false;
        }
    }

    private void itemMarkDown(View item, boolean down) {
        View layoutOverlay = item.findViewById(R.id.layoutOverlay);

        if (layoutOverlay != null) {
            int visibility = down ? View.VISIBLE : View.GONE;
            layoutOverlay.setVisibility(visibility);
        }
    }

    // PLAY / PAUSE
    private boolean videoPaused;
    public void pauseAllVideo() {
        videoPaused = true;
        if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        handler.removeCallbacks(resumeRunnable);
    }

    public void resumeAllVideo() {
        if(videoPaused) {
            handler.postDelayed(resumeRunnable, RESUME_VIDEO_TIME);
        }

        videoPaused = false;
    }

    private static int RESUME_VIDEO_TIME = 150;//ms

    final Runnable resumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) mediaPlayer.start();
        }
    };

    private void clearView(View item) {
        if (item instanceof ViewGroup) {
            ViewGroup v = (ViewGroup) item;
            for (int i = 0; i < v.getChildCount(); i++) {
                View child = v.getChildAt(i);

                if (child == currentItemPlay) {
                    stopCurrentVideo();
                }
            }
        }
    }


    private void tggleMuteVideo(View view) {
        ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);

        if (vh.imgMute.getVisibility() == View.VISIBLE) { //is mute
            vh.imgMute.setVisibility(View.GONE);
            mediaPlayer.setVolume(1, 1);
        } else { //is not mute
            vh.imgMute.setVisibility(View.VISIBLE);
            mediaPlayer.setVolume(0, 0);
        }
    }


    private boolean isPreparePlaying;
    private void playVideo(final Reaction react, final View view) {
        if (isPreparePlaying) return;
        isPreparePlaying = true;

        try {
            final ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                stopCurrentVideo();
            }

            if (vh.layoutVideo != null) {
                vh.imgPlay.setVisibility(View.GONE);
                vh.imgError.setVisibility(View.GONE);

                if (react.isLoadError()) {
                    // show error
                    vh.imgError.setVisibility(View.VISIBLE);
                    vh.imgError.setImageResource(R.drawable.picto_loaderror);
                    isPreparePlaying = false;
                } else if (react.isLoaded()) {
                    // play video
                    vh.layoutVideo.setVisibility(View.VISIBLE);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            vh.imgReact.setVisibility(View.GONE);
                            mediaPlayer.start();
                            currentItemPlay = (ViewGroup) vh.itemView;
                            currentReactPlay = react;
                            isPreparePlaying = false;
                        }
                    });

                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                            L.e("ERROR : play video in MediaPlayer - what=[" + what + "] - extra=[" + extra + "]");
                            vh.imgError.setVisibility(View.VISIBLE);
                            isPreparePlaying = false;
                            return false;
                        }
                    });

                    vh.imgMute.setVisibility(View.GONE);
                    mediaPlayer.setVolume(1,1);
                    mediaPlayer.setLooping(true);
                    //mediaPlayer.setDisplay(videoReact.getHolder());
                    mediaPlayer.setDataSource(react.getTempFilePath(context));
                    mediaPlayer.prepareAsync();
                } else { //loading
                    // wait loading
                    vh.progressBar.setVisibility(View.VISIBLE);
                    react.setLoadVideoEndListener(new Reaction.LoadVideoEndListener() {
                        @Override
                        public void done(boolean ok, Reaction react) {
                            vh.progressBar.setVisibility(View.GONE);
                            isPreparePlaying = false;
                            playVideo(react, view);
                        }
                    });
                }
            }
        } catch (Exception e) {
            if (view instanceof ViewGroup) {
                final ViewGroup item = (ViewGroup)view;
                View imgError = item.findViewById(R.id.imgError);
                if(imgError != null) imgError.setVisibility(View.VISIBLE);
            }

            L.e("Error : playVideo - e:" + e.getMessage());
            e.printStackTrace();

            isPreparePlaying = false;
        }
    }

    public void stopCurrentVideo() {
        if (currentItemPlay != null && currentReactPlay != null) {
            ReactViewHolder vh = (ReactViewHolder) currentItemPlay.getTag(R.id.vh);

            vh.layoutVideo.setVisibility(View.GONE);
            vh.imgReact.setVisibility(View.VISIBLE);
            vh.imgPlay.setVisibility(View.VISIBLE);
            vh.imgMute.setVisibility(View.GONE);
            vh.imgError.setVisibility(View.GONE);
            vh.progressBar.setVisibility(View.GONE);
            currentItemPlay.invalidate();

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentItemPlay = null;
            currentReactPlay = null;
        }
    }

    private int TIME_SHOW_USERNAME = 150; // ms
    public void showUsername(boolean show) {
        isUsernameShow = show;
        for (View textView : listTextViewUsername) {
            if (show) Utile.fadeIn(textView, TIME_SHOW_USERNAME);
            else Utile.fadeOut(textView, TIME_SHOW_USERNAME);
        }
    }

    public void setHeightListView(int heightListView) {
        this.heightListView = heightListView;
    }

    public List<Reaction> addReact(Reaction react) {
        return addReact(react, null);
    }

    public List<Reaction> addReact(Reaction react, Reaction oldReact) {
        listReact.remove(oldReact);
        listReact.add(0, react);
        notifyDataSetChanged();
        return listReact;
    }

    public void setListReact(List<Reaction> listReact) {
        this.listReact = listReact;
        notifyDataSetChanged();
    }

    public boolean markLoadError(Reaction errorReact, boolean error) {
        int i = listReact.indexOf(errorReact);

        if (i >= 0) {
            listReact.get(i).setLoadError(error);
            notifyDataSetChanged();
        }

        return i >= 0;
    }

    @Override
    public void notifyDataSetChanged() {
        nbRow = -1;
        minNbRow = -1;
        super.notifyDataSetChanged();
    }

    //INTERFACE
    public interface Listener {
        public void clickOnReaction(Reaction react);
        public void doubleTapReaction(Reaction react);
        public void showPlaceHolderItem(boolean show);
    }

    class ReactViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.back)
        RelativeLayout layoutBack;
        @InjectView(R.id.imgActionOff)
        ImageView imgActionOff;
        @InjectView(R.id.imgActionOn)
        ImageView imgActionOn;
        @InjectView(R.id.front)
        RelativeLayout layoutFront;
        @InjectView(R.id.imgReact)
        ImageView imgReact;
        @InjectView(R.id.progressBar)
        ProgressBar progressBar;
        @InjectView(R.id.imgPlay)
        ImageView imgPlay;
        @InjectView(R.id.imgMute)
        ImageView imgMute;
        @InjectView(R.id.imgError)
        ImageView imgError;
        @InjectView(R.id.txtUserName)
        TextViewFont txtUserName;
        @InjectView(R.id.layoutOverlay)
        RelativeLayout layoutOverlay;
        @InjectView(R.id.layoutVideo)
        LinearLayout layoutVideo;

        public ReactViewHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }
    }
}