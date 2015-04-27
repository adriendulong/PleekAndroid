package com.pleek.app.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.facebook.AppEventsConstants;
import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.goandup.lib.widget.EditTextFont;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.pleek.app.R;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public class UsernameActivity extends ParentActivity implements View.OnClickListener
{
    public final static String EXTRA_NAME_PHONENUMBER = "extra_phonenumber";

    public final static int MIN_SIZE_USERNAME = 3;//compris

    private EditTextFont editUsername;
    private ImageView imgReponse;
    private View progress;
    private View btnNext;
    private View layoutTextMention;
    private View txtUsernameInvalid;
    private View btnTC;
    private View viewTC;
    private WebView webviewTC;
    private View btnCloseTC;

    private Timer timer;
    private String currentUsername;
    private boolean usernameValide;
    private Animation animationShow;
    private Animation animationHide;
    private String phoneNumber;
    private boolean isSending;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        setup();
        init();
    }

    private void setup()
    {
        if(getIntent() != null)
        {
            phoneNumber = getIntent().getStringExtra(EXTRA_NAME_PHONENUMBER);
        }

        editUsername = (EditTextFont)findViewById(R.id.editUsername);
        imgReponse = (ImageView)findViewById(R.id.imgReponse);
        progress = findViewById(R.id.progress);
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        layoutTextMention = findViewById(R.id.layoutTextMention);
        txtUsernameInvalid = findViewById(R.id.txtUsernameInvalid);
        txtUsernameInvalid.setVisibility(View.INVISIBLE);
        btnTC = findViewById(R.id.btnTC);
        btnTC.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.secondColor), getResources().getColor(R.color.firstColorDark)));
        btnTC.setOnClickListener(this);
        viewTC = findViewById(R.id.viewTC);
        viewTC.setVisibility(View.GONE);
        btnCloseTC = findViewById(R.id.btnCloseTC);
        btnCloseTC.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.blancTouchDown)));
        btnCloseTC.setOnClickListener(this);
        webviewTC = (WebView) findViewById(R.id.webviewTC);
        webviewTC.setWebViewClient(new WebViewClient());
        btnCloseTC = findViewById(R.id.btnCloseTC);

        imgReponse.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        //listener textChange
        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                verifyUsername(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
        });

//        Timer focusTimer = new Timer();
//        focusTimer.schedule(new TimerTask()
//        {
//            @Override
//            public void run()
//            {
//                runOnUiThread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        if(editUsername.requestFocus())
//                        {
//                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                        }
//                    }
//                });
//            }
//        },200);

        int duration = 200;
        animationShow = new AlphaAnimation(0f, 1f);
        animationShow.setDuration(duration);
        animationShow.setFillAfter(true);
        animationHide = new AlphaAnimation(1f, 0f);
        animationHide.setDuration(duration);
        animationHide.setFillAfter(true);

        //disable link
        webviewTC.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
    }

    private void init()
    {
    }

    private Dialog loader;
    @Override
    public void onClick(View view)
    {
        if(view == btnNext)
        {
            if(currentUsername != null)
            {
                loader = showLoader();

                if(isSending){
                    Utile.showToast(R.string.username_is_sending, UsernameActivity.this);
                    return;
                }
                isSending = true;
                final String sendingUsername = currentUsername.toString();

                final ParseObject userInfos = new ParseObject("UserInfos");
                userInfos.put("phoneNumber", phoneNumber);
                userInfos.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        ParseUser user = new ParseUser();
                        user.setUsername(sendingUsername);
                        user.setPassword(sendingUsername);
                        user.put("userInfos", userInfos);

                        user.signUpInBackground(new SignUpCallback()
                        {
                            public void done(ParseException e)
                            {
                                if (e == null)
                                {
                                    //create alias (Adrien)
                                    ParseUser currentUser = ParseUser.getCurrentUser();
                                    if(currentUser != null)
                                    {
                                        mixpanel.alias(currentUser.getObjectId(), mixpanel.getDistinctId());
                                    }

                                    ParseACL acl = new ParseACL();
                                    acl.setWriteAccess(currentUser, true);
                                    acl.setReadAccess(currentUser, true);
                                    userInfos.setACL(acl);
                                    userInfos.saveEventually();

                                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();

                                    if (currentUser != null) {
                                        installation.put("notificationsEnabled", true);

                                        installation.put("user", currentUser);
                                    }
                                    installation.saveInBackground();

                                    mixpanel.track("Sign Up", null);
                                    fbAppEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION);

                                    startActivity(new Intent(UsernameActivity.this, AddUserActivity.class));
                                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

                                    hideDialog(loader);
                                    isSending = false;
                                }
                                else
                                {
                                    Utile.showToast(R.string.username_no_network, UsernameActivity.this);
                                    e.printStackTrace();
                                    hideDialog(loader);
                                    isSending = false;
                                }
                            }
                        });
                    }
                });
            }
        }
        else if(view == btnTC)
        {
            showViewTC();
        }
        else if(view == btnCloseTC)
        {
            hideViewTC();
        }
    }

    private int TIME_TRANSITION_COLOR = 100;//ms
    private void verifyUsername(final String username)
    {
        if(username == null)
        {
            progress.setVisibility(View.GONE);
            imgReponse.setVisibility(View.GONE);
            return;
        }

        progress.setVisibility(username.length() >= MIN_SIZE_USERNAME ? View.VISIBLE : View.GONE);
        imgReponse.setVisibility(View.GONE);
        showTxtUsernameInvalid(false);

        if(lastQueryRunnable != null) handler.removeCallbacks(lastQueryRunnable);
        currentUsername = null;
        if(username.length() >= MIN_SIZE_USERNAME)
        {
            lastQueryRunnable = new QueryRunnable(username);
            handler.postDelayed(lastQueryRunnable, TIMER_QUERY);
        }
        else if(usernameValide)
        {
            TransitionDrawable transition = (TransitionDrawable) btnNext.getBackground();
            transition.reverseTransition(TIME_TRANSITION_COLOR);
            usernameValide = false;
            currentUsername = null;
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
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.findInBackground(new FindCallback<ParseUser>()
            {
                public void done(List<ParseUser> objects, ParseException e)
                {
                    if (e == null)
                    {
                        boolean isValid = objects.isEmpty();

                        //change color button
                        TransitionDrawable transition = (TransitionDrawable) btnNext.getBackground();
                        if(isValid && !usernameValide)//if valid and previous was invalid
                        {
                            transition.startTransition(TIME_TRANSITION_COLOR);
                        }
                        if(!isValid && usernameValide)//if invalid and previous was valid
                        {
                            transition.reverseTransition(TIME_TRANSITION_COLOR);
                        }

                        //show good picto
                        progress.setVisibility(View.GONE);
                        imgReponse.setVisibility(View.VISIBLE);
                        imgReponse.setImageResource(isValid ? R.drawable.picto_ok : R.drawable.picto_error);

                        showTxtUsernameInvalid(!isValid);

                        usernameValide = isValid;
                        currentUsername = isValid ? username : null;
                    }
                    else
                    {
                        Utile.showToast(R.string.username_check_no_network, UsernameActivity.this);
                        e.printStackTrace();
                        usernameValide = false;
                        currentUsername = null;
                    }
                }
            });
        }
    }

    private boolean txtUsernameInvalidIsShow;
    private void showTxtUsernameInvalid(boolean show)
    {
        if(txtUsernameInvalidIsShow != show)
        {
            txtUsernameInvalid.startAnimation(show ? animationShow : animationHide);
            layoutTextMention.startAnimation(show ? animationHide : animationShow);
            txtUsernameInvalidIsShow = show;
        }
    }

    private boolean viewTCIsShow;
    private boolean isFirstShowViewTC = true;
    private void toggleViewTC()
    {
        if(viewTCIsShow) hideViewTC();
        else showViewTC();
    }

    private void showViewTC()
    {
        //close keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editUsername.getWindowToken(), 0);

        if(isFirstShowViewTC)
        {
            webviewTC.loadUrl("http://pleekapp.com/terms/");
            isFirstShowViewTC = false;
        }

        if(viewTCIsShow) return;

        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.view_popin_bottom);

        viewTC.startAnimation(bottomUp);
        viewTC.setVisibility(View.VISIBLE);

        viewTCIsShow = true;
    }

    private void hideViewTC()
    {
        if(!viewTCIsShow) return;

        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.view_popout_bottom);

        viewTC.startAnimation(bottomUp);
        viewTC.setVisibility(View.GONE);

        viewTCIsShow = false;
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.activity_in_reverse, R.anim.activity_out_reverse);
    }
}
