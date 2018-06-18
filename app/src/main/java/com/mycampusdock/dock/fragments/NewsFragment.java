package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.MainActivity;
import com.mycampusdock.dock.adapters.NotificationAdapter;
import com.mycampusdock.dock.objects.Notification;
import com.mycampusdock.dock.services.CustomFirebaseMessagingService;
import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.mycampusdock.dock.Config.REACH_TYPE_NOTIFICATION;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_HASH_SCOPE;


public class NewsFragment extends BaseFragment {

    private RecyclerView notificationList;
    private Realm realm;
    private List<Notification> notifications;
    public static boolean resetAppBar = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences pref;
    public static boolean isNotRefreshed = true;

    public static NewsFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        notificationList = view.findViewById(R.id.notification_list);
        swipeRefreshLayout = view.findViewById(R.id.container);
        pref = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        realm = Realm.getDefaultInstance();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    refreshNews();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        if (isNotRefreshed) {
            isNotRefreshed = false;
            try {
                markReached();
                refreshNews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ((MainActivity) getActivity()).updateToolbarTitle("News", resetAppBar);
        notifications = new ArrayList<>();
        RealmResults<Notification> data = realm.where(Notification.class).sort("updatedOn", Sort.DESCENDING).findAllAsync();
        for (Notification n : data) {
            notifications.add(n);
        }
        NotificationAdapter adapter = new NotificationAdapter(getContext(), data, new DockInterfaces.OnItemClickListener() {
            @Override
            public void onItemClicked(Object item, View view) {

            }
        });
        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getContext());
        notificationList.setLayoutManager(gridLayoutManager);
        notificationList.setHasFixedSize(true);
        notificationList.setItemViewCacheSize(30);
        notificationList.setDrawingCacheEnabled(true);
        notificationList.setAdapter(adapter);
        return view;
    }

    private void markReached(){
        RealmResults<Notification> realmResults = realm.where(Notification.class).equalTo("isReached", false).findAll();
        for(Notification n : realmResults){
            CustomFirebaseMessagingService.markReached(REACH_TYPE_NOTIFICATION, n.getNotificationId(), pref, false, getContext());
        }
    }

    private void refreshNews() throws JSONException {
        final String hashScope = pref.getString(PREF_KEY_HASH_SCOPE, "");
        final JSONObject scopeObj = new JSONObject(hashScope);
        Utils.checkScopeValidation(scopeObj.getJSONObject("2").toString(), "2", pref, getContext(), 7170, new DockInterfaces.OnQueryCompleteListener() {
            @Override
            public void onQueryCompleted(boolean status, int type) {
                if (type == 7170) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}