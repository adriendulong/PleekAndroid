package com.pleek.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.TextViewFont;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.ReadDateProvider;
import com.pleek.app.transformations.RoundedTransformation;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.utils.StringUtils;
import com.pleek.app.views.CircleProgressBar;
import com.pleek.app.views.CircularProgressBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by nicolas on 19/12/14.
 */
public class BestPikiAdapter extends BaseAdapter implements View.OnTouchListener {

    private static final int NB_MAX_REACTS = 2;
    private static final int PIKI_SIZE = 640;
    private static final int REACT_SIZE = 360;

    // CELL TYPES
    private static final int CELL_BEST = 0;

    private List<Piki> listPiki;
    private Listener listener;
    private Context context;

    private int heightListView;
    private Screen screen;

    private int sizePiki;
    private int sizeReact;

    public BestPikiAdapter(List<Piki> listPiki, Listener listener) {
        this(listPiki, listener, listener instanceof Context ? (Context) listener : null);
    }

    public BestPikiAdapter(List<Piki> listPiki, Listener listener, Context context) {
        this.listPiki = listPiki;
        this.listener = listener;
        this.context = context;

        screen = Screen.getInstance(context);
    }

    @Override
    public int getItemViewType(int position) {
        return CELL_BEST;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        if (listPiki == null) return 0;

        if (listPiki.size() % 2 == 0) {
            return listPiki.size() / 2;
        } else {
            return (listPiki.size() / 2) + 1;
        }
    }

    @Override
    public Object getItem(int i) {
        return getCount() > 0 ? listPiki.get(i % getCount()) : null;
    }

    @Override
    public long getItemId(int i) {
        return getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        int viewType = getItemViewType(i);

        if (view == null) {
            switch (viewType) {
                case CELL_BEST:
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_best, parent, false);
                    PikiViewHolder vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    sizePiki = screen.getWidth() / 2 - (int) (15 * screen.getDensity());
                    sizeReact = sizePiki / 3;
                    setLayoutParams(vh.layoutPiki1, ViewGroup.LayoutParams.MATCH_PARENT, sizePiki + sizeReact);
                    setLayoutParams(vh.layoutPiki2, ViewGroup.LayoutParams.MATCH_PARENT, sizePiki + sizeReact);

                    break;
            }
        }

        PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
        Piki piki = listPiki.get(i * 2);
        Piki piki2 = null;

        if (i * 2 + 1 < listPiki.size()) piki2 = listPiki.get(i * 2 + 1);

        if (piki != null) { // If is not placeholder
            switch (viewType) {
                case CELL_BEST:
                    vh.progressBarPiki1.setVisibility(View.VISIBLE);
                    vh.progressBarReact11.setVisibility(View.VISIBLE);
                    vh.progressBarReact12.setVisibility(View.VISIBLE);
                    vh.progressBarReact13.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki1 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetPiki1);
                    if (vh.customTargetReact11 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact11);
                    if (vh.customTargetReact12 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact12);
                    if (vh.customTargetReact13 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact13);

                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(sizePiki, sizePiki).centerCrop().into(vh.customTargetPiki1);

                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact11);
                    PicassoUtils.with(context).load(piki.getUrlReact2()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact12);
                    PicassoUtils.with(context).load(piki.getUrlReact3()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact13);

                    if (piki.isVideo()) {
                        vh.imgPlay.setVisibility(View.VISIBLE);
                    } else {
                        vh.imgPlay.setVisibility(View.GONE);
                    }

                    vh.layoutPiki1.setOnTouchListener(this);
                    view.setTag(new Integer(i * 2));
                    vh.layoutPiki1.setTag(new Integer(i * 2));

                    break;
            }
        }

        if (piki2 != null) { // If is not placeholder
            vh.layoutPiki2.setVisibility(View.VISIBLE);
            switch (viewType) {
                case CELL_BEST:
                    vh.progressBarPiki2.setVisibility(View.VISIBLE);
                    vh.progressBarReact21.setVisibility(View.VISIBLE);
                    vh.progressBarReact22.setVisibility(View.VISIBLE);
                    vh.progressBarReact23.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki2 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetPiki2);
                    if (vh.customTargetReact21 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact21);
                    if (vh.customTargetReact22 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact22);
                    if (vh.customTargetReact23 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact23);

                    PicassoUtils.with(context).load(piki2.getUrlPiki()).resize(sizePiki, sizePiki).centerCrop().into(vh.customTargetPiki2);

                    PicassoUtils.with(context).load(piki2.getUrlReact1()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact21);
                    PicassoUtils.with(context).load(piki2.getUrlReact2()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact22);
                    PicassoUtils.with(context).load(piki2.getUrlReact3()).resize(sizeReact, sizeReact).centerCrop().into(vh.customTargetReact23);

                    if (piki2.isVideo()) {
                        vh.imgPlay2.setVisibility(View.VISIBLE);
                    } else {
                        vh.imgPlay2.setVisibility(View.GONE);
                    }

                    vh.layoutPiki2.setOnTouchListener(this);
                    view.setTag(new Integer(i * 2 + 1));
                    vh.layoutPiki2.setTag(new Integer(i * 2 + 1));

                    break;
            }
        } else {
            vh.layoutPiki2.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        if (view.getTag() instanceof Integer) {
            int position = (Integer) view.getTag();

            DownRunnable down = (DownRunnable) view.getTag(R.string.tag_downpresse);
            if (action == MotionEvent.ACTION_DOWN) {
                //highlight with 90ms delay. Because I dont want highlight if user scroll
                handler.postDelayed(down, DOWN_TIME);
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                //finger move, cancel highlight
                handler.removeCallbacks(down);

                //itemMarkDown(view, false);
            }

            if (action == MotionEvent.ACTION_UP) {
                //highlight just after clicked
                //itemMarkDown(view, true);

                handler.postDelayed(down, DOWN_TIME);

                //send to listener
                if (listener != null) {
                    Piki piki = listPiki.get(position);
                    listener.clickOnPiki(piki);
                }
            }
        }
        return true;
    }

    //DOWN EFFECT
    private static int DOWN_TIME = 90;//ms
    final Handler handler = new Handler();
    class DownRunnable implements Runnable {
        private View view;

        public DownRunnable(View view) {
            this.view = view;
        }

        @Override
        public void run() {
            View layoutOverlay = view.findViewById(R.id.layoutOverlay);
            boolean isVisible = layoutOverlay != null && layoutOverlay.getVisibility() == View.VISIBLE;
            itemMarkDown(view, !isVisible);
        }
    }

    private void itemMarkDown(View item, boolean down) {
        View layoutOverlay = item.findViewById(R.id.layoutOverlay);
        if (layoutOverlay != null) {
            int visibility = down ? View.VISIBLE : View.GONE;
            layoutOverlay.setVisibility(visibility);
        }
    }


    public void setHeightListView(int heightListView) {
        this.heightListView = heightListView;
    }

    public void setListPiki(List<Piki> listPiki) {
        this.listPiki = listPiki;
        notifyDataSetChanged();
    }

    private void setLayoutParams(View v, int width, int height) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.width = width;
        lp.height = height;
        v.setLayoutParams(lp);
    }

    // INTERFACE
    public interface Listener {
        public void clickOnPiki(Piki piki);
        public void showPlaceHolderItem(boolean show);
    }

    class CustomTargetPiki implements Target {

        private CircleProgressBar pgBar;
        private ImageView img;

        public CustomTargetPiki(CircleProgressBar pgBar, ImageView img) {
            this.pgBar = pgBar;
            this.img = img;
        }

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            img.setImageBitmap(bitmap);
            pgBar.setVisibility(View.GONE);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            img.setImageDrawable(errorDrawable);
            pgBar.setVisibility(View.GONE);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            img.setImageDrawable(placeHolderDrawable);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CustomTargetPiki) {
                return ((CustomTargetPiki) o).img.equals(this.img);
            }

            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return img.hashCode();
        }
    }

    class PikiViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.layoutPiki1)
        ViewGroup layoutPiki1;
        @InjectView(R.id.layoutPiki2)
        ViewGroup layoutPiki2;

        @InjectView(R.id.layoutPikiFrame1)
        ViewGroup layoutPikiFrame1;
        @InjectView(R.id.layoutPikiFrame2)
        ViewGroup layoutPikiFrame2;

        @InjectView(R.id.layoutReact1)
        LinearLayout layoutReact1;
        @InjectView(R.id.layoutReact2)
        LinearLayout layoutReact2;

        @InjectView(R.id.imgPiki1)
        ImageView imgPiki1;
        @InjectView(R.id.imgPiki2)
        ImageView imgPiki2;

        @InjectView(R.id.imgPlay)
        ImageView imgPlay;
        @InjectView(R.id.imgPlay2)
        ImageView imgPlay2;

        @InjectView(R.id.progressBarPiki1)
        CircleProgressBar progressBarPiki1;
        @InjectView(R.id.progressBarPiki2)
        CircleProgressBar progressBarPiki2;

        @Optional
        @InjectView(R.id.progressBarReact11)
        CircleProgressBar progressBarReact11;
        @Optional
        @InjectView(R.id.progressBarReact12)
        CircleProgressBar progressBarReact12;
        @Optional
        @InjectView(R.id.progressBarReact13)
        CircleProgressBar progressBarReact13;
        @Optional
        @InjectView(R.id.progressBarReact21)
        CircleProgressBar progressBarReact21;
        @Optional
        @InjectView(R.id.progressBarReact22)
        CircleProgressBar progressBarReact22;
        @Optional
        @InjectView(R.id.progressBarReact23)
        CircleProgressBar progressBarReact23;

        @Optional
        @InjectView(R.id.imgReact11)
        ImageView imgBestReact11;
        @Optional
        @InjectView(R.id.imgReact12)
        ImageView imgBestReact12;
        @Optional
        @InjectView(R.id.imgReact13)
        ImageView imgBestReact13;
        @Optional
        @InjectView(R.id.imgReact21)
        ImageView imgBestReact21;
        @Optional
        @InjectView(R.id.imgReact22)
        ImageView imgBestReact22;
        @Optional
        @InjectView(R.id.imgReact23)
        ImageView imgBestReact23;

        CustomTargetPiki customTargetPiki1;
        CustomTargetPiki customTargetReact11;
        CustomTargetPiki customTargetReact12;
        CustomTargetPiki customTargetReact13;
        CustomTargetPiki customTargetPiki2;
        CustomTargetPiki customTargetReact21;
        CustomTargetPiki customTargetReact22;
        CustomTargetPiki customTargetReact23;

        public PikiViewHolder(View itemView, int type) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            customTargetPiki1 = new CustomTargetPiki(progressBarPiki1, imgPiki1);
            customTargetPiki2 = new CustomTargetPiki(progressBarPiki2, imgPiki2);
            customTargetReact11 = new CustomTargetPiki(progressBarReact11, imgBestReact11);
            customTargetReact12 = new CustomTargetPiki(progressBarReact12, imgBestReact12);
            customTargetReact13 = new CustomTargetPiki(progressBarReact13, imgBestReact13);
            customTargetReact21 = new CustomTargetPiki(progressBarReact21, imgBestReact21);
            customTargetReact22 = new CustomTargetPiki(progressBarReact22, imgBestReact22);
            customTargetReact23 = new CustomTargetPiki(progressBarReact23, imgBestReact23);

            progressBarPiki1.setColorSchemeResources(R.color.progressBar);
            progressBarPiki2.setColorSchemeResources(R.color.progressBar);
            progressBarReact11.setColorSchemeResources(R.color.progressBar);
            progressBarReact12.setColorSchemeResources(R.color.progressBar);
            progressBarReact13.setColorSchemeResources(R.color.progressBar);
            progressBarReact21.setColorSchemeResources(R.color.progressBar);
            progressBarReact22.setColorSchemeResources(R.color.progressBar);
            progressBarReact23.setColorSchemeResources(R.color.progressBar);
        }
    }
}