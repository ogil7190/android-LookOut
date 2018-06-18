package com.mycampusdock.dock.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.activities.MainActivity;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.fragments.ProfileFragment;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.objects.Notification;
import com.mycampusdock.dock.utils.LocalStore;
import com.mycampusdock.dock.utils.NotiUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.mycampusdock.dock.Config.REACH_TYPE_BULLETIN;
import static com.mycampusdock.dock.Config.REACH_TYPE_EVENT;
import static com.mycampusdock.dock.Config.REACH_TYPE_NOTIFICATION;
import static com.mycampusdock.dock.activities.Login.HEADER_TOKEN;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;


/**
 * Created by ogil on 14/01/18.
 */

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private SharedPreferences pref;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        console.setContext(getApplicationContext());
        console.error("From: " + remoteMessage.getFrom());
        if (remoteMessage == null) return;

        if (remoteMessage.getData().size() > 0) {
            console.error("RealmData Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                console.error("Exception: " + e.getMessage());
            }
        }
    }

    public static final String PAYLOAD_KEY_TYPE = "type";
    public static final String PAYLOAD_TYPE_EVENT = "event";
    public static final String PAYLOAD_TYPE_BULLETIN = "bulletin";
    public static final String PAYLOAD_TYPE_NOTIFICATION = "notification";
    public static final String TYPE_BULLETIN_MSSG = "bulletin_mssg";
    public static final String TYPE_REMOTE_CONFIG = "remote_config";
    public static final String PREF_KEY_APK_INVALID = "invalid_apk";
    public static final String PAYLOAD_KEY_CONTENT = "content";
    public static final String FLAG_NEW_UPDATE = "new_update";
    public static final String FLAG_SHOW_NEW_EVENT = "new_event";
    public static final String FLAG_SHOW_NEW_BULLETIN = "new_bulletin";
    public static final String FLAG_SHOW_NEW_NOTIFICATION = "new_notification";

    private void handleDataMessage(JSONObject obj) throws JSONException {
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        console.error("pushed json: " + obj.toString());
        String type = obj.getString(PAYLOAD_KEY_TYPE);
        JSONObject content = obj.getJSONObject(PAYLOAD_KEY_CONTENT);
        Realm realm;
        switch (type) {
            case PAYLOAD_TYPE_EVENT:
                Event e = new Event().parseFromJson(content);
                markReached(REACH_TYPE_EVENT, e.getEventId(), pref, false, getApplicationContext());
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealm(e);
                realm.commitTransaction();
                realm.close();
                if (!NotiUtil.isAppIsInBackground(getApplicationContext())) {
                    Intent notify = new Intent(FLAG_NEW_UPDATE);
                    notify.putExtra(FLAG_NEW_UPDATE, FLAG_SHOW_NEW_EVENT);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(notify);
                } else {
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                    resultIntent.setAction(FLAG_SHOW_NEW_EVENT);
                    resultIntent.putExtra(PAYLOAD_TYPE_EVENT, content.toString());
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (e.getPosters().size() > 0)
                        showNotificationMessageWithBigImage(getApplicationContext(), e.getTitle(), "Click to view the event", resultIntent, e.getPosters().get(0).getData());
                    else
                        showNotificationMessage(getApplicationContext(), e.getTitle(), "Click to View the Event.", resultIntent);
                }
                break;
            case PAYLOAD_TYPE_BULLETIN:
                Bulletin b = new Bulletin().parseFromJSON(content);
                markReached(Config.REACH_TYPE_BULLETIN, b.getBulletinId(), pref, false, getApplicationContext());
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealm(b);
                realm.commitTransaction();
                realm.close();
                if (!NotiUtil.isAppIsInBackground(getApplicationContext())) {
                    Intent notify = new Intent(FLAG_NEW_UPDATE);
                    notify.putExtra(FLAG_NEW_UPDATE, FLAG_SHOW_NEW_EVENT);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(notify);
                } else {
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                    resultIntent.setAction(FLAG_SHOW_NEW_BULLETIN);
                    resultIntent.putExtra(PAYLOAD_TYPE_BULLETIN, content.toString());
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    showNotificationMessage(getApplicationContext(), b.getTitle(), "Click to View this Bulletin.", resultIntent);
                }
                console.error("GOT A NEW BULLETIN");
                break;
            case PAYLOAD_TYPE_NOTIFICATION:
                Notification n = new Notification().parseFromJSON(content);
                markReached(Config.REACH_TYPE_NOTIFICATION, n.getNotificationId(), pref, false, getApplicationContext());
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealm(n);
                realm.commitTransaction();
                realm.close();
                if (!NotiUtil.isAppIsInBackground(getApplicationContext())) {
                    Intent notify = new Intent(FLAG_NEW_UPDATE);
                    notify.putExtra(FLAG_NEW_UPDATE, FLAG_SHOW_NEW_EVENT);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(notify);
                } else {
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                    resultIntent.setAction(FLAG_SHOW_NEW_NOTIFICATION);
                    resultIntent.putExtra(PAYLOAD_TYPE_NOTIFICATION, content.toString());
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    showNotificationMessage(getApplicationContext(), n.getData(), "Click to View this News.", resultIntent);
                }
                break;

            case TYPE_BULLETIN_MSSG:
                realm = Realm.getDefaultInstance();
                Bulletin bulletin = realm.where(Bulletin.class).equalTo("bulletinId", content.getString("bulletin_id")).findFirst();
                if (bulletin != null) {
                    if (!NotiUtil.isAppIsInBackground(getApplicationContext())) {
                        Intent notify = new Intent(FLAG_NEW_UPDATE);
                        notify.putExtra(FLAG_NEW_UPDATE, FLAG_SHOW_NEW_EVENT);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(notify);
                    } else {
                        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                        resultIntent.setAction(FLAG_SHOW_NEW_EVENT);
                        resultIntent.putExtra(PAYLOAD_TYPE_NOTIFICATION, content.toString());
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showNotificationMessage(getApplicationContext(), "Discussion Going On : " + bulletin.getTitle(), "Click to View the Discussion.", resultIntent);
                    }
                }
                break;

            case TYPE_REMOTE_CONFIG:
                switch (content.getString("configure")) {
                    case "reset":
                        ProfileFragment.logout(pref); //force logout
                        break;
                    case "invalidate":
                        pref.edit().putBoolean(PREF_KEY_APK_INVALID, true).apply(); //force to update
                        break;
                    case "force_update_token":
                        pref.edit().putString(PREF_KEY_TOKEN, content.getString("token")).apply(); //force to update token
                        break;
                }
                break;
        }
    }

    public static final String PARAM_REACH_TYPE = "type";
    public static final String PARAM_ID = "id";
    public static final String PARAM_KEY_VIEW = "view";

    public static void markReached(final int type, final String id, final SharedPreferences pref, final boolean viewed, final Context context) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_REACH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                return;
                            } else {
                                Realm realm = Realm.getDefaultInstance();
                                switch (type) {
                                    case REACH_TYPE_EVENT:
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                Event event = realm.where(Event.class).equalTo("eventId", id).findFirst();
                                                event.setReached(true);
                                                if (viewed)
                                                    event.setViewed(true);
                                                realm.copyToRealmOrUpdate(event);
                                            }
                                        });
                                        break;
                                    case REACH_TYPE_BULLETIN:
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                Bulletin bulletin = realm.where(Bulletin.class).equalTo("bulletinId", id).findFirst();
                                                bulletin.setReached(true);
                                                if (viewed)
                                                    bulletin.setViewed(true);
                                                realm.copyToRealmOrUpdate(bulletin);
                                            }
                                        });
                                        break;
                                    case REACH_TYPE_NOTIFICATION:
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                Notification notification = realm.where(Notification.class).equalTo("notificationId", id).findFirst();
                                                notification.setReached(true);
                                                realm.copyToRealmOrUpdate(notification);
                                            }
                                        });
                                        break;

                                }
                                realm.close();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Reach Error:" + error);
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
                params.put(PARAM_REACH_TYPE, "" + type);
                params.put(PARAM_ID, id);
                if (viewed)
                    params.put(PARAM_KEY_VIEW, "Viewed");
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    /**
     * Showing notification with text only
     */

    private void showNotificationMessage(Context context, String title, String message, Intent intent) {
        NotiUtil notificationUtils = new NotiUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, Intent intent, String imageUrl) {
        NotiUtil notificationUtils = new NotiUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, intent, imageUrl);
    }
}
