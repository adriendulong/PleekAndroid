package com.pleek.app.fragment;

import android.os.Handler;
import android.view.View;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Piki;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxSearchFragment extends InboxFragment implements PikiAdapter.Listener {

    private String currentFiltreSearch;
    private ParseUser user;

    public static InboxSearchFragment newInstance(int type, View header) {
        InboxSearchFragment fragment = new InboxSearchFragment();
        fragment.type = type;
        fragment.header = header;
        fragment.isHeaderScrollEnabled = false;
        return fragment;
    }

    protected boolean loadNext(ParseUser user, boolean withCache) {
        this.user = user;
        return loadNext(withCache);
    }

    @Override
    protected boolean loadNext(final boolean withCache) {
        setIsHeaderScrollEnabled(false);
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
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Piki");
        query.whereEqualTo("user", user);
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.include("user");
        query.orderByDescending("lastUpdate");
        query.setSkip(currentPage * NB_BY_PAGE);
        query.setLimit(NB_BY_PAGE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null && parseObjects != null) {
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
    }

    public void filtreSearchChange(String filtreSearch, ParseUser parseUser) {
        try {
            currentFiltreSearch = filtreSearch;
            if(lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
            lastQueryRunnable = new QueryRunnable(parseUser);
            handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
        }
        catch (Exception e) {
            L.w(">>>>>> ERROR : filtreSearchChange - e=" + e.getMessage());
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
