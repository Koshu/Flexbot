package de.koshu.flextime.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Shift;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShiftListFragment extends Fragment implements ShiftListAdapter.OnShiftListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ShiftListAdapter shiftListAdapter;
    private Realm realm;

    public static ShiftListFragment newInstance() {
        ShiftListFragment fragment = new ShiftListFragment();
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
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        realm = DataManager.getManager().getRealm();

        recyclerView = view.findViewById(R.id.recy_eventlist);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        shiftListAdapter = new ShiftListAdapter(this,realm.where(Shift.class).findAll());
        recyclerView.setAdapter(shiftListAdapter);

        return view;
    }

    @Override
    public void onShiftStartClick(Shift shift) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onShiftEndClick(Shift shift) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onShiftPauseClick(Shift shift) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getChildFragmentManager(), "timePicker");
    }

    @Override
    public void onShiftDeleteClick(Shift shift) {

    }

    @Override
    public void onShiftTagClick(Shift shift) {

    }
}