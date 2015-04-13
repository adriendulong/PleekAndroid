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
import com.pleek.app.utils.PicassoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicolas on 19/12/14.
 */
public class AddUserOnLoginAdapter extends BaseAdapter implements View.OnTouchListener
{
    private List<User> listUser;
    private List<User> listUserSelected = new ArrayList<User>();
    private Listener listener;
    private Context context;

    public AddUserOnLoginAdapter(Listener listener)
    {
        this.listener = listener;
        if(listener instanceof Context)
        {
            this.context = (Context) listener;
        }
    }

    public AddUserOnLoginAdapter(List<User> listUser, Listener listener, Context context)
    {
        this.listUser = listUser;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return listUser != null ? listUser.size() : 0;
    }

    @Override
    public Object getItem(int i)
    {
        return getCount() > 0 ? listUser.get(i % getCount()) : null;
    }

    @Override
    public long getItemId(int i)
    {
        return getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent)
    {
        User user = listUser.get(i);

        if (view == null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }

        ImageView imgUser = (ImageView) view.findViewById(R.id.imgUser);
        PicassoUtils.with(context).load(user.imageUrl).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(imgUser);
        TextView txtUserName = (TextView) view.findViewById(R.id.txtUserName);
        txtUserName.setText(user.name);
        TextView txtUserBaseline = (TextView) view.findViewById(R.id.txtUserBaseline);
        txtUserBaseline.setText(user.baseline);
        ImageView pictoAddUser = (ImageView) view.findViewById(R.id.pictoAddUser);
        pictoAddUser.setImageResource(listUserSelected.contains(user) ? R.drawable.picto_added : R.drawable.picto_add_user);

        view.setTag(new Integer(i));
        view.setTag(R.string.tag_downpresse, new DownRunnable(view));
        view.setOnTouchListener(this);

        return view;
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

                User user = listUser.get(position);
                //remove or add to listUserSelected
                if(!listUserSelected.remove(user)) listUserSelected.add(user);
                //send to listener
                if(listener != null) listener.clickOnUser(user);

                notifyDataSetChanged();
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

    //INTERFACE
    public interface Listener
    {
        public void clickOnUser(User user);
    }

    public void setListUser(List<User> listUser) {
        this.listUser = listUser;
    }

    public List<User> getListUserSelected() {
        return listUserSelected;
    }

    //BEAN
    public class User implements Comparable<User>
    {
        public String id;
        public String name;
        public String baseline;
        public String imageUrl;

        public User(String id, String name, String baseline, String imageUrl)
        {
            this.id = id;
            this.name = name;
            this.baseline = baseline;
            this.imageUrl = imageUrl;
        }

        @Override
        public int compareTo(User user)
        {
            if(user == null && user.name != null) return 1;
            if(name == null) return -1;
            return name.compareTo(user.name);
        }
    }
}