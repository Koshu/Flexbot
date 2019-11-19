package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.R;
import de.koshu.flextime.data.Event;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class EventListAdapter extends RealmRecyclerViewAdapter<Event, EventListAdapter.ViewHolder> {
    private Context context;

    public EventListAdapter(Context context, OrderedRealmCollection<Event> data) {
        super(data, true);
        setHasStableIds(false);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(eventView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Event obj = getItem(position);
        holder.data = obj;

        holder.txtType.setText(obj.type);
        holder.txtInfo.setText(obj.info);
        holder.txtDate.setText(obj.date.toString());
        holder.txtSource.setText(obj.source);
        holder.txtTag.setText(obj.tag);
        if(obj.type.equals("CONNECTED") || obj.type.equals("INSIDE")){
            holder.imgEvent.setImageResource(R.drawable.location_enter);
        } else if(obj.type.equals("DISCONNECTED") || obj.type.equals("OUTSIDE")) {
            holder.imgEvent.setImageResource(R.drawable.location_exit);
        } else {
            holder.imgEvent.setImageResource(R.drawable.help_circle_outline);
        }

        if(obj.filtered){
            holder.imgEvent.setColorFilter(ContextCompat.getColor(context,R.color.colorLightGrey));
        } else if(obj.type.equals("CONNECTED") || obj.type.equals("INSIDE")){
            holder.imgEvent.setColorFilter(ContextCompat.getColor(context,R.color.colorGreen));
        } else if(obj.type.equals("DISCONNECTED") || obj.type.equals("OUTSIDE")) {
            holder.imgEvent.setColorFilter(ContextCompat.getColor(context,R.color.colorOrange));
        } else {
            holder.imgEvent.setColorFilter(ContextCompat.getColor(context,R.color.colorYellow));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtType, txtDate, txtSource,txtInfo,txtTag;
        public ImageView imgEvent;
        public Event data;

        public ViewHolder(View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txt_type);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtSource = itemView.findViewById(R.id.txt_source);
            txtInfo = itemView.findViewById(R.id.txt_info);
            txtTag = itemView.findViewById(R.id.txt_tag);
            imgEvent = itemView.findViewById(R.id.img_eventIcon);
        }
    }
}