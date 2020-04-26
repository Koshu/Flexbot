package de.koshu.flextime.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Day;
import de.koshu.flextime.ui.activities.DayActivity;
import de.koshu.flextime.R;
import de.koshu.flextime.ui.activities.OverviewActivity;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A placeholder fragment containing a simple view.
 */
public class DayListFragment extends Fragment implements DayListAdapter.OnDayListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DayListAdapter listAdapter;
    private Realm realm;

    public static DayListFragment newInstance() {
        DayListFragment fragment = new DayListFragment();
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
        View view = inflater.inflate(R.layout.fragment_days, container, false);

        recyclerView = view.findViewById(R.id.recy_list);

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        realm = DataManager.getManager().getRealm();

        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RealmResults<Day> dayList = DataManager.getManager().getLastDaysSorted(60);

        listAdapter = new DayListAdapter(getContext(),this, dayList);
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onDayClick(int date, int month, int year) {
        Intent intent = new Intent(getActivity(), OverviewActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("pageType",OverviewActivity.PAGETYPE_DAY);
        bundle.putInt("dayInt", date);
        bundle.putInt("monthInt", month);
        bundle.putInt("yearInt", year);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void update(){
        listAdapter.notifyDataSetChanged();
    }
}