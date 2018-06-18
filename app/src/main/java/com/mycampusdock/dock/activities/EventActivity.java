package com.mycampusdock.dock.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.fragments.EventBasicFragment;
import com.mycampusdock.dock.fragments.EventOverviewFragment;
import com.mycampusdock.dock.fragments.EventSocialFragment;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.services.CustomFirebaseMessagingService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

import static com.mycampusdock.dock.Config.REACH_TYPE_EVENT;

public class EventActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView poster;
    private Event event;
    private SharedPreferences pref;
    public static final String PARAM_ROLL = "roll_no";
    public static final String PARAM_EVENT_ID = "event_id";
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_event);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Event Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Bundle data = getIntent().getExtras();
        String id = data.getString("event_id");
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        realm = Realm.getDefaultInstance();
        event = realm.where(Event.class).equalTo("eventId", id).findFirst();
        viewPager = findViewById(R.id.viewpager);
        viewPager.setNestedScrollingEnabled(true);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        poster = findViewById(R.id.poster);
        populateImages(event);
        if (!event.isViewed()) {
            CustomFirebaseMessagingService.markReached(REACH_TYPE_EVENT, event.getEventId(), pref, true, getApplicationContext());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static final String PARAM_FEEDBACK_RATING = "feedback_rating";
    public static final String PARAM_FEEDBACK_COMMENT = "feedback_comment";
    public static final String PARAM_PAYLOAD = "payload";
    private ViewPagerAdapter viewPagerAdapter;

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new EventBasicFragment().setFragment(event.getEventId(), pref, getApplicationContext()), "Details");
        viewPagerAdapter.addFragment(new EventSocialFragment().setFragment(event.getEventId()), "Social");
        viewPagerAdapter.addFragment(new EventOverviewFragment().setFragment(event.getEventId(), pref, getApplicationContext()), "Overview");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        int currentPosition = 0;

        @Override
        public void onPageSelected(int newPosition) {

            DockInterfaces.FragmentLifecycle fragmentToShow = (DockInterfaces.FragmentLifecycle) viewPagerAdapter.getItem(newPosition);
            fragmentToShow.onResumeFragment();

            DockInterfaces.FragmentLifecycle fragmentToHide = (DockInterfaces.FragmentLifecycle) viewPagerAdapter.getItem(currentPosition);
            fragmentToHide.onPauseFragment();

            currentPosition = newPosition;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    };

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


    private void populateImages(Event item) {
        if (item.getPosters().size() > 0) {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
            File f = new File(folder, item.getPosters().get(0).getData());
            if (f.exists())
                Glide.with(getApplicationContext()).load(f).into(poster);
            else
                Glide.with(getApplicationContext()).load(item.getBanner()).into(poster);
        } else {
            Glide.with(getApplicationContext()).load(item.getBanner()).into(poster);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


}
