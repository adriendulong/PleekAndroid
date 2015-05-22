package com.pleek.app.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.ListViewScrollingOff;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.InboxActivity;
import com.pleek.app.activity.ParentActivity;
import com.pleek.app.activity.PikiActivity;
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Piki;
import com.pleek.app.common.Constants;
import com.pleek.app.interfaces.QuickReturnListViewOnScrollListener;
import com.pleek.app.views.CircleProgressBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 11/05/2015.
 */
public abstract class PikiFragment extends ScrollTabHolderFragment implements PikiAdapter.Listener {

    protected int type;
    protected View header;
    protected QuickReturnListViewOnScrollListener scrollListener;

    @InjectView(R.id.listViewPiki)
    ListViewScrollingOff listViewPiki;
    @InjectView(R.id.refreshSwipe)
    SwipeRefreshLayout refreshSwipe;

    protected View footer;

    protected List<Piki> listPiki;
    protected List<ParseUser> friends;

    protected boolean shouldReinit = false;

    protected int initialX;
    protected int lastItemShow;

    protected boolean isHeaderScrollEnabled = true;

    public static PikiFragment newInstance(int type, View header) {
        PikiFragment fragment;

        if (type == InboxActivity.TYPE_BEST) {
            fragment = BestFragment.newInstance(type, header);
        } else {
            fragment = InboxFragment.newInstance(type, header);
        }

        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_inbox, null);

        ButterKnife.inject(this, v);

        return v;
    }

    protected void setup() {
        scrollListener = new QuickReturnListViewOnScrollListener.Builder()
                .header(header)
                .minHeaderTranslation(-getResources().getDimensionPixelSize(R.dimen.top_bar_height))
                .build();

        View placeHolderView = getActivity().getLayoutInflater().inflate(R.layout.item_empty_header, listViewPiki, false);
        listViewPiki.setOnScrollListener(new MyOnScrollListener());
        listViewPiki.addHeaderView(placeHolderView);
        listViewPiki.setHeaderDividersEnabled(false);
        refreshSwipe.setColorSchemeResources(R.color.secondColor, R.color.firstColor, R.color.secondColor, R.color.firstColor);
        refreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shouldReinit = true;
                init(false);
            }
        });
        refreshSwipe.setProgressViewOffset(false, 0, (int) (getResources().getDimensionPixelSize(R.dimen.header_height) + 20 * screen.getDensity()));

        footer = getActivity().getLayoutInflater().inflate(R.layout.item_footer, null);
        ((CircleProgressBar) footer.findViewById(R.id.progressBar)).setColorSchemeResources(R.color.progressBar);
        listViewPiki.addFooterView(footer);
    }

    protected void init() {
        init(true);
    }
    protected void init(final boolean withCache) {
        currentPage = 0;
        lastItemShow = 0;

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        endOfLoading = false;
        refreshSwipe.post(new Runnable() {
            @Override public void run() {
                //refreshSwipe.setRefreshing(loadNext(withCache));
                loadNext(withCache);
            }
        });
    }

    protected boolean fromCache;
    protected int currentPage;
    protected final int NB_BY_PAGE = 25;
    protected List<Piki> listBeforreRequest;
    protected boolean isLoading;
    protected boolean endOfLoading;
    protected boolean shouldRefreshFriends = true;

    protected abstract boolean loadNext(final boolean withCache);
    protected abstract void loadPikis(boolean withCache);

    @Override
    public void clickOnPiki(Piki piki) {
        PikiActivity.initActivity(piki);
        startActivity(new Intent(getActivity(), PikiActivity.class));
        getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void reload() {
        shouldReinit = true;
        init(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected class MyOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (isHeaderScrollEnabled) {
                scrollListener.onScrollStateChanged(absListView, i);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (isHeaderScrollEnabled) {
                scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            ((InboxActivity) getActivity()).adjustScroll(scrollListener.getPrevScrollY());

            //pagination
            final int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (lastItemShow < lastItem) {
                    if (loadNext(true)) {
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }

    public boolean isHeaderScrollEnabled() {
        return isHeaderScrollEnabled;
    }

    public void setIsHeaderScrollEnabled(boolean isHeaderScrollEnabled) {
        this.isHeaderScrollEnabled = isHeaderScrollEnabled;
    }
}
