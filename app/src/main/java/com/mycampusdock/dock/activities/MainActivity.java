package com.mycampusdock.dock.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.fragments.BaseFragment;
import com.mycampusdock.dock.fragments.BulletinsFragment;
import com.mycampusdock.dock.fragments.DiscoverFragment;
import com.mycampusdock.dock.fragments.HomeFragment;
import com.mycampusdock.dock.fragments.NewsFragment;
import com.mycampusdock.dock.fragments.ProfileFragment;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.objects.ChatMessage;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.objects.Interest;
import com.mycampusdock.dock.objects.Notification;
import com.mycampusdock.dock.objects.RealmData;
import com.mycampusdock.dock.utils.ChatApplication;
import com.mycampusdock.dock.utils.FragmentHistory;
import com.mycampusdock.dock.utils.NotiUtil;
import com.mycampusdock.dock.utils.Utils;
import com.mycampusdock.dock.views.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.mycampusdock.dock.activities.Login.PREF_KEY_EMAIL;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_LOGGED_IN;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_HASH_SCOPE;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_NAME;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_SUBSCRIPTIONS;
import static com.mycampusdock.dock.activities.WelcomeActivity.PREF_KEY_INTERESTS;
import static com.mycampusdock.dock.activities.WelcomeActivity.PREF_KEY_USER_INTERESTS;
import static com.mycampusdock.dock.services.CustomFirebaseMessagingService.FLAG_SHOW_NEW_BULLETIN;
import static com.mycampusdock.dock.services.CustomFirebaseMessagingService.FLAG_SHOW_NEW_EVENT;
import static com.mycampusdock.dock.services.CustomFirebaseMessagingService.FLAG_SHOW_NEW_NOTIFICATION;
import static com.mycampusdock.dock.services.CustomFirebaseMessagingService.PREF_KEY_APK_INVALID;

public class MainActivity extends BaseActivity implements BaseFragment.FragmentNavigation, FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    private FrameLayout contentFrame;
    private Toolbar toolbar;
    private TextView topTitle;
    private int[] mTabIconsSelected = {
            R.drawable.ic_home_black_72dp,
            R.drawable.ic_work_black_24dp,
            R.drawable.ic_public_black_24dp,
            R.drawable.ic_notifications_black_24dp,
            R.drawable.ic_person_black_24dp};
    private String[] TABS;
    private TabLayout bottomTabLayout;
    private Realm realm;
    private FragNavController mNavController;
    private FragmentHistory fragmentHistory;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appbar;
    private AppBarLayout.Behavior behavior;
    int w, h;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private SharedPreferences pref;
    public static final String PREF_KEY_FIRST_LOGIN = "first_time";
    public static final String PREF_KEY_USER_DATA = "user_data";
    public static final String PREF_KEY_SELECTED_TABS = "selected_tabs_data";
    private Socket socket;
    private String selection;
    public static int TAB = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String action = getIntent().getAction();
        NotiUtil.clearNotifications(getApplicationContext());
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        realm = Realm.getDefaultInstance();
        if (pref.getBoolean(PREF_KEY_APK_INVALID, false)) {
            showVersionAlert();
        } else if (!pref.getBoolean(PREF_KEY_LOGGED_IN, false)) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        } else if (!pref.getBoolean(PREF_KEY_USER_DATA, false)) {
            startActivity(new Intent(getApplicationContext(), Profile.class));
            finish();
        } else if (!pref.getBoolean(PREF_KEY_USER_INTERESTS, false)) {
            if (pref.getString(PREF_KEY_INTERESTS, "").length() == 0) {
                saveInterests();
            }
            startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
            finish();
        } else {
            if(action != null)
            if (action.equals(FLAG_SHOW_NEW_EVENT)) {
                TAB = 0;
            } else if (action.equals(FLAG_SHOW_NEW_BULLETIN)) {
                TAB = 1;
            } else if (action.equals(FLAG_SHOW_NEW_NOTIFICATION)) {
                TAB = 3;
            }

            contentFrame = findViewById(R.id.content_frame);
            toolbar = findViewById(R.id.toolbar);
            topTitle = findViewById(R.id.topTitle);
            TABS = getResources().getStringArray(R.array.tab_name);
            bottomTabLayout = findViewById(R.id.bottom_tab_layout);
            if (pref.getBoolean(PREF_KEY_FIRST_LOGIN, true)) {
                saveDummyData();
                try {
                    handleInterests();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pref.edit().putBoolean(PREF_KEY_FIRST_LOGIN, false).apply();
            }
            ChatApplication app = (ChatApplication) getApplication();
            socket = app.getSocket();
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeOut);
            socket.on("feedback", onFeedBack);
            socket.on("new_message_in_bulletin", onNewBulletinMessage);
            socket.connect();
            selection = pref.getString(PREF_KEY_SELECTED_TABS, "");
            if (selection.equals("")) {
                selection = getTemplateSelectionData();
                pref.edit().putString(PREF_KEY_SELECTED_TABS, selection).apply();
            }

            initToolbar();
            try {
                initTab(new JSONObject(selection));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fragmentHistory = new FragmentHistory();

            mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content_frame)
                    .transactionListener(this)
                    .rootFragmentListener(this, TABS.length)
                    .build();
            switchTab(TAB);

            bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    fragmentHistory.push(tab.getPosition());
                    switchTab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    mNavController.clearStack();
                    switchTab(tab.getPosition());
                }
            });

            coordinatorLayout = findViewById(R.id.coordinator);
            appbar = findViewById(R.id.appbar);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
            behavior = (AppBarLayout.Behavior) params.getBehavior();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            h = displayMetrics.heightPixels;
            w = displayMetrics.widthPixels;
        }
        getStoragePermission();
    }

    private void showVersionAlert() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.PinDialog);
        final AlertDialog alertDialog = dialogBuilder.setCancelable(true).setView(dialogView).create();
        alertDialog.show();
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("Too old version of Application");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText("Please try to update the application.If problem persists please try clearing app data.");
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        Button negative = dialogView.findViewById(R.id.negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7190);
        }
    }

    private void handleInterests() throws JSONException {
        RealmResults<Interest> interests = realm.where(Interest.class).findAll();
        String str = "";
        for (Interest interest : interests) {
            FirebaseMessaging.getInstance().subscribeToTopic(interest.getName());
            str = str + interest.getName() + ",";
        }
        String subscriptions = pref.getString(PREF_KEY_SUBSCRIPTIONS, "") + ",";
        String scope = subscriptions + str;
        String[] s = scope.split(",");
        JSONObject scopeObject = new JSONObject();
        for (String sc : s) {
            scopeObject.put(sc, "-1");
        }
        JSONObject finalHashScope = new JSONObject();
        finalHashScope.put("0", scopeObject);
        finalHashScope.put("1", scopeObject);
        finalHashScope.put("2", scopeObject);
        pref.edit().putString(PREF_KEY_SUBSCRIPTIONS, scope).apply();
        pref.edit().putString(PREF_KEY_HASH_SCOPE, finalHashScope.toString()).apply();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initTab(JSONObject selected) {
        if (bottomTabLayout != null) {
            for (int i = 0; i < TABS.length; i++) {
                console.log("<< INFLATING TABS");
                bottomTabLayout.addTab(bottomTabLayout.newTab());
                TabLayout.Tab tab = bottomTabLayout.getTabAt(i);
                if (tab != null) {
                    try {
                        tab.setCustomView(getTabView(i, selected.getBoolean("" + i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private View getTabView(int position, boolean isNotified) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item_bottom, null);
        ImageView icon = view.findViewById(R.id.tab_icon);
        //TextView notification = view.findViewById(R.id.noti);
        icon.setImageDrawable(Utils.setDrawableSelector(MainActivity.this, mTabIconsSelected[position], mTabIconsSelected[position]));
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void switchTab(int position) {
        mNavController.switchTab(position);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {

            if (fragmentHistory.isEmpty()) {
                super.onBackPressed();
            } else {
                if (fragmentHistory.getStackSize() > 1) {
                    int position = fragmentHistory.popPrevious();
                    switchTab(position);
                    updateTabSelection(position);

                } else {
                    switchTab(0);
                    updateTabSelection(0);
                    fragmentHistory.emptyStack();
                }
            }
        }
    }


    private void updateTabSelection(int currentTab) {
        for (int i = 0; i < TABS.length; i++) {
            console.log("<< UPDATING TABS");
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }


    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar();
        }
    }

    private void updateToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        Drawable backArrow = getResources().getDrawable(R.drawable.lnr_arrow_left);
        backArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);
    }


    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar();
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case FragNavController.TAB1:
                return new HomeFragment();
            case FragNavController.TAB2:
                return new BulletinsFragment();
            case FragNavController.TAB3:
                return new DiscoverFragment();
            case FragNavController.TAB4:
                return new NewsFragment();
            case FragNavController.TAB5:
                return new ProfileFragment();
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    private void resetAppBar(boolean reset) {
        appbar.setExpanded(reset, false);
    }

    public void updateToolbarTitle(String title, boolean resetAppBar) {
        resetAppBar(resetAppBar);
        topTitle.setText(title);
    }


    private void saveDummyData() {
        realm.beginTransaction();
        RealmList<RealmData> dummyContact = new RealmList<>();
        dummyContact.add(new RealmData("Dock", "help@mycampusdock.com"));
        Event one = new Event(
                "event_dummy",
                "A Dummy Event",
                "Event happen all time around you, with the help of this app you can see" +
                        " events that are suitable for you, easily get enrolled for the one you are " +
                        "interested in and get cool offers also.\n\nNow Event participation becomes very easy.",
                "2018-05-31T14:23:50.000Z",
                "2018-05-31T14:23:50.000Z",
                "Dock Inc.",
                "FREE",
                "1",
                null,
                "Dummy",
                "OGIL",
                "global",
                "R.drawable.test_icon",
                dummyContact,
                "128",
                "Dock Inc.",
                "Dock",
                1527864070902L,
                true);
        one.setDummy(true);
        one.setBanner(R.drawable.event3);
        realm.copyToRealm(one);
        Bulletin b2 = new Bulletin("feedback", "Give Feedback", "This is an initiative by <b>Dock Team</b> to enhance the information flow within colleges. Please provide your valuable feedback by messaging how you feel about the same. Feel feel to give advice", "ADMIN", null, null, 1527864070900L);
        b2.setImportant(true);
        b2.setDummy(true);
        realm.copyToRealm(b2);
        Notification n1 = new Notification("noti-one", "Dock Inc.", "Hey " + pref.getString(PREF_KEY_NAME, "Dummy") + ", Hope you are enjoying the beta testing, Please give your feedback, it is very important.", 1527864070902L);
        n1.setDummy(true);
        realm.copyToRealm(n1);
        realm.commitTransaction();
    }

    private void saveInterests() {
        realm.beginTransaction();
        Interest i1 = new Interest("Business", "" + R.drawable.business, 101);
        Interest i9 = new Interest("Technology", "" + R.drawable.technology, 101);
        Interest i2 = new Interest("Dance", "" + R.drawable.dance, 101);
        Interest i3 = new Interest("Music", "" + R.drawable.music, 101);
        Interest i4 = new Interest("Gaming", "" + R.drawable.gaming, 101);
        Interest i5 = new Interest("Photography", "" + R.drawable.photography, 101);
        Interest i6 = new Interest("Fitness", "" + R.drawable.gym, 101);
        Interest i7 = new Interest("Art", "" + R.drawable.art, 101);
        Interest i8 = new Interest("Coding", "" + R.drawable.coding, 101);
        realm.copyToRealm(i1);
        realm.copyToRealm(i2);
        realm.copyToRealm(i3);
        realm.copyToRealm(i4);
        realm.copyToRealm(i5);
        realm.copyToRealm(i6);
        realm.copyToRealm(i7);
        realm.copyToRealm(i8);
        realm.copyToRealm(i9);
        realm.commitTransaction();
        pref.edit().putString(PREF_KEY_INTERESTS, "Done").apply();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            console.log("<< CONNECT >>");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            console.log("<< DISCONNECT >>");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            console.log("<< CONNECTION ERROR >>");
        }
    };

    private Emitter.Listener onTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            console.log("<< CONNECTION TIME OUT >>");
        }
    };

    private Emitter.Listener onFeedBack = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject obj = new JSONObject((String) args[0]);
                if (obj.getBoolean("error")) {
                    console.error(" << SOCKET CONNECTION >> " + obj.getString("mssg"));
                } else {
                    int type = obj.getInt("data");
                    console.log("Data:" + args[0]);
                    switch (type) {
                        case 101:
                            console.log("<< CONNECTION SUCCESS >>");
                            break;
                        case 105:
                            handleChatBackupData(obj.getJSONObject("payload"));
                            break;
                        case 500:
                            console.log("<< AUTH REQUIRED >>");
                            auth();
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private String getTemplateSelectionData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("0", false);
            obj.put("1", false);
            obj.put("2", false);
            obj.put("3", false);
            obj.put("4", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    private void handleChatBackupData(final JSONObject obj) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        final String bulletin_id = obj.getString("bulletin_id");
        final Bulletin bulletin = realm.where(Bulletin.class).equalTo("bulletinId", bulletin_id).findFirst();
        final long previousUpdateTime = bulletin.getUpdatedOn();
        console.error("<< Last updated on :" + previousUpdateTime);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    boolean changes = false;
                    JSONArray mssgs = obj.getJSONArray("messages");
                    console.log("<< Message Data :" + mssgs);
                    for (int i = 0; i < mssgs.length(); i++) {
                        ChatMessage message = new ChatMessage().parseFromJSON(mssgs.getJSONObject(i), bulletin_id, pref.getString(PREF_KEY_EMAIL, ""));
                        if (message.getTimestamp() > previousUpdateTime) {
                            bulletin.insertChat(message);
                            changes = true;
                        }
                    }

                    console.error("<< Have Changes :" + changes);
                    if (changes) {
                        bulletin.setUpdatedOn(bulletin.getChats().get(bulletin.getChats().size() - 1).getTimestamp());
                        bulletin.insertChat(new ChatMessage("", "", "", previousUpdateTime + 1, "", "", 103));
                        bulletin.setHaveNewChanges(true);
                        console.error("We have changes");
                    } else if (!changes && bulletin.isHaveNewChanges()) {
                        bulletin.insertChat(new ChatMessage("", "", "", previousUpdateTime + 1, "", "", 103));
                        console.error("We have changes duplicate array came");
                    } else {
                        bulletin.setHaveNewChanges(false);
                        console.error("We have no changes");
                    }
                    bulletin.setSession_synchronised(ChatApplication.SESSION_ID);
                    realm.copyToRealmOrUpdate(bulletin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        realm.close();
    }

    private Emitter.Listener onNewBulletinMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                final JSONObject obj = new JSONObject((String) args[0]);
                if (obj.getBoolean("error")) {
                    console.error(" << SOCKET ON MESSAGE >> " + obj.getString("mssg"));
                } else {
                    console.log("Data:" + args[0]);
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                final String bulletin_id = obj.getString("bulletin");
                                final Bulletin bulletin = realm.where(Bulletin.class).equalTo("bulletinId", bulletin_id).findFirst();
                                ChatMessage message = new ChatMessage().parseFromJSON(obj, bulletin_id, pref.getString(PREF_KEY_EMAIL, ""));
                                bulletin.insertChat(message);
                                bulletin.setHaveNewChanges(true);
                                bulletin.setUpdatedOn(message.getTimestamp());
                                realm.copyToRealmOrUpdate(bulletin);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    realm.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void auth() {
        Realm realm = Realm.getDefaultInstance();
        final RealmResults<Bulletin> data = realm.where(Bulletin.class).findAll();
        if (data.size() > 0) {
            String ids = data.get(0).getBulletinId();
            for (int i = 0; i < data.size(); i++) {
                ids = ids + "," + data.get(i).getBulletinId();
            }
            try {
                JSONObject obj = new JSONObject().put("token", pref.getString(PREF_KEY_TOKEN, "")).put("bulletins", ids);
                socket.emit("auth request", obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
