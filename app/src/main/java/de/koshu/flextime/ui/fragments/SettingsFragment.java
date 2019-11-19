package de.koshu.flextime.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.threeten.bp.DayOfWeek;

import java.text.NumberFormat;

import de.koshu.flextime.R;
import de.koshu.flextime.data.AppSettings;
import de.koshu.flextime.data.DataManager;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends Fragment{
    private EditText txtStartVacation, txtStartOvertime;
    private TextView[] txtDay = new TextView[7];
    private EditText[] txtDayHours = new EditText[7];
    private Switch[] swtDay = new Switch[7];

    private View view;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        txtDay[0] = view.findViewById(R.id.txt_monday);
        txtDay[1] = view.findViewById(R.id.txt_tuesday);
        txtDay[2] = view.findViewById(R.id.txt_wednesday);
        txtDay[3] = view.findViewById(R.id.txt_thursday);
        txtDay[4] = view.findViewById(R.id.txt_friday);
        txtDay[5] = view.findViewById(R.id.txt_saturday);
        txtDay[6] = view.findViewById(R.id.txt_sunday);

        swtDay[0] = view.findViewById(R.id.swt_monday);
        swtDay[1] = view.findViewById(R.id.swt_tuesday);
        swtDay[2] = view.findViewById(R.id.swt_wednesday);
        swtDay[3] = view.findViewById(R.id.swt_thursday);
        swtDay[4] = view.findViewById(R.id.swt_friday);
        swtDay[5] = view.findViewById(R.id.swt_saturday);
        swtDay[6] = view.findViewById(R.id.swt_sunday);

        txtDayHours[0] = view.findViewById(R.id.txt_hoursMonday);
        txtDayHours[1] = view.findViewById(R.id.txt_hoursTuesday);
        txtDayHours[2] = view.findViewById(R.id.txt_hoursWednesday);
        txtDayHours[3] = view.findViewById(R.id.txt_hoursThursday);
        txtDayHours[4] = view.findViewById(R.id.txt_hoursFriday);
        txtDayHours[5] = view.findViewById(R.id.txt_hoursSaturday);
        txtDayHours[6] = view.findViewById(R.id.txt_hoursSunday);

        txtStartVacation = view.findViewById(R.id.txt_startVacationDays);
        txtStartOvertime = view.findViewById(R.id.txt_startOvertime);

        for(int i = 0; i < 7; i++){
            attachListenerToDay(i);
        }
        return view;
    }

    private void attachListenerToDay(final int day){
        swtDay[day].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    txtDayHours[day].setEnabled(true);
                    txtDayHours[day].setText("8.0");
                } else {
                    txtDayHours[day].setEnabled(false);
                    txtDayHours[day].setText("0.0");
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        DataManager dataManager = DataManager.getManager();
        AppSettings settings = dataManager.getSettings();

        for(int i=0; i<7; i++){
            DayOfWeek dayOfWeek = DayOfWeek.of(i+1);

            float value = settings.getHoursToWork(dayOfWeek);
            String valS = String.format("%.2f", value);

            if(value == 0.0f){
                swtDay[i].setChecked(false);
                txtDayHours[i].setEnabled(false);
                txtDayHours[i].setText(valS);
            } else {
                swtDay[i].setChecked(true);
                txtDayHours[i].setEnabled(true);
                txtDayHours[i].setText(valS);
            }
        }

        txtStartOvertime.setText(String.format("%.2f",settings.startOvertime));
        txtStartVacation.setText(String.format("%d",settings.startVacationDays));
    }

    @Override
    public void onPause(){
        super.onPause();
        DataManager dataManager = DataManager.getManager();
        Realm realm = dataManager.getRealm();
        AppSettings settings = dataManager.getSettings();

        realm.beginTransaction();

        NumberFormat nf = NumberFormat.getInstance();
        for(int i=0; i<7; i++){
            DayOfWeek dayOfWeek = DayOfWeek.of(i+1);
            float value = stringToFloat(txtDayHours[i].getText().toString());
            settings.setHoursToWork(dayOfWeek, value);
        }

        settings.startOvertime = stringToFloat(txtStartOvertime.getText().toString());
        settings.startVacationDays = stringToInt(txtStartVacation.getText().toString());

        realm.commitTransaction();
    }

    private float stringToFloat(String s){
        s = s.replace(',','.');
        float value = Float.parseFloat(s);
        return value;
    }

    private int stringToInt(String s){
        int value = Integer.parseInt(s);
        return value;
    }
}