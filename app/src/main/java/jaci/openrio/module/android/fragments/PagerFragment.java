package jaci.openrio.module.android.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jaci.openrio.module.android.PagedConnectedActivity;
import jaci.openrio.module.android.R;

public class PagerFragment extends Fragment {

    public String tab_name;
    public PagedConnectedActivity activity;

    public static PagerFragment newInstance(String tab_name, Class<? extends PagerFragment> clazz, PagedConnectedActivity act) {
        try {
            PagerFragment fragment = clazz.newInstance();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            fragment.tab_name = tab_name;
            fragment.activity = act;
            return fragment;
        } catch (Exception e) {
            return new PagerFragment();
        }
    }

    public PagerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible)
            update();
    }

    public void update() {}
}
