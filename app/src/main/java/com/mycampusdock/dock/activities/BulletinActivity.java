package com.mycampusdock.dock.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.mycampusdock.dock.Config;
import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.adapters.BulletinsChatTestAdapter;
import com.mycampusdock.dock.adapters.BulletinsFileAdapter;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.objects.ChatMessage;
import com.mycampusdock.dock.objects.RealmData;
import com.mycampusdock.dock.services.CustomFirebaseMessagingService;
import com.mycampusdock.dock.utils.ChatApplication;
import com.mycampusdock.dock.utils.DownloadFileFromURL;
import com.mycampusdock.dock.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Socket;

import static com.mycampusdock.dock.Config.REACH_TYPE_BULLETIN;

public class BulletinActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private Button send;
    private Socket socket;
    private Bulletin bulletin;
    private String id;
    private RecyclerView files, chats;
    private TextView title, creator, network_status;
    private ExpandableTextView description;
    private RelativeLayout container;
    private EditText message;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ImageView show_files;
    private Realm realm;
    private BulletinsFileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bulletin);
        pref = getSharedPreferences(Config.SHARED_PREF_NAME, MODE_PRIVATE);
        send = findViewById(R.id.send);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        creator = findViewById(R.id.creator);
        container = findViewById(R.id.container);
        files = findViewById(R.id.files);
        network_status = findViewById(R.id.network_status);
        chats = findViewById(R.id.chats);
        message = findViewById(R.id.message);
        show_files = findViewById(R.id.show_files);
        Bundle data = getIntent().getExtras();
        id = data.getString("bulletin_id");
        realm = Realm.getDefaultInstance();
        RealmResults<Bulletin> items = realm.where(Bulletin.class).equalTo("bulletinId", id).findAllAsync();
        bulletin = items.get(0);
        if (bulletin.getFiles().size() > 0) {
            show_files.setVisibility(View.VISIBLE);
        }
        ChatApplication app = (ChatApplication) getApplication();
        socket = app.getSocket();
        if (!socket.connected()) {
            socket.connect();
        }
        adapter = new BulletinsFileAdapter(getApplicationContext(), bulletin.getFiles(), new BulletinsFileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RealmData item, final int pos, View view) {
                if (Utils.fileExists(item.getData())) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = myMime.getMimeTypeFromExtension(fileExt(Utils.fileUrl(item.getData()).getAbsolutePath()).substring(1));
                    newIntent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".my.package.name.provider", Utils.fileUrl(item.getData())), mimeType);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        startActivity(newIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "No app available for this type of file.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL("Dock", new DownloadFileFromURL.OnFileDownloadListener() {
                        @Override
                        public void onFileDownloadStarted() {
                            Toast.makeText(getApplicationContext(), "Downloading..", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFileDownloadProgress(String value) {
                            console.log("Progress value:" + value);
                        }

                        @Override
                        public void onFileDownloadError() {
                            Toast.makeText(getApplicationContext(), "Error Downloading", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFileDownloadComplete() {
                            Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(pos);
                        }
                    });
                    downloadFileFromURL.execute(item.getData());
                }
            }
        });

        files.setNestedScrollingEnabled(false);
        files.setAdapter(adapter);
        files.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

        final RealmResults<ChatMessage> messages = realm.where(ChatMessage.class).equalTo("relatedTo", bulletin.getBulletinId()).sort("timestamp", Sort.ASCENDING).findAllAsync();
        final BulletinsChatTestAdapter chat_adapter = new BulletinsChatTestAdapter(getApplicationContext(), messages, bulletin);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        chats.setLayoutManager(manager);
        chats.setAdapter(chat_adapter);
        messages.addChangeListener(new RealmChangeListener<RealmResults<ChatMessage>>() {
            @Override
            public void onChange(RealmResults<ChatMessage> chatMessages) {
                if (chat_adapter.getItemCount() > 0)
                    chats.scrollToPosition(chat_adapter.getItemCount() - 1);
            }
        });

        title.setText(bulletin.getTitle());
        description.setText(Html.fromHtml(bulletin.getDescription()));
        creator.setText(bulletin.getCreator());
        container.setBackgroundColor(Helper.getColorFor(bulletin.getCreator(), getApplicationContext()));
        if (socket != null)
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (sendMessage()) {
                            message.setText("");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        items.addChangeListener(new RealmChangeListener<RealmResults<Bulletin>>() {
            @Override
            public void onChange(RealmResults<Bulletin> bulletins) {
                int size = chat_adapter.getItemCount();
                if (size > 0)
                    chats.scrollToPosition(-1);
            }
        });

        show_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_files.setScaleY(-1 * show_files.getScaleY());
                files.setVisibility(files.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        if (socket == null || !socket.connected()) {
            socket.connect();
            send.setEnabled(false);
            send.setBackground(getDrawable(R.drawable.button_normal_disable));
            network_status.setVisibility(View.VISIBLE);
        }

        if (!bulletin.isViewed()) {
            CustomFirebaseMessagingService.markReached(REACH_TYPE_BULLETIN, bulletin.getBulletinId(), pref, true, getApplicationContext());
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private boolean sendMessage() throws JSONException {
        if (message.getText().toString().length() > 0) {
            String sentMessage = new JSONObject()
                    .put("bulletin", id)
                    .put("message", message.getText().toString())
                    .toString();
            console.log("<<< message sent : " + sentMessage);
            socket.emit("send to bulletin", sentMessage);
            return true;
        }
        else {
            Toast.makeText(getApplicationContext(), "Put Something in message", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Bulletin bulletin = realm.where(Bulletin.class).equalTo("bulletinId", id).findFirst();
        bulletin.setHaveNewChanges(false);
        realm.commitTransaction();
        realm.close();
    }
}
