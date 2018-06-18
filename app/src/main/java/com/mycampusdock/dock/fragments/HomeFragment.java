package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.EventActivity;
import com.mycampusdock.dock.activities.MainActivity;
import com.mycampusdock.dock.adapters.EventsAdapter;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.services.CustomFirebaseMessagingService;
import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.mycampusdock.dock.Config.REACH_TYPE_EVENT;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_HASH_SCOPE;


public class HomeFragment extends BaseFragment {
    private RecyclerView event_list;
    private int fragCount;
    public static boolean resetAppBar = true;
    public static long scroll = 0;
    private EventsAdapter adapter;

    private Realm realm;
    private TextView newIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences pref;
    public static boolean isNotRefreshed = true;

    public static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        event_list = view.findViewById(R.id.event_list);
        newIndicator = view.findViewById(R.id.new_indicator);
        swipeRefreshLayout = view.findViewById(R.id.container);
        pref = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        realm = Realm.getDefaultInstance();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    refreshEvents();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        if (isNotRefreshed) {
            isNotRefreshed = false;
            try {
                markReached();
                refreshEvents();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Bundle args = getArguments();
        if (args != null) {
            fragCount = args.getInt(ARGS_INSTANCE);
        }
        populateData();
        ((MainActivity) getActivity()).updateToolbarTitle("Home", resetAppBar);
        return view;
    }

    private void markReached() {
        RealmResults<Event> realmResults = realm.where(Event.class).equalTo("isReached", false).findAll();
        for (Event e : realmResults) {
            CustomFirebaseMessagingService.markReached(REACH_TYPE_EVENT, e.getEventId(), pref, false, getContext());
        }
    }

    private void refreshEvents() throws JSONException {
        final String hashScope = pref.getString(PREF_KEY_HASH_SCOPE, "");
        final JSONObject scopeObj = new JSONObject(hashScope);
        Utils.checkScopeValidation(scopeObj.getJSONObject("0").toString(), "0", pref, getContext(), 7190, new DockInterfaces.OnQueryCompleteListener() {
            @Override
            public void onQueryCompleted(boolean status, int type) {
                if (type == 7190) {
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


    private void populateData() {
        RealmResults<Event> data = realm.where(Event.class).sort("updatedOn", Sort.DESCENDING).findAllAsync();

        adapter = new EventsAdapter(getContext(), data, new DockInterfaces.OnItemClickListener() {
            @Override
            public void onItemClicked(Object item, View view) {
                Event event = (Event) item;
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("event_id", event.getEventId());
                startActivity(intent);
            }
        });

        LinearLayoutManager gridLayoutManager = new LinearLayoutManager(getContext());
        event_list.setLayoutManager(gridLayoutManager);
        event_list.setHasFixedSize(true);
        event_list.setItemViewCacheSize(30);
        event_list.setDrawingCacheEnabled(true);
        event_list.setAdapter(adapter);
        event_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scroll += dy;
                if (scroll == 0) {
                    resetAppBar = true;
                    newIndicator.setVisibility(View.GONE);
                } else
                    resetAppBar = false;
            }
        });
        newIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_list.scrollToPosition(0);
                newIndicator.setVisibility(View.GONE);
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
