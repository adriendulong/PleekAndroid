package com.pleek.app.fragment;

import android.widget.AbsListView;

import com.pleek.app.interfaces.QuickReturnListViewOnScrollListener;
import com.pleek.app.interfaces.ScrollTabHolder;

public abstract class ScrollTabHolderFragment extends ParentFragment implements ScrollTabHolder {

    protected ScrollTabHolder scrollTabHolder;

	public void setScrollTabHolder(ScrollTabHolder pScrollTabHolder) {
        scrollTabHolder = pScrollTabHolder;
	}
}