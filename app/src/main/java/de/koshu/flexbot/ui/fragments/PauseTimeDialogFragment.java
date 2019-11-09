package de.koshu.flexbot.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import de.koshu.flexbot.R;

public class PauseTimeDialogFragment extends DialogFragment {
    private int pauseTime = 0;
    private TextView txtPause;
    private SeekBar seekMin;
    private OnPauseTimeListener listener;

    public PauseTimeDialogFragment(OnPauseTimeListener listener, int startValue){
        this.listener = listener;
        this.pauseTime = startValue;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onTimePicked(pauseTime);
            }
        })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);

        builder.setView(layout);


        txtPause = layout.findViewById(R.id.txt_pauseTime);
        seekMin= layout.findViewById(R.id.seek_min);

        layout.findViewById(R.id.btn_20min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPauseTime(20);
            }
        });

        layout.findViewById(R.id.btn_30min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPauseTime(30);
            }
        });

        layout.findViewById(R.id.btn_45min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPauseTime(45);
            }
        });

        seekMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setPauseTime(progress * 5);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setPauseTime(pauseTime);
        return builder.create();
    }

    private void setPauseTime(int time){
        pauseTime = time;
        txtPause.setText(time + " min");
        seekMin.setProgress(time/5);
    }

    public interface OnPauseTimeListener{
        void onTimePicked(int time);
    }
}