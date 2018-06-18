package com.mycampusdock.dock.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.fragments.ProfileFragment;
import com.mycampusdock.dock.utils.LocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mycampusdock.dock.activities.Login.HEADER_TOKEN;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_COLLEGE;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_EMAIL;
import static com.mycampusdock.dock.activities.Login.PREF_KEY_TOKEN;
import static com.mycampusdock.dock.activities.MainActivity.PREF_KEY_USER_DATA;

public class Profile extends AppCompatActivity {
    private EditText name, roll;
    private Spinner dept, target, gender;
    private SharedPreferences pref;
    private String college, email;
    private ArrayList<String> dept_data, target_data;
    private JSONArray data;
    private Button next;
    private String dept_name, target_name, gender_name;
    private String scope;
    private ProgressBar loading;
    public static final String PREF_KEY_NAME = "user_name";
    public static final String PREF_KEY_ROLL = "user_roll";
    public static final String PREF_KEY_GENDER = "gender";
    public static final String PREF_KEY_SCOPE = "scope";
    public static final String PREF_KEY_SUBSCRIPTIONS = "subscriptions";
    public static final String PREF_KEY_HASH_SCOPE = "hash_scope";

    private String[] genders = {"Select Your Gender ⬇️", "M for Male \uD83D\uDC68", "F for Female \uD83D\uDC69"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        name = findViewById(R.id.full_name);
        next = findViewById(R.id.next);
        loading = findViewById(R.id.progress);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name != null) {
                    disable();
                    scope = getResolvedScope(dept_name, target_name);
                    updateUserData(name.getText().toString().trim(), roll.getText().toString().toLowerCase().trim(), gender_name);
                }
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name!=null) {
                    enableNext();
                } else
                    disableNext();
            }
        });
        roll = findViewById(R.id.roll);
        roll.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name!=null) {
                    enableNext();
                } else
                    disableNext();
            }
        });
        gender = findViewById(R.id.gender);
        ArrayAdapter gender_adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_college, genders);
        gender_adapter.setDropDownViewResource(R.layout.dropdown_college);
        gender.setAdapter(gender_adapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    gender_name = null;
                else
                    gender_name = genders[position].substring(0, 1);
                if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name!=null) {
                    enableNext();
                } else
                    disableNext();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dept = findViewById(R.id.dept);
        target = findViewById(R.id.target);
        dept_data = new ArrayList<>();
        target_data = new ArrayList<>();
        dept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position > 0) {
                        dept_name = dept_data.get(position);
                        populateClass(position);
                        console.log("Dept:" + dept_name);
                    } else {
                        dept_name = null;
                    }

                    if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name!=null) {
                        enableNext();
                    } else
                        disableNext();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        college = pref.getString(PREF_KEY_COLLEGE, "");
        email = pref.getString(PREF_KEY_EMAIL, "");
        target.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    target_name = target_data.get(position);
                } else
                    target_name = null;
                if (dept_name != null && target_name != null && name.length() > 3 && roll.length() > 10 && gender_name!=null) {
                    enableNext();
                } else
                    disableNext();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadHierarchy();
    }

    public static final String PARAM_NAME = "username";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_ROLL = "roll_no";
    public static final String PARAM_SCOPE = "scope";
    public static final String PARAM_COLLEGE = "college";
    public static final String PARAM_GENDER = "gender";

    private void updateUserData(final String name, final String roll, final String gender) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_UPDATE_USER_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log(response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                console.log("Error:" + obj.get("mssg"));
                                ProfileFragment.logout(pref);
                                finish();
                            } else {
                                String newToken = obj.getString("token");
                                pref.edit().putString(PREF_KEY_TOKEN, newToken).apply();
                                pref.edit().putString(PREF_KEY_NAME, name).apply();
                                pref.edit().putString(PREF_KEY_ROLL, roll).apply();
                                pref.edit().putString(PREF_KEY_SCOPE, scope).apply();
                                pref.edit().putString(PREF_KEY_GENDER, gender).apply();
                                scope = scope + "," + roll.replace("/", "-").toLowerCase().trim() + "," + email.replace("@", "-").toLowerCase().trim();
                                pref.edit().putString(PREF_KEY_SUBSCRIPTIONS, scope).apply();
                                pref.edit().putBoolean(PREF_KEY_USER_DATA, true).apply();
                                handleScope(scope);
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            enable();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Error:" + error);
                enable();
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
                params.put(PARAM_NAME, name);
                params.put(PARAM_COLLEGE, pref.getString(PREF_KEY_COLLEGE, ""));
                params.put(PARAM_ROLL, roll.toLowerCase());
                params.put(PARAM_GENDER, gender);
                params.put(PARAM_EMAIL, pref.getString(PREF_KEY_EMAIL, ""));
                params.put(PARAM_SCOPE, scope);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(getApplicationContext()).add(stringRequest);
    }

    private void handleScope(String scope) {
        String[] topics = scope.split(",");
        for (String s : topics) {
            FirebaseMessaging.getInstance().subscribeToTopic(s);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("global");
    }

    private String getResolvedScope(String dept, String target) {
        String f, s;
        String d[] = dept.split("-");
        String t[] = target.split("-");
        f = d[0];
        s = d[0];
        for (int i = 1; i < d.length; i++) {
            f = f + "-" + d[i];
            s = s + "," + f;
        }

        for (int i = 0; i < t.length; i++) {
            f = f + "-" + t[i];
            s = s + "," + f;
        }
        return s;
    }

    public static final String PARAM_COLLEGE_ID = "college_id";

    private void loadHierarchy() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_HIERARCHY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error")) {
                                console.log("Error:" + obj.get("mssg"));
                            } else {
                                data = obj.getJSONArray("data");
                                parseData(data);
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
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(PARAM_COLLEGE_ID, college);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(getApplicationContext()).add(stringRequest);
    }

    private void populateClass(int pos) throws JSONException {
        target_data.clear();
        target_data.add("Select Class ⬇️");
        JSONObject obj = (JSONObject) data.get(pos - 1);
        JSONArray arr = obj.getJSONArray("children");
        for (int i = 0; i < arr.length(); i++) {
            String s = (String) arr.get(i);
            target_data.add(s);
        }
        ArrayAdapter target_adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_college, target_data);
        target_adapter.setDropDownViewResource(R.layout.dropdown_college);
        target.setAdapter(target_adapter);
    }

    private void parseData(JSONArray data) throws JSONException {
        dept_data.add("Select Dept ⬇️");
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = (JSONObject) data.get(i);
            String d = obj.getString("label");
            dept_data.add(d);
        }
        ArrayAdapter dept_adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_college, dept_data);
        dept_adapter.setDropDownViewResource(R.layout.dropdown_college);
        dept.setAdapter(dept_adapter);
        target_data.add("Select Class");
        ArrayAdapter target_adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_college, target_data);
        target_adapter.setDropDownViewResource(R.layout.dropdown_college);
        target.setAdapter(target_adapter);
    }

    private void enable() {
        next.setTextColor(getResources().getColor(R.color.white));
        next.setText("Try Again");
        next.setEnabled(true);
        loading.setVisibility(View.GONE);
    }

    private void disable() {
        next.setText("Please Wait...");
        loading.setVisibility(View.VISIBLE);
        next.setEnabled(false);
    }

    private void enableNext() {
        next.setBackground(getDrawable(R.drawable.login_button_enable));
        next.setTextColor(getResources().getColor(R.color.white));
    }

    private void disableNext() {
        next.setBackground(getDrawable(R.drawable.login_button_disable));
        next.setTextColor(getResources().getColor(R.color.my_blue_light));
    }
}
