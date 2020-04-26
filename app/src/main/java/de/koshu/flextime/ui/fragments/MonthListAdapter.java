package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.threeten.bp.DayOfWeek;

import de.koshu.flextime.LocalizationHelper;
import de.koshu.flextime.R;
import de.koshu.flextime.data.Day;
import de.koshu.flextime.data.Month;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class MonthListAdapter extends RealmRecyclerViewAdapter<Month, MonthListAdapter.ViewHolder> {
    private Context context;
    private OnMonthListener onMonthListener;

    public MonthListAdapter(Context context, OnMonthListener onMonthListener, OrderedRealmCollection<Month> data) {
        super(data, true, true);
        setHasStableIds(false);

        this.onMonthListener = onMonthListener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month, parent, false);
        return new ViewHolder(eventView, onMonthListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Month obj = getItem(position);

        holder.data = obj;

        holder.txtMonthInt.setText(String.valueOf(obj.getMonthInt()));
        holder.txtMonth.setText(obj.getMonthString());
        holder.txtOvertimeMonth.setText(obj.getOvertimeString());
        holder.txtOvertimeRest.setText(obj.getRestOvertimeString());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView txtMonthInt, txtMonth, txtOvertimeMonth, txtOvertimeRest;
        public Month data;

        OnMonthListener onMonthListener;

        public ViewHolder(View itemView, OnMonthListener onMonthListener) {
            super(itemView);

            txtMonthInt = itemView.findViewById(R.id.txt_monthInt);
            txtMonth = itemView.findViewById(R.id.txt_month);
            txtOvertimeMonth = itemView.findViewById(R.id.txt_overtimeMonth);
            txtOvertimeRest = itemView.findViewById(R.id.txt_overtimeRest);

            this.onMonthListener = onMonthListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMonthListener.onClick(data.getYearInt(), data.getMonthInt());
        }
    }

    public interface  OnMonthListener{
        void onClick(int year, int month);
    }
}