package com.pleek.app.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pleek.app.R;
import com.pleek.app.bean.Friend;
import com.pleek.app.fragment.FriendsAllFragment;
import com.pleek.app.fragment.FriendsFindFragment;
import com.pleek.app.fragment.InboxFragment;
import com.viewpagerindicator.UnderlinePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxActivity extends ParentActivity implements View.OnClickListener {

    @InjectView(R.id.toolbar)
    private Toolbar toolbar;
    @InjectView(R.id.imgSettings)
    private View btnSettings;
    @InjectView(R.id.imgFriends)
    private View btnFriends;
    @InjectView(R.id.imgLogo)
    private View btnLogo;
    @InjectView(R.id.btnTab1)
    private View btnTab1;
    @InjectView(R.id.btnTab2)
    private View btnTab2;
    @InjectView(R.id.btnTab3)
    private View btnTab3;
    @InjectView(R.id.tabIndicator)
    private UnderlinePageIndicator tabIndicator;
    @InjectView(R.id.viewPager)
    private ViewPager viewPager;
    @InjectView(R.id.imgInboxSel)
    private ImageView imgInboxSel;
    @InjectView(R.id.imgSentSel)
    private ImageView imgSentSel;
    @InjectView(R.id.imgBestSel)
    private ImageView imgBestSel;

    private InboxFragment page1;
    private InboxFragment page2;
    private InboxFragment page3;

    private FragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        ButterKnife.inject(this);

        btnSettings.setOnClickListener(this);
        btnFriends.setOnClickListener(this);

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
            public void onPageScrollStateChanged(int state) {
                
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnSettings) {

        } else if (v == btnFriends) {

        }
    }

    private void setAlphaTab(View tab, float alpha) {
        if (tab == btnTab1) {
            imgInboxSel.setAlpha(alpha);
        } else if (tab == btnTab2) {
            imgSentSel.setAlpha(alpha);
        } else if (tab == btnTab3) {
            imgBestSel.setAlpha(alpha);
        }
    }
}
