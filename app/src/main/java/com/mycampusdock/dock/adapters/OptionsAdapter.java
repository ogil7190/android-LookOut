package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.RealmData;

import java.util.List;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RealmData item, int pos, View view);
    }

    private final List<RealmData> items;
    private Context context;
    private final OnItemClickListener listener;

    public OptionsAdapter(Context context, List<RealmData> items, OnItemClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_layout, parent, false);
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
        private TextView title;
        private CardView container;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
            container = view.findViewById(R.id.container);
        }

        public void bind(final Context context, final RealmData item, final int pos, final OnItemClickListener listener) {
            title.setText(item.getData());
            Glide.with(context).load(Integer.parseInt(item.getExtra())).into(icon);
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, pos, itemView);
                }
            });
        }
    }
}