package com.pleek.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.fragment.FriendsAllFragment;
import com.pleek.app.fragment.FriendsFindFragment;

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
    private View tabIndicator;
    private ViewPager viewPager;
    private EditText editSearch;
    private TextView txtTab2Add;

    private FriendsFindFragment page1;
    private FriendsAllFragment page2;

    private FragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        setup();
        init();
    }

    private void setup()
    {
        btnTopBar = findViewById(R.id.btnTopBar);
        int colorDown = getResources().getColor(R.color.firstColorDark);
        int colorUp = getResources().getColor(R.color.firstColor);
        btnTopBar.setOnTouchListener(new DownTouchListener(colorDown, colorUp));
        btnTopBar.setOnClickListener(this);
        btnBack = findViewById(R.id.btnBack);
        colorUp = getResources().getColor(R.color.firstColorLight);
        btnBack.setOnTouchListener(new DownTouchListener(colorDown, colorUp));
        btnBack.setOnClickListener(this);
        btnTab1 = findViewById(R.id.btnTab1);
        btnTab1.setOnClickListener(this);
        btnTab2 = findViewById(R.id.btnTab2);
        btnTab2.setOnClickListener(this);
        txtTab2Add = (TextView) findViewById(R.id.txtTab2Add);
        updateNbFriends();
        editSearch = (EditText)findViewById(R.id.editSearch);
        editSearch.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                String filtreSearch = editable.toString();
                if(filtreSearch.trim().length() == 0) filtreSearch = null;
                page1.filtreSearchChange(filtreSearch);
                page2.filtreSearchChange(filtreSearch);
            }
        });
        tabIndicator = findViewById(R.id.tabIndicator);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            private int width = screen.getWidth();

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if(position == 1) positionOffset = 1;
                tabIndicator.setX((int)(positionOffset * width) >> 1);

                setAlphaTab(btnTab1, 1-positionOffset);
                setAlphaTab(btnTab2, positionOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }

    private void init()
    {
        page1 = FriendsFindFragment.newInstance();
        page2 = FriendsAllFragment.newInstance();
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            @Override
            public int getCount()
            {
                return 2;
            }

            @Override
            public Fragment getItem(int position)
            {
                return position == 0 ? page1 : page2;
            }
        };
        viewPager.setAdapter(pagerAdapter);
    }

    private void updateNbFriends()
    {
        TextView txtLabelTab2Disable = (TextView) ((ViewGroup)btnTab2).getChildAt(0);
        TextView txtLabelTab2Enable = (TextView) ((ViewGroup)btnTab2).getChildAt(1);
        List listFriends = ParseUser.getCurrentUser().getList("usersFriend");
        String textLabelTab2 = (listFriends != null ? listFriends.size() : 0) + " " +  getString(R.string.friends_tab2);
        txtLabelTab2Disable.setText(textLabelTab2);
        txtLabelTab2Enable.setText(textLabelTab2);
        txtTab2Add.setText(textLabelTab2);
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
        else if(view == btnTopBar)
        {
            startActivity(new Intent(this, ParameterActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        }
        else if(view == btnTab1)
        {
            viewPager.setCurrentItem(0, true);
        }
        else if(view == btnTab2)
        {
            viewPager.setCurrentItem(1, true);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    private void setAlphaTab(View tab, float alpha)
    {
        if(tab instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) tab;
            if(viewGroup.getChildCount() >= 2)
            {
                View txtEnable = viewGroup.getChildAt(1);
                txtEnable.setAlpha(alpha);
            }
        }
    }

    private final int DURATION_ANIM = 200;//ms
    public void startAddFriendAnimation()
    {
        updateNbFriends();
        initPage2();

        final TextView txtLabelTab2Disable = (TextView) ((ViewGroup)btnTab2).getChildAt(0);
        final TextView txtLabelTab2Enable = (TextView) ((ViewGroup)btnTab2).getChildAt(1);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.add_friend);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(DURATION_ANIM);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                txtTab2Add.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                Animation animationReverse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.add_friend_reverse);
                animationReverse.setInterpolator(new DecelerateInterpolator());
                animationReverse.setFillAfter(true);
                animationReverse.setDuration(DURATION_ANIM);
                txtTab2Add.startAnimation(animationReverse);
                (new Timer()).schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Utile.fadeIn(txtLabelTab2Disable, DURATION_ANIM >> 1);
                                Utile.fadeIn(txtLabelTab2Enable, DURATION_ANIM >> 1);
                            }
                        });
                    }
                }, DURATION_ANIM >> 1);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        txtTab2Add.startAnimation(animation);
        Utile.fadeOut(txtLabelTab2Disable, DURATION_ANIM >> 1);
        Utile.fadeOut(txtLabelTab2Enable, DURATION_ANIM >> 1);
    }

    public void initPage2()
    {
        page2.init();
    }

    public interface Listener
    {
        public void filtreSearchChange(String filtreSearch);
    }
}
