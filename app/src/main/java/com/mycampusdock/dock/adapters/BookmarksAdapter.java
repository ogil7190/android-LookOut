package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.console;
import com.mycampusdock.dock.objects.Event;
import com.mycampusdock.dock.utils.GlideApp;
import com.mycampusdock.dock.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static com.mycampusdock.dock.Config.URL_BASE_MEDIA;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Event event, int pos, View view);
    }

    private final List<Event> items;
    private Context context;
    private final OnItemClickListener listener;

    public BookmarksAdapter(Context context, List<Event> items, OnItemClickListener listener) {
        this.items = items;
        console.log("<<<<< Size:"+items.size());
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(context, items.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title, overview;
        private CardView container;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
            container = view.findViewById(R.id.container);
            overview = view.findViewById(R.id.overview);
        }

        public void bind(final Context context, final Event item, final int pos, final OnItemClickListener listener) {
            title.setText(item.getTitle());
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, pos, itemView);
                }
            });

            title.setText(item.getTitle());
            overview.setText(item.getCreatedBy() + "  ●  " + item.getReach() + " Views  ●  " + Utils.getTimeElapsed(item.getUpdatedOn(), false));
            if (item.getPosters().size() > 0) {
                File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Dock");
                File f = new File(folder, item.getPosters().get(0).getData());
                if (f.exists() && f.length() > 0)
                    Glide.with(context).load(f).into(icon);
                else {
                    icon.setImageResource(R.drawable.event3);
                    GlideApp.with(context).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).load(URL_BASE_MEDIA + item.getPosters().get(0).getData()).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Glide.with(context).load(resource).into(icon);
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
                Glide.with(context).load(item.getBanner()).into(icon);
            }
        }
    }
}