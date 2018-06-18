package com.mycampusdock.dock.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.EventActivity;
import com.mycampusdock.dock.adapters.BookmarksAdapter;
import com.mycampusdock.dock.objects.Event;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ProfileMyEventsFragment extends Fragment {
    private RecyclerView events;
    private Realm realm;

    public ProfileMyEventsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        events = view.findViewById(R.id.events);
        realm = Realm.getDefaultInstance();
        RealmResults<Event> events_data = realm.where(Event.class).equalTo("isEnrolled", true).sort("updatedOn", Sort.DESCENDING).findAll();
        BookmarksAdapter adapter = new BookmarksAdapter(getContext(), events_data, new BookmarksAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event, int pos, View view) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("event_id", event.getEventId());
                startActivity(intent);
            }
        });
        events.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        events.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}