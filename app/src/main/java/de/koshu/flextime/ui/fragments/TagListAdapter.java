package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.R;
import de.koshu.flextime.data.WorkTag;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class TagListAdapter extends RealmRecyclerViewAdapter<WorkTag, TagListAdapter.ViewHolder> {
    private OnTagListener onTagListener;

    public TagListAdapter(Context context, OnTagListener onTagListener, OrderedRealmCollection<WorkTag> data) {
        super(data, true, true);
        setHasStableIds(false);

        this.onTagListener = onTagListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(eventView, onTagListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final WorkTag obj = getItem(position);

        holder.data = obj;
        holder.txtName.setText(obj.name);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public WorkTag data;
        public TextView txtName;

        OnTagListener onTagListener;

        public ViewHolder(View itemView, OnTagListener onTagListener) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_tagName);

            this.onTagListener = onTagListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTagListener.onTagClick(data.name);
        }
    }

    public interface OnTagListener{
        void onTagClick(String name);
    }
}