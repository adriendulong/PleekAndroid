package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
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
import com.pleek.app.adapter.AddUserOnLoginAdapter;
import com.pleek.app.common.Constants;
import com.pleek.app.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddUserActivity extends ParentActivity implements View.OnClickListener, AddUserOnLoginAdapter.Listener
{
    private final int NB_MUST_SELECT_USER = 2;

    private ListView listviewUser;
    private View btnNext;
    private TextView txtBtnNext;
    private View btnDismiss;
    private View loadeur;

    private AddUserOnLoginAdapter adapter;

    private boolean isFromFriends = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        if (getIntent().hasExtra(Constants.EXTRA_FROM_FRIENDS)) {
            isFromFriends = true;
        }

        setup();
        init();
    }

    private void setup()
    {
        listviewUser = (ListView)findViewById(R.id.listviewUser);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnDismiss = findViewById(R.id.btnDismiss);
        btnDismiss.setOnClickListener(this);

        if (!isFromFriends) {
            txtBtnNext = (TextView) findViewById(R.id.txtBtnNext);
            String txt = getResources().getString(R.string.adduser_btn_next);
            txt = txt.replace("_nbstay_", "" + (NB_MUST_SELECT_USER - getFriendsPrefs().size() + 1));
            txtBtnNext.setText(txt);
        } else {
            btnNext.setVisibility(View.GONE);
            btnDismiss.setVisibility(View.VISIBLE);
        }

        loadeur = findViewById(R.id.loadeur);
    }

    private boolean error;
    private void init()
    {
        adapter = new AddUserOnLoginAdapter(this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Certified");
        query.whereEqualTo("isRecommend", Boolean.TRUE);

        String locale = "";
        if (StringUtils.isStringEmpty(Locale.getDefault().getCountry().toLowerCase())) {
            locale = Locale.US.getCountry().toLowerCase();
        } else {
            locale = Locale.getDefault().getCountry().toLowerCase();
        }
        query.whereEqualTo("recommendLocalisation", locale);
        query.include("user");
        query.orderByDescending("recommendOrder");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseUsers, ParseException e) {
                if (e == null) {
                    List<AddUserOnLoginAdapter.User> listUser = new ArrayList<AddUserOnLoginAdapter.User>();

                    if (parseUsers != null) {
                        for(ParseObject u : parseUsers) {
                            listUser.add(adapter.new User(u.getParseObject("user").getObjectId(), "@" + u.getParseObject("user").get("username"), u.getString("recommendDescription"), (u.getParseFile("recommendPicture") != null ? u.getParseFile("recommendPicture").getUrl() : null)));
                        }
                    }

                    adapter.setListUser(listUser);
                    listviewUser.setAdapter(adapter);

                    if (listUser.size() < NB_MUST_SELECT_USER) forceBtnToHome();
                } else {
                    forceBtnToHome();
                    L.e(">>>>>>>>>>>>>> ERROR parsequery user - e=" + e.getMessage());
                }

                loadeur.setVisibility(View.GONE);
            }
        });
    }

    private void forceBtnToHome() {
        if (!isFromFriends) {
            error = true;
            changeColorBtn(true);
            txtBtnNext.setText(getResources().getString(R.string.adduser_btn_next_done));
        }
    }

    private int nbFriendAdd;
    @Override
    public void onClick(View view) {
        if (view == btnNext || view == btnDismiss) {
            final List<AddUserOnLoginAdapter.User> listUser = adapter.getListUserSelected();
            if (view == btnDismiss || (listUser.size() >= (NB_MUST_SELECT_USER - getFriendsPrefs().size() + 1) || error)) {
                if (listUser.size() > 0) {
                    final Dialog loader = showLoader();

                    final FunctionCallback functionCallback = new FunctionCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            getFriendsBg(new FunctionCallback() {
                                @Override
                                public void done(Object o, ParseException e) {
                                    nbFriendAdd++;
                                    if (nbFriendAdd == listUser.size()) { //LAST > end
                                        goHome();
                                    }
                                }
                            });
                        }
                    };

                    for(AddUserOnLoginAdapter.User u : listUser) {
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("friendId", u.id);
                        ParseCloud.callFunctionInBackground("addFriendV2", param, functionCallback);
                    }
                }
                else {
                    goHome();
                }
            } else if (isFromFriends) {
                goHome();
            }
        }
    }

    private void goHome()
    {
        if (!isFromFriends) {
            Intent i = new Intent(AddUserActivity.this, InboxActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        } else {
            finish();
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }

    private boolean wasValid;
    private int TIME_TRANSITION_COLOR = 100;//ms
    @Override
    public void clickOnUser(AddUserOnLoginAdapter.User user) {
        if (!isFromFriends) {
            int nbUserSelected = NB_MUST_SELECT_USER - adapter.getListUserSelected().size() - getFriendsPrefs().size() + 1;
            boolean isValid = nbUserSelected <= 0;
            if (isValid) {
                txtBtnNext.setText(getResources().getString(R.string.adduser_btn_next_done));
            } else {
                String txt = getResources().getString(R.string.adduser_btn_next);
                txt = txt.replace("_nbstay_", "" + nbUserSelected);
                txtBtnNext.setText(txt);
            }

            changeColorBtn(isValid);
        }
    }

    private void changeColorBtn(boolean isValid)
    {
        //change color button
        TransitionDrawable transition = (TransitionDrawable) btnNext.getBackground();
        if(isValid && !wasValid)//if valid and previous was invalid
        {
            transition.startTransition(TIME_TRANSITION_COLOR);
        }
        if(!isValid && wasValid)//if invalid and previous was valid
        {
            transition.reverseTransition(TIME_TRANSITION_COLOR);
        }

        wasValid = isValid;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (!isFromFriends) {
            if (keyCode == KeyEvent.KEYCODE_BACK) return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
