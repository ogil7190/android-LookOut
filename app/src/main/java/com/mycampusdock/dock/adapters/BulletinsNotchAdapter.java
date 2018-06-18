package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.mycampusdock.dock.Helper;
import com.mycampusdock.dock.R;

import java.util.List;

public class BulletinsNotchAdapter extends RecyclerView.Adapter<BulletinsNotchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String creator, View view);
    }

    private final List<String> items;
    private Context context;
    private final OnItemClickListener listener;

    public BulletinsNotchAdapter(Context context, List<String> items, OnItemClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notch_view, parent, false);
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

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
        }

        public void bind(final Context context, final String item, final int pos, final OnItemClickListener listener) {
            TextDrawable drawable = TextDrawable.builder().buildRound(""+item.charAt(0), Helper.getColorFor(item, context));
            icon.setImageDrawable(drawable);
            title.setText(item);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, itemView);
                }
            });
        }
    }
}