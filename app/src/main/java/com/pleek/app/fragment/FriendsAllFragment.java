package com.pleek.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.FriendsActivity;
import com.pleek.app.adapter.FriendsAdapter;
import com.pleek.app.bean.ViewLoadingFooter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by nicolas on 13/01/15.
 */
public class FriendsAllFragment extends ParentFragment implements FriendsActivity.Listener, FriendsAdapter.Listener
{
    private StickyListHeadersListView listView;
    private TextView txtNoFriends;
    private ViewLoadingFooter footer;

    private FriendsAdapter adapter;
    private List<FriendsAdapter.Friend> listFriend;

    private String currentFiltreSearch;

    public static FriendsAllFragment newInstance()
    {
        FriendsAllFragment fragment = new FriendsAllFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_friends_all, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        setup();
        init();
    }

    private void setup()
    {
        listView = (StickyListHeadersListView)getView().findViewById(R.id.listView);
        listView.setOnScrollListener(new MyOnScrollListener());
        txtNoFriends = (TextView) getView().findViewById(R.id.txtNoFriends);

        footer = new ViewLoadingFooter(getActivity());
    }

    public void init()
    {
        List<String> listFriends = ParseUser.getCurrentUser().getList("usersFriend");

        if(listFriends != null && listFriends.size() > 0)
        {
            if(loadNext()) listView.addFooterView(footer);
            txtNoFriends.setVisibility(View.GONE);
        }
        else
        {
            txtNoFriends.setVisibility(View.VISIBLE);
        }

        adapter = new FriendsAdapter(FriendsAllFragment.this, getActivity());
        listView.setAdapter(adapter);
    }

    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 50;
    private List<FriendsAdapter.Friend> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean loadNext()
    {
        //si déjà en train de loader, on fait rien
        if(isLoading) return false;

        //il n'y a plus rien a charger
        if(endOfLoading) return false;

        final List<String> listFriends = ParseUser.getCurrentUser().getList("usersFriend");
        if(listFriends == null || listFriends.isEmpty()) return false;

        //copie de la liste actuel des piki
        if(listFriend == null) listFriend = new ArrayList<FriendsAdapter.Friend>();
        listBeforreRequest = new ArrayList<FriendsAdapter.Friend>(listFriend);

        ParseUser parseUser = ParseUser.getCurrentUser();
        final List<String> usersIMuted = parseUser.getList("usersIMuted");

        isLoading = true;
        fromCache = true;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereContainedIn("objectId", listFriends); //TODO : CRASH #9
        query.orderByAscending("username");
        query.setSkip(currentPage * NB_BY_PAGE);
        query.setLimit(NB_BY_PAGE);
        query.findInBackground(new FindCallback<ParseUser>()
        {
            @Override
            public void done(List<ParseUser> parseObjects, ParseException e)
            {
                if (e == null)
                {
                    if(!fromCache) currentPage++;

                    //copie de la liste d'avant la request
                    listFriend = new ArrayList<FriendsAdapter.Friend>(listBeforreRequest);
                    for (ParseUser user : parseObjects)
                    {
                        int image = usersIMuted != null && usersIMuted.contains(user.getObjectId()) ? R.drawable.picto_mute_user_on : R.drawable.picto_mute_user;
                        FriendsAdapter.Friend friend = adapter.new Friend(null, R.string.friends_section, image);
                        friend.username = user.getString("username");
                        friend.parseId = user.getObjectId();
                        listFriend.add(friend);
                    }

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = parseObjects.size() < NB_BY_PAGE;

                    filtreSearchChange(currentFiltreSearch);
                }
                else
                {
                    L.e(">>>>>>>>>>>>>> ERROR parsequery user - e=" + e.getMessage());
                    e.printStackTrace();
                }

                //si réponse network (2eme reponse)
                if(!fromCache)
                {
                    listView.removeFooterView(footer);//fix : crash #30
                    isLoading = false;
                }

                fromCache = false;
            }
        });

        return true;
    }

    @Override
    public void filtreSearchChange(String filtreSearch)
    {
        try//fix : crash #40
        {
            currentFiltreSearch = filtreSearch;

            ArrayList<FriendsAdapter.Friend> listFiltred = new ArrayList<FriendsAdapter.Friend>();

            boolean nofriend = listFriend == null || listFriend.size() == 0;
            boolean nofiltre = filtreSearch == null;

            if(!nofiltre) filtreSearch = filtreSearch.toLowerCase();

            if(!nofriend)
            {
                for(FriendsAdapter.Friend friend : listFriend)
                {
                    if(filtreSearch == null
                            || (friend.name != null && friend.name.toLowerCase().contains(filtreSearch))
                            || (friend.username != null && friend.username.toLowerCase().contains(filtreSearch)))
                    {
                        listFiltred.add(friend);
                    }
                }
            }

            boolean noresult = listFiltred.size() == 0;

            txtNoFriends.setVisibility(noresult ? View.VISIBLE : View.GONE);
            txtNoFriends.setText(nofriend && nofiltre ? R.string.friends_nofriend : R.string.friends_noresult);

            adapter.setListFriend(listFiltred);
            adapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            L.w(">>>>>> ERROR : filtreSearchChange - e="+e.getMessage());
        }
    }

    @Override
    public void clickOnName(final FriendsAdapter.Friend friend)
    {
        if(friend.sectionLabel == R.string.friends_section)//mute
        {
            adapter.addFriendLoading(friend);

            FunctionCallback callback = new FunctionCallback<Object>()
            {
                @Override
                public void done(Object o, ParseException e)
                {
                    if(e == null)
                    {
                        if(friend.image == R.drawable.picto_adduser) friend.image = R.drawable.picto_mute_user;
                        else if(friend.image == R.drawable.picto_mute_user) friend.image = R.drawable.picto_mute_user_on;
                        else if(friend.image == R.drawable.picto_mute_user_on) friend.image = R.drawable.picto_mute_user;

                        adapter.removeFriendLoading(friend);

                        ParseUser.getCurrentUser().fetchInBackground();
                    }
                    else
                    {
                        adapter.removeFriendLoading(friend);
                        Utile.showToast(R.string.pikifriends_action_nok, getActivity());
                    }
                }
            };

            ParseUser currentUser = ParseUser.getCurrentUser();
            List<String> usersIMuted = currentUser.getList("usersIMuted");
            boolean alreadyMuted = usersIMuted != null && usersIMuted.contains(friend.parseId);

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("friendId", friend.parseId);
            ParseCloud.callFunctionInBackground(alreadyMuted ? "unMuteFriend" : "muteFriend", param, callback);
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
                boolean isFiltred = currentFiltreSearch != null && !currentFiltreSearch.trim().isEmpty();

                if(lastItemShow < lastItem || isFiltred)
                {
                    if(loadNext())
                    {
                        listView.addFooterView(footer);
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }
}
