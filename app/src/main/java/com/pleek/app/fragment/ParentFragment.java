package com.pleek.app.fragment;


import android.support.v4.app.Fragment;

import com.pleek.app.activity.ParentActivity;

import java.util.Set;

/**
 * Created by nicolas on 13/01/15.
 */
public class ParentFragment extends Fragment {

    protected Set<String> getFriendsPrefs() {
        return ((ParentActivity) getActivity()).getFriendsPrefs();
    }
}
