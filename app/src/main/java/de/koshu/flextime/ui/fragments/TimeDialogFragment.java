package de.koshu.flextime.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import de.koshu.flextime.LocalizationHelper;
import de.koshu.flextime.R;

public class TimeDialogFragment extends DialogFragment {
    private float time;
    private TextView txtTime;
    private OnTimeListener listener;

    public TimeDialogFragment(OnTimeListener listener, float startValue){
        this.listener = listener;
        this.time = startValue;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         time = LocalizationHelper.hourStringToFloat(txtTime.getText().toString());
                        listener.onTimePicked(time);
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_time, null);

        builder.setView(layout);


        txtTime = layout.findViewById(R.id.txtTime);

        setTime(time);
        return builder.create();
    }

    private void setTime(float time){
        txtTime.setText(LocalizationHelper.floatToHourString(time,false));
    }

    public interface OnTimeListener{
        void onTimePicked(float time);
    }
}