package com.pleek.app.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
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
import com.pleek.app.adapter.PikiAdapter;
import com.pleek.app.bean.Friend;
import com.pleek.app.bean.Piki;
import com.pleek.app.common.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by tiago on 11/05/2015.
 */
public class InboxSearchFragment extends InboxFragment implements PikiAdapter.Listener {

    private String currentFiltreSearch;

    public static InboxSearchFragment newInstance(int type, View header) {
        InboxSearchFragment fragment = new InboxSearchFragment();
        fragment.type = type;
        fragment.header = header;
        return fragment;
    }

    @Override
    protected boolean loadNext(final boolean withCache) {
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

    }

    public void filtreSearchChange(String filtreSearch) {
        try {
            currentFiltreSearch = filtreSearch;
            if(lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
            lastQueryRunnable = new QueryRunnable(filtreSearch);
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
        private String username;

        public QueryRunnable(String username) {
            this.username = username;
        }

        @Override
        public void run() {
            if (username == null) {
                return;
            }

            loadNext(false);
        }
    }
}
