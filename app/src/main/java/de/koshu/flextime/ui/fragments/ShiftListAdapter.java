package de.koshu.flextime.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.R;
import de.koshu.flextime.data.Shift;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class ShiftListAdapter extends RealmRecyclerViewAdapter<Shift, ShiftListAdapter.ViewHolder> {
    private OnShiftListener onShiftListener;

    public ShiftListAdapter(OnShiftListener onShiftListener, OrderedRealmCollection<Shift> data) {
        super(data, true);
        setHasStableIds(false);
        this.onShiftListener = onShiftListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift, parent, false);
        return new ViewHolder(eventView,onShiftListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Shift obj = getItem(position);
        holder.data = obj;

        holder.txtTag.setText(obj.tag);
        holder.txtStart.setText(obj.getStartTimeString());
        holder.txtEnd.setText(obj.getEndTimeString());
        holder.txtPause.setText(obj.getPauseDurationString() + " min");
        holder.txtDuration.setText(obj.getNettoDurationString());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTag, txtStart, txtEnd, txtPause, txtDuration;
        public Shift data;
        public OnShiftListener onShiftListener;
        public ImageButton btnDelete;

        public ViewHolder(View itemView, OnShiftListener listener) {
            super(itemView);
            txtTag = itemView.findViewById(R.id.txt_tag);
            txtStart = itemView.findViewById(R.id.txt_startTime);
            txtEnd = itemView.findViewById(R.id.txt_endTime);
            txtPause = itemView.findViewById(R.id.txt_pauseDuration);
            txtDuration = itemView.findViewById(R.id.txt_shiftDuration);
            btnDelete = itemView.findViewById(R.id.btn_delete);

            this.onShiftListener = listener;

            txtStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShiftListener.onShiftStartClick(data);
                }
            });

            txtEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShiftListener.onShiftEndClick(data);
                }
            });

            txtPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShiftListener.onShiftPauseClick(data);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShiftListener.onShiftDeleteClick(data);
                }
            });

            txtTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShiftListener.onShiftTagClick(data);
                }
            });
        }
    }

    public interface  OnShiftListener{
        void onShiftStartClick(Shift shift);
        void onShiftEndClick(Shift shift);
        void onShiftPauseClick(Shift shift);
        void onShiftDeleteClick(Shift shift);
        void onShiftTagClick(Shift shift);
    }
}