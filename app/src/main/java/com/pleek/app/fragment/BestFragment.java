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

import com.goandup.lib.utile.Screen;
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
import com.pleek.app.adapter.BestPikiAdapter;
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Piki;
import com.pleek.app.common.Constants;
import com.pleek.app.interfaces.QuickReturnListViewOnScrollListener;

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
public class BestFragment extends PikiFragment implements BestPikiAdapter.Listener {

    private BestPikiAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_best, null);

        ButterKnife.inject(this, v);

        return v;
    }

    public static BestFragment newInstance(int type, View header, View viewNew) {
        BestFragment fragment = new BestFragment();
        fragment.type = type;
        fragment.header = header;
        return fragment;
    }

    @Override
    protected void setup() {
        super.setup();
        adapter = new BestPikiAdapter(new ArrayList<Piki>(), this, getActivity());
        listViewPiki.post(new Runnable() {
            @Override
            public void run() {
                listViewPiki.setAdapter(adapter);
                adapter.setHeightListView(listViewPiki.getHeight());
            }
        });
    }

    protected boolean loadNext(final boolean withCache) {
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

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Best");
        query.setCachePolicy(withCache ? ParseQuery.CachePolicy.CACHE_THEN_NETWORK : ParseQuery.CachePolicy.NETWORK_ONLY);
        query.include("pleek.user");
        query.orderByDescending("updatedAt");
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

                    for (ParseObject parseBest : parseObjects) {
                        listPiki.add(new Piki(parseBest.getParseObject("pleek")));
                    }

                    adapter.setListPiki(listPiki);

                    if (!fromCache && currentPage == 1 && parseObjects.size() > 0) {
                        if (pref.contains(prefsKey)) {
                            long timeStamp = pref.getLong(prefsKey, 0L);
                            if (timeStamp != listPiki.get(0).getUpdatedAt().getTime()) {
                                if (onNewContentListener != null) onNewContentListener.onNewContent(type, true);
                                pref.edit().putLong(prefsKey, listPiki.get(0).getUpdatedAt().getTime()).commit();
                            }
                        } else {
                            if (onNewContentListener != null) onNewContentListener.onNewContent(type, true);
                            pref.edit().putLong(prefsKey, listPiki.get(0).getUpdatedAt().getTime()).commit();
                        }
                    }

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = parseObjects.size() < NB_BY_PAGE;
                } else if (e != null) {
                    if (!fromCache) Utile.showToast(R.string.home_piki_nok, getActivity());
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

        return true;
    }

    @Override
    protected void loadPikis(boolean withCache) {

    }

    @Override
    public void clickOnPiki(Piki piki) {
        PikiActivity.initActivity(piki);
        Intent intent = new Intent(getActivity(), PikiActivity.class);
        intent.putExtra(Constants.EXTRA_FROM_INBOX, type);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @Override
    public void showPlaceHolderItem(boolean show) {
        listViewPiki.setVerticalScrollBarEnabled(!show);
        listViewPiki.setScrollingEnabled(!show);
    }

    @Override
    public void adjustScroll(int scrollHeight) {
        if (listViewPiki.getFirstVisiblePosition() >= 1) {
            return;
        }

        listViewPiki.setSelectionFromTop(0, (int) header.getTranslationY());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
