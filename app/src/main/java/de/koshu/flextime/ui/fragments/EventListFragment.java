package de.koshu.flextime.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.R;
import de.koshu.flextime.data.Event;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class EventListFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EventListAdapter eventListAdapter;
    private Realm realm;

    public static EventListFragment newInstance() {
        EventListFragment fragment = new EventListFragment();
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

        eventListAdapter = new EventListAdapter(getActivity(),realm.where(Event.class)
                .equalTo("filtered", false)
                .findAll());

        recyclerView.setAdapter(eventListAdapter);

        return view;
    }

    public void setFiltered(boolean filtered){
        if(filtered) {
            eventListAdapter.updateData(realm.where(Event.class)
                    .equalTo("filtered", false)
                    .findAll());
        } else {
            eventListAdapter.updateData(realm.where(Event.class)
                    .findAll());
        }
    }
}