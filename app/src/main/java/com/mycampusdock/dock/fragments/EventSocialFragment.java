package com.mycampusdock.dock.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Event;

import io.realm.Realm;

public class EventSocialFragment extends Fragment implements DockInterfaces.FragmentLifecycle {
    private String eventId;
    private Realm realm;
    private Event event;
    private CardView share;
    private TextView message;
    private TextView countView;
    public EventSocialFragment() {
    }

    public EventSocialFragment setFragment(String id) {
        this.eventId = id;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_social, container, false);
        share = view.findViewById(R.id.share);
        countView = view.findViewById(R.id.social_count);
        message = view.findViewById(R.id.social_message);
        realm = Realm.getDefaultInstance();
        fillUI();
        message.setText("\"The best thing about crowd is that it make events amazing.\"");
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareEvent();
            }
        });
        return view;
    }

    private void shareEvent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.setPackage("com.whatsapp");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, Check out this cool event on Dock\nhttps://mycampusdock.com/share/" + eventId);
        try {
            getActivity().startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Oops, WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillUI(){
        event = realm.where(Event.class).equalTo("eventId", eventId).findFirst();
        countView.setText("" + event.getCount() + " people are going to this event");
    }

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {
        fillUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
