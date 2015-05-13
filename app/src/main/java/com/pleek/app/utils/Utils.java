package com.pleek.app.utils;

import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by tiago on 13/05/2015.
 */
public class Utils {

    private static Dictionary<Integer, Integer> sListViewItemHeights = new Hashtable<Integer, Integer>();
    private static Dictionary<Integer, Integer> sRecyclerViewItemHeights = new Hashtable<Integer, Integer>();

    public static int getScrollY(ListView lv) {
        View c = lv.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = lv.getFirstVisiblePosition();
        int top = c.getTop();

        int scrollY = -top + firstVisiblePosition * c.getHeight();
        return scrollY;
    }

    public static int getScrollY(AbsListView lv) {
        View c = lv.getChildAt(0);

        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = lv.getFirstVisiblePosition();
        int scrollY = -(c.getTop());

        sListViewItemHeights.put(lv.getFirstVisiblePosition(), c.getHeight());

        if (scrollY < 0)
            scrollY = 0;

        for (int i = 0; i < firstVisiblePosition; ++i) {
           if (sListViewItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += sListViewItemHeights.get(i); //add all heights of the views that are gone
        }

        return scrollY;
    }
}
