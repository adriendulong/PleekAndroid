package com.pleek.app;

import android.app.Application;

import com.appsflyer.AppsFlyerLib;
import com.goandup.lib.utile.L;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by nicolas on 21/08/2014.
 */
public class PleekApplication extends Application {
    public static final String MIXPANEL_TOKEN = "8ed35339994dd90dec6bda7d83c3d3eb";//DEV
    //public static final String MIXPANEL_TOKEN = "4b77ab10bab4b1d18103525dd39a7630";//DEV (Nico Up)
    //public static final String MIXPANEL_TOKEN = "bdde62cd933f58205b7cb98da8a2bca8";//PROD (Piki team)

    @Override
    public void onCreate() {
        // APPS FLYER
        AppsFlyerLib.setAppsFlyerKey("yDYHfJU4mxGatiELBhyx83");

        //Parse init
        Parse.initialize(this, "BA7FMG5LmMRx0RIPw3XdrOkR7FTnnSe4SIMRrnRG", "DrWgjs7EII2Sm1tVYwJICkjoWGA23oW42JXcI3BF");//DEV
        //Parse.initialize(this, "Yw204Svyg7sXIwvWdAZ9EmOOglqxpqk71ICpHDY9", "EPCJfqJIWtsTzARaPE4GvFsWHzfST8atBw3NCuxj");//PROD
        ParsePush.subscribeInBackground("", new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null) L.d(">>> ParsePush.subscribeInBackground OK");
                else L.e(">>> ParsePush.subscribeInBackground FAILED - e=" + e.getMessage());
            }
        });

        if (ParseUser.getCurrentUser() != null) ParseUser.getCurrentUser().fetchInBackground();
        if (ParseInstallation.getCurrentInstallation() != null) ParseInstallation.getCurrentInstallation().fetchInBackground();

        super.onCreate();
    }
}
