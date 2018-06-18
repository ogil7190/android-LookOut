package com.mycampusdock.dock.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mycampusdock.dock.R;
import com.mycampusdock.dock.activities.MainActivity;
import com.mycampusdock.dock.activities.WelcomeActivity;
import com.mycampusdock.dock.adapters.OptionsAdapter;
import com.mycampusdock.dock.objects.RealmData;

import java.util.ArrayList;
import java.util.List;

import static com.mycampusdock.dock.fragments.ProfileFragment.logout;

public class ProfileBasicFragment extends Fragment {
    private RecyclerView options;
    private SharedPreferences pref;

    private static List<RealmData> optionsList;

    {
        optionsList = new ArrayList<>();
        optionsList.add(new RealmData("About You", "" + R.drawable.ic_person_black_24dp));
        optionsList.add(new RealmData("Interests", "" + R.drawable.ic_apps_black_24dp));
        optionsList.add(new RealmData("Settings", "" + R.drawable.ic_settings_black_24dp));
        optionsList.add(new RealmData("Logout Now", "" + R.drawable.ic_lock_black_24dp));
    }

    public ProfileBasicFragment() {
    }

    public ProfileBasicFragment setFragment(SharedPreferences pref) {
        this.pref = pref;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_basic, container, false);
        options = view.findViewById(R.id.options);
        OptionsAdapter option_adapter = new OptionsAdapter(getContext(), optionsList, new OptionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RealmData item, int pos, View view) {
                switch (pos) {
                    case 0:
                        Toast.makeText(getContext(), "We ❤️ You ", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Intent intent = new Intent(getContext(), WelcomeActivity.class);
                        intent.putExtra("type", 7190);
                        startActivity(intent);
                        break;
                    case 2:
                        Toast.makeText(getContext(), "I hope there is nothing to set currently!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        askForLogout();
                        break;
                }
            }
        });
        options.setAdapter(option_adapter);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        options.setLayoutManager(gridLayoutManager);
        return view;
    }

    private void askForLogout() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.PinDialog);
        final AlertDialog alertDialog = dialogBuilder.setCancelable(true).setView(dialogView).create();
        alertDialog.show();
        final TextView disable = dialogView.findViewById(R.id.disableView);
        final ProgressBar progress = dialogView.findViewById(R.id.progress);
        TextView title = dialogView.findViewById(R.id.alert_title);
        title.setText("Logout");
        TextView message = dialogView.findViewById(R.id.alert_message);
        message.setText("Are you sure, you want to logout?");
        message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Button positive = dialogView.findViewById(R.id.positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disable.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                alertDialog.dismiss();
                getActivity().finish();
                logout(pref);
                startActivity(new Intent(getContext(), MainActivity.class));
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

}