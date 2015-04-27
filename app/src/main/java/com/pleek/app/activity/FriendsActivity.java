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
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.bean.Friend;
import com.pleek.app.fragment.FriendsAllFragment;
import com.pleek.app.fragment.FriendsFindFragment;
import com.pleek.app.utils.StringUtils;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicolas on 18/12/14.
 */
public class FriendsActivity extends ParentActivity implements View.OnClickListener {
    private View btnTopBar;
    private View btnBack;
    private View btnTab1;
    private View btnTab2;
    private View btnTab3;
    private UnderlinePageIndicator tabIndicator;
    private ViewPager viewPager;
    private EditText editSearch;
    private TextView txtArobas;
    private TextView txtTab2Add;
    private ImageView btnSearch;
    private ImageView btnClose;
    private TextView txtTitle;
    private RelativeLayout searchOverlay;
    private TextView txtName;
    private TextView txtUsername;
    private ImageView imgAction;
    private ProgressBar progressBar;
    private RelativeLayout searchLayout;
    private Friend friend;
    private int nbFriends;

    private TextView txtFindEnabled;
    private TextView txtNbFriend2Disabled;
    private TextView txtNbFriend2Enabled;
    private TextView txtAdded2Enabled;
    private TextView txtNbFriend3Disabled;
    private TextView txtNbFriend3Enabled;
    private TextView txtAdded3Enabled;

    private FriendsFindFragment page1;
    private FriendsAllFragment page2;
    private FriendsAllFragment page3;
    private FriendsFindFragment searchFragment;

    private FragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setup();
        init();
    }

    private void setup() {
        int colorDown = getResources().getColor(R.color.firstColorDark);
        int colorUp = getResources().getColor(R.color.firstColor);
        btnBack = findViewById(R.id.btnBack);
        colorUp = getResources().getColor(R.color.firstColorLight);
        btnBack.setOnTouchListener(new DownTouchListener(colorDown, colorUp));
        btnBack.setOnClickListener(this);
        btnTab1 = findViewById(R.id.btnTab1);
        btnTab1.setOnClickListener(this);
        btnTab2 = findViewById(R.id.btnTab2);
        btnTab2.setOnClickListener(this);
        btnTab3 = findViewById(R.id.btnTab3);
        btnTab3.setOnClickListener(this);
        txtTab2Add = (TextView) findViewById(R.id.txtTab2Add);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnSearch = (ImageView) findViewById(R.id.btnSearch);
        btnSearch.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnSearch.setOnClickListener(this);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark)));
        btnClose.setOnClickListener(this);
        searchOverlay = (RelativeLayout) findViewById(R.id.searchOverlay);
        searchLayout = (RelativeLayout) findViewById(R.id.searchLayout);
        searchLayout.setOnClickListener(this);
        txtUsername = (TextView) findViewById(R.id.txtUserName);
        txtName = (TextView) findViewById(R.id.txtName);
        imgAction = (ImageView) findViewById(R.id.imgAction);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtArobas = (TextView) findViewById(R.id.arobas);

        txtFindEnabled = (TextView) findViewById(R.id.txtFindEnabled);
        txtNbFriend2Disabled = (TextView) findViewById(R.id.txtNbFriend2Disabled);
        txtNbFriend2Enabled = (TextView) findViewById(R.id.txtNbFriend2Enabled);
        txtAdded2Enabled = (TextView) findViewById(R.id.txtAdded2Enabled);
        txtNbFriend3Disabled = (TextView) findViewById(R.id.txtNbFriend3Disabled);
        txtNbFriend3Enabled = (TextView) findViewById(R.id.txtNbFriend3Enabled);
        txtAdded3Enabled = (TextView) findViewById(R.id.txtAdded3Enabled);

        // My Friends
        ParseQuery<ParseObject> youAddedQuery = ParseQuery.getQuery("Friend");
        youAddedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        youAddedQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        youAddedQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    updateNbYouAdded(list.size());
                }
            }
        });

        ParseQuery<ParseObject> addedYouQuery = ParseQuery.getQuery("Friend");
        addedYouQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        addedYouQuery.whereEqualTo("friend", ParseUser.getCurrentUser());
        addedYouQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    updateNbAddedYou(list.size());
                }
            }
        });

        editSearch = (EditText)findViewById(R.id.editSearch);
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    editSearch.requestFocus();
                }

                return true;
            }
        });
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                launchSearch();
            }
        });
        tabIndicator = (UnderlinePageIndicator) findViewById(R.id.tabIndicator);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    setAlphaTab(btnTab1, 1 - positionOffset);
                    setAlphaTab(btnTab2, positionOffset);
                } else if (position == 1) {
                    setAlphaTab(btnTab2, 1 - positionOffset);
                    setAlphaTab(btnTab3, positionOffset);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }

    private void init()
    {
        page1 = FriendsFindFragment.newInstance();
        page2 = FriendsAllFragment.newInstance(FriendsAllFragment.TYPE_YOU_ADDED);
        page3 = FriendsAllFragment.newInstance(FriendsAllFragment.TYPE_ADDED_YOU);
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            @Override
            public int getCount()
            {
                return 3;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0 : return page1;
                    case 1 : return page2;
                    case 2 : return page3;
                }

                return null;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabIndicator.setViewPager(viewPager);
        tabIndicator.setFades(false);
    }

    private void launchSearch() {
        String filtreSearch = editSearch.getText().toString();

        if (filtreSearch != null && filtreSearch.length() >= 3) {
            if (lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
            lastQueryRunnable = new QueryRunnable(filtreSearch);
            handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
            searchLayout.setVisibility(View.VISIBLE);
            searchOverlay.setBackgroundResource(R.color.blanc);
            txtName.setText("@" + filtreSearch);
            txtUsername.setText("");
            progressBar.setVisibility(View.VISIBLE);
            imgAction.setVisibility(View.GONE);
        } else {
            txtUsername.setText("");
            txtName.setText("");
            imgAction.setImageDrawable(null);
            searchLayout.setVisibility(View.GONE);
            searchOverlay.setBackgroundResource(R.color.whiteOverlay);
            progressBar.setVisibility(View.INVISIBLE);
            imgAction.setVisibility(View.GONE);
        }
    }

    private void updateNbYouAdded(int i) {
        nbFriends = i;
        String textLabelTab2 = "" + i;
        txtNbFriend2Disabled.setText(textLabelTab2);
        txtNbFriend2Enabled.setText(textLabelTab2);
        txtTab2Add.setText(textLabelTab2);
    }

    private void updateNbAddedYou(int i) {
        String textLabelTab3 = "" + i;
        txtNbFriend3Disabled.setText(textLabelTab3);
        txtNbFriend3Enabled.setText(textLabelTab3);
    }

    @Override
    public void onClick(View view) {
        if (view == btnBack) {
            finish();
        } else if (view == btnTab1) {
            viewPager.setCurrentItem(0, true);
        } else if (view == btnTab2) {
            viewPager.setCurrentItem(1, true);
        } else if (view == btnTab3){
            viewPager.setCurrentItem(2, true);
        } else if (view == btnSearch) {
            showEditTextSearch(true);
        } else if (view == btnClose) {
            showEditTextSearch(false);
        } else if (view == searchLayout && friend != null) {
            progressBar.setVisibility(View.VISIBLE);
            Set<String> friendsIds = getFriendsPrefs();

            final FunctionCallback callback = new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    if (e == null) {
                        getFriendsBg(new FunctionCallback() {
                            @Override
                            public void done(Object o, ParseException e) {
                                if (getFriendsPrefs().contains(friend.parseId)) {
                                    updateNbYouAdded(nbFriends + 1);
                                    imgAction.setImageResource(R.drawable.picto_added);
                                } else {
                                    updateNbYouAdded(nbFriends - 1);
                                    imgAction.setImageResource(R.drawable.picto_add_user);
                                }

                                progressBar.setVisibility(View.INVISIBLE);
                                initPage2();
                                reloadPage3();
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Utile.showToast(R.string.pikifriends_action_nok, FriendsActivity.this);
                    }
                }
            };

            if (!friendsIds.contains(friend.parseId)) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("friendId", friend.parseId);
                ParseCloud.callFunctionInBackground("addFriendV2", param, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        callback.done(o, e);
                    }
                });
            } else {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("friendId", friend.parseId);
                ParseCloud.callFunctionInBackground("removeFriendV2", param, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, ParseException e) {
                        callback.done(o, e);
                    }
                });
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    private void setAlphaTab(View tab, float alpha) {
        if (tab == btnTab1) {
            txtFindEnabled.setAlpha(alpha);
        } else if (tab == btnTab2) {
            txtNbFriend2Enabled.setAlpha(alpha);
            txtAdded2Enabled.setAlpha(alpha);
        } else if (tab == btnTab3) {
            txtNbFriend3Enabled.setAlpha(alpha);
            txtAdded3Enabled.setAlpha(alpha);
        }
    }

    private final int DURATION_ANIM = 200;//ms
    public void startAddFriendAnimation() {
        getFriends(false, new FunctionCallback<ArrayList<Friend>>() {
            @Override
            public void done(ArrayList<Friend> friends, ParseException e) {
                updateNbYouAdded(friends != null ? friends.size() : 0);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.add_friend);
                animation.setInterpolator(new AccelerateInterpolator());
                animation.setDuration(DURATION_ANIM);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        txtTab2Add.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation animationReverse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.add_friend_reverse);
                        animationReverse.setInterpolator(new DecelerateInterpolator());
                        animationReverse.setFillAfter(true);
                        animationReverse.setDuration(DURATION_ANIM);
                        txtTab2Add.startAnimation(animationReverse);
                        (new Timer()).schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utile.fadeIn(txtNbFriend2Disabled, DURATION_ANIM >> 1);
                                        Utile.fadeIn(txtNbFriend2Enabled, DURATION_ANIM >> 1);
                                    }
                                });
                            }
                        }, DURATION_ANIM >> 1);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                txtTab2Add.startAnimation(animation);
                Utile.fadeOutWithoutGone(txtNbFriend2Disabled, DURATION_ANIM >> 1, null);
                Utile.fadeOutWithoutGone(txtNbFriend2Enabled, DURATION_ANIM >> 1, null);
            }
        });
    }

    public void initPage2() {
        page2.init(false);
    }

    public void reloadPage2() {
        page2.reload();
    }

    public void reloadPage3() {
        page3.reload();
    }

    public interface Listener {
        public void filtreSearchChange(String filtreSearch);
    }

    private final int TIME_ANIM = 250;//ms
    private boolean isAnimating;
    private boolean isEditTextShow;
    private void showEditTextSearch(final boolean show) {
        if (isAnimating || isEditTextShow == show) return;

        isAnimating = true;
        isEditTextShow = show;
        if (show) {
            Utile.fadeOut(btnBack, TIME_ANIM);
            Utile.fadeOut(txtTitle, TIME_ANIM);
        } else {
            editSearch.setText("");
            Utile.fadeIn(btnBack, TIME_ANIM);
            Utile.fadeIn(txtTitle, TIME_ANIM);
            Utile.fadeOut(btnClose, TIME_ANIM);
            Utile.fadeOut(searchOverlay, TIME_ANIM);
        }

        ObjectAnimator objectAnimator;

        if (show) {
            objectAnimator = ObjectAnimator.ofFloat(btnSearch, "translationX", 0, -screen.getWidth() + btnSearch.getWidth());
        } else {
            objectAnimator = ObjectAnimator.ofFloat(btnSearch, "translationX", -screen.getWidth() + btnSearch.getWidth(), 0);
        }
        objectAnimator.setDuration(TIME_ANIM);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!show) {
                    editSearch.setVisibility(View.GONE);
                    txtArobas.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (show) {
                    Utile.fadeIn(txtArobas, TIME_ANIM >> 2);
                    Utile.fadeIn(editSearch, TIME_ANIM >> 2);
                    Utile.fadeIn(btnClose, TIME_ANIM >> 2);
                    Utile.fadeIn(searchOverlay, TIME_ANIM >> 2);
                    editSearch.requestFocus();
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editSearch, 0);
                } else {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                }

                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        objectAnimator.start();
    }

    private void eraseSearchResults() {
        txtUsername.setText("");
        txtName.setText("");
        imgAction.setImageDrawable(null);
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
                    progressBar.setVisibility(View.INVISIBLE);

                    if (e == null && username.equals(editSearch.getText().toString())) {
                        if (list.size() > 0) {
                            friend = new Friend(list.get(0));

                            if (!StringUtils.isStringEmpty(friend.name)) {
                                txtUsername.setVisibility(View.VISIBLE);
                                txtName.setText(friend.name);
                                txtUsername.setText("@" + friend.username);
                            } else {
                                txtName.setText("@" + friend.username);
                                txtUsername.setVisibility(View.GONE);
                            }

                            if (getFriendsPrefs().contains(friend.parseId)) {
                                imgAction.setImageResource(R.drawable.picto_added);
                            } else {
                                imgAction.setImageResource(R.drawable.picto_add_user);
                            }

                            imgAction.setVisibility(View.VISIBLE);
                        }
//                        else {
//                            eraseSearchResults();
//                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (isEditTextShow) {
            showEditTextSearch(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateNbYouAdded(getFriendsPrefs().size());
        initPage2();
        reloadPage3();

        super.onActivityResult(requestCode, resultCode, data);
    }
}
