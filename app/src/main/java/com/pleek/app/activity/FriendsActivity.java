package com.pleek.app.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.bean.Friend;
import com.pleek.app.fragment.FriendsAllFragment;
import com.pleek.app.fragment.FriendsFindFragment;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nicolas on 18/12/14.
 */
public class FriendsActivity extends ParentActivity implements View.OnClickListener
{
    private View btnTopBar;
    private View btnBack;
    private View btnTab1;
    private View btnTab2;
    private View btnTab3;
    private UnderlinePageIndicator tabIndicator;
    private ViewPager viewPager;
    private EditText editSearch;
    private TextView txtTab2Add;
    private ImageView btnSearch;
    private TextView txtTitle;

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
//        editSearch.addTextChangedListener(new TextWatcher()
//        {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable editable)
//            {
//                String filtreSearch = editable.toString();
//                if(filtreSearch.trim().length() == 0) filtreSearch = null;
//                page1.filtreSearchChange(filtreSearch);
//            }
//        });
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

    private void updateNbYouAdded(int i) {
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
        page2.init(true);
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
    private void showEditTextSearch(final boolean show)
    {
        if(isAnimating || isEditTextShow == show) return;

        isAnimating = true;
        isEditTextShow = show;
        if(show)
        {
            Utile.fadeOut(btnBack, TIME_ANIM);
            Utile.fadeOut(txtTitle, TIME_ANIM);
        }
        else
        {
            Utile.fadeIn(btnBack, TIME_ANIM);
            Utile.fadeIn(txtTitle, TIME_ANIM);
        }
//        ResizeWidthAnimation anim = new ResizeWidthAnimation(marginLeftBtnSearch, show ? 0 : initialWidthMarginLeftBtnSearch);
//        anim.setDuration(TIME_ANIM);
//        marginLeftBtnSearch.startAnimation(anim);
//        RotateAnimation rotateAnim = new RotateAnimation(show ? 0 : 45, show ? 45 : 0, btnTopBar.getWidth() >> 1, btnTopBar.getHeight() >> 1);
//        rotateAnim.setDuration(TIME_ANIM);
//        rotateAnim.setFillAfter(true);
//        imgBnTopBar.startAnimation(rotateAnim);
//        rotateAnim.setAnimationListener(new Animation.AnimationListener()
//        {
//            @Override
//            public void onAnimationStart(Animation animation)
//            {
//                if(!show)
//                {
//                    editSearch.setVisibility(View.GONE);
//                }
//            }
//            @Override
//            public void onAnimationRepeat(Animation animation) {}
//            @Override
//            public void onAnimationEnd(Animation animation)
//            {
//                if(show)
//                {
//                    Utile.fadeIn(editSearch, TIME_ANIM >> 2);
//                    editSearch.requestFocus();
//                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editSearch, 0);
//                }
//                else
//                {
//                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
//                }
//                isAnimating = false;
//            }
//        });
    }
}
