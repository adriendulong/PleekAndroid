package com.pleek.app.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.FriendsActivity;
import com.pleek.app.adapter.FriendsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by nicolas on 13/01/15.
 */
public class FriendsFindFragment extends ParentFragment implements FriendsAdapter.Listener, FriendsActivity.Listener
{
    private StickyListHeadersListView listView;
    private TextView txtNoContact;

    private List<FriendsAdapter.Friend> listFriend;
    private FriendsAdapter adapter;
    private String currentFiltreSearch;

    private PhoneNumberUtil phoneUtil;

    public static FriendsFindFragment newInstance()
    {
        FriendsFindFragment fragment = new FriendsFindFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_friends_find, container, false);
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
        phoneUtil = PhoneNumberUtil.getInstance();

        listView = (StickyListHeadersListView)getView().findViewById(R.id.listView);
        txtNoContact = (TextView) getView().findViewById(R.id.txtNoContact);

        adapter = new FriendsAdapter(this, getActivity());
        listView.setAdapter(adapter);
    }

    public void init()
    {
        listFriend = new ArrayList<FriendsAdapter.Friend>();

        //get all contact phone number
        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if(phones != null)//fix : crash #14
        {
            phones.moveToLast();
            while (phones.moveToPrevious())
            {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if(isMobile(phoneNumber))
                {
                    FriendsAdapter.Friend friend = adapter.new Friend(name, R.string.friends_section_out, R.drawable.picto_sms);
                    friend.phoneNumber = formatNumber(phoneNumber);
                    listFriend.remove(friend);
                    listFriend.add(friend);
                }
            }
            phones.close();
        }


        if(listFriend.size() > 0)
        {
            List<String> listNumber = new ArrayList<String>();
            for(FriendsAdapter.Friend friend : listFriend)
            {
                listNumber.add(friend.phoneNumber);
            }

            //get ParseUser by all listFormatedNumber
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("phoneNumbers", listNumber);
            ParseCloud.callFunctionInBackground("checkContactOnPiki", param, new FunctionCallback<ArrayList<HashMap<String, String>>>()
            {
                @Override
                public void done(ArrayList<HashMap<String, String>> rep, ParseException e)
                {
                    if(e == null)
                    {
                        //get user if no already friend
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        List<String> usersFriend = currentUser.getList("usersFriend");
                        for (HashMap<String, String> user : rep)
                        {
                            if(usersFriend == null || !usersFriend.contains(user.get("userObjectId")))
                            {
                                FriendsAdapter.Friend friend = adapter.new Friend(getNameByNum(user.get("phoneNumber")), R.string.friends_section_on, R.drawable.picto_adduser);
                                friend.username = user.get("username");
                                friend.phoneNumber = user.get("phoneNumber");
                                friend.parseId = user.get("userObjectId");
                                listFriend.add(friend);
                            }
                        }

                        //add all contact not already friend to list
                        Collections.sort(listFriend);
                        adapter.setListFriend(listFriend);
                        adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        L.e("ERROR checkContactOnPiki - e="+e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            //add all contact to list
            Collections.sort(listFriend);
            adapter.setListFriend(listFriend);
            adapter.notifyDataSetChanged();

            txtNoContact.setVisibility(View.GONE);
        }
        else
        {
            txtNoContact.setVisibility(View.VISIBLE);
            txtNoContact.setText(R.string.friends_nocontact);
        }
    }

    private String getNameByNum(String phoneNumber)
    {
        for(FriendsAdapter.Friend friend : listFriend)
        {
            if(phoneNumber.equals(friend.phoneNumber))
            {
                return friend.name;
            }
        }
        return null;
    }

    private boolean isMobile(String number)
    {
        Phonenumber.PhoneNumber numberProto = getPhoneNumber(number);
        if(numberProto != null)
        {
            PhoneNumberUtil.PhoneNumberType type = phoneUtil.getNumberType(numberProto);
            return type == PhoneNumberUtil.PhoneNumberType.MOBILE ||
                   type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE ||
                   isMobileFR07(numberProto);
        }
        return false;
    }

    //libphonenumber don't know french 07 number is Mobile
    private boolean isMobileFR07(Phonenumber.PhoneNumber numberProto)
    {
        return numberProto != null &&
               phoneUtil.getNumberType(numberProto) == PhoneNumberUtil.PhoneNumberType.UNKNOWN &&
               numberProto.getCountryCode() == 33 &&
               (numberProto.getNationalNumber()+"").startsWith("7");
    }



    private String formatNumber(String number)
    {
        Phonenumber.PhoneNumber numberProto = getPhoneNumber(number);
        if(numberProto != null)
        {
            number = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        }

        return number;
    }

    private Phonenumber.PhoneNumber getPhoneNumber(String number)
    {
        try
        {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "");
            number = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            if(phoneUtil.isPossibleNumber(numberProto))
            {
                return numberProto;
            }
        }
        catch (NumberParseException e)
        {
            try
            {
                L.w("WARNING : formatNumber - number("+number+") necessite un code pays. Utilisation du code du device : "+Locale.getDefault().getCountry());
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, Locale.getDefault().getCountry());
                if(phoneUtil.isPossibleNumber(numberProto))
                {
                    return numberProto;
                }
                else
                {
                    L.w("WARNING : formatNumber - number("+numberProto+") is NOT PossibleNumber");
                }
            }
            catch (NumberParseException e2)
            {
                L.e("NumberParseException ("+number+") was thrown 2 : " + e2.toString());
            }
        }

        return null;
    }

    @Override
    public void filtreSearchChange(String filtreSearch)
    {
        try
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

            txtNoContact.setVisibility(noresult ? View.VISIBLE : View.GONE);//TODO : crash #13
            txtNoContact.setText(nofriend && nofiltre ? R.string.friends_nofriend : R.string.friends_noresult);

            adapter.setListFriend(listFiltred);
            adapter.notifyDataSetChanged();

            if(lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
            lastQueryRunnable = new QueryRunnable(filtreSearch);
            handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
        }
        catch (Exception e)
        {
            L.w(">>>>>> ERROR : filtreSearchChange - e="+e.getMessage());
        }
    }


    private static int TIMER_QUERY = 500;//ms
    final Handler handler = new Handler();
    private QueryRunnable lastQueryRunnable;
    class QueryRunnable implements Runnable
    {
        private String username;

        public QueryRunnable(String username) {
            this.username = username;
        }

        @Override
        public void run()
        {
            if(username == null)
            {
                adapter.removePikiUser();
                return;
            }

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.findInBackground(new FindCallback<ParseUser>()
            {
                public void done(List<ParseUser> list, ParseException e)
                {
                    if(e == null && username.equals(currentFiltreSearch))
                    {
                        if(list.size() > 0)
                        {
                            txtNoContact.setVisibility(View.GONE);
                            adapter.addPikiUser(adapter.new Friend(list.get(0), R.string.friends_section_pikiuser));
                        }
                        else
                        {
                            adapter.removePikiUser();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void clickOnName(final FriendsAdapter.Friend friend)
    {
        if(friend.sectionLabel == R.string.friends_section_on)//add
        {
            adapter.addFriendLoading(friend);

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("friendId", friend.parseId);
            ParseCloud.callFunctionInBackground("addFriendV2", param, new FunctionCallback<Object>()
            {
                @Override
                public void done(Object o, ParseException e)
                {
                    if(e == null)
                    {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("friendId", friend.parseId);
                        ParseCloud.callFunctionInBackground("addToLastPublicPiki", param, null);

                        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>()
                        {
                            @Override
                            public void done(ParseObject parseObject, ParseException e)
                            {
                                adapter.removeFriend(friend);
                                if (getActivity() instanceof FriendsActivity)
                                {
                                    ((FriendsActivity) getActivity()).startAddFriendAnimation();
                                }
                            }
                        });
                    }
                    else
                    {
                        adapter.removeFriend(friend);
                        Utile.showToast(R.string.pikifriends_action_nok, getActivity());
                    }
                }
            });
        }
        else if(friend.sectionLabel == R.string.friends_section_out)//sms
        {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", friend.phoneNumber, null));
            i.putExtra("sms_body", getString(R.string.sms_invit));
            startActivity(i);
        }
        else if(friend.sectionLabel == R.string.friends_section_pikiuser)//mute
        {
            adapter.addFriendLoading(friend);

            ParseUser currentUser = ParseUser.getCurrentUser();
            List<String> usersFriend = currentUser.getList("usersFriend");
            final boolean alreadyFriend = usersFriend != null && usersFriend.contains(friend.parseId);
            List<String> usersIMuted = currentUser.getList("usersIMuted");
            final boolean alreadyMuted = usersIMuted != null && usersIMuted.contains(friend.parseId);

            final FunctionCallback callback = new FunctionCallback<Object>()
            {
                @Override
                public void done(Object o, ParseException e)
                {
                    if(e == null)
                    {
                        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>()
                        {
                            @Override
                            public void done(ParseObject parseObject, ParseException e)
                            {
                                List<String> usersIMuted = parseObject.getList("usersIMuted");
                                boolean isMuted = usersIMuted != null && usersIMuted.contains(friend.parseId);

                                adapter.removeFriend(friend);
                                if(!alreadyFriend && getActivity() instanceof FriendsActivity)
                                {
                                    ((FriendsActivity) getActivity()).startAddFriendAnimation();
                                }

                                friend.image = isMuted ? R.drawable.picto_mute_user_on : R.drawable.picto_mute_user;
                                if(alreadyMuted != isMuted && getActivity() instanceof FriendsActivity)
                                {
                                    ((FriendsActivity) getActivity()).initPage2();
                                }

                                adapter.addPikiUser(friend);
                            }
                        });
                    }
                    else
                    {
                        adapter.removeFriend(friend);
                        Utile.showToast(R.string.pikifriends_action_nok, getActivity());
                    }
                }
            };

            if(alreadyFriend)
            {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("friendId", friend.parseId);
                ParseCloud.callFunctionInBackground(alreadyMuted ? "unMuteFriend" : "muteFriend", param, callback);
            }
            else
            {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("friendId", friend.parseId);
                ParseCloud.callFunctionInBackground("addFriendV2", param, new FunctionCallback<Object>()
                {
                    @Override
                    public void done(Object o, ParseException e)
                    {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("friendId", friend.parseId);
                        ParseCloud.callFunctionInBackground("addToLastPublicPiki", param, null);

                        callback.done(o, e);
                    }
                });
            }
        }
    }
}
