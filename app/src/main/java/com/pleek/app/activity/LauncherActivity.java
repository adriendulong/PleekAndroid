package com.pleek.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.goandup.lib.widget.ButtonRoundedMaterialDesign;
import com.parse.ParseUser;
import com.pleek.app.R;

public class LauncherActivity extends ParentActivity
{
    private ButtonRoundedMaterialDesign btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        fbAppEventsLogger.logEvent("ActivateApp");
        AppsFlyerLib.sendTracking(getApplicationContext());

        if(ParseUser.getCurrentUser() != null)
        {
            mixpanel.identify(ParseUser.getCurrentUser().getObjectId());
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        else
        {
            //mixpanel set identify (Adrien)
            mixpanel.identify(mixpanel.getDistinctId());

            setContentView(R.layout.activity_launcher);

            btnNext = (ButtonRoundedMaterialDesign) findViewById(R.id.btnNext);
            btnNext.setOnPressedListener(new ButtonRoundedMaterialDesign.OnPressedListener()
            {
                @Override
                public void endPress(ButtonRoundedMaterialDesign button)
                {
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
