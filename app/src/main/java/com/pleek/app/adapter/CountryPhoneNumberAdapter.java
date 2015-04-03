package com.pleek.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pleek.app.R;

import java.text.Normalizer;
import java.util.List;

/**
 * Created by nicolas on 18/12/14.
 */
public class CountryPhoneNumberAdapter extends BaseAdapter implements View.OnTouchListener
{
    private List<Country> listCountry;
    private Listener listener;
    private Context context;

    public CountryPhoneNumberAdapter(Listener listener)
    {
        this.listener = listener;
        if(listener instanceof Context)
        {
            this.context = (Context) listener;
        }
    }

    public CountryPhoneNumberAdapter(List<Country> listCountry, Listener listener, Context context)
    {
        this.listCountry = listCountry;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return listCountry != null ? listCountry.size() : 0;
    }

    @Override
    public Object getItem(int i)
    {
        return getCount() > 0 ? listCountry.get(i % getCount()) : null;
    }

    @Override
    public long getItemId(int i)
    {
        return getCount();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent)
    {
        if (view == null)
        {
            view = LayoutInflater.from(context).inflate(R.layout.item_country, parent, false);
        }

        TextView txtCountry = (TextView) view.findViewById(R.id.txtCountry);
        txtCountry.setText(listCountry.get(i).name);

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

                if(listener != null) listener.clickOnClountry(listCountry.get(position));
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

        TextView txtCountry = (TextView) item.findViewById(R.id.txtCountry);
        int colorBlanc = context.getResources().getColor(R.color.blanc);
        int colorNoir = context.getResources().getColor(R.color.noir);
        txtCountry.setTextColor(down ? colorBlanc : colorNoir);
    }

    public void setListCountry(List<Country> listCountry) {
        this.listCountry = listCountry;
    }

    //INTERFACE
    public interface Listener
    {
        public void clickOnClountry(Country country);
    }


    //BEAN
    public class Country implements Comparable<Country>
    {
        public String code;
        public String name;

        public Country(String code, String name)
        {
            this.code = code;
            this.name = name;
        }

        @Override
        public int compareTo(Country other)
        {
            if(other == null) return 1;

            //remove accent
            String name1 = Normalizer.normalize(this.name, Normalizer.Form.NFD);
            name1 = name1.replaceAll("[^\\p{ASCII}]", "");
            String name2 = Normalizer.normalize(other.name, Normalizer.Form.NFD);
            name2 = name2.replaceAll("[^\\p{ASCII}]", "");

            return name1.compareTo(name2);
        }
    }
}
