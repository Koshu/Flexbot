package de.koshu.flexbot.ui.fragments;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;


import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.data.Shift;
import de.koshu.flexbot.R;
import de.koshu.flexbot.data.Day;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;

public class DayFragment extends Fragment implements ShiftListAdapter.OnShiftListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ShiftListAdapter shiftListAdapter;
    private Realm realm;
    private Day day;

    private TextView txtDayOfWeek;
    private TextView txtDate;
    private TextView txtHoursWorked;
    private TextView txtOvertime;
    private ProgressBar progWork;
    private ProgressBar progRequi;
    private ImageView imgIcon;
    private FloatingActionButton fab;
    private Context context;
    private View view;

    public static DayFragment newInstance() {
        DayFragment fragment = new DayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_day, container, false);

        txtDayOfWeek = view.findViewById(R.id.txt_dayOfTheWeek);
        txtDate = view.findViewById(R.id.txt_date);
        txtHoursWorked = view.findViewById(R.id.txt_hoursWorked);
        txtOvertime = view.findViewById(R.id.txt_overtime);
        progWork = view.findViewById(R.id.prog_work);
        imgIcon = view.findViewById(R.id.img_dayIcon);
        recyclerView = view.findViewById(R.id.recy_eventlist);
        fab = view.findViewById(R.id.fab);
        progRequi = view.findViewById(R.id.prog_required);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        realm = DataManager.getManager().getRealm();

        context = getActivity();

        int date, month, year;

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            date = bundle.getInt("date");
            month = bundle.getInt("month");
            year = bundle.getInt("year");

            day = DataManager.getManager().getDay(LocalDate.of(year,month,date));
        } else {
            day = DataManager.getManager().getToday();
        }

        day.shifts.addChangeListener(new RealmChangeListener<RealmList<Shift>>() {
            @Override
            public void onChange(RealmList<Shift> shifts) {
                updateDayGui();
            }
        });

        day.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel realmModel) {
                updateDayGui();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.beginTransaction();
                day.addEmptyShift();
                realm.commitTransaction();
            }
        });

        imgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayTypeDialogFragment pauseFragment = new DayTypeDialogFragment(new DayTypeDialogFragment.OnDayTypeListener() {
                    @Override
                    public void onTypePicked(int type) {
                        realm.beginTransaction();
                        day.setType(type);
                        realm.commitTransaction();
                    }
                });

                pauseFragment.show(getFragmentManager(),"DayTypeDialog");
            }
        });

        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        shiftListAdapter = new ShiftListAdapter(this,day.shifts);
        recyclerView.setAdapter(shiftListAdapter);

        updateDayGui();
    }

    private void updateDayGui(){
        txtDayOfWeek.setText(day.getDayOfWeekString());
        txtDate.setText(day.getDateString());

        float hoursWorked = day.getCumulatedNettoDuration();
        float overtime = day.getOvertime();
        float reqWork = day.getRequiredWork();

        txtHoursWorked.setText(String.format("%.1fh / %.1fh",hoursWorked,reqWork));
        txtOvertime.setText(String.format("%.1fh",overtime));
        txtOvertime.setTextColor(day.getProgColor(context));

        progWork.setProgress((int) (hoursWorked*60));
        progWork.setProgressTintList(ColorStateList.valueOf(day.getProgColor(context)));

        int requMin = (int)(day.getRequiredWork()*60);
        progRequi.setProgress(requMin-2);
        progRequi.setSecondaryProgress(requMin+2);

        imgIcon.setImageResource(day.getIcon());
        imgIcon.setBackgroundColor(day.getIconColor(context));
    }

    private int getThemeColor(int attr){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        @ColorInt int color = typedValue.data;

        return color;
    }

    @Override
    public void onShiftStartClick(final Shift shift) {
        LocalTime lt = shift.getStartLocalTime();

        TimePickerDialog newFragment = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                realm.beginTransaction();
                shift.setStartTime(LocalTime.of(hourOfDay,minute), Shift.CONFIDENCE_MANUAL);
                realm.commitTransaction();
            }
        }, lt.getHour(), lt.getMinute(), DateFormat.is24HourFormat(getActivity()));

        newFragment.show();
    }

    @Override
    public void onShiftEndClick(final Shift shift) {
        LocalTime lt = shift.getStartLocalTime();

        TimePickerDialog newFragment = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                realm.beginTransaction();
                shift.setEndTime(LocalTime.of(hourOfDay,minute), Shift.CONFIDENCE_MANUAL);
                realm.commitTransaction();
            }
        }, lt.getHour(), lt.getMinute(), DateFormat.is24HourFormat(getActivity()));

        newFragment.show();
    }

    @Override
    public void onShiftPauseClick(final Shift shift) {
        PauseTimeDialogFragment pauseFragment = new PauseTimeDialogFragment(new PauseTimeDialogFragment.OnPauseTimeListener() {
            @Override
            public void onTimePicked(int time) {
                realm.beginTransaction();
                shift.setPauseInMin(time);
                realm.commitTransaction();
            }
        }, (int) (shift.getPauseDuration()/60));

        pauseFragment.show(getFragmentManager(),"PauseDialog");
    }

    @Override
    public void onShiftDeleteClick(final Shift shift) {
        new AlertDialog.Builder(getActivity())
                .setMessage("Eintrag löschen?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        realm.beginTransaction();
                        shift.deleteFromRealm();
                        realm.commitTransaction();
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    @Override
    public void onShiftTagClick(final Shift shift) {
        ShiftTagDialogFragment shiftTagFragment = new ShiftTagDialogFragment(new ShiftTagDialogFragment.OnShiftTagListener() {
                    @Override
                    public void onTagPicked(String tagName) {
                        realm.beginTransaction();
                        shift.tag = tagName;
                        realm.commitTransaction();
                    }
                });

        shiftTagFragment.show(getFragmentManager(), "ShiftTagDialog");
    }
}