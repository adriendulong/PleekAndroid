package com.pleek.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.Utile;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.ReadDateProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

/**
 * Created by nicolas on 19/12/14.
 */
public class PikiAdapter extends BaseAdapter implements View.OnTouchListener
{
    public static final int HEIGHT_ITEM = 111;//dp
    private final int NB_THUMBNAIL_REACT = 3;

    private List<Piki> listPiki;
    private Listener listener;
    private Context context;

    private int heightListView;
    private Screen screen;
    private ReadDateProvider readDataProvider;

    public PikiAdapter(List<Piki> listPiki, Listener listener)
    {
        this(listPiki, listener, listener instanceof Context ? (Context) listener : null);
    }

    public PikiAdapter(List<Piki> listPiki, Listener listener, Context context)
    {
        this.listPiki = listPiki;
        this.listener = listener;
        this.context = context;

        screen = Screen.getInstance(context);
        readDataProvider = ReadDateProvider.getInstance(context);
    }

    @Override
    public int getCount()
    {
        int nb = listPiki != null ? listPiki.size() : 0;
        int minItem = (int) (((float)heightListView / screen.dpToPx(HEIGHT_ITEM)) + 0.5f);
        if(listener != null) listener.showPlaceHolderItem(minItem > nb);
        return Math.max(nb, minItem);
    }

    @Override
    public Object getItem(int i)
    {
        return getCount() > 0 ? listPiki.get(i % getCount()) : null;
    }

    @Override
    public long getItemId(int i)
    {
        return getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent)
    {
        Piki piki = i < listPiki.size() ? listPiki.get(i) : null;

        if(view != null)
        {
            //if need item_pikigroup_placeholder and recycleview is item_pikigroup
            if(piki == null && view.findViewById(R.id.placeholder) == null) view = null;
            //if need item_pikigroup and recycleview is item_pikigroup_placeholder
            if(piki != null && view.findViewById(R.id.placeholder) != null) view = null;
        }

        if (view == null)
        {
            view = LayoutInflater.from(context).inflate(piki != null ? R.layout.item_piki : R.layout.item_piki_placeholder, parent, false);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = screen.dpToPx(screen.getWidthDp() + 10);//add 10 dp with to item (for horizontal scroll item)
            view.setLayoutParams(lp);
        }

        if(piki != null)//if is not placeholder
        {
            TextView txtUserName = (TextView) view.findViewById(R.id.txtUserName);
            txtUserName.setText("@"+piki.getName());
            TextView txtDeleteOn = (TextView) view.findViewById(R.id.txtDeleteOn);
            TextView txtDeleteOff = (TextView) view.findViewById(R.id.txtDeleteOff);
            txtDeleteOn.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);
            txtDeleteOff.setText(piki.iamOwner() ? R.string.home_delete : R.string.home_hide);
            ImageView imgPikigroup = (ImageView) view.findViewById(R.id.imgPiki);
            ImageView imgPiki1 = (ImageView) view.findViewById(R.id.imgPiki1);
            ImageView imgPiki2 = (ImageView) view.findViewById(R.id.imgPiki2);
            ImageView imgPiki3 = (ImageView) view.findViewById(R.id.imgPiki3);


            //image piki
            Picasso.with(context).load(piki.getUrlPiki()).into(imgPikigroup);
            Picasso.with(context).load(piki.getUrlReact1()).into(imgPiki1);
            imgPiki1.setVisibility(piki.thereIsReact1() ? View.VISIBLE : View.GONE);
            Picasso.with(context).load(piki.getUrlReact2()).into(imgPiki2);
            imgPiki2.setVisibility(piki.thereIsReact2() ? View.VISIBLE : View.GONE);
            Picasso.with(context).load(piki.getUrlReact3()).into(imgPiki3);
            imgPiki3.setVisibility(piki.thereIsReact3() ? View.VISIBLE : View.GONE);


            //label piki
            TextView txtFirst = (TextView) view.findViewById(R.id.txtFirst);
            TextView txtUnread = (TextView) view.findViewById(R.id.txtUnread);
            TextView txtRead = (TextView) view.findViewById(R.id.txtRead);
            Date readDate = readDataProvider.getReadDate(piki.getId());
            Date updateDate = piki.getUpdatedAt();
            boolean isNew = readDate == null;
            boolean isUpdated = !isNew && readDate.before(updateDate);
            boolean isMoreReact = piki.getNbReact() > NB_THUMBNAIL_REACT;

            //txtFirst
            boolean me = piki.getName().equals(ParseUser.getCurrentUser().getUsername());//TODO : crash #7 > NullPointerException
            txtFirst.setVisibility(piki.getNbReact() == 0 && !me ? View.VISIBLE : View.GONE);

            //txtUnread
            if(isNew)
            {
                txtUnread.setText(context.getResources().getString(R.string.home_new));
                txtUnread.setBackgroundResource(R.drawable.second_color_label_back);
                txtUnread.setVisibility(View.VISIBLE);
            }
            else
            {
                txtUnread.setText("+"+Utile.formatBigNumber(piki.getNbReact()-NB_THUMBNAIL_REACT));
                txtUnread.setBackgroundResource(isUpdated ? R.drawable.first_color_label_back : R.drawable.gris_label_back);
                txtUnread.setVisibility(isMoreReact && isUpdated ? View.VISIBLE : View.GONE);
            }
            txtUnread.setPadding(screen.dpToPx(7), 0, screen.dpToPx(7), 0);//fix bug : no padding after setBackgroundResource

            //txtRead
            txtRead.setText("+"+Utile.formatBigNumber(piki.getNbReact()-NB_THUMBNAIL_REACT));
            txtRead.setVisibility(isMoreReact ? View.VISIBLE : View.GONE);


            view.setTag(new Integer(i));
            view.setTag(R.string.tag_downpresse, new DownRunnable(view));
            view.setOnTouchListener(this);
        }

        view.setVisibility(View.VISIBLE);
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

    //INTERFACE
    public interface Listener
    {
        public void clickOnPiki(Piki piki);
        public void showPlaceHolderItem(boolean show);
    }
}