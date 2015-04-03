package com.goandup.lib.widget;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;

public class PaginationScrollListener implements OnScrollListener
{
    private int VISIBLE_THRESHOLD = 5;
    
    private int currentPage;
    private int previousTotal;
    private boolean loading = true;

    private LoadingListener listener;
    private OnScrollListener anotherScrollListener;

    public PaginationScrollListener(LoadingListener listener) {
    	this(listener, null);
    }

    public PaginationScrollListener(LoadingListener listener, OnScrollListener anotherScrollListener) {
    	this.listener = listener;
    	this.anotherScrollListener = anotherScrollListener;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (loading)
        {
            if (totalItemCount > previousTotal)
            {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }
        else if (totalItemCount > 0 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD))
        {
            listener.loadingNewPage(currentPage);
            loading = true;
            Adapter a = view.getAdapter();
            if(a instanceof HeaderViewListAdapter) a = ((HeaderViewListAdapter)a).getWrappedAdapter();
            if(a != null && a instanceof ScrollAdapter)
            {
                ((ScrollAdapter)a).markToLoading(true);
            }
        }
    	if(anotherScrollListener != null) anotherScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

    @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
    	if(anotherScrollListener != null) anotherScrollListener.onScrollStateChanged(view, scrollState);
    }
	
	public interface LoadingListener
	{
		public void loadingNewPage(int page);
	}
	
	public interface ScrollAdapter
	{
		public void markToLoading(boolean loading);
	}
}
