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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.WorkTag;
import de.koshu.flextime.ui.activities.TagActivity;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class TagListFragment extends Fragment implements TagListAdapter.OnTagListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TagListAdapter listAdapter;
    private Realm realm;
    private FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        recyclerView = view.findViewById(R.id.recy_list);
        fab = view.findViewById(R.id.fab);

        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        realm = DataManager.getManager().getRealm();

        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        listAdapter = new TagListAdapter(getContext(),this, DataManager.getManager().getAllTags());
        recyclerView.setAdapter(listAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.beginTransaction();
                WorkTag tag = realm.createObject(WorkTag.class);
                realm.commitTransaction();
            }
        });
    }

    @Override
    public void onTagClick(String name) {
        Intent intent = new Intent(getActivity(), TagActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("tagName", name);

        intent.putExtras(bundle);
        startActivity(intent);
    }
}