package com.pleek.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nicolas on 19/12/14.
 */
public class PikiAdapter extends BaseAdapter implements View.OnTouchListener {
    public static final int HEIGHT_ITEM = 111;//dp
    private final int NB_THUMBNAIL_REACT = 3;

    // CELL TYPES
    private static final int CELL_WITH_PIKI = 0;
    private static final int CELL_WITHOUT_PIKI = 1;

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
        return (piki == null ? CELL_WITHOUT_PIKI : CELL_WITH_PIKI);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        int nb = listPiki != null ? listPiki.size() : 0;
        int minItem = (int) (((float)heightListView / screen.dpToPx(HEIGHT_ITEM)) + 0.5f);
        if(listener != null) listener.showPlaceHolderItem(minItem > nb);
        return Math.max(nb, minItem);
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
            case CELL_WITH_PIKI :
                Piki piki = i < listPiki.size() ? listPiki.get(i) : null;

                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_piki, parent, false);
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.width = screen.dpToPx(screen.getWidthDp() + 10); // add 10 dp width to item (for horizontal scroll item)
                    view.setLayoutParams(lp);

                    PikiViewHolder vh = new PikiViewHolder(view);
                    view.setTag(R.id.vh, vh);
                }

                if (piki != null) { // If is not placeholder
                    PikiViewHolder vh = (PikiViewHolder) view.getTag(R.id.vh);
                    vh.txtUserName.setText("@" + piki.getName());
                    vh.txtDeleteOn.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);
                    vh.txtDeleteOff.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);

                    // Image Piki
                    PicassoUtils.with(context).load(piki.getUrlPiki()).resize((int) (imgPikiWidth * 0.80), (int) (imgPikiHeight * 0.80)).into(vh.imgPiki);
                    PicassoUtils.with(context).load(piki.getUrlReact1()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki1);
                    vh.imgPiki1.setVisibility(piki.thereIsReact1() ? View.VISIBLE : View.GONE);
                    PicassoUtils.with(context).load(piki.getUrlReact2()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki2);
                    vh.imgPiki2.setVisibility(piki.thereIsReact2() ? View.VISIBLE : View.GONE);
                    PicassoUtils.with(context).load(piki.getUrlReact3()).resize(imgPikiSmallWidth, imgPikiSmallHeight).into(vh.imgPiki3);
                    vh.imgPiki3.setVisibility(piki.thereIsReact3() ? View.VISIBLE : View.GONE);


                    // Label Piki
                    Date readDate = readDataProvider.getReadDate(piki.getId());
                    Date updateDate = piki.getUpdatedAt();
                    boolean isNew = readDate == null;
                    boolean isUpdated = !isNew && readDate.before(updateDate);
                    boolean isMoreReact = piki.getNbReact() > NB_THUMBNAIL_REACT;

                    // txtFirst
                    boolean me = piki.getName().equals(ParseUser.getCurrentUser().getUsername());//TODO : crash #7 > NullPointerException
                    vh.txtFirst.setVisibility(piki.getNbReact() == 0 && !me ? View.VISIBLE : View.GONE);

                    // txtUnread
                    if (isNew) {
                        vh.txtUnread.setText(context.getResources().getString(R.string.home_new));
                        vh.txtUnread.setBackgroundResource(R.drawable.second_color_label_back);
                        vh.txtUnread.setVisibility(View.VISIBLE);
                    } else {
                        vh.txtUnread.setText("+"+Utile.formatBigNumber(piki.getNbReact()-NB_THUMBNAIL_REACT));
                        vh.txtUnread.setBackgroundResource(isUpdated ? R.drawable.first_color_label_back : R.drawable.gris_label_back);
                        vh.txtUnread.setVisibility(isMoreReact && isUpdated ? View.VISIBLE : View.GONE);
                    }
                    vh.txtUnread.setPadding(screen.dpToPx(7), 0, screen.dpToPx(7), 0);//fix bug : no padding after setBackgroundResource

                    // TxtRead
                    vh.txtRead.setText("+"+Utile.formatBigNumber(piki.getNbReact()-NB_THUMBNAIL_REACT));
                    vh.txtRead.setVisibility(isMoreReact ? View.VISIBLE : View.GONE);


                    view.setTag(new Integer(i));
                    view.setTag(R.string.tag_downpresse, new DownRunnable(view));
                    view.setOnTouchListener(this);
                }

                break;

            case CELL_WITHOUT_PIKI :
                view = LayoutInflater.from(context).inflate(R.layout.item_piki_placeholder, parent, false);
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = screen.dpToPx(screen.getWidthDp() + 10); // add 10 dp width to item (for horizontal scroll item)
                view.setLayoutParams(lp);

                break;
        }

        return view;
    }

    private static int FADE_TIME = 150;//ms

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        int action = event.getAction();

        if(view.getTag() instanceof Integer)
        {
            int position = (Integer)view.getTag();

            DownRunnable down = (DownRunnable)view.getTag(R.string.tag_downpresse);
            if(action == MotionEvent.ACTION_DOWN)
            {
                //highlight with 90ms delay. Because I dont want highlight if user scroll
                handler.postDelayed(down, DOWN_TIME);
            }
            else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            {
                //finger move, cancel highlight
                handler.removeCallbacks(down);

                itemMarkDown(view, false);
            }
            if(action == MotionEvent.ACTION_UP)
            {
                //highlight just after clicked
                itemMarkDown(view, true);

                handler.postDelayed(down, DOWN_TIME);

                //send to listener
                if(listener != null)
                {
                    Piki piki = listPiki.get(position);
                    final TextView txtUnread = (TextView) view.findViewById(R.id.txtUnread);
                    final TextView txtRead = (TextView) view.findViewById(R.id.txtRead);
                    if(txtUnread.getVisibility() == View.VISIBLE)
                    {
                        Utile.fadeOut(txtUnread, FADE_TIME);
                        if(piki.getNbReact() > NB_THUMBNAIL_REACT) Utile.fadeIn(txtRead, FADE_TIME);
                    }

                    listener.clickOnPiki(piki);
                }
            }
        }
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

    public void setListPiki(List<Piki> listPiki)
    {
        this.listPiki = listPiki;
        notifyDataSetChanged();
    }

    // INTERFACE
    public interface Listener {
        public void clickOnPiki(Piki piki);
        public void showPlaceHolderItem(boolean show);
    }

    class PikiViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.back)
        RelativeLayout layoutBack;
        @InjectView(R.id.imgDeleteItemOff)
        LinearLayout imgDeleteItemOff;
        @InjectView(R.id.txtDeleteOff)
        TextView txtDeleteOff;
        @InjectView(R.id.imgDeleteItemOn)
        LinearLayout imgDeleteItemOn;
        @InjectView(R.id.txtDeleteOn)
        TextView txtDeleteOn;
        @InjectView(R.id.front)
        RelativeLayout layoutFront;
        @InjectView(R.id.imgPiki)
        ImageView imgPiki;
        @InjectView(R.id.txtUserName)
        TextViewFont txtUserName;
        @InjectView(R.id.imgPiki1)
        ImageView imgPiki1;
        @InjectView(R.id.imgPiki2)
        ImageView imgPiki2;
        @InjectView(R.id.imgPiki3)
        ImageView imgPiki3;
        @InjectView(R.id.txtFirst)
        TextView txtFirst;
        @InjectView(R.id.txtRead)
        TextView txtRead;
        @InjectView(R.id.txtUnread)
        TextView txtUnread;
        @InjectView(R.id.layoutOverlay)
        RelativeLayout layoutOverlay;

        public PikiViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}