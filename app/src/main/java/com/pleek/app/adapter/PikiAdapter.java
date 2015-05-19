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
public class PikiAdapter extends BaseAdapter implements View.OnTouchListener {

    private static final int NB_MAX_REACTS = 2;
    private static final int PIKI_SIZE = 640;
    private static final int REACT_SIZE = 360;

    // CELL TYPES
    private static final int CELL_WITH_PIKI_WITH_ANSWERS = 0;
    private static final int CELL_WITH_PIKI_WITH_ONE_ANSWER = 1;
    private static final int CELL_WITH_PIKI_WITHOUT_ANSWERS = 2;
    private static final int CELL_WITHOUT_PIKI = 3;
    private static final int CELL_BEST = 4;

    private List<Piki> listPiki;
    private Listener listener;
    private Context context;

    private int heightListView;
    private Screen screen;
    private ReadDateProvider readDataProvider;

    public PikiAdapter(List<Piki> listPiki, Listener listener) {
        this(listPiki, listener, listener instanceof Context ? (Context) listener : null);
    }

    public PikiAdapter(List<Piki> listPiki, Listener listener, Context context) {
        this.listPiki = listPiki;
        this.listener = listener;
        this.context = context;

        screen = Screen.getInstance(context);
        readDataProvider = ReadDateProvider.getInstance(context);
    }

    @Override
    public int getItemViewType(int position) {
        Piki piki = position < listPiki.size() ? listPiki.get(position) : null;

        //if (piki == null) {
        //    return CELL_WITHOUT_PIKI;
        //}

        if (piki.isBest()) {
            return CELL_BEST;
        }

        if (piki.getNbReact() == 0) {
            return CELL_WITH_PIKI_WITHOUT_ANSWERS;
        }

        if (piki.getNbReact() == 1) {
            return CELL_WITH_PIKI_WITH_ONE_ANSWER;
        }

        if (piki.getNbReact() >= 2) {
            return CELL_WITH_PIKI_WITH_ANSWERS;
        }

        return CELL_WITH_PIKI_WITHOUT_ANSWERS;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getCount() {
        if (listPiki == null) return 0;

        return listPiki.size();
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
                case CELL_WITH_PIKI_WITHOUT_ANSWERS:
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_no_answers, parent, false);
                    PikiViewHolder vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    ViewGroup.LayoutParams lp = vh.imgPiki.getLayoutParams();
                    int size = screen.dpToPx(screen.getWidthDp());
                    lp.width = size;
                    lp.height = size;
                    vh.imgPiki.setLayoutParams(lp);

                    setLayoutParams(vh.layoutFront, ViewGroup.LayoutParams.MATCH_PARENT, size + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height));
                    setLayoutParams(vh.layoutBackBg, ViewGroup.LayoutParams.MATCH_PARENT, size + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height) - ((int) (2 * screen.getDensity())));

                    break;

                case CELL_WITH_PIKI_WITH_ONE_ANSWER:
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_one_answer, parent, false);
                    vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    int height = (int) (screen.getWidth() * (float) 2 / 3);

                    setLayoutParams(vh.layoutPiki, (int) height, height);
                    setLayoutParams(vh.viewBg, screen.getWidth(), height - (int) (3 * screen.getDensity()));
                    setLayoutParams(vh.layoutReact, (int) (screen.getWidth() * (float) 1 / 3), height);
                    setLayoutParams(vh.layoutFront, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height));
                    setLayoutParams(vh.layoutBackBg, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height) - ((int) (4 * screen.getDensity())));

                    break;

                case CELL_WITH_PIKI_WITH_ANSWERS:
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_with_answers, parent, false);
                    vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    height = (int) (screen.getWidth() * (float) 2/3);
                    setLayoutParams(vh.layoutPiki, height, height);
                    setLayoutParams(vh.viewBg, screen.getWidth(), height - (int) (3 * screen.getDensity()));
                    setLayoutParams(vh.layoutReact, (int) (screen.getWidth() * (float) 1/3), height);
                    setLayoutParams(vh.layoutFront, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height));
                    setLayoutParams(vh.layoutBackBg, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height) - ((int) (4 * screen.getDensity())));

                    break;

                case CELL_BEST:
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_best, parent, false);
                    vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    height = (int) (screen.getWidth() * (float) 2/3);
                    setLayoutParams(vh.layoutPiki, height, height);
                    setLayoutParams(vh.viewBg, screen.getWidth(), height - (int) (3 * screen.getDensity()));
                    setLayoutParams(vh.layoutReact, (int) (screen.getWidth() * (float) 1/3), height);
                    setLayoutParams(vh.layoutFront, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height));
                    setLayoutParams(vh.layoutBackBg, ViewGroup.LayoutParams.MATCH_PARENT, height + context.getResources().getDimensionPixelSize(R.dimen.piki_infos_height) - ((int) (4 * screen.getDensity())));

                    break;
            }
        }

        PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
        Piki piki = i < listPiki.size() ? listPiki.get(i) : null;

        if (piki != null) { // If is not placeholder
            switch (viewType) {
                case CELL_WITH_PIKI_WITHOUT_ANSWERS:
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);
                    vh.progressBarPiki.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null)
                        Picasso.with(context).cancelRequest(vh.customTargetPiki);

                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(PIKI_SIZE, PIKI_SIZE).into(vh.customTargetPiki);
                    break;

                case CELL_WITH_PIKI_WITH_ONE_ANSWER:
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);

                    vh.progressBarPiki.setVisibility(View.VISIBLE);
                    vh.progressBarReact.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null)
                        Picasso.with(context).cancelRequest(vh.customTargetPiki);
                    if (vh.customTargetReact1 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact1);

                    int resize = vh.layoutPiki.getLayoutParams().width > PIKI_SIZE ? PIKI_SIZE : vh.layoutPiki.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(resize, resize).centerCrop().into(vh.customTargetPiki);

                    int resizeReact = vh.layoutReact.getLayoutParams().width > REACT_SIZE ? REACT_SIZE : vh.layoutReact.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(resizeReact, resizeReact).centerCrop().into(vh.customTargetReact1);

                    break;

                case CELL_WITH_PIKI_WITH_ANSWERS:
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);

                    vh.progressBarPiki.setVisibility(View.VISIBLE);
                    vh.progressBarReact.setVisibility(View.VISIBLE);
                    vh.progressBarReact1.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null)
                        Picasso.with(context).cancelRequest(vh.customTargetPiki);
                    if (vh.customTargetReact1 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact1);
                    if (vh.customTargetReact2 != null)
                        Picasso.with(context).cancelRequest(vh.customTargetReact2);

                    resize = vh.layoutPiki.getLayoutParams().width > PIKI_SIZE ? PIKI_SIZE : vh.layoutPiki.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(resize, resize).centerCrop().into(vh.customTargetPiki);

                    resizeReact = vh.layoutReact.getLayoutParams().width > REACT_SIZE ? REACT_SIZE : vh.layoutReact.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(resizeReact, resizeReact).into(vh.customTargetReact1);
                    PicassoUtils.with(context).load(piki.getUrlReact2()).resize(resizeReact, resizeReact).into(vh.customTargetReact2);

                    break;
            }

            // Label Piki
            Date readDate = readDataProvider.getReadDate(piki.getId());
            Date updateDate = piki.getUpdatedAt();
            boolean isNew = readDate == null;
            boolean isUpdated = !isNew && readDate.before(updateDate);
            boolean isMoreReact = piki.getNbReact() > NB_MAX_REACTS;

            // txtFirst
            boolean me = piki.getName().equals(ParseUser.getCurrentUser().getUsername());//TODO : crash #7 > NullPointerException
            if (isNew) {
                if (piki.getNbReact() == 0) {
                    if (piki.isVideo()) {
                        vh.txtType.setText(context.getResources().getString(R.string.home_new_video));
                        vh.imgType.setImageResource(R.drawable.picto_new_video);
                        vh.imgType.setVisibility(View.VISIBLE);
                    } else {
                        vh.txtType.setText(context.getResources().getString(R.string.home_new_picture));
                        vh.imgType.setImageResource(R.drawable.picto_new_picture);
                        vh.imgType.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (isUpdated) {
                    vh.txtType.setText(context.getResources().getString(R.string.home_new_replies));
                    vh.imgType.setImageResource(R.drawable.picto_new_replies);
                    vh.imgType.setVisibility(View.VISIBLE);
                } else if (piki.getNbReact() == 0 && !me) {
                    vh.txtType.setText(R.string.piki_reply_first);
                    vh.imgType.setVisibility(View.GONE);
                } else {
                    vh.txtType.setText(Utile.formatBigNumber(piki.getNbReact()) + " " + (piki.getNbReact() > 1 ? context.getString(R.string.piki_replies) : context.getString(R.string.piki_reply)));
                    vh.imgType.setVisibility(View.GONE);
                }
            }
        }

            //case CELL_WITHOUT_PIKI :
            //    view = LayoutInflater.from(context).inflate(R.layout.item_piki_placeholder, parent, false);
            //    ViewGroup.LayoutParams lp = view.getLayoutParams();
            //    lp.width = screen.dpToPx(screen.getWidthDp() + 10); // add 10 dp width to item (for horizontal scroll item)
            //    view.setLayoutParams(lp);

            //    break;

        vh.layoutFront.setTag(new Integer(i));
        view.setTag(new Integer(i));

        return view;
    }

    private static int FADE_TIME = 150;//ms

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();

        if (view.getTag() instanceof Integer) {
            int position = (Integer)view.getTag();

            DownRunnable down = (DownRunnable)view.getTag(R.string.tag_downpresse);
            if (action == MotionEvent.ACTION_DOWN) {
                //highlight with 90ms delay. Because I dont want highlight if user scroll
                handler.postDelayed(down, DOWN_TIME);
            } else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
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
                    //final TextView txtType = (TextView) view.findViewById(R.id.txtType);
                    //final ImageView imgType = (ImageView) view.findViewById(R.id.imgType);



                    listener.clickOnPiki(piki);
                }
            }
        }
        return true;
    }

    public void refactorTitles(Piki piki, TextView txtType, ImageView imgType) {

    }

    public Piki removePiki(int position) {
        Piki removed = listPiki.remove(position);
        notifyDataSetChanged();
        return removed;
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
        if(layoutOverlay != null)
        {
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
        @InjectView(R.id.back)
        RelativeLayout layoutBack;
        @InjectView(R.id.backBg)
        RelativeLayout layoutBackBg;
        @InjectView(R.id.bgDeleteItemOff)
        View bgDeleteItemOff;
        @InjectView(R.id.bgDeleteItemOn)
        View bgDeleteItemOn;
        @InjectView(R.id.imgDeleteItemOff)
        LinearLayout imgDeleteItemOff;
        @InjectView(R.id.imgDeleteItemOn)
        LinearLayout imgDeleteItemOn;
        @InjectView(R.id.front)
        FrameLayout layoutFront;
        @InjectView(R.id.imgPiki)
        ImageView imgPiki;
        @InjectView(R.id.imgPlay)
        ImageView imgPlay;
        @InjectView(R.id.txtUserName)
        TextViewFont txtUserName;
        @InjectView(R.id.progressBarPiki)
        CircleProgressBar progressBarPiki;
        @Optional
        @InjectView(R.id.progressBarReact)
        CircleProgressBar progressBarReact;
        @Optional
        @InjectView(R.id.imgReact)
        ImageView imgReact;
        @Optional
        @InjectView(R.id.layoutPiki)
        ViewGroup layoutPiki;
        @Optional
        @InjectView(R.id.layoutReact)
        ViewGroup layoutReact;
        @Optional
        @InjectView(R.id.progressBarReact1)
        CircleProgressBar progressBarReact1;
        @Optional
        @InjectView(R.id.imgReact1)
        ImageView imgReact1;
        @Optional
        @InjectView(R.id.viewBg)
        View viewBg;
        @InjectView(R.id.txtType)
        TextViewFont txtType;
        @InjectView(R.id.imgType)
        ImageView imgType;

        CustomTargetPiki customTargetPiki;
        CustomTargetPiki customTargetReact1;
        CustomTargetPiki customTargetReact2;

        public PikiViewHolder(View itemView, int type) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            if (type == CELL_WITH_PIKI_WITHOUT_ANSWERS) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);

                progressBarPiki.setColorSchemeResources(R.color.progressBar);
            } else if (type == CELL_WITH_PIKI_WITH_ONE_ANSWER) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);
                customTargetReact1 = new CustomTargetPiki(progressBarReact, imgReact);

                progressBarReact.setColorSchemeResources(R.color.progressBar);
            } else if (type == CELL_WITH_PIKI_WITH_ANSWERS) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);
                customTargetReact1 = new CustomTargetPiki(progressBarReact, imgReact);
                customTargetReact2 = new CustomTargetPiki(progressBarReact1, imgReact1);

                progressBarReact1.setColorSchemeResources(R.color.progressBar);
                progressBarReact.setColorSchemeResources(R.color.progressBar);
            }

            layoutFront.setOnTouchListener(PikiAdapter.this);
        }
    }
}