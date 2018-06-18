package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Bulletin;
import com.mycampusdock.dock.objects.ChatMessage;
import com.mycampusdock.dock.utils.Utils;

import java.util.Date;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class BulletinsChatTestAdapter extends RealmRecyclerViewAdapter<ChatMessage, RecyclerView.ViewHolder> {
    private final List<ChatMessage> items;
    private Context context;
    private Bulletin bulletin;

    public BulletinsChatTestAdapter(Context context, OrderedRealmCollection<ChatMessage> items, Bulletin bulletin) {
        super(items, true);
        this.items = items;
        this.context = context;
        this.bulletin = bulletin;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case 102:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view_other, parent, false);
                return new ViewHolder(v);
            case 101:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view, parent, false);
                return new ViewHolderOther(v);
            case 103:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view_noti, parent, false);
                return new NotiViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 102:
                ((ViewHolder) holder).bind(context, items.get(position));
                break;
            case 101:
                ((ViewHolderOther) holder).bind(context, items.get(position), bulletin.getCreator());
                break;
            case 103:
                ((NotiViewHolder) holder).bind(context, bulletin.isHaveNewChanges());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView sender;
        private TextView overview;

        public ViewHolder(View view) {
            super(view);
            message = view.findViewById(R.id.message);
            sender = view.findViewById(R.id.sender);
            overview = view.findViewById(R.id.overview);
        }

        public void bind(final Context context, final ChatMessage item) {
            sender.setText("" + item.getSenderName());
            sender.setTextColor(Helper.getColorFor("" + item.getSender(), context));
            message.setText(item.getMessage());
            Date date = new Date(item.getTimestamp());
            overview.setText(Utils.parseChatDate(date));
        }
    }

    static class NotiViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout container;

        public NotiViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.message_container);
        }

        public void bind(final Context context, final boolean flag) {
            if (flag) {
                container.setVisibility(View.VISIBLE);
            } else {
                container.setVisibility(View.GONE);
            }
        }
    }


    static class ViewHolderOther extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView overview;
        private LinearLayout message_container;

        public ViewHolderOther(View view) {
            super(view);
            message = view.findViewById(R.id.message);
            overview = view.findViewById(R.id.overview);
            message_container = view.findViewById(R.id.message_container);
        }

        public void bind(final Context context, final ChatMessage item, final String creator) {
            message.setText(item.getMessage());
            message_container.setBackgroundTintList(context.getResources().getColorStateList(Helper.getColorResourceFor(creator)));
            Date date = new Date(item.getTimestamp());
            overview.setText(Utils.parseChatDate(date));
        }
    }
}