package com.mycampusdock.dock.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.objects.Notification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.mycampusdock.dock.activities.Login.HEADER_TOKEN;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;
import static com.mycampusdock.dock.activities.Profile.PARAM_SCOPE;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_HASH_SCOPE;

/**
 * Created by f22labs on 07/03/17.
 */

public class Utils {
    public static final String PARAM_TYPE = "type";
    private static JSONObject hashScope;
    static long time;

    public static void checkScopeValidation(final String scope, final String type, final SharedPreferences pref, final Context context, final int requestType, final DockInterfaces.OnQueryCompleteListener listener) {
        console.log(scope);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_SCOPE_VALIDATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                obj = obj.getJSONObject("data");
                                hashScope = obj;
                                JSONArray keys = obj.names();
                                if (keys != null) {
                                    String sc = keys.getString(0);
                                    for (int i = 1; i < keys.length(); i++) {
                                        sc = sc + "," + keys.getString(i);
                                    }
                                    getDataFromScope(sc, type, pref, context, requestType, listener);
                                } else {
                                    handleHashScope(hashScope, type, pref);
                                    listener.onQueryCompleted(true, requestType);
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
                listener.onQueryCompleted(true, requestType);
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
                params.put(PARAM_SCOPE, scope);
                params.put(PARAM_TYPE, type);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    public static boolean fileExists(String file) {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
        folder = new File(folder, file);
        if (folder.exists())
            return true;
        else
            return false;
    }

    public static String parseChatDate(Date date) {
        return "" + date.getHours() + ":" + date.getMinutes() + ", " + parseMonth(date.getMonth()) + " " + date.getDate();
    }

    public static File fileUrl(String localFile) {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
        folder = new File(folder, localFile);
        if (folder.exists()) {
            return folder;
        } else
            return null;
    }

    public static void getDataFromScope(final String scope, final String type, final SharedPreferences pref, Context context, final int requestType, final DockInterfaces.OnQueryCompleteListener listener) {
        time = System.currentTimeMillis();
        console.log(scope);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_DATA_FROM_SCOPE_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                        console.log("Time in ms.." + (System.currentTimeMillis() - time));
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                JSONArray arr = obj.getJSONArray("data");
                                parseData(arr, type);
                                handleHashScope(hashScope, type, pref);
                                listener.onQueryCompleted(true, requestType);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Error:" + error);
                listener.onQueryCompleted(true, requestType);
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
                params.put(PARAM_SCOPE, scope);
                params.put(PARAM_TYPE, type);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(context).add(stringRequest);
    }

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });
    }

    static class MySpannable extends ClickableSpan {

        private boolean isUnderline = true;

        /**
         * Constructor
         */
        public MySpannable(boolean isUnderline) {
            this.isUnderline = isUnderline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(isUnderline);
            ds.setColor(Color.parseColor("#1b76d3"));
        }

        @Override
        public void onClick(View widget) {


        }
    }

    public static String getTimeElapsed(long timestamp, boolean round) {
        long current = System.currentTimeMillis();
        long diff = current - timestamp;
        if(round){
            if(diff < 0){
                diff = diff * -1;
            }
        }
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays == 0) {
            if (diffHours == 0) {
                if (diffMinutes == 0) {
                    return diffSeconds + "sec";
                } else
                    return diffMinutes + "min";
            } else
                return diffHours + "hour";
        } else
            return diffDays + "day";
    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {

            ssb.setSpan(new MySpannable(false) {
                @Override
                public void onClick(View widget) {
                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "See Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, ".. See More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }

    private static void parseData(final JSONArray arr, final String type) throws JSONException {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                switch (type) {
                    case "0":
                        for (int i = 0; i < arr.length(); i++) {
                            try {
                                Event event = new Event().parseFromJson(arr.getJSONObject(i));
                                realm.copyToRealmOrUpdate(event);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "1":
                        for (int i = 0; i < arr.length(); i++) {
                            try {
                                Bulletin bulletin = new Bulletin().parseFromJSON(arr.getJSONObject(i));
                                realm.copyToRealmOrUpdate(bulletin);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "2":
                        for (int i = 0; i < arr.length(); i++) {
                            try {
                                Notification notification = new Notification().parseFromJSON(arr.getJSONObject(i));
                                realm.copyToRealmOrUpdate(notification);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });
    }

    private static void handleHashScope(JSONObject obj, String type, SharedPreferences pref) throws JSONException {
        JSONArray keys = obj.names();
        JSONObject original = new JSONObject(pref.getString(PREF_KEY_HASH_SCOPE, ""));
        JSONObject change = original.getJSONObject(type);
        if (keys != null) {
            for (int i = 0; i < keys.length(); i++) {
                change.put(keys.getString(i), obj.getString(keys.getString(i)));
            }
            original.put(type, change);
            pref.edit().putString(PREF_KEY_HASH_SCOPE, original.toString()).apply();
        }
    }

    public static final void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static final String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String parseMonth(int month) {
        switch (month) {
            case 0:
                return "JAN";
            case 1:
                return "FEB";
            case 2:
                return "MAR";
            case 3:
                return "APR";
            case 4:
                return "MAY";
            case 5:
                return "JUN";
            case 6:
                return "JUL";
            case 7:
                return "AUG";
            case 8:
                return "SEP";
            case 9:
                return "OCT";
            case 10:
                return "NOV";
            case 11:
                return "DEC";
        }
        return "MONTH";
    }

    public static int parseYear(int year) {
        return year + 1900;
    }

    public static Date fromISO8601UTC(String dateStr) {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
        try {
            return df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String getVersionName(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;

    }

    public static void setButtonBackgroundColor(Context context, Button button, int color) {
        if (Build.VERSION.SDK_INT >= 23) {
            button.setBackgroundColor(context.getResources().getColor(color, null));
        } else {
            button.setBackgroundColor(context.getResources().getColor(color));
        }
    }


    public static void setButtonBackgroundColor(Context context, TextView textView, int color) {

        if (Build.VERSION.SDK_INT >= 23) {
            textView.setBackgroundColor(context.getResources().getColor(color, null));
        } else {
            textView.setBackgroundColor(context.getResources().getColor(color));
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Bitmap getBitmapFromVector(Drawable drawable) {
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    public static Drawable setDrawableSelector(Context context, int normal, int selected) {
        Drawable state_normal = ContextCompat.getDrawable(context, normal);
        Drawable state_pressed = ContextCompat.getDrawable(context, selected);
        //Bitmap state_normal_bitmap = ((BitmapDrawable)state_normal).getBitmap();

        Bitmap state_normal_bitmap = getBitmapFromVector(state_normal);

        // Setting alpha directly just didn't work, so we draw a new bitmap!
        Bitmap disabledBitmap = Bitmap.createBitmap(
                state_normal.getIntrinsicWidth(),
                state_normal.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);

        Paint paint = new Paint();
        paint.setAlpha(126);
        canvas.drawBitmap(state_normal_bitmap, 0, 0, paint);

        BitmapDrawable state_normal_drawable = new BitmapDrawable(context.getResources(), disabledBitmap);

        StateListDrawable drawable = new StateListDrawable();

        drawable.addState(new int[]{android.R.attr.state_selected},
                state_pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                state_normal_drawable);

        return drawable;
    }


    public static StateListDrawable selectorRadioImage(Context context, Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, pressed);
        states.addState(new int[]{}, normal);
        //                imageView.setImageDrawable(states);
        return states;
    }

    public static StateListDrawable selectorRadioButton(Context context, int normal, int pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(pressed));
        states.addState(new int[]{}, new ColorDrawable(normal));
        return states;
    }

    public static ColorStateList selectorRadioText(Context context, int normal, int pressed) {
        ColorStateList colorStates = new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{pressed, normal});
        return colorStates;
    }


    public static StateListDrawable selectorRadioDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_checked}, pressed);
        states.addState(new int[]{}, normal);
        return states;
    }

    public static StateListDrawable selectorBackgroundColor(Context context, int normal, int pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        states.addState(new int[]{}, new ColorDrawable(normal));
        return states;
    }

    public static StateListDrawable selectorBackgroundDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{}, normal);
        return states;
    }

    public static ColorStateList selectorText(Context context, int normal, int pressed) {
        ColorStateList colorStates = new ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}, new int[]{}}, new int[]{pressed, normal});
        return colorStates;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
