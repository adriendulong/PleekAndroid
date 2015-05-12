package com.pleek.app.fragment;

import android.widget.AbsListView;

import com.pleek.app.interfaces.ScrollTabHolder;

public abstract class ScrollTabHolderFragment extends ParentFragment implements ScrollTabHolder {

	protected ScrollTabHolder mScrollTabHolder;

	public void setScrollTabHolder(ScrollTabHolder scrollTabHolder) {
        mScrollTabHolder = scrollTabHolder;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
		// nothing
	}
}