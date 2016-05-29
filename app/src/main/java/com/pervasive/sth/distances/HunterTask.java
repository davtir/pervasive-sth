package com.pervasive.sth.distances;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.pervasive.sth.entities.Device;
import com.pervasive.sth.rest.RESTClient;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

/**
 * Created by davtir on 15/05/16.
 */
public class HunterTask extends AsyncTask<Void, Void, Void> {

    Context _context;
    GPSTracker _gps;
    BluetoothTracker _bluetooth;
    WSInterface _webserver;
    SensorsReader _sensors;
    Device _hunter;
    String _treasureID;
    double distance;

    public HunterTask(Context context, GPSTracker gps, BluetoothTracker ble, Device hunter) {
        _context = context;
        _gps = gps;
        _bluetooth = ble;
        _treasureID = "";
        _sensors = new SensorsReader(context);
        _hunter = hunter;
        _webserver = new WSInterface();
    }

    public String getTreasureID() {
        return _treasureID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("HunterTask", "Started");

        // End when a cancel request is received
        while ( !isCancelled() ) {

            try {
                if ( _webserver.retrieveTreasureStatus() ) {
                    Log.i("HunterTask", "The treasure have been found!");
                    break;
                }
            } catch ( Exception e ) {
                Log.e("HunterTask", e.getMessage());
                continue;
            }
            // Get treasure string from WS
            Device treasure;
            try {
                treasure = _webserver.retrieveDevice();
                _treasureID = treasure.getMACAddress();
            } catch (Exception e) {
                // Error while executing get on WS
                Log.e("HunterTask", e.getMessage());
                continue;
            }

            try {
                _hunter.setAcceleration(_sensors.getAcceleration());
                _hunter.setRotation(_sensors.getRotation());
                _hunter.setLuminosity(_sensors.getLuminosity());
                _hunter.setTemperature(_sensors.getTemperature());
            } catch ( RuntimeException e) {
                //Log.e("HunterTask", e.getMessage());
                //continue;
            }

            double t_lat = treasure.getLatitude();
            double t_lon = treasure.getLongitude();

            // Compute gps distance
            distance = _gps.gpsDistance(t_lat, t_lon);
            Log.d("HunterTask", "Distance from treasure: " + distance + " m");

            Intent intent = new Intent(HunterActivity.GPS_ACTION);
            intent.putExtra("GPS_DISTANCE", distance);
            _context.sendBroadcast(intent);

            // Start bluetooth discovery for 5 seconds
            _bluetooth.discover();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d("HunterTask", "Stop searching");
        }

        try {
            _webserver.deleteDevice(_hunter.getMACAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(HunterActivity.FOUND_ACTION);
        intent.putExtra("TREASURE_FOUND", true);
        _context.sendBroadcast(intent);

        return null;
    }

    public double getDistance() {
        return distance;
    }

}
