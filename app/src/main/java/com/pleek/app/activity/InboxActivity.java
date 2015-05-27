package com.pleek.app.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.EditTextFont;
import com.goandup.lib.widget.TextViewFont;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.fragment.BestFragment;
import com.pleek.app.fragment.InboxFragment;
import com.pleek.app.fragment.InboxSearchFragment;
import com.pleek.app.fragment.PikiFragment;
import com.pleek.app.interfaces.OnNewContentListener;
import com.pleek.app.interfaces.OnRefreshInboxListener;
import com.pleek.app.interfaces.ScrollTabHolder;
import com.pleek.app.views.ViewPagerUnswippable;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxActivity extends ParentActivity implements View.OnClickListener, ScrollTabHolder, OnRefreshInboxListener, OnNewContentListener {

    public static int TYPE_INBOX = 0;
    public static int TYPE_SENT = 1;
    public static int TYPE_BEST = 2;
    public static int TYPE_SEARCH = 3;

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
    @InjectView(R.id.viewInboxNew)
    View viewInboxNew;
    @InjectView(R.id.viewSentNew)
    View viewSentNew;
    @InjectView(R.id.viewBestNew)
    View viewBestNew;

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

                if (position == TYPE_INBOX) {
                    viewInboxNew.setVisibility(View.GONE);
                    mixpanel.track("Inbox View", null);
                } else if (position == TYPE_SENT) {
                    viewSentNew.setVisibility(View.GONE);
                    mixpanel.track("Sent View", null);
                } else {
                    viewBestNew.setVisibility(View.GONE);
                    mixpanel.track("Best View", null);
                }
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
        inboxSearchFragment = (InboxSearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSearchInbox);
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

        layoutSearch.setTranslationY(-2 * getResources().getDimensionPixelSize(R.dimen.header_height_without_shadow));

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
            if (!isSearchEnabled)
                viewPager.setCurrentItem(0, true);
        } else if (v == btnTab2) {
            if (!isSearchEnabled)
                viewPager.setCurrentItem(1, true);
        } else if (v == btnTab3) {
            if (!isSearchEnabled)
                viewPager.setCurrentItem(2, true);
        } else if (v == btnPlus) {
            startActivity(new Intent(this, CaptureActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (v == btnSearch) {
            showEditTextSearch();
        } else if (v == btnErase) {
            eraseSearchResults();
        }
    }

    private void showEditTextSearch() {
        if (isSearchEnabled) {
            pagerAdapter.setIsHeaderScrollEnabled(true);
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
            layoutOverlayWhite.setVisibility(View.GONE);
            layoutInboxSearchFragment.setVisibility(View.GONE);

            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            isSearchEnabled = false;
        } else {
            editSearch.setText("");
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
            layoutOverlayWhite.setVisibility(View.VISIBLE);

            editSearch.requestFocus();
            editSearch.postDelayed(new Runnable() {

                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(editSearch, 0);
                }
            }, 200);
            isSearchEnabled = true;
        }
    }

    private void setTab(View tab) {
        if (isSearchEnabled) {
            showEditTextSearch();
        }

        if (tab == btnTab1) {
            btnSearch.setVisibility(View.VISIBLE);
            imgInboxSel.setVisibility(View.VISIBLE);
            imgSentSel.setVisibility(View.INVISIBLE);
            imgBestSel.setVisibility(View.INVISIBLE);
        } else if (tab == btnTab2) {
            btnSearch.setVisibility(View.GONE);
            imgSentSel.setVisibility(View.VISIBLE);
            imgInboxSel.setVisibility(View.INVISIBLE);
            imgBestSel.setVisibility(View.INVISIBLE);
        } else if (tab == btnTab3) {
            imgBestSel.setVisibility(View.VISIBLE);
            imgInboxSel.setVisibility(View.INVISIBLE);
            imgSentSel.setVisibility(View.INVISIBLE);
        }
    }

    private void eraseSearchResults() {
        editSearch.setText("");
        launchSearch();
    }

    @Override
    public void adjustScroll(int scrollHeight) {
        oldScroll = scrollHeight;
    }

    @Override
    public void onNewContent(int type, boolean show) {
        if (type == viewPager.getCurrentItem()) return;

        if (type == TYPE_INBOX) {
            viewInboxNew.setVisibility(View.VISIBLE);
        } else if (type == TYPE_SENT) {
            viewSentNew.setVisibility(View.VISIBLE);
        } else {
            viewBestNew.setVisibility(View.VISIBLE);
        }
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
                fragment = (PikiFragment) BestFragment.newInstance(position, header, viewBestNew);
            } else {
                fragment = (PikiFragment) InboxFragment.newInstance(position, header, position == 0 ? viewInboxNew : viewSentNew);
            }

            fragments.put(position, fragment);
            if (listener != null) {
                fragment.setScrollTabHolder(listener);
                fragment.setOnRefreshInboxListener(InboxActivity.this);
                fragment.setOnNewContentListener(InboxActivity.this);
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
    public void onRefresh() {
        pagerAdapter.reload();
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

        editSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(editSearch, 0);
            }
        }, 200);
    }

    private static int TIMER_QUERY = 500;
    final Handler handler = new Handler();
    private QueryRunnable lastQueryRunnable;
    class QueryRunnable implements Runnable {

        private String username;

        public QueryRunnable(String username) {
            if (username != null && !username.isEmpty())
                this.username = username.toLowerCase();
        }

        @Override
        public void run() {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> list, ParseException e) {
                    if (e == null && list != null && list.size() > 0 && username.equals(editSearch.getText().toString())) {
                        inboxSearchFragment.filtreSearchChange(username, list.get(0));
                    } else {
                        inboxSearchFragment.filtreSearchChange(username, null);
                    }

                    layoutInboxSearchFragment.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchEnabled) {
            showEditTextSearch();
        }
        else
            super.onBackPressed();
    }

    public void hideKeyboard() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }
}
