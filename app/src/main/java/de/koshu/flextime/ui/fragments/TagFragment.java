package de.koshu.flextime.ui.fragments;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.WorkTag;
import io.realm.Realm;

/**
 * A placeholder fragment containing a simple view.
 */
public class TagFragment extends Fragment implements ItemSelectAdapter.ItemSelectListener {
    private LinearLayoutManager layoutManager;
    private ItemSelectAdapter itemSelectAdapter;
    private Realm realm;
    private WorkTag tag;

    private EditText txtTagName, txtAutoPauseDuration, txtGeoLong, txtGeoLat;
    private RadioGroup radGrpMode, radGrpTrack;
    private Switch swtAutoPause;
    private RecyclerView recyWifis;

    private Context context;
    private View view;

    public static TagFragment newInstance() {
        TagFragment fragment = new TagFragment();
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
        view = inflater.inflate(R.layout.fragment_tag, container, false);

        txtTagName = view.findViewById(R.id.txt_tagName);
        radGrpMode = view.findViewById(R.id.radGrp_mode);
        radGrpTrack = view.findViewById(R.id.radGrp_trackmode);
        swtAutoPause = view.findViewById(R.id.swt_autoPause);
        txtAutoPauseDuration = view.findViewById(R.id.txt_autoPauseDuration);
        txtGeoLong = view.findViewById(R.id.txt_geoLongitude);
        txtGeoLat = view.findViewById(R.id.txt_geoLatitude);
        recyWifis = view.findViewById(R.id.recy_wifis);


        radGrpMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                realm.beginTransaction();
                switch(checkedId){
                    case R.id.rad_manual:
                        tag.mode = WorkTag.MODE_MANUAL; break;
                    case R.id.rad_semiauto:
                        tag.mode = WorkTag.MODE_SEMIAUTO; break;
                    case R.id.rad_auto:
                        tag.mode = WorkTag.MODE_AUTO; break;
                }
                realm.commitTransaction();
            }
        });

        radGrpMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                realm.beginTransaction();
                switch(checkedId){
                    case R.id.rad_trackhybrid:
                        tag.trackMode = WorkTag.TRACKMODE_HYBRID; break;
                    case R.id.rad_trackwifi:
                        tag.trackMode = WorkTag.TRACKMODE_WIFI; break;
                    case R.id.rad_trackgeo:
                        tag.trackMode = WorkTag.TRACKMODE_GEO; break;
                }
                realm.commitTransaction();
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        realm = DataManager.getManager().getRealm();

        context = getActivity();

        Bundle bundle = getArguments();

        if(bundle != null)
        {
            String tagName = bundle.getString("tagName");
            tag = realm.where(WorkTag.class).equalTo("name", tagName).findFirst();
        }

        recyWifis.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(getContext());
        recyWifis.setLayoutManager(layoutManager);

        List<ItemSelectAdapter.SelectItem> wifis = new ArrayList<>();
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> wifiList = wifiMan.getConfiguredNetworks();

        for(WifiConfiguration conf : wifiList){
            String ssid = conf.SSID;
            ssid = ssid.replaceAll("^\"|\"$", "");
            wifis.add(new ItemSelectAdapter.SelectItem(ssid, false));
        }

        itemSelectAdapter = new ItemSelectAdapter(this,wifis);
        recyWifis.setAdapter(itemSelectAdapter);

        updateGui();
    }

    @Override
    public void onPause(){
        super.onPause();

        realm.beginTransaction();
        tag.name = txtTagName.getText().toString();
        tag.addAutoPauseTime = Integer.parseInt(txtAutoPauseDuration.getText().toString());

        String str = txtGeoLat.getText().toString().replace(',','.');
        tag.geoLatitude = Float.parseFloat(str);

        str = txtGeoLong.getText().toString().replace(',','.');
        tag.geoLongitute = Float.parseFloat(str);

        tag.addAutoPause = swtAutoPause.isChecked();

        switch(radGrpMode.getCheckedRadioButtonId()){
            case R.id.rad_manual:
                tag.mode = WorkTag.MODE_MANUAL; break;
            case R.id.rad_semiauto:
                tag.mode = WorkTag.MODE_SEMIAUTO; break;
            case R.id.rad_auto:
                tag.mode = WorkTag.MODE_AUTO; break;
        }

        switch(radGrpMode.getCheckedRadioButtonId()){
            case R.id.rad_trackhybrid:
                tag.trackMode = WorkTag.TRACKMODE_HYBRID; break;
            case R.id.rad_trackwifi:
                tag.trackMode = WorkTag.TRACKMODE_WIFI; break;
            case R.id.rad_trackgeo:
                tag.trackMode = WorkTag.TRACKMODE_GEO; break;
        }

        List<String> wifis = itemSelectAdapter.getCheckedItems();
        tag.associatedWIFIs.clear();
        for(String wifi : wifis){
            tag.associatedWIFIs.add(wifi);
        }

        realm.commitTransaction();
    }

    private void updateGui(){
        txtTagName.setText(tag.name);
        txtAutoPauseDuration.setText(String.format("%d",tag.addAutoPauseTime));
        txtGeoLong.setText(String.format("%f",tag.geoLongitute));
        txtGeoLat.setText(String.format("%f",tag.geoLatitude));

        switch(tag.mode){
            case WorkTag.MODE_MANUAL:
                radGrpMode.check(R.id.rad_manual);
                break;
            case WorkTag.MODE_SEMIAUTO:
                radGrpMode.check(R.id.rad_semiauto);
                break;
            case WorkTag.MODE_AUTO:
                radGrpMode.check(R.id.rad_auto);
                break;
        }

        switch(radGrpMode.getCheckedRadioButtonId()){
            case WorkTag.TRACKMODE_HYBRID:
                radGrpTrack.check(R.id.rad_trackhybrid);break;
            case WorkTag.TRACKMODE_WIFI:
                radGrpTrack.check(R.id.rad_trackwifi);break;
            case WorkTag.TRACKMODE_GEO:
                radGrpTrack.check(R.id.rad_trackgeo);break;
        }

        swtAutoPause.setChecked(tag.addAutoPause);

        itemSelectAdapter.clearAllChecks();

        for(String wifi : tag.associatedWIFIs){
            itemSelectAdapter.setChecked(wifi,true);
        }

        itemSelectAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(String name, boolean checked) {

    }
}