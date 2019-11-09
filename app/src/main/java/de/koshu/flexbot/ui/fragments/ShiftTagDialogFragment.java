package de.koshu.flexbot.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import de.koshu.flexbot.data.DataManager;
import de.koshu.flexbot.data.WorkTag;
import io.realm.Realm;
import io.realm.RealmResults;

public class ShiftTagDialogFragment extends DialogFragment {
    private OnShiftTagListener listener;

    public ShiftTagDialogFragment(OnShiftTagListener listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Realm realm = DataManager.getManager().getRealm();

        RealmResults<WorkTag> tags = realm.where(WorkTag.class).findAll();

        final String[] tagNames = new String[tags.size()];

        for(int i = 0; i < tagNames.length; i++){
            tagNames[i] = tags.get(i).name;
        }
        builder.setTitle("Schichtart")
                .setItems(tagNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onTagPicked(tagNames[which]);
                    }
                });

        return builder.create();
    }

    public interface OnShiftTagListener{
        void onTagPicked(String tagName);
    }
}