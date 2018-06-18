package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

import static com.mycampusdock.dock.activities.Profile.PREF_KEY_GENDER;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_NAME;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_SUBSCRIPTIONS;


public class ProfileFragment extends BaseFragment {
    public static boolean resetAppBar = true;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SharedPreferences pref;
    private TextView name, overview;
    private ImageView profileIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        pref = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ((MainActivity) getActivity()).updateToolbarTitle("Profile", resetAppBar);
        profileIcon = view.findViewById(R.id.profile_icon);
        if (pref.getString(PREF_KEY_GENDER, "").equals("M")) {
            Glide.with(this).load(R.drawable.man).into(profileIcon);
        } else
            Glide.with(this).load(R.drawable.girl).into(profileIcon);
        name = view.findViewById(R.id.profile_name);
        overview = view.findViewById(R.id.profile_overview);
        name.setText(pref.getString(PREF_KEY_NAME, "").toUpperCase());
        overview.setText("We ❤️ You ");
        viewPager = view.findViewById(R.id.viewpager);
        viewPager.setNestedScrollingEnabled(true);
        setupViewPager(viewPager);
        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new ProfileBasicFragment().setFragment(pref), "You");
        adapter.addFragment(new ProfileMyEventsFragment(), "My Events");
        adapter.addFragment(new ProfileBookmarksFragment(), "Bookmarks");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public static void logout(SharedPreferences pref) {
        String scope = pref.getString(PREF_KEY_SUBSCRIPTIONS, "");
        String[] topics = scope.split(",");
        for (String s : topics) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(s);
        }
        pref.edit().clear().commit();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }
}
