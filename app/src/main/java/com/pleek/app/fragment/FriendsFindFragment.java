package com.pleek.app.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goandup.lib.utile.L;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.activity.AddUserActivity;
import com.pleek.app.activity.FriendsActivity;
import com.pleek.app.activity.ParentActivity;
import com.pleek.app.adapter.FriendsAdapter;
import com.pleek.app.bean.Friend;
import com.pleek.app.common.Constants;
import com.pleek.app.utils.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by nicolas on 13/01/15.
 */
public class FriendsFindFragment extends ParentFragment implements FriendsAdapter.Listener, FriendsActivity.Listener
{
    private StickyListHeadersListView listView;
    private TextView txtNoContact;

    private List<Friend> listFriend;
    private FriendsAdapter adapter;
    private String currentFiltreSearch;

    private PhoneNumberUtil phoneUtil;

    private View header;

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

        header = getActivity().getLayoutInflater().inflate(R.layout.header_find_friend, null);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddUserActivity.class);
                intent.putExtra(Constants.EXTRA_FROM_FRIENDS, true);
                startActivityForResult(intent, Constants.REQUEST_ADD_USER);
            }
        });

        listView = (StickyListHeadersListView) getView().findViewById(R.id.listView);
        listView.addHeaderView(header);
        txtNoContact = (TextView) getView().findViewById(R.id.txtNoContact);

        adapter = new FriendsAdapter(this, getActivity());
        listView.setAdapter(adapter);
    }

    public void init()
    {
        listFriend = new ArrayList<Friend>();

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
                    Friend friend = new Friend(name, R.string.friends_section_out, R.drawable.picto_sms);
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
            for(Friend friend : listFriend) {
                listNumber.add(friend.phoneNumber);
            }

            //get ParseUser by all listFormatedNumber
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("phoneNumbers", listNumber);
            ParseCloud.callFunctionInBackground("checkContactOnPiki", param, new FunctionCallback<ArrayList<HashMap<String, String>>>() {
                @Override
                public void done(final ArrayList<HashMap<String, String>> rep, ParseException e) {
                    if (e == null) {
                        //get user if no already friend
                        ParseUser currentUser = ParseUser.getCurrentUser();

                        Set<String> friendsIds = ((ParentActivity) getActivity()).getFriendsPrefs();

                        for (HashMap<String, String> user : rep) {
                            if (friendsIds == null || !friendsIds.contains(user.get("userObjectId"))) {
                                Friend friend = new Friend(getNameByNum(user.get("phoneNumber")), R.string.friends_section_on, R.drawable.picto_add_user);
                                friend.username = user.get("username");
                                friend.phoneNumber = user.get("phoneNumber");
                                friend.parseId = user.get("userObjectId");
                                listFriend.add(friend);
                            }
                        }

                        // add all contact not already friend to list
                        Collections.sort(listFriend);
                        adapter.setListFriend(listFriend);
                        adapter.notifyDataSetChanged();

                    } else {
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
        for(Friend friend : listFriend)
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
                            adapter.addPikiUser(new Friend(list.get(0), R.string.friends_section_pikiuser));
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
    public void clickOnName(final Friend friend)
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
                    if (e == null) {
                        ((ParentActivity) getActivity()).getFriendsBg(new FunctionCallback() {
                            @Override
                            public void done(Object o, ParseException e) {
                                adapter.removeFriend(friend);

                                if (getActivity() instanceof FriendsActivity) {
                                    ((FriendsActivity) getActivity()).startAddFriendAnimation();
                                    ((FriendsActivity) getActivity()).initPage2();
                                    ((FriendsActivity) getActivity()).reloadPage3();
                                }
                            }
                        });
                    } else {
                        adapter.removeFriend(friend);
                        Utile.showToast(R.string.pikifriends_action_nok, getActivity());
                    }
                }
            });
        }
        else if(friend.sectionLabel == R.string.friends_section_out) { //sms
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                    bitmaps.add(getBitmapFromAsset("mosaic_1.png"));
                    bitmaps.add(getBitmapFromAsset("mosaic_2.png"));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                    encoder.start(bos);
                    for (Bitmap bitmap : bitmaps) {
                        encoder.addFrame(bitmap);
                    }
                    encoder.finish();

                    FileOutputStream fos = null;
                    try {
                        File tmpDir = new File(Environment.getExternalStorageDirectory() + "/pleek/tmp/");
                        if(!tmpDir.exists()) tmpDir.mkdirs();

                        File tmpFile = new File(tmpDir, "invite.gif");

                        if (!tmpFile.exists()) {
                            tmpFile.delete();
                        }

                        //tmpFile.deleteOnExit();
                        fos = new FileOutputStream(tmpFile);
                        fos.write(bos.toByteArray());

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("smsto:" + friend.phoneNumber));
                        intent.putExtra("address", friend.phoneNumber);
                        intent.putExtra("sms_body", getString(R.string.sms_invit));
                        intent.putExtra(Intent.EXTRA_STREAM, (Uri.fromFile(tmpFile)));
                        intent.setType("image/gif");
                        startActivity(Intent.createChooser(intent, "Send"));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity().getBaseContext(), R.string.phonenumber_numberinvalid, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        L.e(">> ERROR : Couldn't save gif e=" + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            });
        } else if(friend.sectionLabel == R.string.friends_section_pikiuser) {
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
                                ((FriendsActivity) getActivity()).initPage2();
                                ((FriendsActivity) getActivity()).reloadPage3();
                                adapter.addPikiUser(friend);
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
    }

    private Bitmap getBitmapFromAsset(String strName) {
        AssetManager assetManager = getResources().getAssets();
        InputStream istr = null;

        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }
}
