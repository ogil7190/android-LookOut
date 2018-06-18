package com.mycampusdock.dock.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mycampusdock.dock.utils.LocalStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

import static com.mycampusdock.dock.activities.MainActivity.PREF_KEY_USER_DATA;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_GENDER;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_NAME;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_ROLL;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_SCOPE;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_SUBSCRIPTIONS;

public class Login extends AppCompatActivity {
    private boolean goodCollege = false;
    private boolean goodEmail = false;
    private TextView emailError, help;
    private Spinner college;
    private EditText email;
    private ProgressBar loading;
    private Button login;
    private String[] colleges = {"Select College ⬇️", "MRIIRS", "MREI", "AMITY"};
    private int selection = 0;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        console.setContext(getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        college = findViewById(R.id.college);
        email = findViewById(R.id.email);
        login = findViewById(R.id.login);
        loading = findViewById(R.id.loading);
        emailError = findViewById(R.id.email_error);
        help = findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help();
            }
        });
        emailError.setPaintFlags(emailError.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        emailError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailHelp();
            }
        });
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_college, colleges);
        adapter.setDropDownViewResource(R.layout.dropdown_college);
        college.setAdapter(adapter);
        college.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selection = position;
                if (position == 0)
                    goodCollege = false;
                else
                    goodCollege = true;

                if (goodEmail && goodCollege) {
                    enableLogin();
                } else {
                    disableLogin();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                goodEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (goodEmail && goodCollege) {
                    enableLogin();
                } else {
                    disableLogin();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodCollege && goodEmail) {
                    disable();
                    String e = email.getText().toString();
                    String c = colleges[selection];
                    verify(e, c);
                }
            }
        });
        getStoragePermission();
    }

    private void emailHelp(){
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.PinDialog);
        alertDialog = dialogBuilder.setCancelable(true).setView(dialogView).create();
        alertDialog.show();
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("E-mail Help");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText(Config.EMAIL_HELP_STRING);
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void help(){
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.PinDialog);
        alertDialog = dialogBuilder.setCancelable(true).setView(dialogView).create();
        alertDialog.show();
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("How can we help?");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText(Config.HELP_STRING);
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_COLLEGE = "college";
    public static final String HEADER_TOKEN = "x-access-token";
    public static final String PARAM_PIN = "pin";

    private void verify(final String email, final String college) {
        this.college.setEnabled(false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_SIGNIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        console.log("<<< Response:"+response);
                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (responseObj.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), "Try again later", Toast.LENGTH_SHORT).show();
                                enable();
                            } else {
                                String token = responseObj.getString("token");
                                enable();
                                askPin(email, college, token);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                console.log("Error:" + error);
                Toast.makeText(getApplicationContext(), "Check your Internet", Toast.LENGTH_LONG).show();
                enable();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(PARAM_EMAIL, email);
                params.put(PARAM_COLLEGE, college);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(getApplicationContext()).add(stringRequest);
    }

    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7190);
        }
    }

    private TextView error, disable;
    private ProgressBar progressBar;
    private EditText p1, p2, p3, p4;
    private AlertDialog alertDialog;

    private void askPin(final String email, final String college, final String token) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.verify_pin, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.PinDialog);
        alertDialog = dialogBuilder.setCancelable(false).setView(dialogView).create();
        alertDialog.show();
        progressBar = dialogView.findViewById(R.id.pinProgress);
        progressBar.setVisibility(View.GONE);
        p1 = dialogView.findViewById(R.id.p1);
        p2 = dialogView.findViewById(R.id.p2);
        p3 = dialogView.findViewById(R.id.p3);
        p4 = dialogView.findViewById(R.id.p4);
        error = dialogView.findViewById(R.id.textError);
        disable = dialogView.findViewById(R.id.disableView);
        final TextView errorHelp = dialogView.findViewById(R.id.textHelp);
        errorHelp.setText("An e-mail with PIN is sent to " + email + ".\nPlease check Spam folder also for the e-mail.");
        p1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 == 1) {
                    p2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        p2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 == 1) {
                    p3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        p3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2 == 1) {
                    p4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        p4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (p4.getText().toString().length() > 0) {
                    error.setVisibility(View.GONE);
                    disable.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    String pin = p1.getText().toString() + p2.getText().toString() + p3.getText().toString() + p4.getText().toString();
                    verifyPin(pin, email, college, token);
                } else {
                    error.setVisibility(View.GONE);
                    disable.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public static final String PREF_KEY_LOGGED_IN = "is_logged_in";
    public static final String PREF_KEY_TOKEN = "api_token";
    public static final String PREF_KEY_COLLEGE = "college";
    public static final String PREF_KEY_EMAIL = "email";

    private void verifyPin(final String pin, final String email, final String college, final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.REQ_URL_VERIFY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (responseObj.getBoolean("error")) {
                                p1.setText("");
                                p2.setText("");
                                p3.setText("");
                                p4.setText("");
                                p1.requestFocus();
                                error.setVisibility(View.VISIBLE);
                                disable.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                            } else {
                                alertDialog.dismiss();
                                String newToken = responseObj.getString("token"); //acquire new token
                                console.error("<<< New Token:"+newToken);
                                pref.edit().putBoolean(PREF_KEY_LOGGED_IN, true).apply();
                                pref.edit().putString(PREF_KEY_TOKEN, newToken).apply();
                                pref.edit().putString(PREF_KEY_COLLEGE, college).apply();
                                pref.edit().putString(PREF_KEY_EMAIL, email).apply();
                                if (responseObj.get("data").toString().equals("{}")) {
                                    startActivity(new Intent(getApplicationContext(), Profile.class));
                                    finish();
                                } else {
                                    pref.edit().putBoolean(PREF_KEY_USER_DATA, true).apply();
                                    handleUserData(responseObj.getJSONObject("data"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                console.log("Error:" + e);
                p1.setText("");
                p2.setText("");
                p3.setText("");
                p4.setText("");
                p1.requestFocus();
                error.setVisibility(View.VISIBLE);
                disable.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Check your Internet", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(HEADER_TOKEN, token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(PARAM_EMAIL, email);
                params.put(PARAM_PIN, pin);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Config.REQ_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        LocalStore.getRequestQueueInstance(getApplicationContext()).add(stringRequest);
    }

    private void handleUserData(JSONObject obj) throws JSONException {
        String roll = obj.getString("roll_no");
        String scope = obj.getString("scope");
        String name = obj.getString("username");
        String college = colleges[selection]; //TODO change later fix api
        String email = obj.getString("email");
        String gender = obj.getString("gender");
        pref.edit().putString(PREF_KEY_ROLL, roll.toLowerCase()).apply();
        pref.edit().putString(PREF_KEY_NAME, name).apply();
        pref.edit().putString(PREF_KEY_COLLEGE, college).apply();
        pref.edit().putString(PREF_KEY_EMAIL, email).apply();
        pref.edit().putString(PREF_KEY_SCOPE, scope).apply();
        pref.edit().putBoolean(PREF_KEY_USER_DATA, true).apply();
        pref.edit().putString(PREF_KEY_GENDER, gender).apply();
        scope = scope + "," + roll.replace("/", "-").toLowerCase().trim() + "," + email.replace("@", "-").toLowerCase().trim();
        pref.edit().putString(PREF_KEY_SUBSCRIPTIONS, scope).apply();
        handleScope(scope);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void handleScope(String scope) {
        String[] topics = scope.split(",");
        for (String s : topics) {
            FirebaseMessaging.getInstance().subscribeToTopic(s);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("global");
    }

    private void enable() {
        login.setTextColor(getResources().getColor(R.color.white));
        loading.setVisibility(View.GONE);
        login.setEnabled(true);
    }

    private void disable() {
        login.setTextColor(getResources().getColor(R.color.my_blue));
        loading.setVisibility(View.VISIBLE);
        login.setEnabled(false);
    }

    private void enableLogin() {
        login.setBackground(getDrawable(R.drawable.login_button_enable));
        login.setTextColor(getResources().getColor(R.color.white));
    }

    private void disableLogin() {
        login.setBackground(getDrawable(R.drawable.login_button_disable));
        login.setTextColor(getResources().getColor(R.color.my_blue_light));
    }
}
