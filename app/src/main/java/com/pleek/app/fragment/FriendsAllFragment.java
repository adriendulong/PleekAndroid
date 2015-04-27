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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.FriendsActivity;
import com.pleek.app.activity.ParentActivity;
import com.pleek.app.adapter.FriendsAdapter;
import com.pleek.app.bean.Friend;
import com.pleek.app.bean.ViewLoadingFooter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by nicolas on 13/01/15.
 */
public class FriendsAllFragment extends ParentFragment implements FriendsActivity.Listener, FriendsAdapter.Listener
{
    public static final int TYPE_YOU_ADDED = 0;
    public static final int TYPE_ADDED_YOU = 1;

    private int type = 0;

    private StickyListHeadersListView listView;
    private TextView txtNoFriends;
    private ViewLoadingFooter footer;

    private FriendsAdapter adapter;
    private List<Friend> listFriend;

    private String currentFiltreSearch;

    private View header;
    private TextView txtHeader;

    private ParseQuery<ParseObject> innerQuery;

    public static FriendsAllFragment newInstance(int type)
    {
        FriendsAllFragment fragment = new FriendsAllFragment();
        fragment.type = type;
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
        init(false);
    }

    private void setup()
    {
        listView = (StickyListHeadersListView) getView().findViewById(R.id.listView);
        listView.setOnScrollListener(new MyOnScrollListener());
        txtNoFriends = (TextView) getView().findViewById(R.id.txtNoFriends);

        footer = new ViewLoadingFooter(getActivity());
        header = getActivity().getLayoutInflater().inflate(R.layout.header_friend, null);
        txtHeader = (TextView) header.findViewById(R.id.txtHeader);
        txtHeader.setText(type == TYPE_YOU_ADDED ? R.string.friends_you_added : R.string.friends_added_you);
        listView.addHeaderView(header);
    }

    public void init(boolean withCache) {
        isLoading = false;
        endOfLoading = false;
        currentPage = 0;
        lastItemShow = 0;
        listFriend = new ArrayList<Friend>();
        listBeforreRequest = new ArrayList<Friend>();

        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(withCache ? ParseQuery.CachePolicy.CACHE_THEN_NETWORK : ParseQuery.CachePolicy.NETWORK_ONLY);

        if (type == TYPE_YOU_ADDED) {
            innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
            innerQuery.include("friend");
        } else {
            innerQuery.whereEqualTo("friend", ParseUser.getCurrentUser());
            innerQuery.include("user");
        }

        innerQuery.orderByAscending("username");
        innerQuery.setSkip(currentPage * NB_BY_PAGE);
        innerQuery.setLimit(NB_BY_PAGE);
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list != null && list.size() > 0) {
                    for (ParseObject obj : list) {
                        listFriend.add(new Friend((ParseUser) obj.get(type == TYPE_YOU_ADDED ? "friend":"user")));
                    }
                    if (loadNext()) listView.addFooterView(footer);
                    txtNoFriends.setVisibility(View.GONE);
                } else {
                    if (type == TYPE_YOU_ADDED) txtNoFriends.setText(R.string.friends_nofriend);
                    else txtNoFriends.setText(R.string.friends_nofriend_added_you);
                    txtNoFriends.setVisibility(View.VISIBLE);
                }

                adapter = new FriendsAdapter(FriendsAllFragment.this, getActivity());
                listView.setAdapter(adapter);
            }
        });
    }

    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 50;
    private List<Friend> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean loadNext() {
        //si déjà en train de loader, on fait rien
        if (isLoading) return false;

        //il n'y a plus rien a charger
        if (endOfLoading) return false;

        if (listFriend == null || listFriend.isEmpty()) return false;

        // copie de la liste actuel des friends
        listBeforreRequest = new ArrayList<Friend>(listFriend);

        isLoading = true;
        fromCache = true;

        innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        if (type == TYPE_YOU_ADDED) {
            innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
            innerQuery.include("friend");
        } else {
            innerQuery.whereEqualTo("friend", ParseUser.getCurrentUser());
            innerQuery.include("user");
        }
        innerQuery.orderByAscending("username");
        innerQuery.setSkip(currentPage * NB_BY_PAGE);
        innerQuery.setLimit(NB_BY_PAGE);
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && getActivity() != null) {
                    if (currentPage > 0) {
                        listFriend = new ArrayList<Friend>(listBeforreRequest);
                    } else {
                        listFriend = new ArrayList<Friend>();
                    }

                    if (!fromCache) currentPage++;

                    for (ParseObject obj : list) {
                        ParseUser user;
                        if (type == TYPE_YOU_ADDED) {
                            user = (ParseUser) obj.get("friend");
                        } else {
                            user = (ParseUser) obj.get("user");
                        }

                        int image;
                        Set<String> friendsIds = ((ParentActivity) getActivity()).getFriendsPrefs();
                        if (friendsIds!= null && friendsIds.contains(user.getObjectId())) {
                            image = R.drawable.picto_added;
                        } else {
                            image = R.drawable.picto_add_user;
                        }
                        Friend friend = new Friend(null, 0, image);
                        friend.username = user.getString("username");
                        friend.parseId = user.getObjectId();
                        listFriend.add(friend);
                    }

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = list.size() < NB_BY_PAGE;

                    filtreSearchChange(currentFiltreSearch);
                } else if (e != null){
                    L.e(">>>>>>>>>>>>>> ERROR parsequery user - e=" + e.getMessage());
                    e.printStackTrace();
                }

                //si réponse network (2eme reponse)
                if (!fromCache) {
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

            ArrayList<Friend> listFiltred = new ArrayList<Friend>();

            boolean nofriend = listFriend == null || listFriend.size() == 0;
            boolean nofiltre = filtreSearch == null;

            if(!nofiltre) filtreSearch = filtreSearch.toLowerCase();

            if(!nofriend)
            {
                for(Friend friend : listFriend)
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
    public void clickOnName(final Friend friend) {
        adapter.addFriendLoading(friend);

        final FunctionCallback callback = new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    ((ParentActivity) getActivity()).getFriendsBg(new FunctionCallback() {
                        @Override
                        public void done(Object o, ParseException e) {
                            if (friend.image == R.drawable.picto_add_user) {
                                friend.image = R.drawable.picto_added;
                            } else {
                                friend.image = R.drawable.picto_add_user;
                            }

                            adapter.removeFriend(friend);
                            ((FriendsActivity) getActivity()).startAddFriendAnimation();

                            if (type == TYPE_ADDED_YOU) {
                                ((FriendsActivity) getActivity()).initPage2();
                            }

                            ((FriendsActivity) getActivity()).reloadPage3();

                            if (friend.image == R.drawable.picto_added || type == TYPE_ADDED_YOU) {
                                adapter.addPikiUser(friend);
                            }
                        }
                    });
                } else {
                    adapter.removeFriend(friend);
                    Utile.showToast(R.string.pikifriends_action_nok, getActivity());
                }
            }
        };

        if (friend.image == R.drawable.picto_add_user) {
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

    private int lastItemShow;
    private class MyOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //pagination
            final int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                boolean isFiltred = currentFiltreSearch != null && !currentFiltreSearch.trim().isEmpty();

                if (lastItemShow < lastItem || isFiltred) {
                    if (loadNext()) {
                        listView.addFooterView(footer);
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }

    public void reload() {
        adapter.refactorImages(((ParentActivity) getActivity()).getFriendsPrefs());
        adapter.notifyDataSetChanged();
    }
}
