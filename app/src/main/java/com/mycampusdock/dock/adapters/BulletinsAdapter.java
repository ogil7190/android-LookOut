package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Bulletin;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class BulletinsAdapter extends RealmRecyclerViewAdapter<Bulletin, BulletinsAdapter.ViewHolder> {
    private Context context;
    private DockInterfaces.OnItemClickListener listener;

    public BulletinsAdapter(Context context, OrderedRealmCollection<Bulletin> data, DockInterfaces.OnItemClickListener onItemClickListener) {
        super(data, true);
        this.context = context;
        this.listener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bulletin_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(context, getItem(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, notification, description, creator;
        private ImageView files;
        private RelativeLayout container;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            description = view.findViewById(R.id.description);
            notification = view.findViewById(R.id.notification);
            creator = view.findViewById(R.id.creator);
            container = view.findViewById(R.id.container);
            files = view.findViewById(R.id.files);
        }

        public void bind(final Context context, final Bulletin item, final int pos, final DockInterfaces.OnItemClickListener listener) {
            title.setText(item.getTitle());
            description.setText(Html.fromHtml(item.getDescription()));
            creator.setText(item.getCreator());
            container.setBackgroundColor(Helper.getColorFor(item.getCreator(), context));
            if (item.getFiles().size() > 0) {
                files.setVisibility(View.VISIBLE);
            } else {
                files.setVisibility(View.GONE);
            }

            if (item.isHaveNewChanges()) {
                notification.setVisibility(View.VISIBLE);
            } else {
                notification.setVisibility(View.GONE);
            }
            if (pos % 2 == 0) {
                description.setMaxLines(5);
            } else
                description.setMaxLines(7);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item, itemView);
                }
            });

            if (item.isImportant()) {
                title.setText(title.getText().toString() + " [IMPORTANT]");
            }
        }
    }
}