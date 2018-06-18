package com.mycampusdock.dock.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.MainActivity;


public class DiscoverFragment extends BaseFragment {

    public static boolean resetAppBar = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        ((MainActivity) getActivity()).updateToolbarTitle("Discover", resetAppBar);
        return view;
    }
}
