package com.pleek.app.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.goandup.lib.widget.ButtonRoundedMaterialDesign;
import com.parse.ParseUser;
import com.pleek.app.R;
import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends ParentActivity
{
    private ImageView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        fbAppEventsLogger.logEvent("ActivateApp");
        AppsFlyerLib.sendTracking(getApplicationContext());

        if(ParseUser.getCurrentUser() != null)
        {
            mixpanel.identify(ParseUser.getCurrentUser().getObjectId());
            startActivity(new Intent(this, InboxActivity.class));
            finish();
        }
        else
        {
            //mixpanel set identify (Adrien)
            mixpanel.identify(mixpanel.getDistinctId());

            setContentView(R.layout.activity_launcher);

            btnNext = (ImageView) findViewById(R.id.btnNext);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LauncherActivity.this, PhoneNumberActivity.class));
                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }
            });

            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popin);
            animation.setInterpolator(new BounceInterpolator());
            btnNext.startAnimation(animation);
        }
    }
}
