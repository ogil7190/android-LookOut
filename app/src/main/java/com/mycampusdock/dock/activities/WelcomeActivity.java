package com.mycampusdock.dock.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.adapters.CategoryAdapter;
import com.mycampusdock.dock.objects.Interest;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.mycampusdock.dock.activities.Profile.PREF_KEY_GENDER;
import static com.mycampusdock.dock.activities.Profile.PREF_KEY_NAME;

public class WelcomeActivity extends AppCompatActivity {
    private ImageView profile_icon;
    private TextView welcome_text;
    private RecyclerView categories;
    private SharedPreferences pref;
    private Button done;
    private Realm realm;
    private int type;
    public static final String PREF_KEY_USER_INTERESTS = "user_interests";
    public static final String PREF_KEY_INTERESTS = "interests_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        profile_icon = findViewById(R.id.profile_icon);
        welcome_text = findViewById(R.id.welcome_text);
        categories = findViewById(R.id.categories);
        done = findViewById(R.id.done);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
            type = bundle.getInt("type");
        realm = Realm.getDefaultInstance();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.edit().putBoolean(PREF_KEY_USER_INTERESTS, true).apply();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        if(type == 0){
            welcome_text.setText("Welcome " + pref.getString(PREF_KEY_NAME, "Dummy") + ",\n\n Please select the categories you would like to get informed about.");
        } else {
            done.setVisibility(View.GONE);
            welcome_text.setText("Hey " + pref.getString(PREF_KEY_NAME, "Dummy") + "\n\nThese are your selected interests. You can edit them if you want.");
        }
        if (pref.getString(PREF_KEY_GENDER, "").equals("M"))
            Glide.with(this).load(R.drawable.man).into(profile_icon);
        else
            Glide.with(this).load(R.drawable.girl).into(profile_icon);
        RealmResults<Interest> data = realm.where(Interest.class).findAllAsync();
        CategoryAdapter option_adapter = new CategoryAdapter(getApplicationContext(), data, new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Interest item, int pos, View view) {
                if (item.getType() == 101) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            item.setType(102);
                            realm.copyToRealmOrUpdate(item);
                        }
                    });
                    realm.close();
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            item.setType(101);
                            realm.copyToRealmOrUpdate(item);
                        }
                    });
                    realm.close();
                }
            }
        });
        categories.setAdapter(option_adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        categories.setLayoutManager(manager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
