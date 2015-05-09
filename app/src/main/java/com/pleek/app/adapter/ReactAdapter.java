package com.pleek.app.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Screen;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.activity.ParentActivity;
import com.pleek.app.bean.AutoResizeFontTextWatcher;
import com.pleek.app.bean.Reaction;
import com.pleek.app.bean.VideoBean;
import com.pleek.app.listeners.FlipListener;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.views.CircleProgressBar;
import com.pleek.app.views.CircularProgressBar;
import com.pleek.app.views.TextViewFontAutoResize;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nicolas on 19/12/14.
 */
public class ReactAdapter extends BaseAdapter implements View.OnTouchListener, SurfaceHolder.Callback {

    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private final int NB_COLUMN = 2;
    private final int maxSizeReactPx = 225;

    private List<Reaction> listReact;
    private Listener listener;
    private Context context;
    private boolean mIsOwnerOfPiki = false;

    private int heightListView;
    private MediaPlayer mediaPlayer;
    private ReactViewHolder currentItemPlay;
    private VideoBean currentReactPlay;

    private Screen screen;
    private DoubletapRunnable doubletapRunnable;

    int oneDp, heightPiki, widthPiki;

    private SurfaceView surfaceView;
    private ValueAnimator mFlipAnimator;
    private AutoResizeFontTextWatcher autoResizeFontTextWatcher;

    private Set<String> likeReactSet;

    public ReactAdapter(List<Reaction> listReact, Listener listener, boolean isOwnerOfPiki) {
        this(listReact, listener, listener instanceof Context ? (Context) listener : null, isOwnerOfPiki);
    }

    public ReactAdapter(List<Reaction> listReact, Listener listener, Context context, boolean isOwnerOfPiki) {
        this.listReact = listReact;
        this.listener = listener;
        this.context = context;
        this.mIsOwnerOfPiki = isOwnerOfPiki;

        screen = Screen.getInstance(context);
        doubletapRunnable = new DoubletapRunnable();

        oneDp = screen.dpToPx(1);
        heightPiki = (screen.getWidth() - screen.dpToPx(NB_COLUMN - 1)) / NB_COLUMN; //(screen widt - NB_COLUMN-1 divider) / NB_COLUMN
        widthPiki = heightPiki + oneDp;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setZOrderOnTop(true);

        mFlipAnimator = ValueAnimator.ofFloat(0f, 1f);
        mFlipAnimator.setDuration(250);

        likeReactSet = new HashSet<String>();
    }

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
    public View getView(final int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_react, parent, false);
            view.getLayoutParams().height = heightPiki;
            view.getLayoutParams().width = widthPiki;

            ReactViewHolder vh = new ReactViewHolder(view);
            view.setTag(R.id.vh, vh);
        }

        final ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);
        clearView(vh);

        if (vh != null) {
            // All other itm is item_react or placeholder
            boolean isPlaceHolder = false;

            if (vh.imgReact != null) {
                vh.imgReact.setVisibility(View.VISIBLE);
                vh.progressBar.setVisibility(View.GONE);
                vh.progressBarTempReact.setVisibility(View.GONE);
                vh.imgError.setVisibility(View.GONE);
                vh.imgMute.setVisibility(View.GONE);
                vh.progressBar.setColorSchemeResources(R.color.progressBar);

                if (i >= 1 && i <= listReact.size()) {
                    final Reaction react = listReact.get(i - 1);

                    vh.progressBar.setVisibility(View.VISIBLE);

                    if (react.getUrlPhoto() != null) { //is Parse React
                        if (vh.customTarget != null) Picasso.with(context).cancelRequest(vh.customTarget);

                        PicassoUtils.with(context)
                            .load(react.getUrlPhoto())
                            .resize(widthPiki, heightPiki)
                            .into(vh.customTarget);

                        vh.imgPlay.setVisibility(react.isVideo() ? View.VISIBLE : View.GONE);
                        react.loadVideoToTempFile(context);
                    } else {
                        if (react.isVideo()) {
                            vh.progressBar.setVisibility(View.GONE);
                            vh.progressBarTempReact.setVisibility(View.VISIBLE);
                            vh.progressBarTempReact.setProgress((float) (react.getCurrentProgress() / react.getTotalProgress()));
                        } else {
                            vh.imgPlay.setVisibility(View.GONE);
                        }
                        vh.imgReact.setImageBitmap(react.getTmpPhoto());
                        vh.imgPlay.setVisibility(View.GONE);
                        if (react.isLoadError()) {
                            vh.imgError.setVisibility(View.VISIBLE);
                            vh.imgError.setImageResource(R.drawable.picto_reload);
                        }
                    }

                    if (canDeleteReact(react)) {
                        vh.imgReport.setImageResource(R.drawable.picto_trash);
                        vh.txtReport.setText(R.string.piki_react_delete);
                        vh.layoutReport.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                listener.onDeleteReact(i);
                            }
                        });
                    } else {
                        vh.imgReport.setImageResource(R.drawable.picto_report);
                        vh.txtReport.setText(R.string.piki_popup_report_title);
                        vh.layoutReport.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                listener.onReportReact(i);
                            }
                        });
                    }

                    final Set<String> friendsIds = ((ParentActivity) context).getFriendsPrefs();
                    vh.txtAddFriend.setText(react.getNameUser());
                    if (friendsIds != null && friendsIds.contains(react.getUserId())) {
                        vh.txtAddFriend.setTextColor(Color.BLACK);
                        vh.imgAddFriend.setImageResource(R.drawable.picto_added);
                    } else {
                        vh.txtAddFriend.setTextColor(context.getResources().getColor(R.color.grisTextDisable));
                        vh.imgAddFriend.setImageResource(R.drawable.picto_add_user);
                    }

                    vh.layoutAddFriend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vh.friendsProgressBar.setVisibility(View.VISIBLE);
                            vh.imgAddFriend.setVisibility(View.GONE);
                            listener.onAddFriend(i, react, vh.friendsProgressBar, vh.imgAddFriend, vh.txtAddFriend);
                        }
                    });

                    vh.txtLike.setText("" + react.getNbLikes());

                    if (react.getNbLikes() > 0) {
                        vh.txtNBLikesFront.setText("" + react.getNbLikes());
                        vh.layoutNBLikesFront.setVisibility(View.VISIBLE);
                    } else {
                        vh.layoutNBLikesFront.setVisibility(View.GONE);
                    }

                    if (hasLikedReact(react)) {
                        vh.imgLike.setImageResource(R.drawable.picto_like_done);
                        vh.txtLike.setTextColor(Color.BLACK);
                    } else {
                        vh.imgLike.setImageResource(R.drawable.picto_like_grey);
                        vh.txtLike.setTextColor(context.getResources().getColor(R.color.grisTextDisable));
                    }

                    vh.layoutLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            likeAnim(react, vh, i);
                        }
                    });

                    vh.layoutPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) listener.onPreviewReact(i);
                        }
                    });
                } else {
                    isPlaceHolder = true;
                    vh.imgReact.setImageDrawable(null);
                    vh.imgReact.setBackgroundColor(context.getResources().getColor(R.color.fondBackReact));
                    vh.itemView.setOnTouchListener(null);
                    vh.imgPlay.setVisibility(View.GONE);
                }
            }

            if (!isPlaceHolder && i > 0) {
                vh.itemView.setTag(new Integer(i - 1));
                vh.itemView.setTag(R.string.tag_downpresse, new DownRunnable(vh.itemView));
                vh.itemView.setTag(R.string.tag_longpresse, new LongpressRunnable(listReact.get(i - 1), vh, i));
                vh.itemView.setOnTouchListener(this);
            }
        }

        return view;
    }

    private void likeAnim(final Reaction react, final ReactViewHolder vh, final int position) {
        if (!hasLikedReact(react)) {
            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(vh.imgLike, "scaleX", 0.2f, 1f);
            bounceAnimX.setDuration(300);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(vh.imgLike, "scaleY", 0.2f, 1f);
            bounceAnimY.setDuration(300);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
            bounceAnimY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    like(true, react, vh, position);
                }
            });

            animatorSet.play(bounceAnimX).with(bounceAnimY);
            animatorSet.start();
        } else {
            like(false, react, vh, position);
        }

        vh.txtLike.setText("" + react.getLikeCount());
        vh.txtNBLikesFront.setText("" + react.getLikeCount());
    }

    private void like(boolean isLike, final Reaction react, final ReactViewHolder vh, final int position) {
        if (isLike) {
            likeReactSet.add(react.getId());
            react.incrementLike();
            react.setHasLiked(true);
            vh.txtNBLikesFront.setText("" + react.getLikeCount());

            if (listener != null) {
                listener.onLikeReact(position);
            }

            notifyDataSetChanged();
        } else {
            likeReactSet.remove(react.getId());
            react.decrementLike();
            react.setHasLiked(false);

            if (listener != null) {
                listener.onDislikeReact(position);
            }

            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        if (view.getTag() instanceof Integer) {
            int position = (Integer)view.getTag();
            final Reaction react = listReact.get(position);

            DownRunnable down = (DownRunnable) view.getTag(R.string.tag_downpresse);
            LongpressRunnable longpress = (LongpressRunnable)view.getTag(R.string.tag_longpresse);

            if (action == MotionEvent.ACTION_DOWN) {
                //highlight with 90ms delay. Because I dont want highlight if user scroll
                handler.postDelayed(down, DOWN_TIME);
                handler.postDelayed(longpress, LOGNPRESS_TIME);

                if (isLastTapForDoubleTap && listener != null) {
                    listener.doubleTapReaction(listReact.get(position));
                } else {
                    isLastTapForDoubleTap = true;
                    handler.postDelayed(doubletapRunnable, DOUBLETAP_TIME); //this runnable just pass "isLastTapForDoubleTap" to false in 300ms
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                //finger move, cancel highlight
                final ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);
                handler.removeCallbacks(down);
                handler.removeCallbacks(longpress);

                itemMarkDown(view, false);
            }

            if (action == MotionEvent.ACTION_UP) {
                // highlight just after clicked during 90ms
                final ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);

                if (!longpress.wasPressedLong) {
                    if (!isFlipping()) {
                        flipLogic(vh, react);
                    }

                    if (!react.isVideo() || react.equals(currentReactPlay)) {
                        itemMarkDown(view, true);
                    }

                    handler.postDelayed(down, DOWN_TIME);

                    //send to listener
                    if (position >= 0) {
                        if (react.isVideo()) {
                            if (react.equals(currentReactPlay)) stopCurrentVideo();
                            else playVideo(react, view);
                        } else if (listener != null) listener.clickOnReaction(react);
                    }
                } else {
                    longpress.wasPressedLong = false;
                }
            }
        }

        return true;
    }

    private void flipLogic(ReactViewHolder vh, Reaction react) {
        if (isFlipped()) {
            mFlipAnimator.reverse();
        } else if (!react.isVideo() || react.equals(currentReactPlay)) {
            mFlipAnimator.removeAllUpdateListeners();
            mFlipAnimator.start();
            mFlipAnimator.addUpdateListener(new FlipListener(vh.layoutFront, vh.layoutBack));
        }
    }

    //DOWN EFFECT
    private final int DOWN_TIME = 90;//ms
    final Handler handler = new Handler();

    public Reaction removeReaction(int position) {
        Reaction removed = listReact.remove(position - 1);
        notifyDataSetChanged();
        return removed;
    }

    public Reaction getReaction(int position) {
        return listReact.get(position - 1);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (currentReactPlay.isLoadError()) {
            // show error
            currentItemPlay.imgError.setVisibility(View.VISIBLE);
            currentItemPlay.imgError.setImageResource(R.drawable.picto_loaderror);
            isPreparePlaying = false;
        } else if (currentReactPlay.isLoaded()) {
            // play video
            currentItemPlay.layoutVideo.setVisibility(View.VISIBLE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    currentItemPlay.imgReact.setVisibility(View.GONE);
                    mediaPlayer.start();
                    isPreparePlaying = false;
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    L.e("ERROR : play video in MediaPlayer - what=[" + what + "] - extra=[" + extra + "]");
                    currentItemPlay.imgError.setVisibility(View.VISIBLE);
                    isPreparePlaying = false;
                    return false;
                }
            });

            currentItemPlay.imgMute.setVisibility(View.GONE);
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(true);
            mediaPlayer.setDisplay(surfaceView.getHolder());
            try {
                FileInputStream fis = new FileInputStream(currentReactPlay.getTempFilePath(context));
                FileDescriptor fd = fis.getFD();
                long size = fis.getChannel().size();

                if (fd != null) {
                    mediaPlayer.setDataSource(fd, 0, size);
                    mediaPlayer.prepareAsync();
                } else {
                    L.e("ERROR WITH FILE DESCRIPTOR");
                }
            } catch (IOException e) {
                e.printStackTrace();
                currentItemPlay.imgError.setVisibility(View.VISIBLE);
                currentItemPlay.imgError.setImageResource(R.drawable.picto_loaderror);
            }
        } else { //loading
            // wait loading
            currentItemPlay.progressBar.setVisibility(View.VISIBLE);
            currentReactPlay.setLoadVideoEndListener(new VideoBean.LoadVideoEndListener() {
                @Override
                public void done(boolean ok, VideoBean react) {
                    if (currentItemPlay != null) {
                        currentItemPlay.progressBar.setVisibility(View.GONE);
                        isPreparePlaying = false;
                        playVideo(react, currentItemPlay.itemView);
                    }
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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

    private final int LOGNPRESS_TIME = 700;
    class LongpressRunnable implements Runnable {

        private Reaction react;
        private ReactViewHolder vh;
        private int position;
        private AnimatorSet animatorSet1;
        private AnimatorSet animatorSet2;
        private boolean wasPressedLong;

        public LongpressRunnable(Reaction react, ReactViewHolder vh, int position) {
            this.react = react;
            this.vh = vh;
            this.position = position;
            animatorSet1 = new AnimatorSet();
            animatorSet2 = new AnimatorSet();
        }

        @Override
        public void run() {
            wasPressedLong = true;
            animatorSet1.cancel();
            animatorSet2.cancel();
            animatorSet1 = new AnimatorSet();
            animatorSet2 = new AnimatorSet();

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(vh.imgLikeBig, "scaleX", 0.2f, 1f);
            bounceAnimX.setDuration(700);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(vh.imgLikeBig, "scaleY", 0.2f, 1f);
            bounceAnimY.setDuration(700);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
            bounceAnimY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!hasLikedReact(react)) {
                        like(true, react, vh, position);
                    }
                }

                public void onAnimationEnd(Animator animation) {
                    ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(vh.imgLikeBig, "scaleX", 1f, 0.1f);
                    bounceAnimX.setDuration(300);
                    bounceAnimX.setInterpolator(new AccelerateInterpolator());

                    ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(vh.imgLikeBig, "scaleY", 1f, 0.1f);
                    bounceAnimY.setDuration(300);
                    bounceAnimY.setInterpolator(new AccelerateInterpolator());
                    bounceAnimY.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vh.imgLikeBig.setVisibility(View.GONE);
                        }
                    });

                    animatorSet2.play(bounceAnimX).with(bounceAnimY);
                    animatorSet2.start();
                }
            });

            vh.imgLikeBig.setVisibility(View.VISIBLE);
            animatorSet1.play(bounceAnimX).with(bounceAnimY);
            animatorSet1.start();
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

    private void clearView(ReactViewHolder vh) {
        if (vh == currentItemPlay) {
            stopCurrentVideo();
        }
    }

    private boolean isPreparePlaying;
    private void playVideo(final VideoBean react, final View view) {
        if (isPreparePlaying) return;
        isPreparePlaying = true;

        final ReactViewHolder vh = (ReactViewHolder) view.getTag(R.id.vh);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopCurrentVideo();
        }

        if (vh.layoutVideo != null) {
            vh.imgPlay.setVisibility(View.GONE);
            vh.imgError.setVisibility(View.GONE);
            vh.layoutBack.setVisibility(View.GONE);

            if (surfaceView.getParent() != null) {
                ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            vh.layoutVideo.addView(surfaceView, params);

            if (listener != null) listener.onPlayVideo();

            currentReactPlay = react;
            currentItemPlay = vh;
        }
    }

    public void stopCurrentVideo() {
        stopCurrentFlip();

        if (currentItemPlay != null && currentReactPlay != null && mediaPlayer != null) {
            currentItemPlay.layoutBack.setVisibility(View.GONE);
            currentItemPlay.layoutVideo.setVisibility(View.GONE);
            currentItemPlay.layoutVideo.removeAllViews();
            currentItemPlay.imgReact.setVisibility(View.VISIBLE);
            currentItemPlay.imgPlay.setVisibility(View.VISIBLE);
            currentItemPlay.imgMute.setVisibility(View.GONE);
            currentItemPlay.imgError.setVisibility(View.GONE);
            currentItemPlay.progressBar.setVisibility(View.GONE);

            if (currentItemPlay.itemView != null) {
                currentItemPlay.itemView.invalidate();
            }

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentItemPlay = null;
            currentReactPlay = null;
        }
    }

    public void stopCurrentFlip() {
        if (isFlipped()) {
            mFlipAnimator.reverse();
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

    private boolean canDeleteReact(Reaction react) {
        return (react != null && react.iamOwner()) || mIsOwnerOfPiki;
    }

    private boolean hasLikedReact(Reaction react) {
        if (likeReactSet == null || likeReactSet.size() == 0 || react == null) return false;

        return likeReactSet.contains(react.getId());
    }

    private boolean isFlipped() {
        return mFlipAnimator.getAnimatedFraction() == 1;
    }

    private boolean isFlipping() {
        final float currentValue = mFlipAnimator.getAnimatedFraction();
        return (currentValue < 1 && currentValue > 0);
    }

    public Set<String> getLikeReactMap() {
        return likeReactSet;
    }

    public void setLikeReactMap(Set<String> likeReactSet) {
        this.likeReactSet = likeReactSet;
        notifyDataSetChanged();
    }

    public void updateProgressTempVideo(double totalProgress, double currentProgress) {
        if (listReact != null && listReact.size() > 0) {
            Reaction react = (Reaction) getItem(0);

            if (react.getUrlPhoto() == null && currentProgress > react.getCurrentProgress()) {
                react.setTotalProgress(totalProgress);
                react.setCurrentProgress(currentProgress);
                notifyDataSetChanged();
            }
        }
    }

    class CustomTarget implements Target {

        private ReactViewHolder vh;

        public CustomTarget(ReactViewHolder vh) {
            this.vh = vh;
        }

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            vh.imgReact.setImageBitmap(bitmap);
            vh.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            vh.imgReact.setImageDrawable(errorDrawable);
            vh.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            vh.imgReact.setImageDrawable(placeHolderDrawable);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CustomTarget) {
                return ((CustomTarget) o).vh.imgReact.equals(this.vh.imgReact);
            }

            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return vh.imgReact.hashCode();
        }
    }

    //INTERFACE
    public interface Listener {
        public void clickOnReaction(Reaction react);
        public void doubleTapReaction(Reaction react);
        public void showPlaceHolderItem(boolean show);
        public void onPlayVideo();
        public void onDeleteReact(int position);
        public void onReportReact(int position);
        public void onAddFriend(int position, Reaction react, ProgressBar pg, ImageView img, TextViewFontAutoResize txt);
        public void onPreviewReact(int position);
        public void onLikeReact(int position);
        public void onDislikeReact(int position);
    }

    class ReactViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.back)
        RelativeLayout layoutBack;
        @InjectView(R.id.front)
        RelativeLayout layoutFront;
        @InjectView(R.id.imgReact)
        ImageView imgReact;
        @InjectView(R.id.progressBar)
        CircleProgressBar progressBar;
        @InjectView(R.id.imgPlay)
        ImageView imgPlay;
        @InjectView(R.id.imgMute)
        ImageView imgMute;
        @InjectView(R.id.imgError)
        ImageView imgError;
        @InjectView(R.id.layoutVideo)
        LinearLayout layoutVideo;
        @InjectView(R.id.layoutAddFriend)
        RelativeLayout layoutAddFriend;
        @InjectView(R.id.progressBarAddFriend)
        ProgressBar friendsProgressBar;
        @InjectView(R.id.progressBarTempReact)
        CircularProgressBar progressBarTempReact;
        @InjectView(R.id.imgAddFriend)
        ImageView imgAddFriend;
        @InjectView(R.id.txtAddFriend)
        TextViewFontAutoResize txtAddFriend;
        @InjectView(R.id.layoutLike)
        RelativeLayout layoutLike;
        @InjectView(R.id.imgLike)
        ImageView imgLike;
        @InjectView(R.id.imgLikeBig)
        ImageView imgLikeBig;
        @InjectView(R.id.txtLike)
        TextViewFont txtLike;
        @InjectView(R.id.layoutReport)
        RelativeLayout layoutReport;
        @InjectView(R.id.imgReport)
        ImageView imgReport;
        @InjectView(R.id.txtReport)
        TextViewFont txtReport;
        @InjectView(R.id.layoutPreview)
        RelativeLayout layoutPreview;
        @InjectView(R.id.imgPreview)
        ImageView imgPreview;
        @InjectView(R.id.txtPreview)
        TextViewFont txtPreview;
        @InjectView(R.id.txtNBLikesFront)
        TextViewFont txtNBLikesFront;
        @InjectView(R.id.layoutNBLikesFront)
        LinearLayout layoutNBLikesFront;
        CustomTarget customTarget;

        public ReactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            customTarget = new CustomTarget(this);
        }
    }
}