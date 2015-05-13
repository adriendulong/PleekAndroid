package com.pleek.app.interfaces;

import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.AbsListView;
import com.pleek.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on the work of etiennelawlor-.
 */
public class QuickReturnListViewOnScrollListener implements AbsListView.OnScrollListener {

    private final View mHeader;
    private final int mMinHeaderTranslation;
    private int mPrevScrollY = 0;
    private int mHeaderDiffTotal = 0;
    private List<AbsListView.OnScrollListener> mExtraOnScrollListenerList = new ArrayList<AbsListView.OnScrollListener>();
    // endregion

    // region Constructors
    private QuickReturnListViewOnScrollListener(Builder builder) {
        mHeader = builder.mHeader;
        mMinHeaderTranslation = builder.mMinHeaderTranslation;
    }
    // endregion

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.d(getClass().getSimpleName(), "onScrollStateChanged() : scrollState - "+scrollState);
        // apply another list' s on scroll listener
        for (AbsListView.OnScrollListener listener : mExtraOnScrollListenerList) {
            listener.onScrollStateChanged(view, scrollState);
        }

        if (scrollState == SCROLL_STATE_IDLE) {
            int midHeader = -mMinHeaderTranslation / 2;

            if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), 0);
                anim.setDuration(100);
                anim.start();
                mHeaderDiffTotal = 0;
            } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader.getTranslationY(), mMinHeaderTranslation);
                anim.setDuration(100);
                anim.start();
                mHeaderDiffTotal = mMinHeaderTranslation;
            }
        }
    }

    @Override
    public void onScroll(AbsListView listview, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // apply extra on scroll listener
        for (AbsListView.OnScrollListener listener : mExtraOnScrollListenerList) {
            listener.onScroll(listview, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        int scrollY = Utils.getScrollY(listview);
        int diff = mPrevScrollY - scrollY;

        if (diff != 0) {
            if(diff < 0) { // scrolling down
                mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation);
            } else { // scrolling up
                mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0);
            }

            mHeader.setTranslationY(mHeaderDiffTotal);
        }

        mPrevScrollY = scrollY;
    }

    // region Helper Methods
    public void registerExtraOnScrollListener(AbsListView.OnScrollListener listener) {
        mExtraOnScrollListenerList.add(listener);
    }

    public int getPrevScrollY() {
        return mPrevScrollY;
    }

    public void setPrevScrollY(int mPrevScrollY) {
        this.mPrevScrollY = mPrevScrollY;
    }

    public static class Builder {
        private View mHeader = null;
        private int mMinHeaderTranslation = 0;
        private View mFooter = null;
        private int mMinFooterTranslation = 0;

        public Builder header(View header){
            mHeader = header;
            return this;
        }

        public Builder minHeaderTranslation(int minHeaderTranslation){
            mMinHeaderTranslation = minHeaderTranslation;
            return this;
        }

        public Builder footer(View footer){
            mFooter = footer;
            return this;
        }

        public Builder minFooterTranslation(int minFooterTranslation){
            mMinFooterTranslation = minFooterTranslation;
            return this;
        }

        public QuickReturnListViewOnScrollListener build() {
            return new QuickReturnListViewOnScrollListener(this);
        }
    }
}