package de.koshu.flextime.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import de.koshu.flextime.data.DayTypes;

public class DayTypeDialogFragment extends DialogFragment {
    private OnDayTypeListener listener;

    public DayTypeDialogFragment(OnDayTypeListener listener){
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Tagart")
                .setItems(DayTypes.dayStrings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onTypePicked(which);
                    }
                });

        return builder.create();
    }

    public interface OnDayTypeListener{
        void onTypePicked(int type);
    }
}