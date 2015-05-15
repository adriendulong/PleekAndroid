package com.pleek.app.fragment;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.goandup.lib.utile.Screen;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pleek.app.activity.ParentActivity;

import java.util.Set;

/**
 * Created by nicolas on 13/01/15.
 */
public class ParentFragment extends Fragment {

    protected SharedPreferences pref;
    protected Screen screen;
    protected MixpanelAPI mixpanel;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pref = getActivity().getSharedPreferences("PREF_PLEEK", Activity.MODE_PRIVATE);
        mixpanel = ((ParentActivity) getActivity()).getMixpanel();
        screen = Screen.getInstance(getActivity());
    }

    protected Set<String> getFriendsPrefs() {
        return ((ParentActivity) getActivity()).getFriendsPrefs();
    }
}
