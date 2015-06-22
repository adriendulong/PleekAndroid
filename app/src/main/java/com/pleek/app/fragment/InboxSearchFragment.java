package com.pleek.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.InboxActivity;
import com.pleek.app.activity.PikiActivity;
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Piki;
import com.pleek.app.common.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxSearchFragment extends InboxFragment implements PikiAdapter.Listener {

    @InjectView(R.id.layoutNoPleeks)
    ViewGroup layoutNoPleeks;
    @InjectView(R.id.layoutNoUser)
    ViewGroup layoutNoUser;

    private String currentFiltreSearch;
    private ParseUser user;

    public static InboxSearchFragment newInstance(int type, View header) {
        InboxSearchFragment fragment = new InboxSearchFragment();
        fragment.type = type;
        fragment.header = header;
        fragment.isHeaderScrollEnabled = false;
        return fragment;
    }

    @Override
    protected void setup() {
        super.setup();

        listViewPiki.setOnScrollListener(new AbsListView.OnScrollListener() {

            int scrollStateSave;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.scrollStateSave = scrollState;
                onScrollListener.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                if (scrollStateSave == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && view != null && view.getChildAt(0) != null) {
                    int scroll = view.getChildAt(0).getTop();

                    if (scroll < -20 && isAdded()) {
                        ((InboxActivity) getActivity()).hideKeyboard();
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_inbox, null);

        ButterKnife.inject(this, v);

        return v;
    }

    protected boolean loadNext(ParseUser user, boolean withCache) {
        this.user = user;
        return loadNext(withCache);
    }

    @Override
    protected boolean loadNext(final boolean withCache) {
        setIsHeaderScrollEnabled(false);
        refreshSwipe.setEnabled(false);

        if (isLoading) return false;

        // il n'y a plus rien a charger
        if (endOfLoading) return false;

        // on récupère le currentUser, si il n'existe pas, on fait rien
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return false;

        //copie de la liste actuel des piki
        if (listPiki == null) listPiki = new ArrayList<Piki>();
        listBeforreRequest = new ArrayList<Piki>(listPiki);

        //flag pour reconnaitre réponse Parse du cache et réponse Parse network
        fromCache = withCache;

        //mark loading and add footer
        footer.setVisibility(View.VISIBLE);
        isLoading = true;

        loadPikis(withCache);

        return true;
    }

    protected void loadPikis(boolean withCache) {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        ArrayList<String> ids = new ArrayList<String>();
        ids.add(currentUser.getObjectId());

        ParseQuery<ParseObject> queryPublic = ParseQuery.getQuery("Piki");
        queryPublic.whereEqualTo("user", user);
        queryPublic.whereEqualTo("isPublic", true);
        queryPublic.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);

        ParseQuery<ParseObject> queryPrivate = ParseQuery.getQuery("Piki");
        queryPrivate.whereEqualTo("user", user);
        queryPrivate.whereContainsAll("recipients", ids);
        queryPrivate.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(queryPublic);
        queries.add(queryPrivate);

        ParseQuery<ParseObject> queryOr = ParseQuery.or(queries);
        queryOr.orderByDescending("lastUpdate");
        queryOr.include("user");
        queryOr.setSkip(currentPage * NB_BY_PAGE);
        queryOr.setLimit(NB_BY_PAGE);

        queryOr.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null && parseObjects != null) {
                    if (parseObjects.size() == 0) {
                        layoutNoPleeks.setVisibility(View.VISIBLE);
                        layoutNoUser.setVisibility(View.GONE);
                    } else {
                        if (!fromCache) currentPage++;

                        // Copie de la liste d'avant la request
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
                    }

                } else if (e != null) {
                    if (!fromCache) Utile.showToast(R.string.home_piki_nok, getActivity());
                    e.printStackTrace();
                } else if (parseObjects == null || parseObjects.size() == 0 && currentPage == 0) {
                    layoutNoPleeks.setVisibility(View.VISIBLE);
                    layoutNoUser.setVisibility(View.GONE);
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

    @Override
    public void clickOnPiki(Piki piki) {
        PikiActivity.initActivity(piki);
        Intent intent = new Intent(getActivity(), PikiActivity.class);
        intent.putExtra(Constants.EXTRA_FROM_INBOX, InboxActivity.TYPE_SEARCH);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void filtreSearchChange(String filtreSearch, ParseUser parseUser) {
        if (adapter != null) adapter.clear();

        if (parseUser == null) {
            layoutNoUser.setVisibility(View.VISIBLE);
            layoutNoPleeks.setVisibility(View.GONE);
        } else {
            try {
                layoutNoUser.setVisibility(View.GONE);
                layoutNoPleeks.setVisibility(View.GONE);
                currentFiltreSearch = filtreSearch;
                if (lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
                lastQueryRunnable = new QueryRunnable(parseUser);
                handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
            } catch (Exception e) {
                L.w(">>>>>> ERROR : filtreSearchChange - e=" + e.getMessage());
            }
        }
    }

    private static int TIMER_QUERY = 500;//ms
    final Handler handler = new Handler();
    private QueryRunnable lastQueryRunnable;
    class QueryRunnable implements Runnable {
        private ParseUser user;

        public QueryRunnable(ParseUser user) {
            this.user = user;
        }

        @Override
        public void run() {
            if (user == null) {
                return;
            }

            isLoading = false;
            currentPage = 0;
            endOfLoading = false;
            listPiki = null;
            shouldReinit = true;

            loadNext(user, false);
        }
    }
}
