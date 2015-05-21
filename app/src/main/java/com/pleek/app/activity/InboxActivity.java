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
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.fragment.BestFragment;
import com.pleek.app.fragment.InboxFragment;
import com.pleek.app.fragment.PikiFragment;
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

    public static int TYPE_INBOX = 0;
    public static int TYPE_SENT = 1;
    public static int TYPE_BEST = 2;

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
    @InjectView(R.id.btnPlus)
    ImageView btnPlus;

    private PagerAdapter pagerAdapter;

    private int actionBarHeight;
    private int headerHeightOG;
    private int minHeaderTranslation;
    int oldScroll = 0;

    boolean pendingAnimations = true;

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
        btnPlus.setOnClickListener(this);

        tabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                btnPlus.setVisibility((position == 0 || position == 1) ? View.VISIBLE : View.GONE);
                setTab(position == 0 ? btnTab1 : (position == 1 ? btnTab2 : btnTab3));

                SparseArray<PikiFragment> scrollTabHolders = pagerAdapter.getScrollTabHolders();
                ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
                currentHolder.adjustScroll(oldScroll);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        pendingAnimations = savedInstanceState == null;

        init();
    }

    private void init() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.setTabHolderScrollingContent(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabIndicator.setViewPager(viewPager);
        tabIndicator.setFades(false);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        ParseObject userInfos = currentUser.getParseObject("UserInfos");
        if (userInfos != null) mixpanel.getPeople().set("$phone", userInfos.getString("phoneNumber"));
        mixpanel.flush();

        if (pendingAnimations) {
            btnPlus.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_size));
            startAnimation();
            pendingAnimations = false;
        }
    }

    @Override
    protected void onResume() {
        if (AUTO_RELOAD) {
            AUTO_RELOAD = false;

            pagerAdapter.reload();
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
        } else if (v == btnPlus) {
            startActivity(new Intent(this, CaptureActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
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
        oldScroll = scrollHeight;
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private SparseArray<PikiFragment> fragments;
        private ScrollTabHolder listener;

        public PagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            fragments = new SparseArray<PikiFragment>();
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
            PikiFragment fragment;

            if (position == 2) {
                fragment = (PikiFragment) BestFragment.newInstance(position, header);
            } else {
                fragment = (PikiFragment) InboxFragment.newInstance(position, header);
            }

            fragments.put(position, fragment);
            if (listener != null) {
                fragment.setScrollTabHolder(listener);
            }

            return fragment;
        }

        public void reload() {
            fragments.valueAt(0).reload();
            fragments.valueAt(1).reload();
            fragments.valueAt(2).reload();
        }

        public SparseArray<PikiFragment> getScrollTabHolders() {
            return fragments;
        }
    }

    private void startAnimation() {
        btnPlus.animate()
            .translationY(0)
            .setInterpolator(new OvershootInterpolator(1.f))
            .setStartDelay(800)
            .setDuration(300)
            .start();
    }

    @Override
    protected void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }
}
