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
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.R;
import com.pleek.app.adapter.AddUserOnLoginAdapter;

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
    private View loadeur;

    private AddUserOnLoginAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adduser);

        setup();
        init();
    }

    private void setup()
    {
        listviewUser = (ListView)findViewById(R.id.listviewUser);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        txtBtnNext = (TextView)findViewById(R.id.txtBtnNext);
        String txt = getResources().getString(R.string.adduser_btn_next);
        txt = txt.replace("_nbstay_", ""+NB_MUST_SELECT_USER);
        txtBtnNext.setText(txt);
        loadeur = findViewById(R.id.loadeur);
    }

    private boolean error;
    private void init()
    {
        adapter = new AddUserOnLoginAdapter(this);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("isRecommend", Boolean.TRUE);
        query.whereEqualTo("recommendLocalisation", Locale.getDefault().getCountry().toLowerCase());
        query.orderByDescending("recommendOrder");

        query.findInBackground(new FindCallback<ParseUser>()
        {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e)
            {
                if(e == null)
                {
                    List<AddUserOnLoginAdapter.User> listUser = new ArrayList<AddUserOnLoginAdapter.User>();

                    if(parseUsers != null)
                    {
                        for(ParseUser u : parseUsers)
                        {
                            listUser.add(adapter.new User(u.getObjectId(), "@"+u.getUsername(), u.getString("recommendDescription"), (u.getParseFile("recommendPicture") != null ? u.getParseFile("recommendPicture").getUrl() : null)));
                        }
                    }

                    adapter.setListUser(listUser);
                    listviewUser.setAdapter(adapter);

                    if(listUser.size() < NB_MUST_SELECT_USER) forceBtnToHome();
                }
                else
                {
                    forceBtnToHome();
                    L.e(">>>>>>>>>>>>>> ERROR parsequery user - e=" + e.getMessage());
                }

                loadeur.setVisibility(View.GONE);
            }
        });
    }

    private void forceBtnToHome()
    {
        error = true;
        changeColorBtn(true);
        txtBtnNext.setText(getResources().getString(R.string.adduser_btn_next_done));
    }

    private int nbFriendAdd;
    @Override
    public void onClick(View view) {
        if (view == btnNext) {
            final List<AddUserOnLoginAdapter.User> listUser = adapter.getListUserSelected();
            if (listUser.size() >= NB_MUST_SELECT_USER || error) {
                if (listUser.size() > 0) {
                    final Dialog loader = showLoader();

                    final FunctionCallback functionCallback = new FunctionCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                Map<String, Object> param = new HashMap<String, Object>();
                                param.put("friendId", user.getObjectId());
                                ParseCloud.callFunctionInBackground("addToLastPublicPiki", param, new FunctionCallback<Object>() {
                                    @Override
                                    public void done(Object o, ParseException e) {
                                        nbFriendAdd++;
                                        if (nbFriendAdd == listUser.size()) { //LAST > end
                                            goHome();
                                        }
                                    }
                                });
                            }
                            else {
                                Utile.showToast(R.string.pikifriends_action_nok, AddUserActivity.this);
                                hideDialog(loader);//fix : crash #33
                            }
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
            }
        }
    }

    private void goHome()
    {
        Intent i = new Intent(AddUserActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
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
    public void clickOnUser(AddUserOnLoginAdapter.User user)
    {
        int nbUserSelected = NB_MUST_SELECT_USER - adapter.getListUserSelected().size();
        boolean isValid = nbUserSelected <= 0;
        if(isValid)
        {
            txtBtnNext.setText(getResources().getString(R.string.adduser_btn_next_done));
        }
        else
        {
            String txt = getResources().getString(R.string.adduser_btn_next);
            txt = txt.replace("_nbstay_", ""+nbUserSelected);
            txtBtnNext.setText(txt);
        }

        changeColorBtn(isValid);
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
        if(keyCode == KeyEvent.KEYCODE_BACK) return false;

        return true;
    }
}
