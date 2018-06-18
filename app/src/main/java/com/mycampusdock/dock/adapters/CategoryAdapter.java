package com.mycampusdock.dock.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mycampusdock.dock.R;
import com.mycampusdock.dock.objects.Interest;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class CategoryAdapter extends RealmRecyclerViewAdapter<Interest, CategoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Interest item, int pos, View view);
    }

    private final List<Interest> items;
    private Context context;
    private final OnItemClickListener listener;

    public CategoryAdapter(Context context, OrderedRealmCollection<Interest> items, OnItemClickListener listener) {
        super(items, true);
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
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
        private Button add;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            title = view.findViewById(R.id.title);
            add = view.findViewById(R.id.add);
        }

        public void bind(final Context context, final Interest item, final int pos, final OnItemClickListener listener) {
            title.setText(item.getName());
            if (item.getExtra() != null)
                Glide.with(context).load(Integer.parseInt(item.getExtra())).into(icon);

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, pos, itemView);
                }
            });

            if(item.getType() == 102){
                add.setText("Added ✓");
                add.setTextColor(context.getResources().getColor(R.color.my_blue));
                add.setBackground(context.getDrawable(R.drawable.login_button_disable));
            } else {
                add.setText("Add");
                add.setTextColor(context.getResources().getColor(R.color.white));
                add.setBackground(context.getDrawable(R.drawable.login_button_enable));
            }
        }
    }
}