package de.koshu.flexbot.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.ui.activities.DayActivity;
import de.koshu.flexbot.R;
import io.realm.Realm;

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

        listAdapter = new DayListAdapter(getContext(),this, DataManager.getManager().getAllDaysSorted());
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onDayClick(int date, int month, int year) {
        Intent intent = new Intent(getActivity(), DayActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("date", date);
        bundle.putInt("month", month);
        bundle.putInt("year", year);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void update(){
        listAdapter.notifyDataSetChanged();
    }
}