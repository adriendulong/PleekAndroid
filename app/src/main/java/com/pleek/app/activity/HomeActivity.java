package com.pleek.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.ButtonRoundedMaterialDesign;
import com.goandup.lib.widget.DownTouchListener;
import com.goandup.lib.widget.ListViewScrollingOff;
import com.goandup.lib.widget.SwipeRefreshLayoutScrollingOff;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.ViewLoadingFooter;
import com.pleek.app.common.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by nicolas on 18/12/14.
 */
public class HomeActivity extends ParentActivity implements PikiAdapter.Listener, View.OnClickListener
{
    public static boolean AUTO_RELOAD;

    private View btnIcon;
    private View btnTitle;
    private View btnTopBar;
    private View btnSettings;
    private ListViewScrollingOff listViewPiki;
    private ImageView btnPlus;
    private SwipeRefreshLayoutScrollingOff refreshSwipe;
    private View layoutTuto;
    private ViewLoadingFooter footer;

    private PikiAdapter adapter;
    private List<Piki> listPiki;
    private List<ParseUser> friends;

    private boolean shouldReinit = false;

    private int initialX;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setup();
        init();
    }

    private void setup()
    {
        btnTopBar = findViewById(R.id.btnTopBar);
        int colorDown = getResources().getColor(R.color.firstColorDark);
        int colorUp = getResources().getColor(R.color.firstColor);
        btnIcon = findViewById(R.id.imgIcon);
        btnIcon.setOnClickListener(this);
        btnTitle = findViewById(R.id.txtTitle);
        btnTitle.setOnClickListener(this);
        btnTopBar.setOnTouchListener(new DownTouchListener(colorDown, colorUp));
        btnTopBar.setOnClickListener(this);
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);
        listViewPiki = (ListViewScrollingOff)findViewById(R.id.listViewPiki);
        listViewPiki.setOnTouchListener(new MyListTouchListener());
        listViewPiki.setOnScrollListener(new MyOnScrollListener());
        refreshSwipe = (SwipeRefreshLayoutScrollingOff) findViewById(R.id.refreshSwipe);
        refreshSwipe.setColorSchemeResources(R.color.secondColor, R.color.firstColor, R.color.secondColor, R.color.firstColor);
        refreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shouldReinit = true;
                init(false);
            }
        });

        btnPlus = (ImageView) findViewById(R.id.btnPlus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CaptureActivity.class));
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        });

        footer = new ViewLoadingFooter(this);
        listViewPiki.addFooterView(footer);
        adapter = new PikiAdapter(new ArrayList<Piki>(), HomeActivity.this);
        //wait listViewPiki is created for get height
        listViewPiki.post(new Runnable()
        {
            @Override
            public void run()
            {
                listViewPiki.setAdapter(adapter);
                adapter.setHeightListView(listViewPiki.getHeight());
            }
        });

        layoutTuto = findViewById(R.id.layoutTuto);
        layoutTuto.setVisibility(View.GONE);
        layoutTuto.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                Utile.fadeOut(layoutTuto, 300);
                return true;
            }
        });
    }

    private void init() {
        init(true);
    }
    private void init(final boolean withCache) {
        currentPage = 0;
        lastItemShow = 0;

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null) return;

        ParseObject userInfos = currentUser.getParseObject("UserInfos");
        if(userInfos != null) mixpanel.getPeople().set("$phone", userInfos.getString("phoneNumber"));
        mixpanel.flush();

        endOfLoading = false;
        refreshSwipe.post(new Runnable() {
            @Override public void run() {
                refreshSwipe.setRefreshing(loadNext(withCache));
            }
        });
    }


    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 25;
    private List<Piki> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean shouldRefreshFriends = true;
    private boolean loadNext(final boolean withCache)
    {
        //si déjà en train de loader, on fait rien
        if(isLoading) return false;

        //il n'y a plus rien a charger
        if(endOfLoading) return false;

        //on récupère le currentUser, si il n'existe pas, on fait rien
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null) return false;

        //copie de la liste actuel des piki
        if (listPiki == null) listPiki = new ArrayList<Piki>();
        if (friends == null) friends = new ArrayList<ParseUser>();
        listBeforreRequest = new ArrayList<Piki>(listPiki);

        //flag pour reconnaitre réponse Parse du cache et réponse Parse network
        fromCache = withCache;

        //mark loading and add footer
        footer.setVisibility(View.VISIBLE);
        isLoading = true;
        Date date = currentUser.getDate("lastFriendsModification");

        if (pref.contains(Constants.PREF_LAST_FRIENDS_UPDATE) && date != null) {
            if (date.getTime() > pref.getLong(Constants.PREF_LAST_FRIENDS_UPDATE, 0)) {
                shouldRefreshFriends = true;
                pref.edit().putLong(Constants.PREF_LAST_FRIENDS_UPDATE, date.getTime()).commit();
            } else shouldRefreshFriends = false;
        } else {
            shouldRefreshFriends = true;
            pref.edit().putLong(Constants.PREF_LAST_FRIENDS_UPDATE, date == null ? System.currentTimeMillis() : date.getTime()).commit();
        }

        shouldRefreshFriends = true;
        // Parse Query Friends
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(shouldRefreshFriends ? ParseQuery.CachePolicy.NETWORK_ONLY : ParseQuery.CachePolicy.CACHE_ONLY);
        innerQuery.whereEqualTo("user", currentUser);
        innerQuery.include("friend");
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    if (friends != null) friends.clear();
                    HashSet<String> friendsIds = new HashSet<String>();

                    for (ParseObject obj : list) {
                        ParseUser user = (ParseUser) obj.get("friend");
                        friends.add(user);
                        friendsIds.add(user.getObjectId());
                    }

                    // mixpanel update nbFriend;
                    mixpanel.getPeople().set("Nb Friends", friendsIds.size());

                    setFriendsPrefs(friendsIds);
                }

                loadPikis(withCache);
            }
        });

        return true;
    }

    private void loadPikis(boolean withCache) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        friends.add(currentUser);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Piki");
        query.whereContainedIn("user", friends);
        if (ParseUser.getCurrentUser().get("pleeksHide") != null) {
            query.whereNotContainedIn("objectId", (ArrayList<String>) ParseUser.getCurrentUser().get("pleeksHide"));
        }
        query.setCachePolicy(withCache ? ParseQuery.CachePolicy.CACHE_THEN_NETWORK : ParseQuery.CachePolicy.NETWORK_ONLY);
        query.include("user");
        query.orderByDescending("lastUpdate");
        query.setSkip(currentPage * NB_BY_PAGE);
        query.setLimit(NB_BY_PAGE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null && parseObjects != null) {
                    if (!fromCache) currentPage++;

                    //copie de la liste d'avant la request
                    if (shouldReinit) {
                        listPiki.clear();
                        shouldReinit = false;
                    } else {
                        listPiki = new ArrayList<Piki>(listBeforreRequest);
                    }

                    for (ParseObject parsePiki : parseObjects) {
                        listPiki.add(new Piki(parsePiki));
                    }
                    adapter.setListPiki(listPiki);

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = parseObjects.size() < NB_BY_PAGE;

                    //affichage du tuto si necessaire
                    showTuto();
                } else if (e != null) {
                    if (!fromCache) Utile.showToast(R.string.home_piki_nok, HomeActivity.this);
                    e.printStackTrace();
                }

                //si réponse network (2eme reponse)
                if (!fromCache) {
                    refreshSwipe.setRefreshing(false);
                    footer.setVisibility(View.GONE);
                    isLoading = false;
                }

                fromCache = false;
            }
        });
    }

    private void showTuto()
    {
        if(layoutTuto.getVisibility() == View.GONE)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if(preferences.getBoolean("first_tuto_home", true))
            {
                Utile.fadeIn(layoutTuto, 300);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("first_tuto_home", false);
                editor.commit();
            }
            else
            {
                layoutTuto.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clickOnPiki(Piki piki)
    {
        PikiActivity.initActivity(piki);
        startActivity(new Intent(this, PikiActivity.class));
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @Override
    public void showPlaceHolderItem(boolean show)
    {
        listViewPiki.setVerticalScrollBarEnabled(!show);
        listViewPiki.setScrollingEnabled(!show);
    }

    @Override
    protected void onResume()
    {
        if(AUTO_RELOAD)
        {
            AUTO_RELOAD = false;
            shouldReinit = true;
            init(false);
        }
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (view == btnTopBar) {
            startActivity(new Intent(this, FriendsActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else if (view == btnSettings) {
            startActivity(new Intent(this, ParameterActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up);
        } else if (view == btnIcon || view == btnTitle) {
            startActivity(new Intent(this, VideoActivity.class));
            overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up);
        }
    }

    private class MyListTouchListener implements View.OnTouchListener
    {
        private View downItem;
        private final int DURATION_DELETE_ANIM = 300;//ms
        private final int WIDTH_BORDER_BACKVIEW = 120;//dp
        private final int WIDTH_BORDER_BACKVIEW_START_ALPHA = WIDTH_BORDER_BACKVIEW * 3/4;//dp

        @Override
        public boolean onTouch(View view, MotionEvent event)
        {
            try
            {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_MOVE && listViewPiki.isScrollingHorizontalLeft())
                {
                    if(downItem == null)
                    {
                        // 1) first scroll horizontal to an item (initialize)

                        initialX = (int) event.getRawX();

                        // Find the child view that was touched (perform a hit test)
                        Rect rect = new Rect();
                        int childCount = listViewPiki.getChildCount();
                        int[] listViewCoords = new int[2];
                        listViewPiki.getLocationOnScreen(listViewCoords);
                        int x = (int) event.getRawX() - listViewCoords[0];
                        int y = (int) event.getRawY() - listViewCoords[1];
                        for (int i = 0; i < childCount; i++)
                        {
                            View child = listViewPiki.getChildAt(i);
                            child.getHitRect(rect);
                            if (rect.contains(x, y))
                            {
                                downItem = child;
                                break;
                            }
                        }

                        //fake view for no loop
                        if(downItem == null) downItem = new View(HomeActivity.this);

                        //initialize backview
                        View backView = downItem.findViewById(R.id.back);
                        if(backView != null)
                        {
                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                            lp.setMargins(0, 0, 0, 0);
                            backView.setLayoutParams(lp);

                            backView.findViewById(R.id.imgDeleteItemOn).setAlpha(0f);
                        }
                    }
                    else
                    {
                        // 2) When scroll horizontal to an item.

                        View frontView = downItem.findViewById(R.id.front);
                        View backView = downItem.findViewById(R.id.back);
                        if(frontView != null && backView != null)
                        {
                            int moveX = initialX - (int)event.getRawX();

                            refreshSwipe.setScrollingEnabled(false);
                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                            lp.setMargins(-moveX,0,moveX,0);
                            frontView.setLayoutParams(lp);

                            backView.setVisibility(moveX < 0 ? View.GONE : View.VISIBLE);

                            //change alpha DELETE image rouge
                            if(moveX > screen.dpToPx(WIDTH_BORDER_BACKVIEW_START_ALPHA))
                            {
                                //calculate actual alpha of imgDeleteItemOn
                                float alpha = (moveX - screen.dpToPx(WIDTH_BORDER_BACKVIEW_START_ALPHA)) / (float)(screen.dpToPx(WIDTH_BORDER_BACKVIEW - WIDTH_BORDER_BACKVIEW_START_ALPHA));
                                backView.findViewById(R.id.imgDeleteItemOn).setAlpha(Math.min(alpha, 1f));
                            }
                            else
                            {
                                backView.findViewById(R.id.imgDeleteItemOn).setAlpha(0f);//sure alpha is 0 if under WIDTH_BORDER_BACKVIEW_START_ALPHA
                            }

                            //move DELETE image
                            if(moveX > screen.dpToPx(WIDTH_BORDER_BACKVIEW))
                            {
                                //move item frontview of position X with margin
                                moveX -= screen.dpToPx(WIDTH_BORDER_BACKVIEW);
                                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                lp.setMargins(-moveX, 0, moveX, 0);
                                backView.setLayoutParams(lp);
                            }
                            else
                            {
                                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                lp.setMargins(0, 0, 0, 0);
                                backView.setLayoutParams(lp);
                            }

                            return true;//consume touch event
                        }
                    }
                }

                if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                {
                    refreshSwipe.setScrollingEnabled(true);

                    if(downItem != null)
                    {
                        //3) return item view to original state
                        View frontView = downItem.findViewById(R.id.front);
                        View backView = downItem.findViewById(R.id.back);
                        if(frontView != null && backView != null)
                        {
                            final int moveX = initialX - (int)event.getRawX();
                            if(moveX > screen.dpToPx(WIDTH_BORDER_BACKVIEW) && action == MotionEvent.ACTION_UP)
                            {
                                ///////////////
                                // POPUP DELETE
                                final int position = (Integer)downItem.getTag();
                                showDialog(R.string.home_popup_remove_title, R.string.home_popup_remove_texte, new MyDialogListener()
                                {
                                    @Override
                                    public void closed(boolean accept)
                                    {
                                        // DELETE
                                        if(accept)
                                        {
                                            if(downItem == null) return;

                                            final int initialHeight = downItem.getMeasuredHeight();//fix crash : #57
                                            animItemDisappear(downItem, moveX, new Animation.AnimationListener()
                                            {
                                                @Override
                                                public void onAnimationEnd(Animation a)
                                                {
                                                    if(downItem != null)
                                                    {
                                                        //reset view for next
                                                        downItem.getLayoutParams().height = initialHeight;//fix : crash #29
                                                        downItem.requestLayout();
                                                        downItem.invalidate();
                                                        View frontView = downItem.findViewById(R.id.front);
                                                        View backView = downItem.findViewById(R.id.back);
                                                        if(frontView != null && backView != null)
                                                        {
                                                            //reset margin
                                                            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                                                            lp.setMargins(0, 0, 0, 0);
                                                            frontView.setLayoutParams(lp);
                                                            lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                                                            lp.setMargins(0, 0, 0, 0);
                                                            backView.setLayoutParams(lp);
                                                        }
                                                    }
                                                    downItem = null;

                                                    Piki removedPiki = adapter.removePiki(position);

                                                    if (!removedPiki.isPublic()) {
                                                        //Parse remove Piki
                                                        HashMap<String, String> params = new HashMap<String, String>();
                                                        params.put("pikiId", removedPiki.getId());
                                                        ParseCloud.callFunctionInBackground("hideOrRemovePikiV2", params, new FunctionCallback<Object>() {
                                                            @Override
                                                            public void done(Object o, ParseException e) {
                                                                if (e != null)
                                                                    Utile.showToast(R.string.home_piki_remove_nok, HomeActivity.this);
                                                                else {
                                                                    shouldReinit = true;
                                                                    init(false);
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        ParseUser user = ParseUser.getCurrentUser();
                                                        user.add("pleeksHide", removedPiki.getId());
                                                        user.saveEventually();
                                                        shouldReinit = true;
                                                        init(false);
                                                    }
                                                }
                                                @Override public void onAnimationRepeat(Animation animation) {}
                                                @Override public void onAnimationStart(Animation animation) {}
                                            });
                                        }
                                        else // NOT DELETE
                                        {
                                            animRestorePositionItem(downItem, moveX);
                                        }
                                    }
                                });
                            }
                            else
                            {
                                animRestorePositionItem(downItem, moveX);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                L.w(">>>>>> ERROR MyListTouchListener - e" + e.getMessage());
            }
            return false;//no consume touch event
        }

        private void animItemDisappear(final View item, int moveX, final Animation.AnimationListener animListener)
        {
            if(downItem == null) return;

            final int initialHeight = item.getMeasuredHeight();

            final View frontView = downItem.findViewById(R.id.front);
            final View backView = downItem.findViewById(R.id.back);
            if(frontView != null && backView != null)
            {
                TranslateAnimation ta = new TranslateAnimation(-moveX, -screen.getWidth(),0,0);
                ta.setDuration(DURATION_DELETE_ANIM);
                ta.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                        p.setMargins(-screen.getWidth(), 0, 0, 0);
                        frontView.setLayoutParams(p);
                        p = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                        p.setMargins(-screen.getWidth(), 0, 0, 0);
                        backView.setLayoutParams(p);

                        Animation anim = new Animation()
                        {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t)
                            {
                                item.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                                item.requestLayout();
                            }

                            @Override
                            public boolean willChangeBounds()
                            {
                                return true;
                            }
                        };
                        anim.setAnimationListener(animListener);
                        anim.setDuration(DURATION_DELETE_ANIM >> 1);
                        item.startAnimation(anim);
                    }
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationStart(Animation animation) {}
                });
                frontView.startAnimation(ta);
                backView.startAnimation(ta);

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                frontView.setLayoutParams(lp);
                lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                backView.setLayoutParams(lp);
            }

        }

        private void animRestorePositionItem(final View item, int moveX)
        {
            if(item == null) return;

            View frontView = item.findViewById(R.id.front);//fix crash : #41
            View backView = item.findViewById(R.id.back);
            if(frontView != null && backView != null)
            {
                TranslateAnimation ta = new TranslateAnimation(-moveX,0,0,0);
                ta.setDuration(DURATION_DELETE_ANIM);
                frontView.startAnimation(ta);

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) frontView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                frontView.setLayoutParams(lp);

                //anim alpha imgDeleteItemOn to origin
                if(moveX > screen.dpToPx(WIDTH_BORDER_BACKVIEW_START_ALPHA))
                {
                    //calculate actual alpha of imgDeleteItemOn
                    float alpha = (moveX - screen.dpToPx(WIDTH_BORDER_BACKVIEW_START_ALPHA)) / (float)(screen.dpToPx(WIDTH_BORDER_BACKVIEW - WIDTH_BORDER_BACKVIEW_START_ALPHA));

                    AlphaAnimation aa = new AlphaAnimation(alpha, 0);
                    aa.setDuration(DURATION_DELETE_ANIM);
                    //aa.setFillAfter(true); /!\ BUG
                    backView.findViewById(R.id.imgDeleteItemOn).startAnimation(aa);
                }

                //anim position backView to origin
                if(moveX > screen.dpToPx(WIDTH_BORDER_BACKVIEW))
                {
                    //start animation
                    moveX -= screen.dpToPx(WIDTH_BORDER_BACKVIEW);
                    ta = new TranslateAnimation(-moveX,0,0,0);
                    ta.setDuration(DURATION_DELETE_ANIM);
                    backView.startAnimation(ta);

                    //reset margin
                    lp = (ViewGroup.MarginLayoutParams) backView.getLayoutParams();
                    lp.setMargins(0, 0, 0, 0);
                    backView.setLayoutParams(lp);
                }
            }

            downItem = null;
        }
    }

    private int lastItemShow;
    private class MyOnScrollListener implements AbsListView.OnScrollListener
    {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            //pagination
            final int lastItem = firstVisibleItem + visibleItemCount;
            if(lastItem == totalItemCount)
            {
                if(lastItemShow < lastItem)
                {
                    if(loadNext(true))
                    {
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        mixpanel.flush();
        super.onDestroy();
    }
}
