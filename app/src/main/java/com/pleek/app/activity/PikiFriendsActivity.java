package com.pleek.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.adapter.FriendsAdapter;
import com.pleek.app.bean.Friend;
import com.pleek.app.bean.Piki;
import com.pleek.app.bean.ViewLoadingFooter;
import com.pleek.app.utils.PicassoUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicolas on 18/12/14.
 */
public class PikiFriendsActivity extends ParentActivity implements View.OnClickListener, FriendsAdapter.Listener
{
    private View btnBack;
    private ImageView imgPiki;
    private TextView txtNbFriend;
    private ListView listView;
    private ViewLoadingFooter footer;

    private static Piki _piki;
    private Piki piki;
    private FriendsAdapter adapter;
    private List<Friend> listFriend;

    public static void initActivity(Piki piki) {
        _piki = piki;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pikifriends);

        setup();
        init();
    }

    private void setup()
    {
        if(_piki != null)
        {
            piki = _piki;
            _piki = null;
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        imgPiki = (ImageView) findViewById(R.id.imgPiki);
        txtNbFriend = (TextView) findViewById(R.id.txtNbFriend);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnScrollListener(new MyOnScrollListener());

        footer = new ViewLoadingFooter(this);
    }

    private void init()
    {
        PicassoUtils.with(this).load(piki.getUrlPiki()).into(imgPiki);

        loadNext();
        listView.addFooterView(footer);

        adapter = new FriendsAdapter(this);//fix : CRASH #8
        listView.setAdapter(adapter);

        NumberFormat nf_ge = NumberFormat.getInstance(Locale.getDefault());
        String txtTopbar = getString(R.string.pikifriends_text_topbar);
        txtTopbar = txtTopbar.replace("_nb_", nf_ge.format(piki.getNbRecipient()));
        if(piki.getNbRecipient() > 1) txtTopbar += "s";
        txtNbFriend.setText(txtTopbar);
    }

    private boolean fromCache;
    private int currentPage;
    private final int NB_BY_PAGE = 50;
    private List<Friend> listBeforreRequest;
    private boolean isLoading;
    private boolean endOfLoading;
    private boolean loadNext()
    {
        //si déjà en train de loader, on fait rien
        if(isLoading) return false;

        //il n'y a plus rien a charger
        if(endOfLoading) return false;

        //copie de la liste actuel des piki
        if(listFriend == null) listFriend = new ArrayList<Friend>();
        listBeforreRequest = new ArrayList<Friend>(listFriend);

        final Set<String> usersFriend = getFriendsPrefs();
        List<String> friendsId = piki.getFrinedsId();

        isLoading = true;
        fromCache = true;
        ParseQuery query = ParseUser.getQuery();
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereContainedIn("objectId", friendsId);
        query.orderByAscending("username");
        query.setSkip(currentPage * NB_BY_PAGE);
        query.setLimit(NB_BY_PAGE);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if(e == null) {
                    if(!fromCache) currentPage++;

                    //copie de la liste d'avant la request
                    listFriend = new ArrayList<Friend>(listBeforreRequest);
                    for(ParseUser user : list) {
                        int image = -1;
                        if(usersFriend != null && usersFriend.contains(user.getObjectId())) {
                            image = R.drawable.picto_added;
                        } else {
                            image = R.drawable.picto_add_user;
                        }
                        Friend friend = new Friend((String)null, image);
                        friend.username = user.getUsername();
                        friend.parseId = user.getObjectId();
                        listFriend.add(friend);
                    }

                    //si moins de résultat que d'el par page alors c'est la dernière page
                    endOfLoading = list.size() < NB_BY_PAGE;

                    adapter.setListFriend(listFriend);
                    adapter.notifyDataSetChanged();
                }

                //si réponse network (2eme reponse)
                if(!fromCache) {
                    listView.removeFooterView(footer);
                    isLoading = false;
                }

                fromCache = false;
            }
        });

        return true;
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnBack)
        {
            finish();
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    @Override
    public void clickOnName(final Friend friend)
    {
        adapter.addFriendLoading(friend);

        final FunctionCallback callback = new FunctionCallback<Object>()
        {
            @Override
            public void done(Object o, ParseException e)
            {
                if (e == null)
                {
                    getFriendsBg(new FunctionCallback() {
                        @Override
                        public void done(Object o, ParseException e) {
                            adapter.removeFriendLoading(friend);

                            if (friend.image == R.drawable.picto_add_user) {
                                friend.image = R.drawable.picto_added;
                            } else {
                                friend.image = R.drawable.picto_add_user;
                            }

                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                else
                {
                    Utile.showToast(R.string.pikifriends_action_nok, PikiFriendsActivity.this);
                }
            }
        };

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("friendId", friend.parseId);

        if (friend.image == R.drawable.picto_add_user) {
            ParseCloud.callFunctionInBackground("addFriendV2", param, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    callback.done(o, e);
                }
            });
        } else {
            ParseCloud.callFunctionInBackground("removeFriendV2", param, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    callback.done(o, e);
                }
            });
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
                    if(loadNext())
                    {
                        //add footer
                        listView.addFooterView(footer);
                        lastItemShow = lastItem;
                    }
                }
            }
        }
    }
}
