package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Month;
import de.koshu.flextime.data.Year;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

public class YearFragment extends Fragment implements MonthListAdapter.OnMonthListener{
    private Year year;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private MonthListAdapter listAdapter;

    private TextView txtYear;
    private TextView txtOvertime;;
    private TextView txtOvertimeLastYear;
    private TextView txtOvertimePaid;
    private TextView txtOvertimeRest;
    private TextView txtVacationDaysLastYear;
    private TextView txtVacationDaysEntitled;
    private TextView txtVacationDaysTaken;
    private TextView txtVacationDaysRest;

    private Context context;
    private View view;

    public static YearFragment newInstance() {
        YearFragment fragment = new YearFragment();
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
        view = inflater.inflate(R.layout.fragment_year, container, false);

        txtYear = view.findViewById(R.id.txt_year);
        txtOvertime = view.findViewById(R.id.txt_overtime);
        txtOvertimeLastYear = view.findViewById(R.id.txt_overtimeLastYear);
        txtOvertimePaid = view.findViewById(R.id.txt_overtimePaid);
        txtOvertimeRest = view.findViewById(R.id.txt_overtimeRest);
        txtVacationDaysLastYear = view.findViewById(R.id.txt_vacationDaysLastYear);
        txtVacationDaysEntitled = view.findViewById(R.id.txt_vacationDaysEntitled);
        txtVacationDaysTaken = view.findViewById(R.id.txt_vacationDaysTaken);
        txtVacationDaysRest = view.findViewById(R.id.txt_vacationDaysRest);
        recyclerView = view.findViewById(R.id.recy_monthlist);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        context = getActivity();

        int yearInt;

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            yearInt = bundle.getInt("yearInt");

            year = DataManager.getManager().getYear(yearInt);
        } else {
            year = DataManager.getManager().getCurrentYear();
        }

        year.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel realmModel) {
                updateDayGui();
            }
        });

        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RealmResults<Month> monthList = year.getMonths();

        listAdapter = new MonthListAdapter(getContext(),this, monthList);
        recyclerView.setAdapter(listAdapter);

        updateDayGui();
    }

    private void updateDayGui(){
        txtYear.setText(String.valueOf(year.getYearInt()));

        txtOvertime.setText(year.getOvertimeString());
        txtOvertimeLastYear.setText(year.getLastRestOvertimeString());
        txtOvertimePaid.setText(year.getPaidOvertimeString());
        txtOvertimeRest.setText(year.getRestOvertimeString());

        txtVacationDaysLastYear.setText(year.getLastRestVacationDaysString());
        txtVacationDaysEntitled.setText(year.getEntitledVacationDaysString());
        txtVacationDaysTaken.setText(year.getVacationDaysString());
        txtVacationDaysRest.setText(year.getRestVacationDaysString());
    }

    @Override
    public void onClick(int year, int month) {
        /*
        Intent intent = new Intent(getActivity(), DayActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("month", month);
        bundle.putInt("year", year);

        intent.putExtras(bundle);
        startActivity(intent);
        */
    }
}