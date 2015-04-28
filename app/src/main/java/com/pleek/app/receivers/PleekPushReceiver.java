package com.pleek.app.receivers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParsePushBroadcastReceiver;
import com.pleek.app.R;

/**
 * Created by tiago on 28/04/2015.
 */
public class PleekPushReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_piki);
    }
}
