package com.pleek.app.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.goandup.lib.utile.Screen;
import com.goandup.lib.utile.SessionManager;
import com.goandup.lib.widget.DownTouchListener;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pleek.app.PleekApplication;
import com.pleek.app.R;
import com.pleek.app.bean.Friend;
import com.pleek.app.bean.ReadDateProvider;
import com.pleek.app.common.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nicolas on 18/12/14.
 */
public class ParentActivity extends FragmentActivity
{
    protected Screen screen;
    protected ReadDateProvider readDataProvider;
    protected SharedPreferences pref;
    protected MixpanelAPI mixpanel;
    protected AppEventsLogger fbAppEventsLogger;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        screen = Screen.getInstance(this);
        readDataProvider = ReadDateProvider.getInstance(this);
        pref = getSharedPreferences("PREF_PLEEK", MODE_PRIVATE);
        mixpanel = MixpanelAPI.getInstance(this, PleekApplication.MIXPANEL_TOKEN);
        fbAppEventsLogger = AppEventsLogger.newLogger(this);

        sm = SessionManager.getInstance(this, new SessionManager.SessionCompleteCallback()
        {
            @Override
            public void onSessionComplete(SessionManager.Session session)
            {
                sendEndSession(session);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.firstColorDark2));
        }
    }

    @Override
    protected void onPause()
    {
        if(sm != null) sm.endSession();
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        mixpanel.timeEvent("Session");
        if(sm != null) sm.startSession();
        AppEventsLogger.activateApp(this);
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        mixpanel.flush();
        super.onDestroy();
    }


    /******
     UTILE*/
    private void sendEndSession(SessionManager.Session session)
    {
        if(session == null) return;

        mixpanel.track("Session", null);
        mixpanel.flush();
    }


    /************
     MY DIALOGUE*/
    public Dialog showDialog(int titleId, int messageId, MyDialogListener listener)
    {
        Resources r = getResources();
        return showDialog(r.getString(titleId), r.getString(messageId), null, listener);
    }

    public Dialog showDialog(int titleId, int messageId, int iconeId,  MyDialogListener listener)
    {
        Resources r = getResources();
        return showDialog(r.getString(titleId), r.getString(messageId), r.getDrawable(iconeId), listener);
    }

    public Dialog showDialog(String title, String message, MyDialogListener listener)
    {
        return showDialog(title, message, null, listener);
    }

    public Dialog showDialog(String title, String message, Drawable icone, final MyDialogListener listener)
    {
        return showDialog(title, message, null, R.drawable.picto_ok_rose, R.drawable.picto_nok, listener);
    }

    public Dialog showDialog(String title, String message, int iconBtnOk, int iconBtnNok, final MyDialogListener listener)
    {
        return showDialog(title, message, null, iconBtnOk, iconBtnNok, listener);
    }

    public Dialog showDialog(String title, String message, Drawable icone, int iconBtnOk, int iconBtnNok, final MyDialogListener listener)
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setCanceledOnTouchOutside(false);

        //title
        TextView txtTitle = (TextView)dialog.getWindow().findViewById(R.id.txtTitle);
        if(title != null) txtTitle.setText(title);

        //message
        TextView txtMessage = (TextView)dialog.getWindow().findViewById(R.id.txtMessage);
        txtMessage.setText(message);

        //icone
        if(icone != null)
        {
            ImageView imgIcone = (ImageView)dialog.getWindow().findViewById(R.id.imgIcone);
            imgIcone.setImageDrawable(icone);
            imgIcone.setVisibility(View.VISIBLE);
        }

        //btnOk
        ImageView imgBtnOk = (ImageView)dialog.getWindow().findViewById(R.id.imgBtnOk);
        imgBtnOk.setImageResource(iconBtnOk);
        Drawable imgOkDown = getResources().getDrawable(R.drawable.dialog_btn_right_down);
        Drawable imgOkUp = getResources().getDrawable(R.drawable.dialog_btn_right);
        View btnOk = dialog.getWindow().findViewById(R.id.btnOk);
        btnOk.setOnTouchListener(new DownTouchListener(imgOkDown, imgOkUp));
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideDialog(dialog);
                if(listener != null) listener.closed(true);
            }
        });

        //btnNok
        ImageView imgBtnNok = (ImageView)dialog.getWindow().findViewById(R.id.imgBtnNok);
        imgBtnNok.setImageResource(iconBtnNok);
        Drawable imgNokDown = getResources().getDrawable(R.drawable.dialog_btn_left_down);
        Drawable imgNokUp = getResources().getDrawable(R.drawable.dialog_btn_left);
        View btnNok = dialog.getWindow().findViewById(R.id.btnNok);
        btnNok.setOnTouchListener(new DownTouchListener(imgNokDown, imgNokUp));
        btnNok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideDialog(dialog);
                if(listener != null) listener.closed(false);
            }
        });

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideDialog(dialog);
                    if (listener != null) listener.closed(false);
                }

                return true;
            }
        });

        if(!isFinishing() && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed()))
        {
            dialog.show();
        }

        return dialog;
    }

    public interface MyDialogListener
    {
        public void closed(boolean accept);
    }

    /*********
     MY ALERT*/
    public Dialog showAlert(int titleId, int messageId)
    {
        Resources r = getResources();
        return showAlert(r.getString(titleId), r.getString(messageId), null, null);
    }
    public Dialog showAlert(int titleId, int messageId, MyAlertListener listener)
    {
        Resources r = getResources();
        return showAlert(r.getString(titleId), r.getString(messageId), null, listener);
    }

    public Dialog showAlert(int titleId, int messageId, int iconeId,  MyAlertListener listener)
    {
        Resources r = getResources();
        return showAlert(r.getString(titleId), r.getString(messageId), r.getDrawable(iconeId), listener);
    }

    public Dialog showAlert(String title, String message)
    {
        return showAlert(title, message, null, null);
    }

    public Dialog showAlert(String title, String message, MyAlertListener listener)
    {
        return showAlert(title, message, null, listener);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Dialog showAlert(String title, String message, Drawable icone, final MyAlertListener listener)
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.alert_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setCanceledOnTouchOutside(false);

        //title
        TextView txtTitle = (TextView)dialog.getWindow().findViewById(R.id.txtTitle);
        if(title != null) txtTitle.setText(title);

        //message
        TextView txtMessage = (TextView)dialog.getWindow().findViewById(R.id.txtMessage);
        txtMessage.setText(message);

        //icone
        if(icone != null)
        {
            ImageView imgIcone = (ImageView)dialog.getWindow().findViewById(R.id.imgIcone);
            imgIcone.setImageDrawable(icone);
            imgIcone.setVisibility(View.VISIBLE);
        }

        //btnOk
        Drawable imgOkDown = getResources().getDrawable(R.drawable.alert_btn_down);
        Drawable imgOkUp = getResources().getDrawable(R.drawable.alert_btn);
        View btn = dialog.getWindow().findViewById(R.id.btnOk);
        btn.setOnTouchListener(new DownTouchListener(imgOkDown, imgOkUp));
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideDialog(dialog);
            if(listener != null) listener.closed();
            }
        });

        if(!isFinishing() && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed()))
        {
            dialog.show();
        }

        return dialog;
    }

    interface MyAlertListener
    {
        public void closed();
    }

    /**********
     MY LOADER*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Dialog showLoader()
    {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.dialog_loader);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogLoaderAnimation;
        dialog.setCanceledOnTouchOutside(false);

        if (!isFinishing() && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed()))
        {
            dialog.show();
        }

        return dialog;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean hideDialog(Dialog dialog)
    {
        boolean ok = false;
        if(dialog != null)
        {
            if(dialog.isShowing())
            {
                Context context = ((ContextWrapper)dialog.getContext()).getBaseContext();
                if(context instanceof Activity)
                {
                    Activity a = (Activity)context;
                    if(!a.isFinishing() && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1 || !a.isDestroyed()))
                    {
                        dialog.dismiss();
                        ok = true;
                    }
                }
                else
                {
                    dialog.dismiss();
                    ok = true;
                }
            }
        }
        return ok;
    }

    public void getFriends(boolean fromCache, final FunctionCallback<ArrayList<Friend>> callback) {
        final ArrayList<Friend> friends = new ArrayList<Friend>();

        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(fromCache ? ParseQuery.CachePolicy.CACHE_THEN_NETWORK : ParseQuery.CachePolicy.NETWORK_ONLY);
        innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        innerQuery.include("friend");
        innerQuery.setLimit(1000);
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    for (ParseObject obj : list) {
                        friends.add(new Friend((ParseUser) obj.get("friend")));
                    }

                    callback.done(friends, e);
                }
            }
        });
    }

    public void getFriendsBg(final FunctionCallback callback) {
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Friend");
        innerQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        innerQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        innerQuery.include("friend");
        innerQuery.setLimit(1000);
        innerQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    HashSet<String> friendsIds = new HashSet<String>();

                    for (ParseObject obj : list) {
                        ParseUser user = (ParseUser) obj.get("friend");

                        if (user != null)
                            friendsIds.add(user.getObjectId());
                    }

                    setFriendsPrefs(friendsIds);

                    if (callback != null) {
                        callback.done(list, e);
                    }
                }
            }
        });
    }

    public Set<String> getFriendsPrefs() {
        if (pref.contains(Constants.PREF_FRIENDS)) {
            return pref.getStringSet(Constants.PREF_FRIENDS, new HashSet<String>());
        } else {
            return new HashSet<String>();
        }
    }

    public void addFriendPrefs(String friend) {
        Set<String> set = pref.getStringSet(Constants.PREF_FRIENDS, new HashSet<String>());
        set.add(friend);
        pref.edit().putStringSet(Constants.PREF_FRIENDS, set).commit();
    }

    public void removeFriendPrefs(String friend) {
        Set<String> set = pref.getStringSet(Constants.PREF_FRIENDS, new HashSet<String>());
        set.remove(friend);
        pref.edit().putStringSet(Constants.PREF_FRIENDS, set).commit();
    }

    public void setFriendsPrefs(HashSet<String> friends) {
        pref.edit().putStringSet(Constants.PREF_FRIENDS, new HashSet<String>(friends)).commit();
    }

    public SharedPreferences getPref() {
        return pref;
    }

    public void setPref(SharedPreferences pref) {
        this.pref = pref;
    }

    public MixpanelAPI getMixpanel() {
        return mixpanel;
    }

    public void setMixpanel(MixpanelAPI mixpanel) {
        this.mixpanel = mixpanel;
    }
}
