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

import com.pleek.app.R;
import com.pleek.app.bean.Friend;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by nicolas on 13/01/15.
 */
public class FriendsAdapter extends BaseAdapter implements StickyListHeadersAdapter, View.OnTouchListener
{
    private List<Friend> listFriend;
    private List<Friend> listFriendLoading;
    private Listener listener;
    private Context context;

    public FriendsAdapter(Listener listener)
    {
        this(listener, (Context)listener);
    }

    public FriendsAdapter(Listener listener, Context context)
    {
        this.listener = listener;
        this.context = context;

        listFriendLoading = new ArrayList<Friend>();
    }

    @Override
    public int getCount()
    {
        return listFriend != null ? listFriend.size() : 0;
    }

    @Override
    public Friend getItem(int i)
    {
        return listFriend.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return getCount();
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent)
    {
        if (view == null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_friends, parent, false);
        }

        Friend friend = getItem(i);
        TextView txtName = (TextView) view.findViewById(R.id.txtName);
        txtName.setText(friend.name != null ? friend.name : "@"+friend.username);
        TextView txtUserName = (TextView) view.findViewById(R.id.txtUserName);
        if((friend.username != null || friend.phoneNumber != null) && friend.name != null)
        {
            if(friend.username != null) txtUserName.setText("@"+friend.username);
            else if(friend.phoneNumber != null) txtUserName.setText(friend.phoneNumber);
            txtUserName.setVisibility(View.VISIBLE);
        }
        else
        {
            txtUserName.setVisibility(View.GONE);
        }
        ImageView imgAdd = (ImageView) view.findViewById(R.id.imgAdd);
        if(friend.image > 0) imgAdd.setImageResource(friend.image);
        else imgAdd.setImageDrawable(null);
        View progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(listFriendLoading.contains(friend) ? View.VISIBLE : View.INVISIBLE);

        view.setTag(new Integer(i));
        view.setTag(R.string.tag_downpresse, new DownRunnable(view));
        view.setOnTouchListener(this);

        return view;
    }

    @Override
    public View getHeaderView(int position, View view, ViewGroup parent)
    {
        if (view == null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
        }

        TextView txtNameHeader = (TextView) view.findViewById(R.id.txtNameHeader);
        txtNameHeader.setText(listFriend.get(position).sectionLabel);

        return view;
    }

    @Override
    public long getHeaderId(int position)
    {
        return listFriend.get(position).sectionLabel;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        if(view.getTag() instanceof Integer)
        {
            int position = (Integer)view.getTag();

            DownRunnable down = (DownRunnable)view.getTag(R.string.tag_downpresse);
            int action = event.getAction();
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
            if(action == MotionEvent.ACTION_UP && listener != null)
            {
                //highlight just after clicked
                itemMarkDown(view, true);

                handler.postDelayed(down, DOWN_TIME);

                Friend f = listFriend.get(position);
                if(listener != null && !listFriendLoading.contains(f)) listener.clickOnName(f);
            }
        }
        return true;
    }

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
    };

    private void itemMarkDown(View item, boolean down)
    {
        View layoutOverlay = item.findViewById(R.id.layoutOverlay);
        if(layoutOverlay != null)
        {
            int visibility = down ? View.VISIBLE : View.GONE;
            layoutOverlay.setVisibility(visibility);
        }
    }

    public List<Friend> getListFriend()
    {
        return listFriend;
    }

    public void setListFriend(List<Friend> listFriend)
    {
        this.listFriend = listFriend;
    }

    public void addPikiUser(Friend friend)
    {
        removePikiUser();
        listFriend.add(0, friend);
        notifyDataSetChanged();
    }

    public void removePikiUser()
    {
        if(listFriend != null && listFriend.size() > 0 && listFriend.get(0).sectionLabel == R.string.friends_section_pikiuser)
        {
            listFriend.remove(0);
            notifyDataSetChanged();
        }
    }

    public void addFriendLoading(Friend friend)
    {
        listFriendLoading.add(friend);
        notifyDataSetChanged();
    }

    public void removeFriendLoading(Friend friend)
    {
        removeFriendLoading(friend, true);
    }

    public void removeFriendLoading(Friend friend, boolean reload)
    {
        listFriendLoading.remove(friend);
        if(reload) notifyDataSetChanged();
    }

    public void removeFriend(Friend friend)
    {
        removeFriendLoading(friend, false);
        listFriend.remove(friend);
        notifyDataSetChanged();
    }

    //INTERFACE
    public interface Listener {
        public void clickOnName(Friend friend);
    }
}
