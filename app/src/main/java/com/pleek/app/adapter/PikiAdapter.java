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
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.ReadDateProvider;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.utils.StringUtils;
import com.pleek.app.views.CircleProgressBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by nicolas on 19/12/14.
 */
public class PikiAdapter extends BaseAdapter implements View.OnTouchListener {

    private static final int PIKI_SIZE = 640;
    private static final int REACT_SIZE = 360;

    // CELL TYPES
    private static final int CELL_WITH_PIKI_WITH_ANSWERS = 0;
    private static final int CELL_WITH_PIKI_WITH_ONE_ANSWER = 1;
    private static final int CELL_WITH_PIKI_WITHOUT_ANSWERS = 2;
    private static final int CELL_WITHOUT_PIKI = 3;

    private List<Piki> listPiki;
    private Listener listener;
    private Context context;

    private int heightListView;
    private Screen screen;
    private ReadDateProvider readDataProvider;

    private int imgPikiWidth;
    private int imgPikiHeight;
    private int imgPikiSmallWidth;
    private int imgPikiSmallHeight;

    public PikiAdapter(List<Piki> listPiki, Listener listener) {
        this(listPiki, listener, listener instanceof Context ? (Context) listener : null);
    }

    public PikiAdapter(List<Piki> listPiki, Listener listener, Context context) {
        this.listPiki = listPiki;
        this.listener = listener;
        this.context = context;

        screen = Screen.getInstance(context);
        readDataProvider = ReadDateProvider.getInstance(context);

        imgPikiWidth = context.getResources().getDimensionPixelSize(R.dimen.img_piki_width);
        imgPikiHeight = context.getResources().getDimensionPixelSize(R.dimen.img_piki_height);
        imgPikiSmallWidth = context.getResources().getDimensionPixelSize(R.dimen.img_piki_small_width);
        imgPikiSmallHeight = context.getResources().getDimensionPixelSize(R.dimen.img_piki_small_height);
    }

    @Override
    public int getItemViewType(int position) {
        Piki piki = position < listPiki.size() ? listPiki.get(position) : null;

        //if (piki == null) {
        //    return CELL_WITHOUT_PIKI;
        //}

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
        return 4;
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

        switch (viewType) {
            case CELL_WITH_PIKI_WITHOUT_ANSWERS :
                Piki piki = i < listPiki.size() ? listPiki.get(i) : null;

                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_no_answers, parent, false);
                    PikiViewHolder vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    ViewGroup.LayoutParams lp = vh.imgPiki.getLayoutParams();
                    int size = screen.dpToPx(screen.getWidthDp());
                    lp.width = size;
                    lp.height = size;
                    vh.imgPiki.setLayoutParams(lp);
                }

                if (piki != null) { // If is not placeholder
                    PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);

                    vh.progressBarPiki.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null) Picasso.with(context).cancelRequest(vh.customTargetPiki);

                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(640, 640).into(vh.customTargetPiki);
                }

                break;

            case CELL_WITH_PIKI_WITH_ONE_ANSWER :
                piki = i < listPiki.size() ? listPiki.get(i) : null;

                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_one_answer, parent, false);
                    PikiViewHolder vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    setLayoutParams(vh.layoutPiki, (float) 2/3, false);
                    //setLayoutParams(vh.imgPiki, (float) 2/3, false);
                    //setLayoutParams(vh.imgReact, (float) 1/3, true);
                    setLayoutParams(vh.layoutReact, (float) 1 / 3, true);
                }

                if (piki != null) { // If is not placeholder
                    PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);

                    vh.progressBarPiki.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null) Picasso.with(context).cancelRequest(vh.customTargetPiki);

                    int resize = vh.layoutPiki.getLayoutParams().width > PIKI_SIZE ? PIKI_SIZE : vh.layoutPiki.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(resize, resize).centerCrop().into(vh.customTargetPiki);

                    int resizeReact = vh.layoutReact.getLayoutParams().width > REACT_SIZE ? REACT_SIZE : vh.layoutReact.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(resizeReact, resizeReact).centerCrop().into(vh.customTargetReact1);
                }

                break;

            case CELL_WITH_PIKI_WITH_ANSWERS :
                piki = i < listPiki.size() ? listPiki.get(i) : null;

                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki_with_answers, parent, false);
                    PikiViewHolder vh = new PikiViewHolder(view, viewType);
                    view.setTag(R.id.vh, vh);

                    setLayoutParams(vh.layoutPiki, (float) 2/3, false);
                    //setLayoutParams(vh.imgPiki, (float) 2/3, false);
                    //setLayoutParams(vh.imgReact, (float) 1/3, true);
                    setLayoutParams(vh.layoutReact, (float) 1/3, true);
                }

                if (piki != null) { // If is not placeholder
                    PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);

                    vh.progressBarPiki.setVisibility(View.VISIBLE);

                    if (vh.customTargetPiki != null) Picasso.with(context).cancelRequest(vh.customTargetPiki);

                    int resize = vh.layoutPiki.getLayoutParams().width > PIKI_SIZE ? PIKI_SIZE : vh.layoutPiki.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize(resize, resize).centerCrop().into(vh.customTargetPiki);

                    int resizeReact = vh.layoutReact.getLayoutParams().width > REACT_SIZE ? REACT_SIZE : vh.layoutReact.getLayoutParams().width;
                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(resizeReact, resizeReact).into(vh.customTargetReact1);
                    PicassoUtils.with(context).load(piki.getUrlReact2()).resize(resizeReact, resizeReact).into(vh.customTargetReact2);
                }

                break;
//
//                    // Image Piki
//                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize((int) (imgPikiWidth * 0.80), (int) (imgPikiHeight * 0.80)).into(vh.imgPiki);
//                    ViewGroup.LayoutParams lp = view.getLayoutParams();
//                    lp.width = screen.dpToPx(screen.getWidthDp() + 10); // add 10 dp width to item (for horizontal scroll item)
//                    view.setLayoutParams(lp);
//
//                    PikiViewHolder vh = new PikiViewHolder(view);
//                    view.setTag(R.id.vh, vh);
//                }
//
//                if (piki != null) { // If is not placeholder
//                    PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
//                    vh.txtUserName.setText(!StringUtils.isStringEmpty(piki.getFirstName()) ? piki.getFirstName() : piki.getName());
//                    vh.txtDeleteOn.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);
//                    vh.txtDeleteOff.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);
//
//                    vh.imgPlay.setVisibility(piki.isVideo() ? View.VISIBLE : View.GONE);
//
//                    // Image Piki
//                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize((int) (imgPikiWidth * 0.80), (int) (imgPikiHeight * 0.80)).into(vh.imgPiki);
//                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki1);
//                    vh.imgPiki1.setVisibility(piki.thereIsReact1() ? View.VISIBLE : View.GONE);
//                    PicassoUtils.with(context).load(piki.getUrlReact2()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki2);
//                    vh.imgPiki2.setVisibility(piki.thereIsReact2() ? View.VISIBLE : View.GONE);
//                    PicassoUtils.with(context).load(piki.getUrlReact3()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki3);
//                    vh.imgPiki3.setVisibility(piki.thereIsReact3() ? View.VISIBLE : View.GONE);
//
//
//                    // Label Piki
//                    Date readDate = readDataProvider.getReadDate(piki.getId());
//                    Date updateDate = piki.getUpdatedAt();
//                    boolean isNew = readDate == null;
//                    boolean isUpdated = !isNew && readDate.before(updateDate);
//                    boolean isMoreReact = piki.getNbReact() > NB_THUMBNAIL_REACT;
//
//                    // txtFirst
//                    boolean me = piki.getName().equals(ParseUser.getCurrentUser().getUsername());//TODO : crash #7 > NullPointerException
//                    vh.txtFirst.setVisibility(piki.getNbReact() == 0 && !me ? View.VISIBLE : View.GONE);
//
//                    // txtUnread
//                    if (isNew) {
//                        vh.txtUnread.setText(context.getResources().getString(R.string.home_new));
//                        vh.txtUnread.setBackgroundResource(R.drawable.second_color_label_back);
//                        vh.txtUnread.setVisibility(View.VISIBLE);
//                    } else {
//                        vh.txtUnread.setText("+ " + Utile.formatBigNumber(piki.getNbReact() - NB_THUMBNAIL_REACT));
//                        vh.txtUnread.setBackgroundResource(isUpdated ? R.drawable.first_color_label_back : R.drawable.gris_label_back);
//                        vh.txtUnread.setVisibility(isMoreReact && isUpdated ? View.VISIBLE : View.GONE);
//                    }
//                    vh.txtUnread.setPadding(screen.dpToPx(7), 0, screen.dpToPx(7), 0);//fix bug : no padding after setBackgroundResource
//
//                    // TxtRead
//                    vh.txtRead.setText("+"+Utile.formatBigNumber(piki.getNbReact()-NB_THUMBNAIL_REACT));
//                    vh.txtRead.setVisibility(isMoreReact ? View.VISIBLE : View.GONE);
//
//
//                    view.setTag(new Integer(i));
//                    view.setTag(R.string.tag_downpresse, new DownRunnable(view));
//                    view.setOnTouchListener(this);
//                }

                //break;

            //case CELL_WITHOUT_PIKI :
            //    view = LayoutInflater.from(context).inflate(R.layout.item_piki_placeholder, parent, false);
            //    ViewGroup.LayoutParams lp = view.getLayoutParams();
            //    lp.width = screen.dpToPx(screen.getWidthDp() + 10); // add 10 dp width to item (for horizontal scroll item)
            //    view.setLayoutParams(lp);

            //    break;
        }

        return view;
    }

    private static int FADE_TIME = 150;//ms

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
//        int action = event.getAction();
//
//        if(view.getTag() instanceof Integer)
//        {
//            int position = (Integer)view.getTag();
//
//            DownRunnable down = (DownRunnable)view.getTag(R.string.tag_downpresse);
//            if(action == MotionEvent.ACTION_DOWN)
//            {
//                //highlight with 90ms delay. Because I dont want highlight if user scroll
//                handler.postDelayed(down, DOWN_TIME);
//            }
//            else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
//            {
//                //finger move, cancel highlight
//                handler.removeCallbacks(down);
//
//                itemMarkDown(view, false);
//            }
//            if(action == MotionEvent.ACTION_UP)
//            {
//                //highlight just after clicked
//                itemMarkDown(view, true);
//
//                handler.postDelayed(down, DOWN_TIME);
//
//                //send to listener
//                if(listener != null)
//                {
//                    Piki piki = listPiki.get(position);
//                    final TextView txtUnread = (TextView) view.findViewById(R.id.txtUnread);
//                    final TextView txtRead = (TextView) view.findViewById(R.id.txtRead);
//                    if(txtUnread.getVisibility() == View.VISIBLE)
//                    {
//                        Utile.fadeOut(txtUnread, FADE_TIME);
//                        if(piki.getNbReact() > NB_THUMBNAIL_REACT) Utile.fadeIn(txtRead, FADE_TIME);
//                    }
//
//                    listener.clickOnPiki(piki);
//                }
//            }
//        }
        return true;
    }

    public Piki removePiki(int position)
    {
        Piki removed = listPiki.remove(position);
        notifyDataSetChanged();
        return removed;
    }

    //DOWN EFFECT
    private static int DOWN_TIME = 90;//ms
    final Handler handler = new Handler();
    class DownRunnable implements Runnable
    {
        private View view;

        public DownRunnable(View view) {
            this.view = view;
        }

        @Override
        public void run()
        {
            View layoutOverlay = view.findViewById(R.id.layoutOverlay);
            boolean isVisible = layoutOverlay != null && layoutOverlay.getVisibility() == View.VISIBLE;
            itemMarkDown(view, !isVisible);
        }
    }
    private void itemMarkDown(View item, boolean down)
    {
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

    private void setLayoutParams(View v, float proportion, boolean fullHeight) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        int size = screen.dpToPx((int) (screen.getWidthDp() * proportion));
        lp.width = size;

        if (fullHeight) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            lp.height = size;
        }
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

        CustomTargetPiki customTargetPiki;
        CustomTargetPiki customTargetReact1;
        CustomTargetPiki customTargetReact2;

        public PikiViewHolder(View itemView, int type) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            if (type == CELL_WITH_PIKI_WITHOUT_ANSWERS) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);
            } else if (type == CELL_WITH_PIKI_WITH_ONE_ANSWER) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);
                customTargetReact1 = new CustomTargetPiki(progressBarReact, imgReact);
            } else if (type == CELL_WITH_PIKI_WITH_ANSWERS) {
                customTargetPiki = new CustomTargetPiki(progressBarPiki, imgPiki);
                customTargetReact1 = new CustomTargetPiki(progressBarReact, imgReact);
                customTargetReact2 = new CustomTargetPiki(progressBarReact1, imgReact1);
            }
        }
    }
}