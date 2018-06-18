package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.BulletinActivity;
import com.mycampusdock.dock.activities.MainActivity;
import com.mycampusdock.dock.adapters.BulletinsAdapter;
import com.mycampusdock.dock.adapters.BulletinsNotchAdapter;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.services.CustomFirebaseMessagingService;
import com.mycampusdock.dock.utils.ChatApplication;
import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Socket;

import static com.mycampusdock.dock.Config.REACH_TYPE_BULLETIN;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_HASH_SCOPE;


public class BulletinsFragment extends BaseFragment {
    public static boolean resetAppBar = true;

    private RecyclerView bulletin_list;
    private Realm realm;
    public static long scroll = 0;
    private RecyclerView notch;
    private BulletinsAdapter adapter;

    private HashMap<String, String> notches;
    private List<String> notchItems;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences pref;
    int fragCount;
    String creator;
    private static boolean isNotRefreshed = true;
    private Socket socket;

    public static BulletinsFragment newInstance(int instance, String creator) {
        Bundle args = new Bundle();
        args.putInt(ARGS_INSTANCE, instance);
        args.putString(ARGS_EXTRA, creator);
        BulletinsFragment fragment = new BulletinsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public BulletinsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulletins, container, false);
        pref = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        bulletin_list = view.findViewById(R.id.bulletins_list);
        notch = view.findViewById(R.id.notch);
        swipeRefreshLayout = view.findViewById(R.id.container);
        realm = Realm.getDefaultInstance();
        Bundle args = getArguments();
        if (args != null) {
            fragCount = args.getInt(ARGS_INSTANCE);
            creator = args.getString(ARGS_EXTRA);
        }
        ((MainActivity) getActivity()).updateToolbarTitle((fragCount == 0) ? "Bulletin" : creator, resetAppBar);
        ChatApplication app = (ChatApplication) getActivity().getApplication();
        socket = app.getSocket();
        if (!socket.connected()) {
            socket.connect();
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    refreshBulletins();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (isNotRefreshed) {
            isNotRefreshed = false;
            try {
                refreshBulletins();
                markReached();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (fragCount == 0) {
            populateData();
        } else {
            populateSingleCreator();
        }
        return view;
    }

    private void markReached() {
        RealmResults<Bulletin> realmResults = realm.where(Bulletin.class).equalTo("isReached", false).findAll();
        for (Bulletin b : realmResults) {
            CustomFirebaseMessagingService.markReached(REACH_TYPE_BULLETIN, b.getBulletinId(), pref, false, getContext());
        }
    }

    private void refreshBulletins() throws JSONException {
        try {
            final String hashScope = pref.getString(PREF_KEY_HASH_SCOPE, "");
            final JSONObject scopeObj = new JSONObject(hashScope);
            Utils.checkScopeValidation(scopeObj.getJSONObject("1").toString(), "1", pref, getContext(), 7160, new DockInterfaces.OnQueryCompleteListener() {
                @Override
                public void onQueryCompleted(boolean status, int type) {
                    if (type == 7160) {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateData() {
        notches = new HashMap<>();
        notchItems = new ArrayList<>();
        RealmResults<Bulletin> data = realm.where(Bulletin.class).sort("updatedOn", Sort.DESCENDING).findAllAsync();
        for (Bulletin b : data) {
            if (ChatApplication.SESSION_ID.equals(b.getSession_synchronised())) {
                console.log("Synchronised");
            } else {
                try {
                    socket.emit("synchronize", new JSONObject().put("bulletin", b.getBulletinId()).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            console.log("<<< Forming new data set");
            notches.put(b.getCreator(), b.getCreator());
        }

        adapter = new BulletinsAdapter(getContext(), data, new DockInterfaces.OnItemClickListener() {
            @Override
            public void onItemClicked(Object item, View view) {
                Bulletin bulletin = (Bulletin) item;
                Intent intent = new Intent(getContext(), BulletinActivity.class);
                intent.putExtra("bulletin_id", bulletin.getBulletinId());
                startActivity(intent);
            }
        });
        bulletin_list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        });
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        bulletin_list.setLayoutManager(gridLayoutManager);
        bulletin_list.setAdapter(adapter);
        bulletin_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scroll += dy;
                if (scroll == 0)
                    resetAppBar = true;
                else
                    resetAppBar = false;
            }
        });

        for (String s : notches.keySet()) {
            notchItems.add(s);
        }

        notch.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        notch.setAdapter(new BulletinsNotchAdapter(getContext(), notchItems, new BulletinsNotchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String creator, View view) {
                mFragmentNavigation.pushFragment(BulletinsFragment.newInstance(fragCount + 1, creator));
            }
        }));
    }

    private void populateSingleCreator() {
        notches = new HashMap<>();
        notchItems = new ArrayList<>();
        realm = Realm.getDefaultInstance();
        RealmResults<Bulletin> data = realm.where(Bulletin.class).equalTo("creator", creator).sort("updatedOn", Sort.DESCENDING).findAllAsync();
        for (Bulletin b : data) {
            notches.put(b.getCreator(), b.getCreator());
        }

        adapter = new BulletinsAdapter(getContext(), data, new DockInterfaces.OnItemClickListener() {
            @Override
            public void onItemClicked(Object item, View view) {
                Bulletin bulletin = (Bulletin) item;
                Intent intent = new Intent(getContext(), BulletinActivity.class);
                intent.putExtra("bulletin_id", bulletin.getBulletinId());
                startActivity(intent);
            }
        });
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        bulletin_list.setLayoutManager(gridLayoutManager);
        bulletin_list.setAdapter(adapter);
        bulletin_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scroll += dy;
                if (scroll == 0)
                    resetAppBar = true;
                else
                    resetAppBar = false;
            }
        });
        notch.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
