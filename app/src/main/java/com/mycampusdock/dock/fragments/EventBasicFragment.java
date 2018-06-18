package com.mycampusdock.dock.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.objects.RealmData;
import com.mycampusdock.dock.utils.LocalStore;
import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.mycampusdock.dock.activities.EventActivity.PARAM_EVENT_ID;
import static com.mycampusdock.dock.activities.Login.HEADER_TOKEN;
import static com.mycampusdock.dock.activities.Login.PARAM_EMAIL;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_EMAIL;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;
import static com.mycampusdock.dock.activities.Profile.PARAM_ROLL;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_ROLL;

public class EventBasicFragment extends Fragment implements DockInterfaces.FragmentLifecycle {
    private Event event;
    private String event_id;
    private SharedPreferences pref;
    private TextView title, date, venue, cost, creator, categories, contact;
    private Button enroll;
    private ExpandableTextView description;
    private AlertDialog alertDialog;
    private TextView disable;
    private ProgressBar progress;
    private Realm realm;
    private Context context;

    public EventBasicFragment() {
    }

    public EventBasicFragment setFragment(String id, SharedPreferences pref, Context context) {
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
        View view = inflater.inflate(R.layout.fragment_event_basic, container, false);
        title = view.findViewById(R.id.title);
        date = view.findViewById(R.id.date);
        venue = view.findViewById(R.id.venue);
        cost = view.findViewById(R.id.cost);
        contact = view.findViewById(R.id.contact);
        enroll = view.findViewById(R.id.enroll);
        description = view.findViewById(R.id.description);
        categories = view.findViewById(R.id.categories);
        creator = view.findViewById(R.id.creator);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateUI();
    }

    private void populateUI() {
        realm = Realm.getDefaultInstance();
        event = realm.where(Event.class).equalTo("eventId", event_id).findFirst();
        title.setText(event.getTitle());
        description.setText(Html.fromHtml(event.getDescription()));
        categories.setText("" + event.getCategory().toUpperCase());
        creator.setText(event.getCreatedBy());
        Date start = Utils.fromISO8601UTC(event.getStart());
        Date end = Utils.fromISO8601UTC(event.getEnd());
        String d = start.getDate() + " " + Utils.parseMonth(start.getMonth()) + " - " + end.getDate() + " " + Utils.parseMonth(end.getMonth()) + " " + Utils.parseYear(start.getYear());
        String time = Utils.getTimeElapsed(start.getTime(), false);
        if (time.charAt(0) == '-') {
            date.setText(d + " (" + time.substring(1, time.length()) + " left)");
        } else {
            date.setText(d + " (Closed)");
        }
        venue.setText(event.getVenue());
        cost.setText("FREE - Available Only with Dock");
        String co = "";
        for (RealmData c : event.getCoordinators()) {
            co = co + " \uD83D\uDCDE <b> " + c.getData() + "</b> : <u>" + c.getExtra() + "</u><br>";
        }
        contact.setText(Html.fromHtml(co));
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = Utils.fromISO8601UTC(event.getStart());
                if (event.isExpired()) {
                    Toast.makeText(context, "Event is closed now and not valid for enrollment.", Toast.LENGTH_SHORT).show();
                } else if (System.currentTimeMillis() > date.getTime()) {
                    realm.beginTransaction();
                    event.setExpired(true);
                    realm.copyToRealmOrUpdate(event);
                    realm.commitTransaction();
                } else {
                    if (event.isEnrolled()) {
                        unEnroll();
                    } else {
                        askForEnroll();
                    }
                }
            }
        });
        if (!event.isDummy())
            checkEnrollment(event.getEventId());
        else {
            if (event.isEnrolled())
                disable();
            else
                enabled();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        console.log("<<FRAGMENT RESUMED >>");
    }

    private void checkEnrollment(final String event_id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_EVENT_CHECK_ENROLL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                        try {
                            final JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                Toast.makeText(context, "Something Went wrong", Toast.LENGTH_SHORT).show();
                            } else {
                                if (obj.getBoolean("data")) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            Event e = realm.where(Event.class).equalTo("eventId", event_id).findFirst();
                                            e.setEnrolled(true);
                                            try {
                                                e.setReach("" + obj.getInt("reach"));
                                                e.setCount(obj.getInt("people"));
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                            }
                                            realm.copyToRealmOrUpdate(e);
                                        }
                                    });
                                    disable();
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            Event e = realm.where(Event.class).equalTo("eventId", event_id).findFirst();
                                            e.setEnrolled(false);
                                            try {
                                                e.setReach("" + obj.getInt("reach"));
                                                e.setCount(obj.getInt("people"));
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                            }
                                            realm.copyToRealmOrUpdate(e);
                                        }
                                    });
                                    enabled();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                console.error(pref.getString(PREF_KEY_TOKEN, ""));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(PARAM_EVENT_ID, event_id);
                params.put(PARAM_ROLL, pref.getString(PREF_KEY_ROLL, ""));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    private void enroll(final String eventId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_EVENT_ENROLL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                Toast.makeText(context, "Something Went wrong", Toast.LENGTH_SHORT).show();
                                enabled();
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                Event e = realm.where(Event.class).equalTo("eventId", eventId).findFirst();
                                e.setEnrolled(true);
                                realm.commitTransaction();
                                realm.close();
                                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                                disable();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                params.put(PARAM_EMAIL, pref.getString(PREF_KEY_EMAIL, ""));
                params.put(PARAM_ROLL, pref.getString(PREF_KEY_ROLL, ""));
                params.put(PARAM_EVENT_ID, eventId);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    private void unEnroll() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PinDialog);
        alertDialog = dialogBuilder.setCancelable(false).setView(dialogView).create();
        alertDialog.show();
        disable = dialogView.findViewById(R.id.disableView);
        progress = dialogView.findViewById(R.id.progress);
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("Cancel Enrollment");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText("Do you want to cancel your enrollment for this event?");
        message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disable.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                cancelEnrollment(event.getEventId());
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

    private void askForEnroll() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PinDialog);
        alertDialog = dialogBuilder.setCancelable(false).setView(dialogView).create();
        alertDialog.show();
        disable = dialogView.findViewById(R.id.disableView);
        progress = dialogView.findViewById(R.id.progress);
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("Confirm Enrollment");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText("Your basic details like name, email , roll number will be shared with the creator of this event.");
        message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setText("Confirm");
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enroll.setBackground(context.getDrawable(R.drawable.login_button_disable_grey));
                enroll.setText("Please Wait...");
                enroll.setTextColor(getResources().getColor(R.color.cement_black));
                enroll(event.getEventId());
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

    private void enabled() {
        if (getActivity() != null) {
            enroll.setText("Click to Enroll");
            enroll.setTextColor(getResources().getColor(R.color.white));
            enroll.setBackground(context.getDrawable(R.drawable.login_button_enable));
        }
    }

    private void disable() {
        if (getActivity() != null) {
            enroll.setText("Successfully Enrolled");
            enroll.setTextColor(getResources().getColor(R.color.my_blue));
            enroll.setBackground(getActivity().getDrawable(R.drawable.login_button_disable));
        }
    }

    private void cancelEnrollment(final String eventId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_EVENT_UNENROLL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        alertDialog.dismiss();
                        console.log(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                Toast.makeText(context, "Something Went wrong", Toast.LENGTH_SHORT).show();
                                disable();
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                Event e = realm.where(Event.class).equalTo("eventId", eventId).findFirst();
                                e.setEnrolled(false);
                                realm.commitTransaction();
                                realm.close();
                                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                                enabled();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Error:" + error);
                alertDialog.dismiss();
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
                params.put(PARAM_EMAIL, pref.getString(PREF_KEY_EMAIL, ""));
                params.put(PARAM_ROLL, pref.getString(PREF_KEY_ROLL, ""));
                params.put(PARAM_EVENT_ID, eventId);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    @Override
    public void onPauseFragment() {
        console.log("Fragment Paused");
    }

    @Override
    public void onResumeFragment() {
        console.log("Fragment Resumed");
    }
}
