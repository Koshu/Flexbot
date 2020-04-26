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
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class DayListAdapter extends RealmRecyclerViewAdapter<Day, DayListAdapter.ViewHolder> {
    private Context context;
    private OnDayListener onDayListener;
    private boolean showMonths = true;
    private boolean addWeekDistance = true;

    public DayListAdapter(Context context, OnDayListener onDayListener, OrderedRealmCollection<Day> data) {
        super(data, true, true);
        setHasStableIds(false);

        this.onDayListener = onDayListener;
        this.context = context;
    }

    public DayListAdapter(Context context, OnDayListener onDayListener, OrderedRealmCollection<Day> data, boolean showMonths, boolean addWeekDistance) {
        super(data, true, true);
        setHasStableIds(false);

        this.showMonths = showMonths;
        this.addWeekDistance = addWeekDistance;
        this.onDayListener = onDayListener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);

        //View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new ViewHolder(eventView, onDayListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Day obj = getItem(position);

        holder.data = obj;


        if(showMonths && obj.isLastDayOfMonth()){
            holder.layHeader.setVisibility(View.VISIBLE);
            holder.txtMonth.setText(obj.getMonthString());

            float monthOvertime = obj.getMonthObject().getRestOvertime();

            holder.txtMonthOvertime.setText(LocalizationHelper.floatToHourString(monthOvertime, true));
        } else {
            holder.layHeader.setVisibility(View.GONE);
        }

        if(addWeekDistance && obj.getDayOfWeek() == DayOfWeek.MONDAY){
            holder.spaceBottom.setVisibility(View.VISIBLE);
        } else {
            holder.spaceBottom.setVisibility(View.GONE);
        }


        holder.txtDate.setText(obj.getDateStringShort());
        holder.txtDayOfWeek.setText(obj.getDayOfWeekStringShort());

        int workMin = (int)(obj.getWorkHours()*60);
        holder.progWork.setProgress(workMin);

        int requMin = (int)(obj.getRequiredWork()*60);
        holder.progRequi.setProgress(requMin-2);
        holder.progRequi.setSecondaryProgress(requMin+2);

        float overtime = obj.getOvertime();
        holder.txtOvertime.setText(LocalizationHelper.floatToHourString(overtime, true));

        holder.icon.setImageResource(obj.getIcon());
        holder.icon.setBackgroundColor(obj.getIconColor(context));
        holder.progWork.setProgressTintList(ColorStateList.valueOf(obj.getProgColor(context)));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView txtDayOfWeek, txtDate, txtMonth, txtOvertime, txtMonthOvertime;
        public ConstraintLayout layHeader;
        public ProgressBar progWork, progRequi;
        public Day data;
        public View devider;
        public ImageView icon;
        public Space spaceBottom;

        OnDayListener onDayListener;

        public ViewHolder(View itemView, OnDayListener onDayListener) {
            super(itemView);

            layHeader = itemView.findViewById(R.id.layout_header);
            progWork = itemView.findViewById(R.id.prog_work);
            txtDayOfWeek = itemView.findViewById(R.id.txt_dayOfTheWeek);
            txtOvertime = itemView.findViewById(R.id.txt_overtime);
            txtDate = itemView.findViewById(R.id.txt_Date);
            txtMonth = itemView.findViewById(R.id.txt_month);
            txtMonthOvertime = itemView.findViewById(R.id.txt_monthOvertime);
            devider = itemView.findViewById(R.id.divider);
            icon = itemView.findViewById(R.id.img_dayIcon);
            spaceBottom = itemView.findViewById(R.id.space_bottom);
            progRequi = itemView.findViewById(R.id.prog_required);

            this.onDayListener = onDayListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onDayListener.onDayClick(data.getDayInt(), data.getMonthInt(), data.getYearInt());
        }
    }

    public interface  OnDayListener{
        void onDayClick(int date, int month, int year);
    }
}