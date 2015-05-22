package com.pleek.app.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goandup.lib.widget.EditTextFont;
import com.goandup.lib.widget.TextViewFont;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.bean.Friend;
import com.pleek.app.fragment.BestFragment;
import com.pleek.app.fragment.InboxFragment;
import com.pleek.app.fragment.InboxSearchFragment;
import com.pleek.app.fragment.PikiFragment;
import com.pleek.app.fragment.ScrollTabHolderFragment;
import com.pleek.app.interfaces.OnCollapseABListener;
import com.pleek.app.interfaces.ScrollTabHolder;
import com.pleek.app.utils.StringUtils;
import com.pleek.app.views.ViewPagerUnswippable;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.List;

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
    @InjectView(R.id.btnSearch)
    ImageView btnSearch;
    @InjectView(R.id.shadowBar)
    View shadowBar;
    @InjectView(R.id.editSearch)
    EditTextFont editSearch;
    @InjectView(R.id.imgErase)
    View btnErase;
    @InjectView(R.id.txtNBResults)
    TextViewFont txtNBResults;
    @InjectView(R.id.btnTab1)
    View btnTab1;
    @InjectView(R.id.btnTab2)
    View btnTab2;
    @InjectView(R.id.btnTab3)
    View btnTab3;
    @InjectView(R.id.tabIndicator)
    UnderlinePageIndicator tabIndicator;
    @InjectView(R.id.layoutContent)
    ViewGroup layoutContent;
    @InjectView(R.id.layoutOverlayWhite)
    View layoutOverlayWhite;
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
    @InjectView(R.id.layoutSearch)
    ViewGroup layoutSearch;
    @InjectView(R.id.layoutInboxSearchFragment)
    ViewGroup layoutInboxSearchFragment;

    private PagerAdapter pagerAdapter;

    private InboxSearchFragment inboxSearchFragment;

    private int actionBarHeight;
    private int headerHeightOG;
    private int minHeaderTranslation;
    int oldScroll = 0;

    boolean pendingAnimations = true;
    boolean isSearchEnabled = false;

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
        btnErase.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                launchSearch();
            }
        });

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

        layoutSearch.setTranslationY(- 2 * getResources().getDimensionPixelSize(R.dimen.header_height_without_shadow));

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
        } else if (v == btnSearch) {
            if (isSearchEnabled) {
                pagerAdapter.setIsHeaderScrollEnabled(isSearchEnabled);
                btnSearch.setImageResource(R.drawable.picto_search_disabled);
                layoutSearch.animate().translationY(-2 * getResources().getDimensionPixelSize(R.dimen.header_height_without_shadow));
                shadowBar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shadowBar.setVisibility(View.VISIBLE);
                    }
                }, 100);
                layoutContent.animate().setDuration(300).translationY(0);
                layoutOverlayWhite.animate().alpha(0);
            } else {
                pagerAdapter.setIsHeaderScrollEnabled(isSearchEnabled);
                btnSearch.setImageResource(R.drawable.picto_search);
                layoutSearch.animate().translationY(0);
                shadowBar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shadowBar.setVisibility(View.GONE);
                    }
                }, 100);
                layoutContent.animate().setDuration(400).translationY(getResources().getDimensionPixelSize(R.dimen.search_height));
                layoutOverlayWhite.animate().alpha(1);
            }

            isSearchEnabled = !isSearchEnabled;
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

        public void setIsHeaderScrollEnabled(boolean enabled) {
            fragments.valueAt(0).setIsHeaderScrollEnabled(enabled);
            fragments.valueAt(1).setIsHeaderScrollEnabled(enabled);
            fragments.valueAt(2).setIsHeaderScrollEnabled(enabled);
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

    private void launchSearch() {
        String filtreSearch = editSearch.getText().toString();

        if (filtreSearch != null && filtreSearch.length() >= 3) {
            layoutOverlayWhite.setVisibility(View.GONE);
            if (lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
            lastQueryRunnable = new QueryRunnable(filtreSearch);
            handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
        } else {
            layoutOverlayWhite.setVisibility(View.VISIBLE);
            layoutInboxSearchFragment.setVisibility(View.GONE);
        }
    }

    private static int TIMER_QUERY = 500;
    final Handler handler = new Handler();
    private QueryRunnable lastQueryRunnable;
    class QueryRunnable implements Runnable {

        private String username;

        public QueryRunnable(String username) {
            this.username = username;
        }

        @Override
        public void run() {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> list, ParseException e) {
                    if (e == null && username.equals(editSearch.getText().toString())) {
                        layoutInboxSearchFragment.setVisibility(View.VISIBLE);
                        inboxSearchFragment.filtreSearchChange(username);
                    } else {
                        System.out.println("COUCOU");
                    }
                }
            });
        }
    }
}
