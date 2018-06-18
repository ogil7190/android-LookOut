package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.utils.GlideApp;
import com.mycampusdock.dock.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

import static com.mycampusdock.dock.Config.URL_BASE_MEDIA;

public class EventsAdapter extends RealmRecyclerViewAdapter<Event, EventsAdapter.MyViewHolder> {
    private Context context;
    private DockInterfaces.OnItemClickListener listener;

    public EventsAdapter(Context context, OrderedRealmCollection<Event> data, DockInterfaces.OnItemClickListener onItemClickListener) {
        super(data, true);
        this.context = context;
        this.listener = onItemClickListener;
    }

    @Override
    public EventsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card_plain, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventsAdapter.MyViewHolder holder, int position) {
        holder.bind(context, getItem(position), listener);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView overview;
        private RelativeLayout container;
        private ImageView poster, icon;
        private ImageButton bookmark;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            overview = view.findViewById(R.id.overview);
            container = view.findViewById(R.id.main_container);
            poster = view.findViewById(R.id.poster);
            icon = view.findViewById(R.id.creator_logo);
            bookmark = view.findViewById(R.id.bookmark);
        }

        public void bind(final Context context, final Event item, final DockInterfaces.OnItemClickListener listener) {
            title.setText(item.getTitle());
            overview.setText(item.getCreatedBy() + "  ●  " + item.getReach() + " Views  ●  " + Utils.getTimeElapsed(item.getUpdatedOn(), false));
            try {
                if (item.getPosters().size() > 0) {
                    File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
                    File f = new File(folder, item.getPosters().get(0).getData());
                    if (f.exists() && f.length() > 0)
                        Glide.with(context).load(f).into(poster);
                    else {
                        poster.setImageResource(R.drawable.event3);
                        GlideApp.with(context).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).load(URL_BASE_MEDIA + item.getPosters().get(0).getData()).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Glide.with(context).load(resource).into(poster);
                                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
                                if (!folder.exists()) {
                                    folder.mkdirs();
                                }
                                folder = new File(folder, item.getPosters().get(0).getData());
                                if (folder.exists()) {
                                    folder.delete();
                                }
                                try {
                                    FileOutputStream fos = new FileOutputStream(folder);
                                    resource.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        });
                    }
                } else {
                    Glide.with(context).load(item.getBanner()).into(poster);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            TextDrawable drawable = TextDrawable.builder().buildRound("" + item.getCreatedBy().charAt(0), Helper.getColorFor(item.getCreatedBy(), context));
            icon.setImageDrawable(drawable);
            container.setOnClickListener(new View.OnClickListener()

            {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item, v);
                }
            });

            if(item.isBookmarked()){
                bookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_black_24dp));
            } else {
                bookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_border_black_24dp));
            }

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.isBookmarked())
                        bookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_black_24dp));
                    else
                        bookmark.setImageDrawable(context.getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    item.setBookmarked(!item.isBookmarked());
                    realm.commitTransaction();
                    realm.close();
                }
            });
        }
    }
}