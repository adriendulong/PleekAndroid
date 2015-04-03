package com.pleek.app.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.goandup.lib.utile.Utile;
import com.goandup.lib.widget.DownTouchListener;
import com.parse.ParseInstallation;
import com.pleek.app.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by nicolas on 25/02/15.
 */
public class ParameterActivity extends ParentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private TextView txtVersion;
    private View btnTopBar;
    private View btnUsername;
    private View btnNotification;
    private CheckBox chbxNotif;
    private View btnEmail;
    private View btnTwitter;
    private View btnSendapp;
    private View txtGoandup;
    private View hand;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        setup();
        init();
    }

    private void setup()
    {
        int colorDown = getResources().getColor(R.color.overlay);
        txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText("V "+ Utile.getVersionName(this));
        btnTopBar = findViewById(R.id.btnTopBar);
        btnTopBar.setOnTouchListener(new DownTouchListener(getResources().getColor(R.color.firstColorDark), getResources().getColor(R.color.firstColorLight)));
        btnTopBar.setOnClickListener(this);
        btnUsername = findViewById(R.id.btnUsername);
        btnUsername.setOnTouchListener(new DownTouchListener(colorDown));
        btnUsername.setOnClickListener(this);
        btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnTouchListener(new DownTouchListener(colorDown));
        btnNotification.setOnClickListener(this);
        chbxNotif = (CheckBox) findViewById(R.id.chbxNotif);
        chbxNotif.setOnCheckedChangeListener(this);
        btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnTouchListener(new DownTouchListener(colorDown));
        btnEmail.setOnClickListener(this);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnTwitter.setOnTouchListener(new DownTouchListener(colorDown));
        btnTwitter.setOnClickListener(this);
        btnSendapp = findViewById(R.id.btnSendapp);
        btnSendapp.setOnTouchListener(new DownTouchListener(colorDown));
        btnSendapp.setOnClickListener(this);
        txtGoandup = findViewById(R.id.txtGoandup);
        txtGoandup.setOnClickListener(this);
        hand = findViewById(R.id.hand);
        //hand.startAnimation(AnimationUtils.loadAnimation(this, R.anim.hand_point));
    }

    final Handler handHandler = new Handler();
    Runnable handRunnable = null;
    private void init()
    {
        chbxNotif.setChecked(ParseInstallation.getCurrentInstallation().getBoolean("notificationsEnabled"));

//        handRunnable = new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                hand.setVisibility(View.VISIBLE);
//            }
//        };
//        handHandler.postDelayed(handRunnable, 5000);
    }

    @Override
    public void onClick(View view)
    {
        if(view == btnTopBar)
        {
            finish();
        }
        else if(view == btnUsername)
        {
            startActivity(new Intent(this, ChangeUsernameActivity.class));
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        }
        else if(view == btnNotification)
        {
            chbxNotif.setChecked(!chbxNotif.isChecked());
        }
        else if(view == btnEmail)
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.param_mail_to)});
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.param_mail_subject));
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.param_mail_texte));

            startActivity(Intent.createChooser(i, getString(R.string.param_mail)));
        }
        else if(view == btnTwitter)
        {
            String text = getString(R.string.param_twitter_texte);

            Intent tweetIntent = new Intent(Intent.ACTION_SEND);
            tweetIntent.putExtra(Intent.EXTRA_TEXT, text);
            tweetIntent.setType("text/plain");

            PackageManager packManager = getPackageManager();
            List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

            boolean resolved = false;
            for(ResolveInfo resolveInfo: resolvedInfoList)
            {
                if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android"))
                {
                    tweetIntent.setClassName( resolveInfo.activityInfo.packageName,  resolveInfo.activityInfo.name );
                    resolved = true;
                    break;
                }
            }
            if(resolved)
            {
                startActivity(tweetIntent);
            }
            else
            {
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, text);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://twitter.com/intent/tweet?text="+text));
                startActivity(i);
            }
        }
        else if(view == btnSendapp)
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_TEXT, getString(R.string.param_sendapp_text));
            i.setType("text/plain");
            startActivity(Intent.createChooser(i, getString(R.string.param_sendapp)));
        }
        else if(view == txtGoandup)
        {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/goandup_pleek")));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
    {
        ParseInstallation.getCurrentInstallation().put("notificationsEnabled", b);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Override
    protected void onDestroy()
    {
//        try{
//            handHandler.removeCallbacks(handRunnable);
//        }catch (Exception e){}

        super.onDestroy();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }
}
