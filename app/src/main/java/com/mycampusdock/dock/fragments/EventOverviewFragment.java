package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.utils.LocalStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.mycampusdock.dock.activities.EventActivity.PARAM_FEEDBACK_COMMENT;
import static com.mycampusdock.dock.activities.EventActivity.PARAM_FEEDBACK_RATING;
import static com.mycampusdock.dock.activities.EventActivity.PARAM_PAYLOAD;
import static com.mycampusdock.dock.activities.Login.HEADER_TOKEN;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;
import static com.mycampusdock.dock.services.CustomFirebaseMessagingService.PARAM_ID;

public class EventOverviewFragment extends Fragment implements DockInterfaces.FragmentLifecycle{

    private String event_id;
    private TextView overview;
    private EditText feedback;
    private RatingBar rating;
    private Button submit;
    private LinearLayout feedback_form;
    private Event event;
    private Realm realm;
    private SharedPreferences pref;
    private Context context;

    public EventOverviewFragment() {
    }

    public EventOverviewFragment setFragment(String id, SharedPreferences pref, Context context) {
        this.event_id = id;
        this.pref = pref;
        this.context = context;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_overview, container, false);
        overview = view.findViewById(R.id.overview);
        feedback = view.findViewById(R.id.feedback);
        rating = view.findViewById(R.id.rating);
        submit = view.findViewById(R.id.submit);
        feedback_form = view.findViewById(R.id.feedback_form);
        realm = Realm.getDefaultInstance();
        event = realm.where(Event.class).equalTo("eventId", event_id).findFirst();
        checkOverview();
        return view;
    }

    private void checkOverview(){
        if (event.isEnrolled()) {
            overview.setText("Congratulations! You are all set for the event.\nDon't forget to review the event here.");
            feedback_form.setVisibility(View.VISIBLE);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String f = feedback.getText().toString();
                    if (f != null && f.length() > 3) {
                        try {
                            submitFeedback(event.getEventId(), f, rating.getRating());
                            realm.beginTransaction();
                            event.setRated(true);
                            realm.copyToRealmOrUpdate(event);
                            realm.commitTransaction();
                            feedback_form.setVisibility(View.GONE);
                            Toast.makeText(context, "Thanks for reviewing", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "Put Something in feedback", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            overview.setText("You are not enrolled for this event.\nPlease enroll first to see the overviews!");
            feedback_form.setVisibility(View.GONE);
        }
    }

    private void submitFeedback(final String eventId, final String feedbackString, final float rating) throws JSONException {
        final JSONObject obj = new JSONObject();
        obj.put(PARAM_FEEDBACK_COMMENT, feedbackString);
        obj.put(PARAM_FEEDBACK_RATING, rating);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_EVENT_FEEDBACK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Error:" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(HEADER_TOKEN, pref.getString(PREF_KEY_TOKEN, ""));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(PARAM_PAYLOAD, obj.toString());
                params.put(PARAM_ID, eventId);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    @Override
    public void onPauseFragment() {
        console.log("Fragment Paused");
    }

    @Override
    public void onResumeFragment() {
        event = realm.where(Event.class).equalTo("eventId", event_id).findFirst();
        checkOverview();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }
}
