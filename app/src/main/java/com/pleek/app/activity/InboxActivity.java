package com.pleek.app.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pleek.app.R;
import com.pleek.app.fragment.InboxFragment;
import com.pleek.app.fragment.ScrollTabHolderFragment;
import com.pleek.app.interfaces.OnCollapseABListener;
import com.pleek.app.interfaces.ScrollTabHolder;
import com.pleek.app.views.ViewPagerUnswippable;
import com.viewpagerindicator.UnderlinePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxActivity extends ParentActivity implements View.OnClickListener, ScrollTabHolder {

    public static boolean AUTO_RELOAD;

    @InjectView(R.id.imgSettings)
    View btnSettings;
    @InjectView(R.id.imgFriends)
    View btnFriends;
    @InjectView(R.id.imgLogo)
    View btnLogo;
    @InjectView(R.id.btnTab1)
    View btnTab1;
    @InjectView(R.id.btnTab2)
    View btnTab2;
    @InjectView(R.id.btnTab3)
    View btnTab3;
    @InjectView(R.id.tabIndicator)
    UnderlinePageIndicator tabIndicator;
    @InjectView(R.id.viewPager)
    ViewPagerUnswippable viewPager;
    @InjectView(R.id.imgInboxSel)
    ImageView imgInboxSel;
    @InjectView(R.id.imgSentSel)
    ImageView imgSentSel;
    @InjectView(R.id.imgBestSel)
    ImageView imgBestSel;
    @InjectView(R.id.pagerTabLayout)
    RelativeLayout pagerTabLayout;
    @InjectView(R.id.barLayout)
    RelativeLayout barLayout;
    @InjectView(R.id.header)
    View header;

    private PagerAdapter pagerAdapter;

    private int actionBarHeight;
    private int headerHeightOG;
    private int minHeaderTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        ButterKnife.inject(this);

        headerHeightOG = getResources().getDimensionPixelSize(R.dimen.header_height);
        minHeaderTranslation = -getResources().getDimensionPixelSize(R.dimen.top_bar_height);

        btnSettings.setOnClickListener(this);
        btnFriends.setOnClickListener(this);
        btnTab1.setOnClickListener(this);
        btnTab2.setOnClickListener(this);
        btnTab3.setOnClickListener(this);

        tabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTab(position == 0 ? btnTab1 : (position == 1 ? btnTab2 : btnTab3));

                SparseArray<ScrollTabHolder> scrollTabHolders = pagerAdapter.getScrollTabHolders();
                ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
                currentHolder.adjustScroll((int) (headerHeightOG + header.getTranslationY()));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        init();
    }

    private void init() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.setTabHolderScrollingContent(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabIndicator.setViewPager(viewPager);
        tabIndicator.setFades(false);
    }

    @Override
    protected void onResume() {
        if (AUTO_RELOAD) {
            AUTO_RELOAD = false;
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == btnFriends) {
            startActivity(new Intent(this, FriendsActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (v == btnSettings) {
            startActivity(new Intent(this, ParameterActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up);
        } else if (v == btnTab1) {
            viewPager.setCurrentItem(0, true);
        } else if (v == btnTab2) {
            viewPager.setCurrentItem(1, true);
        } else if (v == btnTab3) {
            viewPager.setCurrentItem(2, true);
        }
    }

    private void setTab(View tab) {
        if (tab == btnTab1) {
            imgInboxSel.setVisibility(View.VISIBLE);
            imgSentSel.setVisibility(View.INVISIBLE);
            imgBestSel.setVisibility(View.INVISIBLE);
        } else if (tab == btnTab2) {
            imgSentSel.setVisibility(View.VISIBLE);
            imgInboxSel.setVisibility(View.INVISIBLE);
            imgBestSel.setVisibility(View.INVISIBLE);
        } else if (tab == btnTab3) {
            imgBestSel.setVisibility(View.VISIBLE);
            imgInboxSel.setVisibility(View.INVISIBLE);
            imgSentSel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void adjustScroll(int scrollHeight) {
        // NOTHING
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
        if (viewPager.getCurrentItem() == pagePosition) {
            int scrollY = getScrollY(view);
            header.setTranslationY(Math.max(-scrollY, minHeaderTranslation));
        }
    }

    public int getScrollY(AbsListView view) {
        View c = view.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = headerHeightOG;
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private SparseArray<ScrollTabHolder> fragments;
        private ScrollTabHolder listener;

        public PagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<ScrollTabHolder>();
        }

        public void setTabHolderScrollingContent(ScrollTabHolder pListener) {
            listener = pListener;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) InboxFragment.newInstance(position);

            fragments.put(position, fragment);
            if (listener != null) {
                fragment.setScrollTabHolder(listener);
            }

            return fragment;
        }

        public SparseArray<ScrollTabHolder> getScrollTabHolders() {
            return fragments;
        }

    }
}
