package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mycampusdock.dock.DockInterfaces;
import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Notification;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class NotificationAdapter extends RealmRecyclerViewAdapter<Notification, NotificationAdapter.ViewHolder> {
    private Context context;
    private final DockInterfaces.OnItemClickListener listener;

    public NotificationAdapter(Context context, OrderedRealmCollection<Notification> items, DockInterfaces.OnItemClickListener listener) {
        super(items, true);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(context, getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView creator, title;

        public ViewHolder(View view) {
            super(view);
            creator = view.findViewById(R.id.creator);
            title = view.findViewById(R.id.title);
        }

        public void bind(final Context context, final Notification item, final DockInterfaces.OnItemClickListener listener) {
            creator.setText("@" + item.getCreator());
            creator.setTextColor(Helper.getColorFor(item.getCreator(), context));
            title.setText(Html.fromHtml(item.getData()));
        }
    }
}