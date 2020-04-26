package de.koshu.flextime.ui.fragments;

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

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Day;
import de.koshu.flextime.data.Month;
import de.koshu.flextime.data.Shift;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;

public class MonthFragment extends Fragment implements DayListAdapter.OnDayListener{
    private Month month;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DayListAdapter listAdapter;

    private TextView txtMonth;
    private TextView txtYear;
    private TextView txtOvertimeMonth;
    private TextView txtOvertimePaid;
    private TextView txtOvertimeRest;
    private TextView txtVacationDays;
    private TextView txtSickDays;
    private Context context;
    private View view;

    public static MonthFragment newInstance() {
        MonthFragment fragment = new MonthFragment();
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
        view = inflater.inflate(R.layout.fragment_month, container, false);

        txtMonth = view.findViewById(R.id.txt_month);
        txtYear = view.findViewById(R.id.txt_year);
        txtOvertimeMonth = view.findViewById(R.id.txt_overtimeMonth);
        txtOvertimePaid = view.findViewById(R.id.txt_overtimePaid);
        txtOvertimeRest = view.findViewById(R.id.txt_overtimeRest);
        txtVacationDays = view.findViewById(R.id.txt_vacationDays);
        txtSickDays = view.findViewById(R.id.txt_sickDays);
        recyclerView = view.findViewById(R.id.recy_daylist);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        context = getActivity();

        int monthInt, yearInt;

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            monthInt = bundle.getInt("monthInt");
            yearInt = bundle.getInt("yearInt");

            month = DataManager.getManager().getMonth(yearInt,monthInt);
        } else {
            month = DataManager.getManager().getCurrentMonth();
        }

        month.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel realmModel) {
                updateDayGui();
            }
        });

        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        String []fieldNames={"dayInt"};
        Sort sort[]={Sort.DESCENDING};

        RealmResults<Day> dayList = month.getDays().where().sort(fieldNames,sort).findAll();

        listAdapter = new DayListAdapter(getContext(),this, dayList, false,true);
        recyclerView.setAdapter(listAdapter);

        updateDayGui();
    }

    private void updateDayGui(){
        txtMonth.setText(month.getMonthString());
        txtYear.setText(String.valueOf(month.getYearInt()));
        txtOvertimeMonth.setText(month.getOvertimeString());
        txtOvertimePaid.setText(month.getPaidOvertimeString());
        txtOvertimeRest.setText(month.getRestOvertimeString());
        txtVacationDays.setText(month.getVacationDaysString());
        txtSickDays.setText(month.getSickDaysString());
    }

    @Override
    public void onDayClick(int date, int month, int year) {

    }
}