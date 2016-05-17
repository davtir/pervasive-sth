package com.pervasive.sth.distances;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.pervasive.sth.rest.RESTClient;
import com.pervasive.sth.sensors.SensorsReader;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

/**
 * Created by davtir on 15/05/16.
 */
public class HunterTask extends AsyncTask<Void, Void, Void> {

    Context _context;
    GPSTracker _gps;
    BluetoothTracker _bluetooth;
    RESTClient client;
    SensorsReader _sensors;
    String _treasureID;
    double distance;

    float[] _acc;
    float[] _rot;
    float _lux;
    float _temperature;

    public HunterTask(Context context, GPSTracker gps, BluetoothTracker ble) {
        _context = context;
        _gps = gps;
        _bluetooth = ble;
        _treasureID = "";
        _sensors = new SensorsReader(context);
        Log.d("HunterTask", "Creating REST client");
        client = new RESTClient("http://192.168.1.6:8084/WSPervasiveSTH/webresources/devices");
        client.addHeader("content-type", "text/plain");
    }

    public String getTreasureID() {
        return _treasureID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("HunterTask", "Started");

        // End when a cancel request is received
        while ( !isCancelled() ) {

            // Get treasure string from WS
            String treasure="";
            try {
                treasure = client.executeGet();
            } catch (Exception e) {
                // Error while executing get on WS
                Log.e("HunterTask", e.getMessage());
                continue;
            }

            // Parse received string
            String[] meta = treasure.split(";");
            if ( meta == null ) {
                Log.d("HunterTask", "No treasure found");
                continue;
            }

            _acc = _sensors.getAcceleration();
            _rot = _sensors.getRotation();
            _lux = _sensors.getLuminosity();

            try {
                Log.d("HunterTask", "Luminosity: " + _lux + " lux");
            } catch ( RuntimeException e) {
                Log.e("HunterTask", e.getMessage());
            }
            try {
                Log.d("HunterTask", "Temperature: " + _sensors.getTemperature() + "°");
            } catch ( RuntimeException e) {
                Log.e("HunterTask", e.getMessage());
            }

            try {
                Log.d("HunterTask", "Acceleration: " + _acc[0] + " m/s^2, " + _acc[1] + " m/s^2, " + _acc[2] + " m/s^2");
            } catch ( RuntimeException e) {
                Log.e("HunterTask", e.getMessage());
            }

            try {
                Log.d("HunterTask", "Rotation: " + _rot[0] + " rad/s, " + _rot[1] + " rad/s, " + _rot[2] + " rad/s");
            } catch ( RuntimeException e) {
                Log.e("HunterTask", e.getMessage());
            }

            _treasureID = meta[0];
            double t_lat = Double.parseDouble(meta[2]);
            double t_lon = Double.parseDouble(meta[3]);

            // Compute gps distance
            distance = _gps.gpsDistance(t_lat, t_lon);
            Log.d("HunterTask", "Distance from treasure: " + distance + " m");

            // Start bluetooth discovery for 5 seconds
            _bluetooth.discover();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d("HunterTask", "Stop searching");
        }

        return null;
    }

    public double getDistance() {
        return distance;
    }

}
