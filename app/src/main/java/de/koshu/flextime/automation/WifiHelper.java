package de.koshu.flextime.automation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import de.koshu.flextime.data.Event;


public class WifiHelper extends BroadcastReceiver {
        private AutomationManager manager;
        private static String ssid = "None";
        private static boolean connected = false;

        public WifiHelper(AutomationManager manager) {
            this.manager = manager;
        }

        public void start() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            manager.getContext().registerReceiver(this, intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                String newSSID = wifiInfo.getSSID();
                newSSID = newSSID.replaceAll("^\"|\"$", "");

                String newState = info.getState().name();

                switch(newState){
                    case "CONNECTED":
                        if(!connected){
                            Event event = new Event();
                            event.type = "CONNECTED";
                            event.info = newSSID;
                            event.source = "WIFI";

                            manager.addEvent(event);

                            ssid = newSSID;
                            connected = true;
                        } else if(!ssid.equals(newSSID)) {
                            Event event1 = new Event();
                            event1.type = "DISCONNECTED";
                            event1.info = ssid;
                            event1.source = "WIFI";

                            manager.addEvent(event1);

                            Event event2 = new Event();
                            event2.type = "CONNECTED";
                            event2.info = newSSID;
                            event2.source = "WIFI";

                            manager.addEvent(event2);

                            ssid = newSSID;
                            connected = true;
                        }
                        break;
                    case "DISCONNECTED":
                        if(connected){
                            Event event = new Event();
                            event.type = "DISCONNECTED";
                            event.info = ssid;
                            event.source = "WIFI";

                            manager.addEvent(event);

                            ssid = "";
                            connected = false;
                        }
                        break;
                }
            }
        }
}
