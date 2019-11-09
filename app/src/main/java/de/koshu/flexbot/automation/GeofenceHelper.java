package de.koshu.flexbot.automation;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.koshu.flexbot.data.Event;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class GeofenceHelper {
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList = new LinkedList<>();
    private PendingIntent geofencePendingIntent;
    private AutomationManager manager;

    public GeofenceHelper(AutomationManager manager){
        this.manager = manager;
    }

    public void start(){
        if(geofenceList.isEmpty()) return;

        geofencingClient = LocationServices.getGeofencingClient(manager.getContext());

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("MAIN","GEOFENCE REMOVED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("MAIN","GEOFENCE REMOVED ERROR!!!");
                    }
                });

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("MAIN","GEOFENCE ADDED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("MAIN","GEOFENCE ADDED ERROR!!! Mesg: " + ((ApiException)e).getStatusCode());

                    }
                });
    }

    public void clearGeofences(){
        geofenceList.clear();
    }

    public void addGeofence(String ID, double lat, double lon){
        geofenceList.add(new Geofence.Builder()
                .setRequestId("flexbot."+ID)

                .setCircularRegion(
                        lat,
                        lon,
                        200.0f
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(manager.getContext(), GeofenceHelper.GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(manager.getContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public static class GeofenceBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "GeofenceBroadcastReceiver";
        private AutomationManager manager;

        public void onReceive(Context context, Intent intent) {
            manager = AutomationManager.getManager();

            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "ERROR 1");
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        geofenceTransition,
                        triggeringGeofences
                );

                Event event = new Event();
                event.source = "GEO";
                event.type = "INSIDE";
                event.info = geofenceTransitionDetails;

                manager.addEvent(event);

                Log.i(TAG, "CLOCKED IN");
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        geofenceTransition,
                        triggeringGeofences
                );


                Event event = new Event();
                event.source = "GEO";
                event.type = "OUTSIDE";
                event.info = geofenceTransitionDetails;

                manager.addEvent(event);

                // Send notification and log the transition details.
                //sendNotification(geofenceTransitionDetails);
                Log.i(TAG, "CLOCKED OUT");
            } else {
                // Log the error.
                Log.e(TAG, "ERROR 2");
            }
        }

        private String getGeofenceTransitionDetails(
                int geofenceTransition,
                List<Geofence> triggeringGeofences) {

            // Get the Ids of each geofence that was triggered.
            ArrayList triggeringGeofencesIdsList = new ArrayList();
            for (Geofence geofence : triggeringGeofences) {
                triggeringGeofencesIdsList.add(geofence.getRequestId());
            }
            String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

            return triggeringGeofencesIdsString;
        }
    }
}
